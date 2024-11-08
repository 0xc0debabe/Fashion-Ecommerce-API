package hmw.ecommerce.entity.dto;


import hmw.ecommerce.entity.Category;
import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.Member;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ItemRegisterDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @NotEmpty
        private String title;
        @NotEmpty
        private String itemName;
        @NotEmpty
        private String itemDescription;

        @Min(1000)
        @NotNull
        private int price;

        @Min(1)
        @NotNull
        private int stockQuantity;

        @NotEmpty
        private Set<String> categoryNames;

        public static Item toItemEntity(Request request, Member member) {
            return Item.builder()
                    .title(request.title)
                    .itemName(request.itemName)
                    .itemDescription(request.itemDescription)
                    .price(request.price)
                    .stockQuantity(request.stockQuantity)
                    .member(member)
                    .build();
        }

        public static Category toCategoryEntity(String categoryName) {
           return Category.builder()
                    .categoryName(categoryName)
                    .build();
        }

    }

    @Builder
    @Getter
    public static class Response {
        private String title;
        private String itemName;
        private int price;
        private int stockQuantity;
        private Set<String> categoryNames;
        private String loginId;
        private String nickName;

        public static Response fromRequest(Request request, Member member) {
            return Response.builder()
                    .title(request.getTitle())
                    .itemName(request.getItemName())
                    .price(request.getPrice())
                    .stockQuantity(request.getStockQuantity())
                    .categoryNames(request.getCategoryNames())
                    .loginId(member.getLoginId())
                    .nickName(member.getNickName())
                    .build();
        }
    }


}
