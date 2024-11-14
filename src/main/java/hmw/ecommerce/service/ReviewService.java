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

    public Page<GetReviewDto> getReviewsByLatest(Long itemId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Review> reviewByItem = reviewRepository.findReviewLatestByItemId(itemId, pageable);
        return reviewByItem.map(GetReviewDto::fromEntity);
    }

    public Page<GetReviewDto> getReviewsByOldest(Long itemId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Review> reviewByItem = reviewRepository.findReviewOldestByItemId(itemId, pageable);
        return reviewByItem.map(GetReviewDto::fromEntity);
    }

    public Page<GetReviewDto> getReviewsByRatingAsc(Long itemId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Review> reviewByItem = reviewRepository.findReviewsRatingAscByItemId(itemId, pageable);
        return reviewByItem.map(GetReviewDto::fromEntity);
    }

    public Page<GetReviewDto> getReviewsByRatingDesc(Long itemId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Review> reviewByItem = reviewRepository.findReviewsRatingDescByItemId(itemId, pageable);
        return reviewByItem.map(GetReviewDto::fromEntity);
    }

}
