package hmw.ecommerce.entity.vo;

public abstract class Const {
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER  = "Bearer ";

    public static final String ROLE_MEMBER = "ROLE_MEMBER";
    public static final String ROLE_SELLER = "ROLE_SELLER";
    public static final String HAS_ROLE_MEMBER = "hasRole('MEMBER')";
    public static final String HAS_ROLE_SELLER = "hasRole('SELLER')";
    public static final String HAS_ROLE_MEMBER_OR_SELLER = "hasRole('MEMBER') or hasRole('SELLER')";

    public static final String TOP_RANKING_ITEM_KEY = "TOP_RANKING_ITEM_KEY";
    public static final String RANKING_KEY = "RANKING_KEY";
    public static final String VIEW_COUNT = "VIEW_COUNT";

    public static final String CART_ITEMS = "CART_ITEMS";
}