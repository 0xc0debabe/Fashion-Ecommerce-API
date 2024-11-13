package hmw.ecommerce.entity;

import hmw.ecommerce.entity.dto.Item.ItemUpdateForm;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    private String title;
    private String itemName;
    private String itemDescription;
    private int price;
    private int stockQuantity;

    private int viewCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_type_id")
    private CategoryType categoryType;

    public void addViewCount() {
        this.viewCount += 1;
    }

    public void changeItemInfo(ItemUpdateForm updateForm) {
        this.title = updateForm.getTitle();
        this.itemName = updateForm.getItemName();
        this.itemDescription = updateForm.getItemDescription();
        this.price = updateForm.getPrice();
        this.stockQuantity = updateForm.getStockQuantity();
    }

    public void decreaseStock(int count) {
        this.stockQuantity -= count;
    }

    public void increaseStock(int count) {
        this.stockQuantity += count;
    }

    public boolean isStockAvailability(int count) {
        if (this.getStockQuantity() - count < 0) {
            return false;
        }

        return true;
    }

}