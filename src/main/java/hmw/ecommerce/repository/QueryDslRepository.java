package hmw.ecommerce.repository;

import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.OrderItem;
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

    Optional<OrderItem> findOrderItemByLoginId(String loginId, Long itemId, Long orderId);
}
