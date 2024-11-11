package hmw.ecommerce.entity.dto.Item;


import hmw.ecommerce.entity.Category;
import hmw.ecommerce.entity.CategoryType;
import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.Member;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;


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
        private String categoryName;
        @NotEmpty
        private String type;


        public static Item toItemEntity(Request request, Category category, Member member, CategoryType categoryType) {
            return Item.builder()
                    .title(request.title)
                    .itemName(request.itemName)
                    .itemDescription(request.itemDescription)
                    .price(request.price)
                    .stockQuantity(request.stockQuantity)
                    .member(member)
                    .category(category)
                    .categoryType(categoryType)
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
        private String loginId;
        private String nickName;
        private String categoryName;
        private String type;

        public static Response fromRequest(Request request, Member member) {
            return Response.builder()
                    .title(request.getTitle())
                    .itemName(request.getItemName())
                    .price(request.getPrice())
                    .stockQuantity(request.getStockQuantity())
                    .loginId(member.getLoginId())
                    .nickName(member.getNickName())
                    .categoryName(request.getCategoryName())
                    .type(request.getType())
                    .build();
        }

    }


}
