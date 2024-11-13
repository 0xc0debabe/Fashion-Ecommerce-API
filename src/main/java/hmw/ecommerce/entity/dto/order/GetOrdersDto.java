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

        private String itemName;
        private int price;
        private int count;
        private OrderStatus orderStatus;

        public static Response fromEntity(OrderItem orderItem) {
            return Response.builder()
                    .orderDate(orderItem.getOrderDate())
                    .itemName(orderItem.getItemName())
                    .price(orderItem.getOrderPrice())
                    .count(orderItem.getOrderQuantity())
                    .orderStatus(orderItem.getOrderStatus())
                    .build();
        }

    }

}
