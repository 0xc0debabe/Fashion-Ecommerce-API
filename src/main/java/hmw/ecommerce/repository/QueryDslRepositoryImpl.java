package hmw.ecommerce.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.QCategory;
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
