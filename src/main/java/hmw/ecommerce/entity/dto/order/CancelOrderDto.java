package hmw.ecommerce.entity.dto.order;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CancelOrderDto {

    @NotNull
    private Long orderId;
    @NotNull
    private Long itemId;

}

