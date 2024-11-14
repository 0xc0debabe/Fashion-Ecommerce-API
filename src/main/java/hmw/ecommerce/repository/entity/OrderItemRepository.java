package hmw.ecommerce.repository.entity;

import hmw.ecommerce.entity.OrderItem;
import hmw.ecommerce.repository.QueryDslRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>, QueryDslRepository {

    Page<OrderItem> findOrderItemsByBuyerId(String buyerId, Pageable pageable);
}
