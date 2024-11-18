package hmw.ecommerce.controller;

import hmw.ecommerce.entity.dto.cart.AddToCartDto;
import hmw.ecommerce.entity.dto.cart.EditToCartDto;
import hmw.ecommerce.entity.vo.Const;
import hmw.ecommerce.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * 장바구니 관련 요청을 처리하는 컨트롤러 클래스.
 * 장바구니 아이템 추가, 조회, 수정, 삭제 기능을 제공.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    /**
     * 장바구니에 아이템 추가 요청을 처리.
     *
     * @param request      로그인 상태가 아닌 경우 쿠키에 넣기 위해 가져옴
     * @param response     로그인 상태가 아닌 경우 쿠키에 넣기 위해 가져옴
     * @param token        사용자 인증 토큰
     * @param itemId       추가할 아이템의 ID
     * @param cartRequest  장바구니 추가 요청 데이터
     * @param bindingResult 유효성 검사
     * @return 추가된 장바구니 아이템 정보
     */
    @PostMapping("/{itemId}")
    public ResponseEntity<?> addToCart(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(name = Const.AUTHORIZATION, required = false) String token,
            @PathVariable(name = "itemId") Long itemId,
            @Valid @RequestBody AddToCartDto.Request cartRequest,
            BindingResult bindingResult) {
        return ResponseEntity.ok(cartService.addToCart(itemId, cartRequest, token, request, response));
    }

    /**
     * 장바구니 아이템 조회 요청을 처리.
     *
     * @param request  로그인 상태가 아닌 경우 쿠키에서 들고오기 위해 가져옴
     * @param response 로그인 상태가 아닌 경우 쿠키에서 들고오기 위해 가져옴
     * @param token    사용자 인증 토큰
     * @return 장바구니에 담긴 아이템 목록
     */
    @GetMapping
    public ResponseEntity<?> getCartItem(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(name = Const.AUTHORIZATION, required = false) String token) {
        return ResponseEntity.ok(cartService.getCartItem(request, response, token));
    }

    /**
     * 장바구니 아이템 수정 요청을 처리.
     *
     * @param request      로그인 상태가 아닌 경우 쿠키에서 수정하기 위해 위해 가져옴
     * @param response     로그인 상태가 아닌 경우 쿠키에서 수정하기 위해 위해 가져옴
     * @param token        사용자 인증 토큰
     * @param itemId       수정할 아이템의 ID
     * @param cartRequest  장바구니 수정 요청 데이터
     * @param bindingResult 유효성 검사 결과
     * @return 수정된 장바구니 아이템 정보
     */
    @PutMapping("/{itemId}")
    public ResponseEntity<?> editCartItem(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(name = Const.AUTHORIZATION, required = false) String token,
            @PathVariable(name = "itemId") Long itemId,
            @Valid @RequestBody EditToCartDto.Request cartRequest,
            BindingResult bindingResult) {
        return ResponseEntity.ok(cartService.editCartItem(itemId, cartRequest, token, request, response));
    }

    /**
     * 장바구니 아이템 삭제 요청을 처리.
     *
     * @param request  로그인 상태가 아닌 경우 쿠키에서 삭제하기 위해 위해 가져옴
     * @param response 로그인 상태가 아닌 경우 쿠키에서 삭제하기 위해 위해 가져옴
     * @param token    사용자 인증 토큰
     * @param itemId   삭제할 아이템의 ID
     * @return 삭제 결과 메시지
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteCartItem(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(name = Const.AUTHORIZATION, required = false) String token,
            @PathVariable(name = "itemId") Long itemId) {
        return ResponseEntity.ok(cartService.deleteCartItem(itemId, token, request, response));
    }

}