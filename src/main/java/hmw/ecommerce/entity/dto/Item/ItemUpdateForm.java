package hmw.ecommerce.entity.dto.Item;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Builder
@Getter
public class ItemUpdateForm {

    @NotNull
    private String title;

    @NotNull
    private String itemName;

    @NotNull
    private String itemDescription;

    @NotNull
    @Min(1000)
    private int price;

    @NotNull
    @Min(1)
    private int stockQuantity;

    private Set<String> categoryNames;

}
