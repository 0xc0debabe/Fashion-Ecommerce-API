package hmw.ecommerce.entity.dto.order;

import hmw.ecommerce.entity.OrderItem;
import hmw.ecommerce.entity.vo.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class GetOrdersDto {

    @Getter
    public static class Request {
        @NotNull
        int page;
        @NotNull
        int size;
    }

    @Builder
    @Getter
    public static class Response {
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDateTime orderDate;
        private Long orderId;
        private Long itemId;
        private Long orderItemId;
        private String itemName;
        private int price;
        private int count;
        private OrderStatus orderStatus;

        public static Response fromEntity(OrderItem orderItem) {
            return Response.builder()
                    .orderId(orderItem.getItem().getId())
                    .itemId(orderItem.getItem().getId())
                    .orderItemId(orderItem.getId())
                    .orderDate(orderItem.getOrderDate())
                    .itemName(orderItem.getItemName())
                    .price(orderItem.getUnitPrice())
                    .count(orderItem.getUnitCount())
                    .orderStatus(orderItem.getOrderStatus())
                    .build();
        }

    }

}
