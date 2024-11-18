package hmw.ecommerce.service;

import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.Member;
import hmw.ecommerce.entity.Review;
import hmw.ecommerce.entity.dto.review.AddReviewDto;
import hmw.ecommerce.entity.dto.review.GetReviewDto;
import hmw.ecommerce.entity.dto.review.UpdateReviewDto;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.exception.exceptions.ItemException;
import hmw.ecommerce.exception.exceptions.MemberException;
import hmw.ecommerce.exception.exceptions.ReviewException;
import hmw.ecommerce.jwt.JWTUtil;
import hmw.ecommerce.repository.entity.ReviewRepository;
import hmw.ecommerce.repository.entity.ItemRepository;
import hmw.ecommerce.repository.entity.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final ReviewRepository reviewRepository;
    private final JWTUtil jwtUtil;

    /**
     * 리뷰를 생성하는 메서드.
     * 인증된 사용자가 상품에 대한 리뷰를 작성할 수 있습니다.
     *
     * @param token 로그인한 사용자의 인증 토큰 (JWT).
     * @param reviewRequest 리뷰 생성 요청 DTO.
     * @param itemId 리뷰 대상 상품 ID.
     * @return 생성된 리뷰 정보.
     */
    @Transactional
    public AddReviewDto.Response createReview(
            String token,
            AddReviewDto.Request reviewRequest, Long itemId) {
        String loginId = jwtUtil.extractLoginIdFromToken(token);
        Member findMember = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_LOGIN_ID));
        Item findItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.NOT_EXISTS_ITEM));
        Review savedReview = reviewRepository.save(reviewRequest.toEntity(findItem, findMember));
        return AddReviewDto.Response.fromEntity(savedReview);
    }

    /**
     * 리뷰를 업데이트하는 메서드.
     * 인증된 회원만 자신이 작성한 리뷰를 수정할 수 있습니다.
     *
     * @param token 로그인한 사용자의 인증 토큰 (JWT).
     * @param reviewId 수정할 리뷰 ID.
     * @param reviewRequest 리뷰 수정 요청 DTO.
     * @return 업데이트된 리뷰 DTO.
     */
    @Transactional
    public UpdateReviewDto updateReview(
            String token,
            Long reviewId,
            UpdateReviewDto reviewRequest) {
        String loginId = jwtUtil.extractLoginIdFromToken(token);
        Review findReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(ErrorCode.NOT_EXIST_REVIEW));
        if (!findReview.getMember().getLoginId().equals(loginId)) {
            throw new ReviewException(ErrorCode.UNAUTHORIZED_UPDATE_REVIEW);
        }

        findReview.updateReview(reviewRequest);
        return reviewRequest;
    }

    /**
     * 리뷰를 삭제하는 메서드.
     * 인증된 회원만 자신이 작성한 리뷰를 삭제할 수 있습니다.
     *
     * @param token 로그인한 사용자의 인증 토큰 (JWT).
     * @param reviewId 삭제할 리뷰 ID.
     * @return 삭제된 리뷰 ID.
     */
    @Transactional
    public Long deleteReview(String token, Long reviewId) {
        String loginId = jwtUtil.extractLoginIdFromToken(token);
        Review findReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(ErrorCode.NOT_EXIST_REVIEW));
        if (!findReview.getMember().getLoginId().equals(loginId)) {
            throw new ReviewException(ErrorCode.UNAUTHORIZED_UPDATE_REVIEW);
        }
        reviewRepository.delete(findReview);
        return reviewId;
    }

    /**
     * 최신 순으로 리뷰를 조회하는 메서드.
     *
     * @param itemId 상품 ID.
     * @param page 페이지 번호.
     * @param size 한 페이지당 리뷰 개수.
     * @return 최신 순으로 정렬된 리뷰 목록.
     */
    public Page<GetReviewDto> getReviewsByLatest(Long itemId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Review> reviewByItem = reviewRepository.findReviewLatestByItemId(itemId, pageable);
        return reviewByItem.map(GetReviewDto::fromEntity);
    }

    /**
     * 오래된 순으로 리뷰를 조회하는 메서드.
     *
     * @param itemId 상품 ID.
     * @param page 페이지 번호.
     * @param size 한 페이지당 리뷰 개수.
     * @return 오래된 순으로 정렬된 리뷰 목록.
     */
    public Page<GetReviewDto> getReviewsByOldest(Long itemId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Review> reviewByItem = reviewRepository.findReviewOldestByItemId(itemId, pageable);
        return reviewByItem.map(GetReviewDto::fromEntity);
    }

    /**
     * 평점 오름차순으로 리뷰를 조회하는 메서드.
     *
     * @param itemId 상품 ID.
     * @param page 페이지 번호.
     * @param size 한 페이지당 리뷰 개수.
     * @return 평점 오름차순으로 정렬된 리뷰 목록.
     */
    public Page<GetReviewDto> getReviewsByRatingAsc(Long itemId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Review> reviewByItem = reviewRepository.findReviewsRatingAscByItemId(itemId, pageable);
        return reviewByItem.map(GetReviewDto::fromEntity);
    }

    /**
     * 평점 내림차순으로 리뷰를 조회하는 메서드.
     *
     * @param itemId 상품 ID.
     * @param page 페이지 번호.
     * @param size 한 페이지당 리뷰 개수.
     * @return 평점 내림차순으로 정렬된 리뷰 목록.
     */
    public Page<GetReviewDto> getReviewsByRatingDesc(Long itemId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Review> reviewByItem = reviewRepository.findReviewsRatingDescByItemId(itemId, pageable);
        return reviewByItem.map(GetReviewDto::fromEntity);
    }

}
