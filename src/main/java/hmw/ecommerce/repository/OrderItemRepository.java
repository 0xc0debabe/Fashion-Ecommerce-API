package hmw.ecommerce.repository;

import hmw.ecommerce.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>, QueryDslRepository {
    Page<OrderItem> findOrderItemsByLoginId(String loginId, Pageable pageable);

}
