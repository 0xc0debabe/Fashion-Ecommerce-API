package hmw.ecommerce.entity;

import hmw.ecommerce.entity.vo.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    private int count;
    private int price;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public static Order createOrder(Member member, int count, int price, OrderStatus orderStatus) {
        return Order.builder()
                .count(count)
                .price(price)
                .orderDate(LocalDateTime.now())
                .orderStatus(orderStatus)
                .member(member)
                .build();
    }
}

