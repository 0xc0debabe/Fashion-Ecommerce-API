package hmw.ecommerce.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.dto.cart.AddToCartDto;
import hmw.ecommerce.entity.dto.cart.EditToCartDto;
import hmw.ecommerce.entity.dto.cart.GetCartDto;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.exception.exceptions.CartException;
import hmw.ecommerce.exception.exceptions.ItemException;
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
     *
     * @param itemId       추가할 아이템의 ID
     * @param cartRequest  장바구니 추가 요청 데이터
     * @param token        사용자 인증 토큰
     * @param request      로그인 상태가 아닌 경우 쿠키에 넣기 위해 가져옴
     * @param response     로그인 상태가 아닌 경우 쿠키에 넣기 위해 가져옴
     * @return 추가된 장바구니 아이템 정보
     */
    public AddToCartDto.Response addToCart(
            Long itemId,
            AddToCartDto.Request cartRequest,
            String token,
            HttpServletRequest request,
            HttpServletResponse response) {

        int count = cartRequest.getCount();
        Item findItem = getItemIfExist(itemId);
        if (!findItem.isStockAvailability(count)) {
            throw new ItemException(ErrorCode.OUT_OF_STOCK);
        }

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
     *
     * @param request  로그인 상태가 아닌 경우 쿠키에서 들고오기 위해 가져옴
     * @param res      로그인 상태가 아닌 경우 쿠키에서 들고오기 위해 가져옴
     * @param token    사용자 인증 토큰
     * @return 장바구니에 담긴 아이템 목록과 총 가격 및 수량
     */
    @Transactional(readOnly = true)
    public GetCartDto getCartItem(HttpServletRequest request, HttpServletResponse res, String token) {
        String loginId = getLoginId(token);

        Set<AddToCartDto.Response> responseSet;
        if (!StringUtils.hasText(loginId)) {
            responseSet = getCartFromCookie(request);
        } else {
            responseSet = getCartFromRedis(loginId, request, res);
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
     *
     * @param itemId       수정할 아이템의 ID
     * @param cartRequest  장바구니 수정 요청 데이터
     * @param token        사용자 인증 토큰
     * @param request      로그인 상태가 아닌 경우 쿠키에서 수정하기 위해 가져옴
     * @param response     로그인 상태가 아닌 경우 쿠키에서 수정하기 위해 가져옴
     * @return 수정된 장바구니 아이템 정보
     */
    public EditToCartDto.Response editCartItem(
            Long itemId,
            EditToCartDto.Request cartRequest,
            String token,
            HttpServletRequest request,
            HttpServletResponse response) {

        int count = cartRequest.getCount();
        Item findItem = getItemIfExist(itemId);
        if (findItem.isStockAvailability(count)) {
            throw new ItemException(ErrorCode.OUT_OF_STOCK);
        }
        String loginId = getLoginId(token);
        EditToCartDto.Response cartDtoResponse = EditToCartDto.Response.fromItemEntity(findItem, count);

        boolean updateExist;
        if (!StringUtils.hasText(loginId)) {
            updateExist = updateCartInCookie(request, response, cartDtoResponse);
        } else {
            updateExist = updateCartInRedis(loginId, cartDtoResponse);
        }

        if (!updateExist) {
            throw new CartException(ErrorCode.CANNOT_EDIT_CART_ITEM);
        }
        return cartDtoResponse;
    }

    /**
     * 장바구니에서 아이템을 삭제합니다.
     *
     * @param itemId   삭제할 아이템의 ID
     * @param token    사용자 인증 토큰
     * @param request  로그인 상태가 아닌 경우 쿠키에서 삭제하기 위해 가져옴
     * @param response 로그인 상태가 아닌 경우 쿠키에서 삭제하기 위해 가져옴
     * @return 삭제된 아이템 ID
     */
    public Long deleteCartItem(
            Long itemId,
            String token,
            HttpServletRequest request,
            HttpServletResponse response) {
        String loginId = getLoginId(token);
        boolean deleteExist;
        if (!StringUtils.hasText(loginId)) {
            deleteExist = deleteCartFromCookie(request, response, itemId);
        } else {
            deleteExist = deleteCartFromRedis(loginId, itemId);
        }

        if (!deleteExist) {
            throw new CartException(ErrorCode.CANNOT_DELETE_CART_ITEM);
        }

        return itemId;
    }

    /**
     * 쿠키에 장바구니 아이템을 추가합니다.
     *
     * @param request      로그인 상태가 아닌 경우 쿠키에서 가져오기 위해 사용
     * @param response     로그인 상태가 아닌 경우 쿠키에 추가하기 위해 사용
     * @param cartDtoResponse  추가할 장바구니 아이템 정보
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
                boolean alreadyExist = false;
                for (AddToCartDto.Response cartItem : cartItems) {
                    if (cartItem.getItemId().equals(cartDtoResponse.getItemId())) {
                        cartItem.setCount(cartDtoResponse.getCount());
                        alreadyExist = true;
                        break;
                    }
                }
                if (!alreadyExist) {
                    cartItems.add(cartDtoResponse);
                }

                String updatedJson = objectMapper.writeValueAsString(cartItems);
                String updatedEncrypted = aesUtil.encrypt(updatedJson);

                Cookie cookie = new Cookie(CART_ITEMS, "");
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);

                cookie = new Cookie(CART_ITEMS, updatedEncrypted);
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
     *
     * @param request      로그인 상태 이므로 쿠키에 있을경우 레디스로 옮기고 쿠키 삭제함
     * @param response     로그인 상태 이므로 쿠키에 있을경우 레디스로 옮기고 쿠키 삭제함
     * @param loginId      사용자 로그인 ID
     * @param cartDtoResponse  추가할 장바구니 아이템 정보
     */
    private void addCartInRedis(HttpServletRequest request, HttpServletResponse response, String loginId, AddToCartDto.Response cartDtoResponse) {
        String prevCart = getEncodedCartItemsFromCookie(request);
        Set<AddToCartDto.Response> prevCartItems = null;
        if (StringUtils.hasText(prevCart)) {
            try {
                String prevCartDecode = aesUtil.decrypt(prevCart);
                prevCartItems = objectMapper.readValue(prevCartDecode, new TypeReference<>() {});

                Cookie cookie = new Cookie(CART_ITEMS, "");
                cookie.setMaxAge(0);
                cookie.setPath("/");
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

        boolean itemExist = false;
        for (AddToCartDto.Response cartItem : cartItems) {
            if (cartItem.getItemId().equals(cartDtoResponse.getItemId())) {
                cartItem.setCount(cartDtoResponse.getCount());
                itemExist = true;
                break;
            }
        }
        if (!itemExist) {
            cartItems.add(cartDtoResponse);
        }
        hashOperations.put(CART_ITEMS, loginId, cartItems);
        redisTemplate.expire(CART_ITEMS, 1, TimeUnit.DAYS);
    }

    /**
     * 쿠키에서 장바구니 정보를 가져옵니다.
     *
     * @param request 클라이언트 요청 객체
     * @return 쿠키에 저장된 장바구니 정보 (없으면 null)
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
     *
     * @param loginId 사용자의 로그인 ID
     * @param request 로그인 상태이므로 쿠키에 장바구니 정보 있을경우 레디스로 가져오고 반환
     * @param response 로그인 상태이므로 쿠키에 장바구니 정보 있을경우 레디스로 가져오고 반환
     * @return Redis에 저장된 장바구니 정보
     */
    private Set<AddToCartDto.Response> getCartFromRedis(String loginId, HttpServletRequest request, HttpServletResponse response) {
        Set<AddToCartDto.Response> addToCartDtoFromCookie = getCartFromCookie(request);
        HashOperations<String, String, Set<AddToCartDto.Response>> hashOperations = redisTemplate.opsForHash();

        if (addToCartDtoFromCookie == null) {
            return hashOperations.get(CART_ITEMS, loginId);
        }

        hashOperations.put(CART_ITEMS, loginId, addToCartDtoFromCookie);
        Cookie cookie = new Cookie(CART_ITEMS, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return hashOperations.get(CART_ITEMS, loginId);
    }

    /**
     * 쿠키에서 장바구니 정보를 수정합니다.
     *
     * @param request 쿠키를 수정하기 위한 객체
     * @param response 쿠키를 수정하기 위한 객체
     * @param cartDtoResponse 수정할 장바구니 아이템 정보
     * @return 수정이 성공했으면 true, 실패하면 false
     */
    private boolean updateCartInCookie(
            HttpServletRequest request,
            HttpServletResponse response,
            EditToCartDto.Response cartDtoResponse) {

        Set<AddToCartDto.Response> cartItems = getCartFromCookie(request);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new CartException(ErrorCode.CANNOT_EDIT_CART_ITEM);
        }

        boolean updateExist = false;

        for (AddToCartDto.Response cartItem : cartItems) {
            if (cartItem.getItemId().equals(cartDtoResponse.getItemId())) {
                cartItem.setCount(cartDtoResponse.getCount());
                updateExist = true;
                break;
            }
        }
        if (!updateExist) {
            return false;
        }

        try {
            String updatedCart = objectMapper.writeValueAsString(cartItems);
            String encryptedCart = aesUtil.encrypt(updatedCart);
            Cookie cartCookie = new Cookie(CART_ITEMS, encryptedCart);
            cartCookie.setMaxAge(CART_EXPIRE_TIME);
            cartCookie.setPath("/");
            response.addCookie(cartCookie);
        } catch (Exception e) {
            throw new CartException(ErrorCode.CANNOT_EDIT_CART_ITEM);
        }

        return true;
    }

    /**
     * Redis에서 장바구니 정보를 수정합니다.
     *
     * @param loginId 사용자의 로그인 ID
     * @param cartDtoResponse 수정할 장바구니 아이템 정보
     * @return 수정이 성공했으면 true, 실패하면 false
     */
    private boolean updateCartInRedis(
            String loginId,
            EditToCartDto.Response cartDtoResponse) {

        HashOperations<String, String, Set<AddToCartDto.Response>> hashOperations = redisTemplate.opsForHash();
        Set<AddToCartDto.Response> cartItems = hashOperations.get(CART_ITEMS, loginId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new CartException(ErrorCode.CANNOT_EDIT_CART_ITEM);
        }

        boolean updateExist = false;
        for (AddToCartDto.Response cartItem : cartItems) {
            if (cartItem.getItemId().equals(cartDtoResponse.getItemId())) {
                cartItem.setCount(cartDtoResponse.getCount());
                updateExist = true;
                break;
            }
        }

        if (!updateExist) {
            return false;
        }

        hashOperations.put(CART_ITEMS, loginId, cartItems);
        redisTemplate.expire(CART_ITEMS, 1, TimeUnit.DAYS);
        return true;
    }

    /**
     * 쿠키에서 장바구니 아이템을 삭제합니다.
     *
     * @param request 장바구니에 있는 쿠키 삭제하기 위한 객체
     * @param response 장바구니에 있는 쿠키 삭제하기 위한 객체
     * @param itemId 삭제할 아이템의 ID
     * @return 삭제 성공 여부
     */
    private boolean deleteCartFromCookie(HttpServletRequest request, HttpServletResponse response, Long itemId) {
        Set<AddToCartDto.Response> cartDtoSetFromCookie = getCartFromCookie(request);
        if (cartDtoSetFromCookie == null || cartDtoSetFromCookie.isEmpty()) {
            throw new CartException(ErrorCode.CANNOT_DELETE_CART_ITEM);
        }
        boolean deleteExist = false;
        for (AddToCartDto.Response cartResponse : cartDtoSetFromCookie) {
            if (cartResponse.getItemId().equals(itemId)) {
                cartDtoSetFromCookie.remove(cartResponse);
                deleteExist = true;
                break;
            }
        }

        if (!deleteExist) {
            return false;
        }

        if (cartDtoSetFromCookie.isEmpty()) {
            Cookie cookie = new Cookie(CART_ITEMS, "");
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
            return true;
        }

        try {
            String jsonSet = objectMapper.writeValueAsString(cartDtoSetFromCookie);
            String encryptJsonSet = aesUtil.encrypt(jsonSet);
            Cookie cookie = new Cookie(CART_ITEMS, encryptJsonSet);
            cookie.setMaxAge(60 * 60 * 24);
            cookie.setPath("/");
            response.addCookie(cookie);
        } catch (Exception e) {
            throw new CartException(ErrorCode.CANNOT_DELETE_CART_ITEM);
        }

        return true;
    }

    /**
     * Redis에서 장바구니 아이템을 삭제합니다.
     *
     * @param loginId 사용자의 로그인 ID
     * @param itemId 삭제할 아이템의 ID
     * @return 삭제 성공 여부
     */
    private boolean deleteCartFromRedis(String loginId, Long itemId) {
        HashOperations<String, String, Set<AddToCartDto.Response>> hashOperations = redisTemplate.opsForHash();
        Set<AddToCartDto.Response> cartItems = hashOperations.get(CART_ITEMS, loginId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new CartException(ErrorCode.CANNOT_DELETE_CART_ITEM);
        }
        boolean deleteExist = false;
        for (AddToCartDto.Response cartResponse : cartItems) {
            if (cartResponse.getItemId().equals(itemId)) {
                cartItems.remove(cartResponse);
                deleteExist = true;
                break;
            }
        }
        if (!deleteExist) {
            return false;
        }
        hashOperations.put(CART_ITEMS, loginId, cartItems);
        redisTemplate.expire(CART_ITEMS, 1, TimeUnit.DAYS);
        return true;
    }

    /**
     * 쿠키에서 장바구니 암호화된 값을 가져옵니다.
     *
     * @param request 클라이언트 요청 객체
     * @return 쿠키에 저장된 암호화된 장바구니 값 (없으면 null)
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
     * JWT 토큰에서 로그인 ID를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 토큰에서 추출한 로그인 ID
     */
    private String getLoginId(String token) {
        return token == null ? null : jwtUtil.extractLoginIdFromToken(token);
    }

    /**
     * 주어진 아이템 ID에 해당하는 아이템이 존재하는지 확인합니다.
     *
     * @param itemId 확인할 아이템의 ID
     * @return 존재하는 아이템 객체
     * @throws ItemException 아이템이 존재하지 않으면 예외 발생
     */
    private Item getItemIfExist(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.NOT_EXISTS_ITEM));
    }

}