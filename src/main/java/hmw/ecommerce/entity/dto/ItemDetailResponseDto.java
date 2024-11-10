package hmw.ecommerce.entity.dto;

import hmw.ecommerce.entity.Category;
import hmw.ecommerce.entity.CategoryType;
import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ItemDetailResponseDto {

    private String title;
    private String itemName;
    private String itemDescription;
    private int price;
    private int stockQuantity;

    private String loginId;
    private String nickName;

    private String categoryName;
    private String typeName;


    public static ItemDetailResponseDto fromEntity(Item item, Category category, CategoryType categoryType) {
        return ItemDetailResponseDto.builder()
                .title(item.getTitle())
                .itemName(item.getItemName())
                .itemDescription(item.getItemDescription())
                .price(item.getPrice())
                .stockQuantity(item.getStockQuantity())
                .loginId(item.getMember().getLoginId())
                .nickName(item.getMember().getNickName())
                .categoryName(category.getCategoryName())
                .typeName(categoryType.getTypeName())
                .build();
    }

}
