package hmw.ecommerce.scheduler;

import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.dto.ItemThumbnailResponseDto;
import hmw.ecommerce.entity.vo.Const;
import hmw.ecommerce.repository.entity.ItemRepository;
import hmw.ecommerce.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
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

//    @Scheduled(cron = "0 0/26 * * * *", zone = "Asia/Seoul")
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


    @Transactional(readOnly = true)
    private List<ItemThumbnailResponseDto> getTop15Items() {
        List<Item> itemsByIds = itemRepository.findItemsFetchMemberByItemIds(getTop15LongItemIds());
        return itemsByIds.stream()
                .map(item -> ItemThumbnailResponseDto.fromItemEntity(item, item.getMember()))
                .collect(Collectors.toList());
    }

    private Set<Long> getTop15LongItemIds() {
        Set<Object> top15ObjectItemIds = redisTemplate.opsForZSet().reverseRange(Const.RANKING_KEY, RANGE_START, RANGE_END);
        return top15ObjectItemIds != null
                ? top15ObjectItemIds.stream()
                .map(obj -> Long.valueOf((Integer) obj))
                .collect(Collectors.toSet())
                : null;
    }

}