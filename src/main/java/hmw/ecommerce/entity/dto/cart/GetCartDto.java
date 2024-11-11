package hmw.ecommerce.entity.dto.cart;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Builder
@Getter
public class GetCartDto {

    private Set<AddToCartDto.Response> addToCartDtos;
    private int totalPrice;
    private int totalCount;

    public static GetCartDto getCartDtoResponse(
            Set<AddToCartDto.Response> responseSet,
            int totalPrice,
            int totalCount) {
        return GetCartDto.builder()
                .addToCartDtos(responseSet)
                .totalCount(totalCount)
                .totalPrice(totalPrice)
                .build();
    }
}
