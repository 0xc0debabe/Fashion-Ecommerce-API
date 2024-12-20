package hmw.ecommerce.entity.dto.cart;

import hmw.ecommerce.entity.Item;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.io.Serializable;

public class AddToCartDto {

    @Getter
    public static class Request{
        @Min(1)
        private int count;
    }

    @Builder
    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    public static class Response implements Serializable {
        private Long itemId;
        private String itemName;
        private int price;
        private int count;

        public static AddToCartDto.Response fromItemEntity(Item item, int count) {
            return Response.builder()
                    .itemId(item.getId())
                    .itemName(item.getItemName())
                    .price(item.getPrice())
                    .count(count)
                    .build();
        }
    }

}
