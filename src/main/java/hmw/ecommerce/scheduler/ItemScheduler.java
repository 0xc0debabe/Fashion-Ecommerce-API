package hmw.ecommerce.scheduler;

import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.dto.Item.ItemThumbnailResponseDto;
import hmw.ecommerce.entity.vo.Const;
import hmw.ecommerce.repository.entity.ItemRepository;
import hmw.ecommerce.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ItemScheduler {

    private static final long RANGE_START = 0;
    private static final long RANGE_END = 14;

    private final ItemRepository itemRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 매주 월요일 오전 6시에 Top 15 아이템을 Redis에 갱신합니다.
     */
    @Scheduled(cron = "0 0 6 * * MON", zone = "Asia/Seoul")
    public void showTop15Items() {
        ItemService.NEXT_RANK = 14;
        HashOperations<String, Long, Object> hashOperation = redisTemplate.opsForHash();
        List<ItemThumbnailResponseDto> top15Items = getTop15Items();

        top15Items.forEach(ItemThumbnailResponseDto ->
                hashOperation.put(
                        Const.TOP_RANKING_ITEM_KEY, ItemThumbnailResponseDto.getItemId(), ItemThumbnailResponseDto));
        redisTemplate.expire(Const.TOP_RANKING_ITEM_KEY, 7, TimeUnit.DAYS);
        redisTemplate.delete(Const.RANKING_KEY);
    }

    /**
     * Top 15 아이템을 DB에서 조회하여 ItemThumbnailResponseDto로 변환하여 반환합니다.
     *
     * @return Top 15 아이템 리스트
     */
    @Transactional(readOnly = true)
    private List<ItemThumbnailResponseDto> getTop15Items() {
        List<Item> itemsByIds = itemRepository.findItemsFetchMemberByItemIds(getTop15LongItemIds());
        return itemsByIds.stream()
                .map(item -> ItemThumbnailResponseDto.fromItemEntity(item, item.getMember()))
                .collect(Collectors.toList());
    }

    /**
     * Redis에서 Top 15 아이템 ID를 가져옵니다.
     *
     * @return Top 15 아이템의 ID 목록
     */
    private Set<Long> getTop15LongItemIds() {
        Set<Object> top15ObjectItemIds = redisTemplate.opsForZSet().reverseRange(Const.RANKING_KEY, RANGE_START, RANGE_END);
        return top15ObjectItemIds != null
                ? top15ObjectItemIds.stream()
                .map(obj -> Long.valueOf((Integer) obj))
                .collect(Collectors.toSet())
                : null;
    }

}