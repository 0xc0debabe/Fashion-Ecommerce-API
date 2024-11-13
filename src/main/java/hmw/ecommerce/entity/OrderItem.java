package hmw.ecommerce.entity;

import hmw.ecommerce.entity.vo.OrderStatus;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.exception.exceptions.OrderException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    private String loginId;
    private int unitCount;
    private int unitPrice;
    private String itemName;
    private OrderStatus orderStatus;
    private LocalDateTime orderDate;

    public static OrderItem toEntity(Order order, Item item, int count, int price, String loginId) {
        return OrderItem.builder()
                .order(order)
                .item(item)
                .unitCount(count)
                .unitPrice(price)
                .itemName(item.getItemName())
                .orderStatus(OrderStatus.ORDERED)
                .orderDate(order.getOrderDate())
                .loginId(loginId)
                .build();
    }

    public void orderCancel() {
        if (this.orderStatus == OrderStatus.CANCELED) {
            throw new OrderException(ErrorCode.ALREADY_CANCELED);
        }

        this.orderStatus = OrderStatus.CANCELED;
    }

}
