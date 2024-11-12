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

public class AddReviewDto {

    @Getter
    @Builder
    public static class Request {

        @NotNull
        @Min(1)
        @Max(5)
        private int rating;

        @NotEmpty
        private String comment;

        public Review toEntity(Item item, Member member) {
            return Review.builder()
                    .rating(this.rating)
                    .comment(this.comment)
                    .item(item)
                    .member(member)
                    .build();
        }

    }

    @Getter
    @Builder
    public static class Response {
        private String loginId;
        private int rating;
        private String comment;

        public static Response fromEntity(Review review) {
            return Response.builder()
                    .loginId(review.getMember().getLoginId())
                    .rating(review.getRating())
                    .comment(review.getComment())
                    .build();
        }

    }

}
