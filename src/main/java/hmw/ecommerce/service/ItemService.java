package hmw.ecommerce.service;

import hmw.ecommerce.entity.Category;
import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.ItemCategory;
import hmw.ecommerce.entity.Member;
import hmw.ecommerce.entity.dto.ItemRegisterDto;
import hmw.ecommerce.entity.dto.ItemDetailResponseDto;
import hmw.ecommerce.entity.dto.ItemThumbnailResponseDto;
import hmw.ecommerce.entity.dto.MainItemViewDto;
import hmw.ecommerce.entity.vo.Const;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.exception.ItemException;
import hmw.ecommerce.exception.MemberException;
import hmw.ecommerce.jwt.JWTUtil;
import hmw.ecommerce.repository.CategoryRepository;
import hmw.ecommerce.repository.ItemCategoryRepository;
import hmw.ecommerce.repository.item.ItemRepository;
import hmw.ecommerce.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class ItemService {

    private static final int RANGE_START = 0;
    private static final int RANGE_END = 14;

    private final MemberRepository memberRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    public ItemRegisterDto.Response register(ItemRegisterDto.Request itemRegisterDto, String token) {
        String loginId = jwtUtil.extractLoginIdFromToken(token);

        Member findMember = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_LOGIN_ID));

        Item findItem = ItemRegisterDto.Request.toItemEntity(itemRegisterDto, findMember);
        itemRepository.save(findItem);

        Set<String> categoryNames = itemRegisterDto.getCategoryNames();
        for (String categoryName : categoryNames) {
            Category findCategory = getOrCreateCategory(categoryName);
            itemCategoryRepository.save(ItemCategory.createMapping(findItem, findCategory));
        }

        return ItemRegisterDto.Response.fromRequest(itemRegisterDto, findMember);
    }

    public List<Item> findItemsEagerlyByIds(Set<Long> top15ItemIds) {
        return itemRepository.findItemsEagerlyByIds(top15ItemIds);
    }

    public MainItemViewDto getItemMainPage() {
        HashOperations<String, Long, Object> hashOperations = redisTemplate.opsForHash();
        Map<Long, ItemThumbnailResponseDto> top15ItemsMap = getTop15ItemsToMap(hashOperations);

        Pageable pageable = PageRequest.of(RANGE_START, RANGE_END, Sort.by(Sort.Order.desc("createdAt")));
        Page<Item> recentItems = itemRepository.findByOrderByCreatedAtDesc(pageable);

        List<ItemThumbnailResponseDto> recentItemDtos = recentItems
                .map(item ->
                        ItemThumbnailResponseDto.fromItemEntity(item, item.getMember())
                ).getContent();

        MainItemViewDto mainItemViewDto = new MainItemViewDto(top15ItemsMap, recentItemDtos);
        return mainItemViewDto;
    }

    private Map<Long, ItemThumbnailResponseDto> getTop15ItemsToMap(HashOperations<String, Long, Object> hashOperations) {
        return hashOperations.entries(Const.TOP_RANKING_ITEM_KEY).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (ItemThumbnailResponseDto) entry.getValue()
                ));
    }

    public ItemDetailResponseDto getItemDetail(Long itemId) {
        Item findItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.NOT_EXISTS_ITEM));
        return ItemDetailResponseDto.fromEntity(findItem);
    }

    public Item findByItemId(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.NOT_EXISTS_ITEM));
    }

    private Category getOrCreateCategory(String categoryName) {
        return categoryRepository.findByCategoryName(categoryName)
                .orElseGet(() -> categoryRepository.save(getCategory(categoryName)));
    }

    private Category getCategory(String categoryName) {
        return ItemRegisterDto.Request.toCategoryEntity(categoryName);
    }



}