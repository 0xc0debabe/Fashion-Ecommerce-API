package hmw.ecommerce.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.dto.cart.AddToCartDto;
import hmw.ecommerce.entity.dto.cart.EditToCartDto;
import hmw.ecommerce.entity.dto.cart.GetCartDto;
import hmw.ecommerce.exception.CartException;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.exception.ItemException;
import hmw.ecommerce.jwt.JWTUtil;
import hmw.ecommerce.repository.entity.ItemRepository;
import hmw.ecommerce.util.AESUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static hmw.ecommerce.entity.vo.Const.CART_ITEMS;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private static final int CART_EXPIRE_TIME = 60 * 60 * 24;

    private final ItemRepository itemRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JWTUtil jwtUtil;
    private final AESUtil aesUtil;
    private final ObjectMapper objectMapper;

    /**
     * 장바구니에 아이템을 추가합니다.
     */
    public AddToCartDto.Response addToCart(
            Long itemId,
            AddToCartDto.Request cartRequest,
            String token,
            HttpServletRequest request,
            HttpServletResponse response) {

        int count = cartRequest.getCount();
        Item findItem = getItemIfExist(itemId);
        findItem.checkStockAvailability(count);

        String loginId = getLoginId(token);
        AddToCartDto.Response cartDtoResponse = AddToCartDto.Response.fromItemEntity(findItem, count);

        if (!StringUtils.hasText(loginId)) {
            addCartInCookie(request, response, cartDtoResponse);
        } else {
            addCartInRedis(request, response, loginId, cartDtoResponse);
        }

        return cartDtoResponse;
    }

    /**
     * 현재 장바구니 정보를 가져옵니다.
     */
    public GetCartDto getCartItem(HttpServletRequest request, String token) {
        String loginId = getLoginId(token);

        Set<AddToCartDto.Response> responseSet;
        if (!StringUtils.hasText(loginId)) {
            responseSet = getCartFromCookie(request);
        } else {
            responseSet = getCartFromRedis(loginId, request);
        }

        if (responseSet == null || responseSet.isEmpty()) {
            return new GetCartDto(null, 0, 0);
        }

        int totalCount = 0;
        int totalPrice = 0;
        for (AddToCartDto.Response response : responseSet) {
            totalCount += response.getCount();
            totalPrice += response.getPrice() * response.getCount();
        }

        return GetCartDto.getCartDtoResponse(responseSet, totalPrice, totalCount);
    }

    /**
     * 장바구니 아이템을 수정합니다.
     */
    public EditToCartDto.Response editCartItem(
            Long itemId,
            EditToCartDto.Request cartRequest,
            String token,
            HttpServletRequest request,
            HttpServletResponse response) {

        int count = cartRequest.getCount();
        Item findItem = getItemIfExist(itemId);
        findItem.checkStockAvailability(count);
        String loginId = getLoginId(token);
        EditToCartDto.Response cartDtoResponse = EditToCartDto.Response.fromItemEntity(findItem, count);

        if (!StringUtils.hasText(loginId)) {
            updateCartInCookie(request, response, cartDtoResponse);
        } else {
            updateCartInRedis(loginId, cartDtoResponse);
        }

        return cartDtoResponse;
    }

    /**
     * 장바구니에서 아이템을 삭제합니다.
     */
    public Long deleteCartItem(
            Long itemId,
            String token,
            HttpServletRequest request,
            HttpServletResponse response) {
        String loginId = getLoginId(token);
        if (!StringUtils.hasText(loginId)) {
            deleteCartFromCookie(request, response, itemId);
        } else {
            deleteCartFromRedis(loginId, itemId);
        }

        return itemId;
    }

    /**
     * 쿠키에 장바구니 아이템을 추가합니다.
     */
    private void addCartInCookie(
            HttpServletRequest request,
            HttpServletResponse response,
            AddToCartDto.Response cartDtoResponse) {

        String prevCart = getEncodedCartItemsFromCookie(request);
        try {
            if (!StringUtils.hasText(prevCart)) {
                Set<AddToCartDto.Response> cartList = new HashSet<>();
                cartList.add(cartDtoResponse);
                String jsonResponse = objectMapper.writeValueAsString(cartList);
                String encryptedResponse = aesUtil.encrypt(jsonResponse);

                Cookie cookie = new Cookie(CART_ITEMS, encryptedResponse);
                cookie.setMaxAge(CART_EXPIRE_TIME);
                cookie.setPath("/");
                response.addCookie(cookie);
            } else {
                String prevCartDecode = aesUtil.decrypt(prevCart);
                Set<AddToCartDto.Response> cartItems = objectMapper.readValue(prevCartDecode, new TypeReference<>(){});
                checkDuplicateItemInCart(cartDtoResponse, cartItems);
                cartItems.add(cartDtoResponse);
                String updatedJson = objectMapper.writeValueAsString(cartItems);
                String updatedEncrypted = aesUtil.encrypt(updatedJson);
                Cookie cookie = new Cookie(CART_ITEMS, updatedEncrypted);
                cookie.setMaxAge(CART_EXPIRE_TIME);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        } catch (ItemException e) {
            throw new ItemException(ErrorCode.ALREADY_EXIST_ITEM_TO_CART);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CartException(ErrorCode.CAN_NOT_ADD_TO_CART);
        }
    }

    /**
     * Redis에 장바구니 아이템을 추가합니다.
     */
    private void addCartInRedis(HttpServletRequest request, HttpServletResponse response, String loginId, AddToCartDto.Response cartDtoResponse) {
        String prevCart = getEncodedCartItemsFromCookie(request);
        Set<AddToCartDto.Response> prevCartItems = null;
        if (StringUtils.hasText(prevCart)) {
            try {
                String prevCartDecode = aesUtil.decrypt(prevCart);
                prevCartItems = objectMapper.readValue(prevCartDecode, new TypeReference<>() {});
                checkDuplicateItemInCart(cartDtoResponse, prevCartItems);
                prevCartItems.add(cartDtoResponse);
                Cookie cookie = new Cookie(CART_ITEMS, "");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            } catch (ItemException e) {
                throw new ItemException(ErrorCode.ALREADY_EXIST_ITEM_TO_CART);
            } catch (Exception e) {
                e.printStackTrace();
                throw new CartException(ErrorCode.CAN_NOT_ADD_TO_CART);
            }
        }

        HashOperations<String, String, Set<AddToCartDto.Response>> hashOperations = redisTemplate.opsForHash();
        Set<AddToCartDto.Response> cartItems = hashOperations.get(CART_ITEMS, loginId);
        if (cartItems == null) {
            cartItems = new HashSet<>();
        }
        if (prevCartItems != null) {
            cartItems.addAll(prevCartItems);
        }

        cartItems.add(cartDtoResponse);
        hashOperations.put(CART_ITEMS, loginId, cartItems);
        redisTemplate.expire(CART_ITEMS, 1, TimeUnit.DAYS);
    }

    /**
     * 쿠키에서 장바구니 정보를 가져옵니다.
     */
    private Set<AddToCartDto.Response> getCartFromCookie(HttpServletRequest request) {
        String encodedCartItems = getEncodedCartItemsFromCookie(request);
        if (!StringUtils.hasText(encodedCartItems)) {
            return null;
        }

        try {
            String prevCartDecode = aesUtil.decrypt(encodedCartItems);
            return objectMapper.readValue(prevCartDecode, new TypeReference<>(){});
        } catch (Exception e) {
            e.printStackTrace();
            throw new CartException(ErrorCode.CANNOT_FOUND_CART_ITEM);
        }
    }

    /**
     * Redis에서 장바구니 정보를 가져옵니다.
     */
    private Set<AddToCartDto.Response> getCartFromRedis(String loginId, HttpServletRequest request) {
        Set<AddToCartDto.Response> addToCartDtoFromCookie = getCartFromCookie(request);
        HashOperations<String, String, Set<AddToCartDto.Response>> hashOperations = redisTemplate.opsForHash();

        if (addToCartDtoFromCookie == null) {
            return hashOperations.get(CART_ITEMS, loginId);
        }

        hashOperations.put(CART_ITEMS, loginId, addToCartDtoFromCookie);
        return hashOperations.get(CART_ITEMS, loginId);
    }

    /**
     * 쿠키에서 장바구니 정보를 수정합니다.
     */
    private void updateCartInCookie(
            HttpServletRequest request,
            HttpServletResponse response,
            EditToCartDto.Response cartDtoResponse) {

        Set<AddToCartDto.Response> cartItems = getCartFromCookie(request);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new CartException(ErrorCode.CANNOT_EDIT_CART_ITEM);
        }

        for (AddToCartDto.Response cartItem : cartItems) {
            if (cartItem.getItemId().equals(cartDtoResponse.getItemId())) {
                cartItem.setCount(cartDtoResponse.getCount());
                break;
            }
        }

        try {
            String updatedCart = objectMapper.writeValueAsString(cartItems);
            String encryptedCart = aesUtil.encrypt(updatedCart);
            Cookie cartCookie = new Cookie(CART_ITEMS, encryptedCart);
            cartCookie.setPath("/");
            cartCookie.setMaxAge(CART_EXPIRE_TIME);
            response.addCookie(cartCookie);
        } catch (Exception e) {
            throw new CartException(ErrorCode.CANNOT_EDIT_CART_ITEM);
        }

    }

    /**
     * Redis에서 장바구니 정보를 수정합니다.
     */
    private void updateCartInRedis(
            String loginId,
            EditToCartDto.Response cartDtoResponse) {

        HashOperations<String, String, Set<AddToCartDto.Response>> hashOperations = redisTemplate.opsForHash();
        Set<AddToCartDto.Response> cartItems = hashOperations.get(CART_ITEMS, loginId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new CartException(ErrorCode.CANNOT_EDIT_CART_ITEM);
        }

        for (AddToCartDto.Response cartItem : cartItems) {
            if (cartItem.getItemId().equals(cartDtoResponse.getItemId())) {
                cartItem.setCount(cartDtoResponse.getCount());
                break;
            }
        }

        hashOperations.put(CART_ITEMS, loginId, cartItems);
        redisTemplate.expire(CART_ITEMS, 1, TimeUnit.DAYS);
    }

    /**
     * 쿠키에서 장바구니 아이템을 삭제합니다.
     */
    private void deleteCartFromCookie(HttpServletRequest request, HttpServletResponse response, Long itemId) {
        Set<AddToCartDto.Response> cartDtoSetFromCookie = getCartFromCookie(request);
        if (cartDtoSetFromCookie == null || cartDtoSetFromCookie.isEmpty()) {
            throw new CartException(ErrorCode.CANNOT_DELETE_CART_ITEM);
        }
        for (AddToCartDto.Response cartResponse : cartDtoSetFromCookie) {
            if (cartResponse.getItemId().equals(itemId)) {
                cartDtoSetFromCookie.remove(cartResponse);
                break;
            }
        }
        try {
            String jsonSet = objectMapper.writeValueAsString(cartDtoSetFromCookie);
            String encryptJsonSet = aesUtil.encrypt(jsonSet);
            Cookie cookie = new Cookie(CART_ITEMS, encryptJsonSet);
            response.addCookie(cookie);
            cookie.setMaxAge(60 * 60 * 24);
        } catch (Exception e) {
            throw new CartException(ErrorCode.CANNOT_DELETE_CART_ITEM);
        }

    }

    /**
     * Redis에서 장바구니 아이템을 삭제합니다.
     */
    private void deleteCartFromRedis(String loginId, Long itemId) {
        HashOperations<String, String, Set<AddToCartDto.Response>> hashOperations = redisTemplate.opsForHash();
        Set<AddToCartDto.Response> cartItems = hashOperations.get(CART_ITEMS, loginId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new CartException(ErrorCode.CANNOT_DELETE_CART_ITEM);
        }
        for (AddToCartDto.Response cartResponse : cartItems) {
            if (cartResponse.getItemId().equals(itemId)) {
                cartItems.remove(cartResponse);
                break;
            }
        }
        hashOperations.put(CART_ITEMS, loginId, cartItems);
        redisTemplate.expire(CART_ITEMS, 1, TimeUnit.DAYS);
    }

    /**
     * 쿠키에서 장바구니 암호화된 값을 가져옵니다.
     */
    private String getEncodedCartItemsFromCookie(HttpServletRequest request) {
        String cookieValue = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (CART_ITEMS.equals(cookie.getName())) {
                    cookieValue = cookie.getValue();
                    break;
                }
            }
        }

        return cookieValue;
    }

    /**
     * 장바구니에 아이템 중복을 확인합니다.
     */
    private void checkDuplicateItemInCart(AddToCartDto.Response cartDtoResponse, Set<AddToCartDto.Response> cartItems) {
        boolean itemExists = cartItems.stream()
                .anyMatch(item -> item.getItemId().equals(cartDtoResponse.getItemId()));
        if (itemExists) {
            throw new ItemException(ErrorCode.ALREADY_EXIST_ITEM_TO_CART);
        }
    }

    /**
     * JWT 토큰에서 로그인 ID를 추출합니다.
     */
    private String getLoginId(String token) {
        return token == null ? null : jwtUtil.extractLoginIdFromToken(token);
    }

    /**
     * 주어진 아이템 ID에 해당하는 아이템이 존재하는지 확인합니다.
     */
    private Item getItemIfExist(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.NOT_EXISTS_ITEM));
    }

}