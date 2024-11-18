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

    /**
     * 상품에 대한 리뷰를 생성하는 메서드.
     * 인증된 회원만 리뷰를 생성할 수 있습니다.
     *
     * @param token 로그인한 사용자의 인증 토큰 (JWT).
     * @param itemId 상품 ID.
     * @param reviewRequest 리뷰 생성 요청 DTO.
     * @param bindingResult 유효성 검사 결과.
     * @return 생성된 리뷰 정보.
     */
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

    /**
     * 기존의 리뷰를 업데이트하는 메서드.
     * 인증된 회원만 자신이 작성한 리뷰를 수정할 수 있습니다.
     *
     * @param token 로그인한 사용자의 인증 토큰 (JWT).
     * @param reviewId 리뷰 ID.
     * @param reviewRequest 리뷰 수정 요청 DTO.
     * @param bindingResult 유효성 검사 결과.
     * @return 업데이트된 리뷰 정보.
     */
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

    /**
     * 기존의 리뷰를 삭제하는 메서드.
     * 인증된 회원만 자신이 작성한 리뷰를 삭제할 수 있습니다.
     *
     * @param token 로그인한 사용자의 인증 토큰 (JWT).
     * @param reviewId 리뷰 ID.
     * @return 삭제된 리뷰 ID.
     */
    @PostAuthorize(HAS_ROLE_MEMBER)
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @RequestHeader(name = Const.AUTHORIZATION) String token,
            @PathVariable(name = "reviewId") Long reviewId
    ) {
        return ResponseEntity.ok(reviewService.deleteReview(token, reviewId));
    }

    /**
     * 최신순으로 리뷰를 조회하는 메서드.
     *
     * @param itemId 상품 ID.
     * @param page 페이지 번호.
     * @param size 한 페이지당 리뷰 개수.
     * @return 최신순으로 정렬된 리뷰 목록.
     */
    @GetMapping("/{itemId}/latest")
    public ResponseEntity<?> getReviewsByLatest(
            @PathVariable(name = "itemId") Long itemId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByLatest(itemId, page, size));
    }

    /**
     * 오래된순으로 리뷰를 조회하는 메서드.
     *
     * @param itemId 상품 ID.
     * @param page 페이지 번호.
     * @param size 한 페이지당 리뷰 개수.
     * @return 오래된순으로 정렬된 리뷰 목록.
     */
    @GetMapping("/{itemId}/oldest")
    public ResponseEntity<?> getReviewsByOldest(
            @PathVariable(name = "itemId") Long itemId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByOldest(itemId, page, size));
    }

    /**
     * 평점 오름차순으로 리뷰를 조회하는 메서드.
     *
     * @param itemId 상품 ID.
     * @param page 페이지 번호.
     * @param size 한 페이지당 리뷰 개수.
     * @return 평점 오름차순으로 정렬된 리뷰 목록.
     */
    @GetMapping("/{itemId}/ratingAsc")
    public ResponseEntity<?> getReviewsByRatingAsc(
            @PathVariable(name = "itemId") Long itemId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByRatingAsc(itemId, page, size));
    }

    /**
     * 평점 내림차순으로 리뷰를 조회하는 메서드.
     *
     * @param itemId 상품 ID.
     * @param page 페이지 번호.
     * @param size 한 페이지당 리뷰 개수.
     * @return 평점 내림차순으로 정렬된 리뷰 목록.
     */
    @GetMapping("/{itemId}/ratingDesc")
    public ResponseEntity<?> getReviewsByRatingDesc(
            @PathVariable(name = "itemId") Long itemId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByRatingDesc(itemId, page, size));
    }

}