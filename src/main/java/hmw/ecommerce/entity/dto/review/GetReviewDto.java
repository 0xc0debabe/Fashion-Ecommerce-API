package hmw.ecommerce.entity.dto.review;

import hmw.ecommerce.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
public class GetReviewDto {

    private int rating;
    private String comment;
    private LocalDateTime createAt;
    private String nickName;

    public static GetReviewDto fromEntity(Review review) {
        return GetReviewDto.builder()
                .rating(review.getRating())
                .comment(review.getComment())
                .createAt(review.getCreatedAt())
                .nickName(review.getMember().getNickName())
                .build();
    }

}
