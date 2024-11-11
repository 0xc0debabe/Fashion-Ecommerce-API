package hmw.ecommerce.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.dto.cart.AddToCartDto;
import hmw.ecommerce.entity.dto.cart.GetCartDto;
import hmw.ecommerce.entity.vo.Const;
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

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final ItemRepository itemRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JWTUtil jwtUtil;
    private final AESUtil aesUtil;
    private final ObjectMapper objectMapper;

    public Long addToCart(Long itemId, AddToCartDto.Request cartRequest, String token, HttpServletRequest request, HttpServletResponse response) {
        int count = cartRequest.getCount();

        Item findItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.NOT_EXISTS_ITEM));
        findItem.checkStockAvailability(count);

        String loginId = token == null ? null : jwtUtil.extractLoginIdFromToken(token);
        AddToCartDto.Response cartDtoResponse = AddToCartDto.Response.fromItemEntity(findItem, count);

        if (!StringUtils.hasText(loginId)) {
            addCartToCookie(request, response, cartDtoResponse);
        } else {
            addCartToRedis(request, response, loginId, cartDtoResponse);
        }

        return findItem.getId();
    }

    public GetCartDto getCartItem(HttpServletRequest request, String token) {
        String loginId = token == null ? null : jwtUtil.extractLoginIdFromToken(token);

        Set<AddToCartDto.Response> responseSet;
        if (StringUtils.hasText(loginId)) {
            responseSet = getFromRedis(loginId);
        } else {
            responseSet = getFromCookie(request);
        }

        int totalCount = 0;
        int totalPrice = 0;
        for (AddToCartDto.Response response : Objects.requireNonNull(responseSet)) {
            totalCount += response.getCount();
            totalPrice += response.getPrice() * response.getCount();
        }

        return GetCartDto.getCartDtoResponse(responseSet, totalPrice, totalCount);
    }

    private Set<AddToCartDto.Response> getFromCookie(HttpServletRequest request) {
        String prevCartItems = getCookieValue(request);
        if (!StringUtils.hasText(prevCartItems)) {
            return null;
        }

        try {
            String prevCartDecode = aesUtil.decrypt(prevCartItems);
            return objectMapper.readValue(prevCartDecode, new TypeReference<>() {});
        } catch (Exception e) {
            e.printStackTrace();
            throw new CartException(ErrorCode.CANNOT_FOUND_CART_ITEM);
        }
    }

    private Set<AddToCartDto.Response> getFromRedis(String loginId) {
        HashOperations<String, String, Set<AddToCartDto.Response>> hashOperations = redisTemplate.opsForHash();
        return hashOperations.get(Const.CART_ITEMS, loginId);
    }

    private void addCartToRedis(HttpServletRequest request, HttpServletResponse response, String loginId, AddToCartDto.Response cartDtoResponse) {
        String prevCart = getCookieValue(request);
        Set<AddToCartDto.Response> prevCartItems = null;
        if (StringUtils.hasText(prevCart)) {
            try {
                String prevCartDecode = aesUtil.decrypt(prevCart);
                prevCartItems = objectMapper.readValue(prevCartDecode, new TypeReference<>() {});
                putCookieValid(cartDtoResponse, prevCartItems);
                Cookie cookie = new Cookie(Const.CART_ITEMS, "");
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
        Set<AddToCartDto.Response> cartItems = hashOperations.get(Const.CART_ITEMS, loginId);
        if (cartItems == null) {
            cartItems = new HashSet<>();
        }
        if (prevCartItems != null) {
            cartItems.addAll(prevCartItems);
        }
        cartItems.add(cartDtoResponse);
        hashOperations.put(Const.CART_ITEMS, loginId, cartItems);
        redisTemplate.expire(Const.CART_ITEMS, 1, TimeUnit.DAYS);
    }

    private void addCartToCookie(HttpServletRequest request, HttpServletResponse response, AddToCartDto.Response cartDtoResponse) {
        String prevCart = getCookieValue(request);
        try {
            if (!StringUtils.hasText(prevCart)) {
                Set<AddToCartDto.Response> cartList = new HashSet<>();
                cartList.add(cartDtoResponse);
                String jsonResponse = objectMapper.writeValueAsString(cartList);
                String encryptedResponse = aesUtil.encrypt(jsonResponse);

                Cookie cookie = new Cookie(Const.CART_ITEMS, encryptedResponse);
                cookie.setMaxAge(60 * 60 * 24);
                cookie.setPath("/");
                response.addCookie(cookie);
            } else {
                String prevCartDecode = aesUtil.decrypt(prevCart);
                Set<AddToCartDto.Response> cartItems = objectMapper.readValue(prevCartDecode, new TypeReference<>(){});
                putCookieValid(cartDtoResponse, cartItems);
                cartItems.add(cartDtoResponse);
                String updatedJson = objectMapper.writeValueAsString(cartItems);
                String updatedEncrypted = aesUtil.encrypt(updatedJson);
                Cookie cookie = new Cookie(Const.CART_ITEMS, updatedEncrypted);
                cookie.setMaxAge(60 * 60 * 24);
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

    private void putCookieValid(AddToCartDto.Response cartDtoResponse, Set<AddToCartDto.Response> cartItems) {
        boolean itemExists = cartItems.stream()
                .anyMatch(item -> item.getItemId().equals(cartDtoResponse.getItemId()));
        if (itemExists) {
            throw new ItemException(ErrorCode.ALREADY_EXIST_ITEM_TO_CART);
        }
    }

    private String getCookieValue(HttpServletRequest request) {
        String cookieValue = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (Const.CART_ITEMS.equals(cookie.getName())) {
                    cookieValue = cookie.getValue();
                    break;
                }
            }
        }

        return cookieValue;
    }


}
