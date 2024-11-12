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

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

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

    @GetMapping
    public ResponseEntity<?> getCartItem(
            HttpServletRequest request,
            @RequestHeader(name = Const.AUTHORIZATION, required = false) String token) {

        return ResponseEntity.ok(cartService.getCartItem(request, token));
    }

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

    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteCartItem(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(name = Const.AUTHORIZATION, required = false) String token,
            @PathVariable(name = "itemId") Long itemId) {
        return ResponseEntity.ok(cartService.deleteCartItem(itemId, token, request, response));
    }

}