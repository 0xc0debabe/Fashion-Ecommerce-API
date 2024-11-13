package hmw.ecommerce.service;

import hmw.ecommerce.entity.*;
import hmw.ecommerce.entity.dto.Item.*;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.exception.exceptions.ItemException;
import hmw.ecommerce.exception.exceptions.MemberException;
import hmw.ecommerce.jwt.JWTUtil;
import hmw.ecommerce.repository.entity.CategoryRepository;
import hmw.ecommerce.repository.entity.CategoryTypeRepository;
import hmw.ecommerce.repository.entity.ItemRepository;
import hmw.ecommerce.repository.entity.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static hmw.ecommerce.entity.vo.Const.RANKING_KEY;
import static hmw.ecommerce.entity.vo.Const.TOP_RANKING_ITEM_KEY;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ItemService {

    private static final int RANGE_START = 0;
    private static final int RANGE_END = 14;
    public static int NEXT_RANK = 14;

    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryTypeRepository categoryTypeRepository;
    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public ItemRegisterDto.Response register(ItemRegisterDto.Request itemRegisterDto, String token) {
        String loginId = jwtUtil.extractLoginIdFromToken(token);

        Member findMember = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_LOGIN_ID));

        saveItemAndCategoryAndCategoryType(itemRegisterDto, findMember);

        return ItemRegisterDto.Response.fromRequest(itemRegisterDto, findMember);
    }

    public ItemMainViewDto getItemMainPage() {
        HashOperations<String, Long, Object> hashOperations = redisTemplate.opsForHash();
        Map<Long, ItemThumbnailResponseDto> top15ItemsMap = getTop15ItemsToMap(hashOperations);

        Pageable pageable = PageRequest.of(RANGE_START, RANGE_END, Sort.by(Sort.Order.desc("createdAt")));
        Page<Item> recentItems = itemRepository.findByOrderByCreatedAtDesc(pageable);

        List<ItemThumbnailResponseDto> recentItemDtos = recentItems
                .map(item -> ItemThumbnailResponseDto.fromItemEntity(item, item.getMember()))
                .getContent();

        return new ItemMainViewDto(top15ItemsMap, recentItemDtos);
    }

    private Map<Long, ItemThumbnailResponseDto> getTop15ItemsToMap(HashOperations<String, Long, Object> hashOperations) {
        return hashOperations.entries(TOP_RANKING_ITEM_KEY).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (ItemThumbnailResponseDto) entry.getValue()
                ));
    }

    public ItemDetailResponseDto getItemDetail(Long itemId) {
        Item item = itemRepository.findItemFetchMemberAndCategoryByItemId(itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.NOT_EXISTS_ITEM));
        return ItemDetailResponseDto.fromEntity(item, item.getCategory(), item.getCategoryType());
    }

    public Item findByItemId(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.NOT_EXISTS_ITEM));
    }

    @Transactional
    public Long removeItem(Long itemId, String token) {
        Item findItem = itemRepository.findItemFetchMemberByItemId(itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.NOT_EXISTS_ITEM));

        String loginId = jwtUtil.extractLoginIdFromToken(token);
        if (!findItem.getMember().getLoginId().equals(loginId)) {
            throw new ItemException(ErrorCode.INVALID_ACCESS);
        }

        removeItemFromRanking(itemId);
        return itemId;
    }

    @Transactional
    public Long modifyItem(String token, Long itemId, ItemUpdateForm updateForm) {
        Item findItem = itemRepository.findItemFetchMemberByItemId(itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.NOT_EXISTS_ITEM));

        String loginId = jwtUtil.extractLoginIdFromToken(token);
        if (!findItem.getMember().getLoginId().equals(loginId)) {
            throw new ItemException(ErrorCode.INVALID_ACCESS);
        }

        updateItemInRanking(findItem, itemId, updateForm);
        return findItem.getId();
    }

    public List<ItemThumbnailResponseDto> searchItemByCategory(String category, String type, Pageable pageable) {
        Page<Item> itemByCategoryAndType = itemRepository.findItemByCategoryAndType(category, type, pageable);
        return itemByCategoryAndType
                .map(item -> ItemThumbnailResponseDto.fromItemEntity(item, item.getMember()))
                .toList();
    }

    private void updateItemInRanking(Item item, Long itemId, ItemUpdateForm updateForm) {
        item.changeItemInfo(updateForm);

        if (isExistInRanking(itemId)) {
            HashOperations<String, Object, ItemThumbnailResponseDto> hashOperations = redisTemplate.opsForHash();
            ItemThumbnailResponseDto updatedItemDto = ItemThumbnailResponseDto.fromItemEntity(item, item.getMember());
            hashOperations.put(TOP_RANKING_ITEM_KEY, itemId, updatedItemDto);
        }

    }

    private void removeItemFromRanking(Long itemId) {
        if (isExistInRanking(itemId)) {
            HashOperations<String, Object, ItemThumbnailResponseDto> hashOperations = redisTemplate.opsForHash();
            ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
            hashOperations.delete(TOP_RANKING_ITEM_KEY, itemId);
            zSetOperations.remove(RANKING_KEY, itemId);
        }

        itemRepository.deleteById(itemId);
    }

    private boolean isExistInRanking(Long itemId) {
        HashOperations<String, Object, ItemThumbnailResponseDto> hashOperations = redisTemplate.opsForHash();
        return hashOperations.hasKey(TOP_RANKING_ITEM_KEY, itemId);
    }




    private void saveItemAndCategoryAndCategoryType(ItemRegisterDto.Request itemRegisterDto, Member findMember) {
        String categoryName = itemRegisterDto.getCategoryName();
        Category category = categoryRepository.findByCategoryName(categoryName)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .categoryName(categoryName)
//                        .categoryTypes(new ArrayList<>())
                        .build()));

        String type = itemRegisterDto.getType();
        CategoryType categoryType = categoryTypeRepository.findByTypeName(type)
                .orElseGet(() -> categoryTypeRepository
                        .save(CategoryType.toEntity(type, category)));


        itemRepository.save(ItemRegisterDto.Request.toItemEntity(itemRegisterDto, category, findMember, categoryType));
    }


}