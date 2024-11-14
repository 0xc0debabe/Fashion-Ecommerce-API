package hmw.ecommerce.entity.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CompleteOrderDto {

    @NotNull
    private Long itemId;
    @NotNull
    private Long orderId;

}
