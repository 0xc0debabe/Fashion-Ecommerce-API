package hmw.ecommerce.controller;

import hmw.ecommerce.entity.dto.review.AddReviewDto;
import hmw.ecommerce.entity.dto.review.UpdateReviewDto;
import hmw.ecommerce.entity.vo.Const;
import hmw.ecommerce.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static hmw.ecommerce.entity.vo.Const.HAS_ROLE_MEMBER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;

    @PostAuthorize(HAS_ROLE_MEMBER)
    @PostMapping("/{itemId}")
    public ResponseEntity<?> createReview(
            @RequestHeader(name = Const.AUTHORIZATION) String token,
            @PathVariable(name = "itemId") Long itemId,
            @Valid @RequestBody AddReviewDto.Request reviewRequest,
            BindingResult bindingResult
    ) {
        return ResponseEntity.ok(reviewService.createReview(token, reviewRequest, itemId));
    }

    @PostAuthorize(HAS_ROLE_MEMBER)
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @RequestHeader(name = Const.AUTHORIZATION) String token,
            @PathVariable(name = "reviewId") Long reviewId,
            @Valid @RequestBody UpdateReviewDto reviewRequest,
            BindingResult bindingResult
    ) {
        return ResponseEntity.ok(reviewService.updateReview(token, reviewId, reviewRequest));
    }

    @PostAuthorize(HAS_ROLE_MEMBER)
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @RequestHeader(name = Const.AUTHORIZATION) String token,
            @PathVariable(name = "reviewId") Long reviewId
    ) {
        return ResponseEntity.ok(reviewService.deleteReview(token, reviewId));
    }

    @GetMapping("/{itemId}/latest")
    public ResponseEntity<?> getReviewsByLatest(
            @PathVariable(name = "itemId") Long itemId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByLatest(itemId, page, size));
    }

    @GetMapping("/{itemId}/oldest")
    public ResponseEntity<?> getReviewsByOldest(
            @PathVariable(name = "itemId") Long itemId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByOldest(itemId, page, size));
    }

    @GetMapping("/{itemId}/ratingAsc")
    public ResponseEntity<?> getReviewsByRatingAsc(
            @PathVariable(name = "itemId") Long itemId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByRatingAsc(itemId, page, size));
    }

    @GetMapping("/{itemId}/ratingDesc")
    public ResponseEntity<?> getReviewsByRatingDesc(
            @PathVariable(name = "itemId") Long itemId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByRatingDesc(itemId, page, size));
    }

}