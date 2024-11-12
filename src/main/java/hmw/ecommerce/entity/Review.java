package hmw.ecommerce.entity;

import hmw.ecommerce.entity.dto.review.UpdateReviewDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private int rating;
    private String comment;

    public void updateReview(UpdateReviewDto request) {
        this.setComment(request.getComment());
        this.setRating(request.getRating());
    }

    private void setComment(String comment) {
        this.comment = comment;
    }

    private void setRating(int rating) {
        this.rating = rating;
    }
}
