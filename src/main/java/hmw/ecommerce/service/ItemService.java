package hmw.ecommerce.service;

import hmw.ecommerce.entity.Category;
import hmw.ecommerce.entity.CategoryType;
import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.Member;
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

import java.util.List;
import java.util.Map;
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

    /**
     * 아이템을 등록하는 메서드
     *
     * @param itemRegisterDto 아이템 등록 정보 DTO
     * @param token 인증된 사용자의 JWT 토큰
     * @return 등록된 아이템 정보
     */
    @Transactional
    public ItemRegisterDto.Response register(ItemRegisterDto.Request itemRegisterDto, String token) {
        String loginId = jwtUtil.extractLoginIdFromToken(token);

        Member findMember = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_LOGIN_ID));

        saveItemAndCategoryAndCategoryType(itemRegisterDto, findMember);

        return ItemRegisterDto.Response.fromRequest(itemRegisterDto, findMember);
    }

    /**
     * 메인 페이지에 표시할 아이템을 조회하는 메서드
     *
     * @return 메인 페이지에 표시할 아이템 목록과 순위
     */
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

    /**
     * 랭킹 상위 15개의 아이템을 Redis에서 조회하여 반환하는 메서드
     *
     * @param hashOperations Redis 해시 작업을 위한 객체
     * @return 상위 15개의 아이템
     */
    private Map<Long, ItemThumbnailResponseDto> getTop15ItemsToMap(HashOperations<String, Long, Object> hashOperations) {

        return hashOperations.entries(TOP_RANKING_ITEM_KEY).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (ItemThumbnailResponseDto) entry.getValue()
                ));
    }

    /**
     * 아이템의 상세 정보를 조회하는 메서드
     *
     * @param itemId 조회할 아이템의 ID
     * @return 아이템 상세 정보
     */
    public ItemDetailResponseDto getItemDetail(Long itemId) {
        Item item = itemRepository.findItemFetchMemberAndCategoryByItemId(itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.NOT_EXISTS_ITEM));
        return ItemDetailResponseDto.fromEntity(item, item.getCategory(), item.getCategoryType());
    }

    /**
     * 아이템 ID로 아이템을 찾는 메서드
     *
     * @param itemId 조회할 아이템의 ID
     * @return 찾은 아이템
     */
    public Item findByItemId(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.NOT_EXISTS_ITEM));
    }

    /**
     * 아이템을 삭제하는 메서드
     *
     * @param itemId 삭제할 아이템의 ID
     * @param token 인증된 사용자의 JWT 토큰
     * @return 삭제된 아이템 ID
     */
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

    /**
     * 아이템을 수정하는 메서드
     *
     * @param token 인증된 사용자의 JWT 토큰
     * @param itemId 수정할 아이템의 ID
     * @param updateForm 수정할 아이템 정보
     * @return 수정된 아이템 ID
     */
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

    /**
     * 카테고리 및 타입을 기준으로 아이템을 검색하는 메서드
     *
     * @param category 카테고리
     * @param type 타입
     * @param pageable 페이지 요청 정보
     * @return 검색된 아이템 목록
     */
    public List<ItemThumbnailResponseDto> searchItemByCategory(String category, String type, Pageable pageable) {
        Page<Item> itemByCategoryAndType = itemRepository.findItemByCategoryAndType(category, type, pageable);
        return itemByCategoryAndType
                .map(item -> ItemThumbnailResponseDto.fromItemEntity(item, item.getMember()))
                .toList();
    }

    /**
     * 아이템의 정보를 수정하고 랭킹 정보를 업데이트하는 메서드
     *
     * @param item 수정할 아이템
     * @param itemId 수정할 아이템 ID
     * @param updateForm 수정할 정보
     */
    private void updateItemInRanking(Item item, Long itemId, ItemUpdateForm updateForm) {
        item.changeItemInfo(updateForm);

        if (isExistInRanking(itemId)) {
            HashOperations<String, Object, ItemThumbnailResponseDto> hashOperations = redisTemplate.opsForHash();
            ItemThumbnailResponseDto updatedItemDto = ItemThumbnailResponseDto.fromItemEntity(item, item.getMember());
            hashOperations.put(TOP_RANKING_ITEM_KEY, itemId, updatedItemDto);
        }

    }

    /**
     * 아이템을 랭킹에서 삭제하는 메서드
     *
     * @param itemId 삭제할 아이템 ID
     */
    private void removeItemFromRanking(Long itemId) {
        if (isExistInRanking(itemId)) {
            HashOperations<String, Object, ItemThumbnailResponseDto> hashOperations = redisTemplate.opsForHash();
            ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
            hashOperations.delete(TOP_RANKING_ITEM_KEY, itemId);
            zSetOperations.remove(RANKING_KEY, itemId);
        }

        itemRepository.deleteById(itemId);
    }

    /**
     * 아이템이 랭킹에 존재하는지 확인하는 메서드
     *
     * @param itemId 확인할 아이템 ID
     * @return 아이템이 랭킹에 있으면 true, 아니면 false
     */
    private boolean isExistInRanking(Long itemId) {
        HashOperations<String, Object, ItemThumbnailResponseDto> hashOperations = redisTemplate.opsForHash();
        return hashOperations.hasKey(TOP_RANKING_ITEM_KEY, itemId);
    }

    /**
     * 아이템, 카테고리, 카테고리 타입을 저장하는 메서드
     *
     * @param itemRegisterDto 아이템 등록 정보 DTO
     * @param findMember 아이템 등록을 하는 사용자
     */
    private void saveItemAndCategoryAndCategoryType(ItemRegisterDto.Request itemRegisterDto, Member findMember) {
        String categoryName = itemRegisterDto.getCategoryName();
        Category category = categoryRepository.findByCategoryName(categoryName)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .categoryName(categoryName)
                        .build()));

        String type = itemRegisterDto.getType();
        CategoryType categoryType = categoryTypeRepository.findByTypeName(type)
                .orElseGet(() -> categoryTypeRepository
                        .save(CategoryType.toEntity(type, category)));


        itemRepository.save(ItemRegisterDto.Request.toItemEntity(itemRegisterDto, category, findMember, categoryType));
    }


}