package hmw.ecommerce.entity.dto.order;

import hmw.ecommerce.entity.OrderItem;
import hmw.ecommerce.entity.vo.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class GetSellOrderDto {

    @Getter
    public static class Request {
        @NotNull
        private Integer page;
        @NotNull
        private Integer size;

        private Boolean pending;
        private Boolean canceled;
        private Boolean completed;
    }

    @Getter
    @Builder
    public static class Response {
        private String buyerId;
        private String sellerId;
        private int unitCount;
        private int unitPrice;
        private String itemName;
        private OrderStatus orderStatus;
        private LocalDateTime orderDate;

        public static Response fromEntity(OrderItem orderItem) {
            return GetSellOrderDto.Response.builder()
                    .buyerId(orderItem.getBuyerId())
                    .sellerId(orderItem.getSellerId())
                    .unitCount(orderItem.getUnitCount())
                    .unitPrice(orderItem.getUnitPrice())
                    .itemName(orderItem.getItemName())
                    .orderStatus(orderItem.getOrderStatus())
                    .orderDate(orderItem.getOrderDate())
                    .build();
        }
    }



}
