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

    private BooleanExpression categoryNameEq(String categoryName) {
        if (StringUtils.hasText(categoryName)) {
            return category.categoryName.eq(categoryName);
        }

        return null;
    }

    private BooleanExpression typeNameEq(String type) {
        if (StringUtils.hasText(type)) {
            return categoryType.typeName.eq(type);
        }

        return null;
    }


}
