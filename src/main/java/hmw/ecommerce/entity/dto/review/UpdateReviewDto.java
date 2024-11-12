package hmw.ecommerce.entity.dto.review;

import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.Member;
import hmw.ecommerce.entity.Review;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UpdateReviewDto {

    @NotNull
    @Min(1)
    @Max(5)
    private int rating;

    @NotNull
    private String comment;

}
