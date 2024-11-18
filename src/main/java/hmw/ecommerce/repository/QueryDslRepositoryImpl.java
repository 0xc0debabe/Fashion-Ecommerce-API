package hmw.ecommerce.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hmw.ecommerce.entity.*;
import hmw.ecommerce.entity.dto.order.GetSellOrderDto;
import hmw.ecommerce.entity.vo.OrderStatus;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static hmw.ecommerce.entity.QCategory.category;
import static hmw.ecommerce.entity.QCategoryType.categoryType;
import static hmw.ecommerce.entity.QItem.item;
import static hmw.ecommerce.entity.QMember.*;
import static hmw.ecommerce.entity.QOrderItem.orderItem;
import static hmw.ecommerce.entity.QReview.*;

@Slf4j
public class QueryDslRepositoryImpl implements QueryDslRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public QueryDslRepositoryImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 주어진 아이템 ID 목록에 해당하는 아이템을 멤버 정보와 함께 가져옵니다.
     *
     * @param top15ItemIds 아이템 ID 목록
     * @return 아이템 리스트
     */
    @Override
    public List<Item> findItemsFetchMemberByItemIds(Set<Long> top15ItemIds) {
        return queryFactory
                .select(item)
                .from(item)
                .join(item.member)
                .fetchJoin()
                .where(item.id.in(top15ItemIds))
                .fetch();
    }

    /**
     * 주어진 아이템 ID로 아이템과 그 아이템의 멤버 정보를 가져옵니다.
     *
     * @param itemId 아이템 ID
     * @return 아이템과 그에 해당하는 멤버 정보
     */
    @Override
    public Optional<Item> findItemFetchMemberByItemId(Long itemId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(item)
                        .join(item.member)
                        .fetchJoin()
                        .where(item.id.eq(itemId))
                        .fetchOne()
        );
    }

    /**
     * 주어진 아이템 ID로 아이템, 멤버, 카테고리 정보를 모두 함께 가져옵니다.
     *
     * @param itemId 아이템 ID
     * @return 아이템, 멤버, 카테고리 정보
     */
    @Override
    public Optional<Item> findItemFetchMemberAndCategoryByItemId(Long itemId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(item)
                        .join(item.member)
                        .join(item.category)
                        .join(item.categoryType)
                        .fetchJoin()
                        .where(item.id.eq(itemId))
                        .fetchOne());
    }

    /**
     * 카테고리와 타입에 맞는 아이템을 페이징 처리하여 가져옵니다.
     *
     * @param categoryName 카테고리 이름
     * @param type 타입 이름
     * @param pageable 페이징 정보
     * @return 해당 조건에 맞는 아이템 리스트
     */
    @Override
    public Page<Item> findItemByCategoryAndType(String categoryName, String type, Pageable pageable) {
        List<Item> items = queryFactory
                .selectFrom(item)
                .leftJoin(item.member).fetchJoin()
                .leftJoin(item.category, category).fetchJoin()
                .leftJoin(item.categoryType, categoryType).fetchJoin()
                .where(
                        categoryNameEq(categoryName),
                        typeNameEq(type)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(item.count())
                .leftJoin(item.category, category).fetchJoin()
                .leftJoin(item.categoryType, categoryType).fetchJoin()
                .where(
                        categoryNameEq(categoryName),
                        typeNameEq(type)
                );

        return PageableExecutionUtils.getPage(items, pageable, countQuery::fetchOne);
    }

    /**
     * 아이템 ID에 해당하는 최신 리뷰들을 페이징 처리하여 가져옵니다.
     *
     * @param itemId 아이템 ID
     * @param pageable 페이징 정보
     * @return 최신 리뷰 리스트
     */
    @Override
    public Page<Review> findReviewLatestByItemId(Long itemId, Pageable pageable) {
        List<Review> reviews = queryFactory
                .select(review)
                .from(review)
                .leftJoin(review.member, member).fetchJoin()
                .where(item.id.eq(itemId))
                .orderBy(review.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(review.count())
                .from(review)
                .where(item.id.eq(itemId));

        return PageableExecutionUtils.getPage(reviews, pageable, countQuery::fetchOne);
    }

    /**
     * 아이템 ID에 해당하는 오래된 리뷰들을 페이징 처리하여 가져옵니다.
     *
     * @param itemId 아이템 ID
     * @param pageable 페이징 정보
     * @return 오래된 리뷰 리스트
     */
    @Override
    public Page<Review> findReviewOldestByItemId(Long itemId, Pageable pageable) {
        List<Review> reviews = queryFactory
                .select(review)
                .from(review)
                .leftJoin(review.member, member).fetchJoin()
                .where(item.id.eq(itemId))
                .orderBy(review.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(review.count())
                .from(review)
                .where(item.id.eq(itemId));

        return PageableExecutionUtils.getPage(reviews, pageable, countQuery::fetchOne);
    }

    /**
     * 아이템 ID에 해당하는 평점이 낮은 리뷰들을 페이징 처리하여 가져옵니다.
     *
     * @param itemId 아이템 ID
     * @param pageable 페이징 정보
     * @return 평점이 낮은 리뷰 리스트
     */
    @Override
    public Page<Review> findReviewsRatingAscByItemId(Long itemId, Pageable pageable) {
        List<Review> reviews = queryFactory
                .select(review)
                .from(review)
                .leftJoin(review.member, member).fetchJoin()
                .where(item.id.eq(itemId))
                .orderBy(review.rating.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(review.count())
                .from(review)
                .where(item.id.eq(itemId));

        return PageableExecutionUtils.getPage(reviews, pageable, countQuery::fetchOne);
    }

    /**
     * 아이템 ID에 해당하는 평점이 높은 리뷰들을 페이징 처리하여 가져옵니다.
     *
     * @param itemId 아이템 ID
     * @param pageable 페이징 정보
     * @return 평점이 높은 리뷰 리스트
     */
    @Override
    public Page<Review> findReviewsRatingDescByItemId(Long itemId, Pageable pageable) {
        List<Review> reviews = queryFactory
                .select(review)
                .from(review)
                .leftJoin(review.member, member).fetchJoin()
                .where(item.id.eq(itemId))
                .orderBy(review.rating.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(review.count())
                .from(review)
                .where(item.id.eq(itemId));

        return PageableExecutionUtils.getPage(reviews, pageable, countQuery::fetchOne);
    }

    /**
     * 판매자 ID에 해당하는 최신 주문 아이템을 페이징 처리하여 가져옵니다.
     *
     * @param sellerId 판매자 ID
     * @param request 주문 조건
     * @param pageable 페이징 정보
     * @return 판매자의 최신 주문 아이템 리스트
     */
    @Override
    public Page<OrderItem> findSellLatestBySellerId(String sellerId, GetSellOrderDto.Request request, Pageable pageable) {
        List<OrderItem> orderItems =
                queryFactory
                        .selectFrom(orderItem)
                .where(
                        orderItem.sellerId.eq(sellerId),
                        orderStatusEq(request)
                )
                .orderBy(orderItem.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(orderItem.count())
                .from(orderItem)
                .where(
                        orderItem.sellerId.eq(sellerId),
                        orderStatusEq(request)
                );

        return PageableExecutionUtils.getPage(orderItems, pageable, countQuery::fetchOne);
    }

    /**
     * 구매자 ID, 아이템 ID, 주문 ID에 해당하는 주문 아이템을 가져옵니다.
     *
     * @param buyerId 구매자 ID
     * @param itemId 아이템 ID
     * @param orderId 주문 ID
     * @return 해당 주문 아이템
     */
    @Override
    public Optional<OrderItem> findOrderItemByBuyerId(String buyerId, Long itemId, Long orderId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(orderItem)
                        .leftJoin(orderItem.order).fetchJoin()
                        .leftJoin(orderItem.item).fetchJoin()
                        .where(orderItem.buyerId.eq(buyerId))
                        .where(orderItem.item.id.eq(itemId))
                        .where(orderItem.order.id.eq(orderId))
                        .fetchOne());
    }

    /**
     * 판매자 ID, 아이템 ID, 주문 ID에 해당하는 주문 아이템을 가져옵니다.
     *
     * @param sellerId 판매자 ID
     * @param itemId 아이템 ID
     * @param orderId 주문 ID
     * @return 해당 주문 아이템
     */
    @Override
    public Optional<OrderItem> findOrderItemBySellerId(String sellerId, Long itemId, Long orderId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(orderItem)
                        .leftJoin(orderItem.order).fetchJoin()
                        .leftJoin(orderItem.item).fetchJoin()
                        .where(orderItem.sellerId.eq(sellerId))
                        .where(orderItem.item.id.eq(itemId))
                        .where(orderItem.order.id.eq(orderId))
                        .fetchOne());
    }

    /**
     * 주문 상태에 맞는 조건을 빌드하는 메서드
     *
     * @param request 주문 상태를 포함한 요청 데이터
     * @return 주문 상태 조건을 추가한 BooleanBuilder
     */
    private BooleanBuilder orderStatusEq(GetSellOrderDto.Request request) {
        BooleanBuilder builder = new BooleanBuilder();

        if (Boolean.TRUE.equals(request.getPending())) {
            builder.or(orderItem.orderStatus.eq(OrderStatus.PENDING));
        }
        if (Boolean.TRUE.equals(request.getCanceled())) {
            builder.or(orderItem.orderStatus.eq(OrderStatus.CANCELED));
        }
        if (Boolean.TRUE.equals(request.getCompleted())) {
            builder.or(orderItem.orderStatus.eq(OrderStatus.COMPLETED));
        }

        return builder;
    }

    /**
     * 카테고리 이름이 주어진 값과 일치하는지 확인하는 조건을 반환합니다.
     *
     * @param categoryName 카테고리 이름
     * @return 카테고리 이름과 일치하는 조건
     */
    private BooleanExpression categoryNameEq(String categoryName) {
        if (StringUtils.hasText(categoryName)) {
            return category.categoryName.eq(categoryName);
        }

        return null;
    }

    /**
     * 카테고리 타입 이름이 주어진 값과 일치하는지 확인하는 조건을 반환합니다.
     *
     * @param type 카테고리 타입 이름
     * @return 카테고리 타입 이름과 일치하는 조건
     */
    private BooleanExpression typeNameEq(String type) {
        if (StringUtils.hasText(type)) {
            return categoryType.typeName.eq(type);
        }

        return null;
    }


}
