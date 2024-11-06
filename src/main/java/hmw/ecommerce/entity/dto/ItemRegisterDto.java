package hmw.ecommerce.entity.dto;


import hmw.ecommerce.entity.Category;
import hmw.ecommerce.entity.Item;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class ItemRegisterDto {

    @Getter
    @Builder
    public static class Request {
        @NotEmpty
        private String itemName;
        @NotEmpty
        private String itemDescription;

        @NotEmpty
        @Min(1000)
        private int price;

        @NotEmpty
        @Min(1)
        private int stockQuantity;

        private Set<String> categoryNames;

        public static Item toItemEntity(Request request) {
            return Item.builder()
                    .itemName(request.itemName)
                    .itemDescription(request.itemDescription)
                    .price(request.price)
                    .stockQuantity(request.stockQuantity)
                    .build();
        }

        public static Category toCategoryEntity(String categoryName) {
           return Category.builder()
                    .categoryName(categoryName)
                    .build();
        }

    }

    @Builder
    @AllArgsConstructor
    public static class Response {
        private String itemName;
        private int price;
        private int stockQuantity;
        private Set<String> categoryNames;

    }


}
