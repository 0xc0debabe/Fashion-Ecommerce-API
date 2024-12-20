package hmw.ecommerce.repository;

import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.OrderItem;
import hmw.ecommerce.entity.Review;
import hmw.ecommerce.entity.dto.order.GetSellOrderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface QueryDslRepository {

    List<Item> findItemsFetchMemberByItemIds(Set<Long> top15ItemIds);

    Optional<Item> findItemFetchMemberByItemId(Long itemId);

    Optional<Item> findItemFetchMemberAndCategoryByItemId(Long itemId);

    Page<Item> findItemByCategoryAndType(String categoryName, String categoryType, Pageable pageable);

    Page<Review> findReviewLatestByItemId(Long itemId, Pageable pageable);

    Page<Review> findReviewOldestByItemId(Long itemId, Pageable pageable);

    Page<Review> findReviewsRatingAscByItemId(Long itemId, Pageable pageable);

    Page<Review> findReviewsRatingDescByItemId(Long itemId, Pageable pageable);

    Optional<OrderItem> findOrderItemByBuyerId(String loginId, Long itemId, Long orderId);

    Optional<OrderItem> findOrderItemBySellerId(String sellerId, Long itemId, Long orderId);

    Page<OrderItem> findSellLatestBySellerId(String loginId, GetSellOrderDto.Request request, Pageable pageable);
}
