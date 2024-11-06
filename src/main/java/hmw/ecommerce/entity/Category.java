package hmw.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Entity
@Builder
@Getter
public class Category extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    private String categoryName;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCategory> itemCategories;

    public void addItemCategories(Item item, Category category) {
        this.itemCategories.add(ItemCategory.builder()
                        .category(category)
                        .item(item)
                        .build());
    }

}
