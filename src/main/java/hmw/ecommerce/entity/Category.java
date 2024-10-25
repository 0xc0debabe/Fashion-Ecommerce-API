package hmw.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Category extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    private String categoryName;
}
