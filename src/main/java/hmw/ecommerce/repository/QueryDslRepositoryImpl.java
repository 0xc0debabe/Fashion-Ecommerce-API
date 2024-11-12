package hmw.ecommerce.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hmw.ecommerce.entity.*;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static hmw.ecommerce.entity.QCategory.category;
import static hmw.ecommerce.entity.QCategoryType.categoryType;
import static hmw.ecommerce.entity.QItem.item;
import static hmw.ecommerce.entity.QMember.*;
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
