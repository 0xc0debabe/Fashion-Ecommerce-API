package hmw.ecommerce.controller;

import hmw.ecommerce.entity.dto.order.*;
import hmw.ecommerce.entity.vo.Const;
import hmw.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static hmw.ecommerce.entity.vo.Const.HAS_ROLE_MEMBER;
import static hmw.ecommerce.entity.vo.Const.HAS_ROLE_SELLER;

@RequiredArgsConstructor
@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    /**
     * 회원이 주문을 생성하는 메서드.
     * 인증된 회원만 접근할 수 있습니다.
     *
     * @param request 주문 생성 요청 DTO.
     * @param token 로그인한 사용자의 인증 토큰 (JWT).
     * @return 생성된 주문 ID를 포함한 ResponseEntity.
     */
    @PreAuthorize(HAS_ROLE_MEMBER)
    @PostMapping
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody(required = false) CreateOrderDto request,
            @RequestHeader(name = Const.AUTHORIZATION) String token
    ) {
        return ResponseEntity.ok("orderId = " +
                orderService.createOrder(token, request));
    }

    /**
     * 인증된 회원의 주문 목록을 조회하는 메서드.
     * 회원만 자신의 주문을 조회할 수 있습니다.
     *
     * @param dtoRequest 주문 필터와 페이지네이션을 포함한 요청 DTO.
     * @param token 로그인한 사용자의 인증 토큰 (JWT).
     * @return 회원의 주문 목록을 포함한 ResponseEntity.
     */
    @PreAuthorize(HAS_ROLE_MEMBER)
    @GetMapping
    public ResponseEntity<?> getOrders(
            @Valid @RequestBody GetOrdersDto.Request dtoRequest,
            @RequestHeader(name = Const.AUTHORIZATION) String token
    ) {

        return ResponseEntity.ok(orderService.getOrders(token, dtoRequest));
    }

    /**
     * 기존 주문을 취소하는 메서드.
     * 회원만 자신의 주문을 취소할 수 있습니다.
     *
     * @param cancelOrderDto 주문 취소 요청 DTO.
     * @param token 로그인한 사용자의 인증 토큰 (JWT).
     * @return 취소된 주문 ID를 포함한 ResponseEntity.
     */
    @PreAuthorize(HAS_ROLE_MEMBER)
    @DeleteMapping
    public ResponseEntity<?> cancelOrder(
            @Valid @RequestBody CancelOrderDto cancelOrderDto,
            @RequestHeader(name = Const.AUTHORIZATION) String token
    ) {
        return ResponseEntity.ok("orderId = " +
                orderService.cancelOrder(token, cancelOrderDto));
    }

    /**
     * 판매자가 자신의 판매 주문을 조회하는 메서드.
     * 판매자만 자신의 판매 주문을 조회할 수 있습니다.
     *
     * @param orderDto 판매 주문 필터를 포함한 요청 DTO.
     * @param token 로그인한 판매자의 인증 토큰 (JWT).
     * @return 판매자의 주문 목록을 포함한 ResponseEntity.
     */
    @PreAuthorize(HAS_ROLE_SELLER)
    @GetMapping("/seller")
    public ResponseEntity<?> getSellOrder(
            @Valid @RequestBody GetSellOrderDto.Request orderDto,
            @RequestHeader(name = Const.AUTHORIZATION) String token
    ) {
        return ResponseEntity.ok(orderService.getSellOrder(orderDto, token));
    }

    /**
     * 판매자가 주문을 완료하는 메서드.
     * 판매자만 주문을 완료할 수 있습니다.
     *
     * @param completeOrderDto 주문 완료 요청 DTO.
     * @param token 로그인한 판매자의 인증 토큰 (JWT).
     * @return 완료된 주문 ID를 포함한 ResponseEntity.
     */
    @PreAuthorize(HAS_ROLE_SELLER)
    @PostMapping("/complete")
    public ResponseEntity<?> completeOrder(
            @Valid @RequestBody CompleteOrderDto completeOrderDto,
            @RequestHeader(name = Const.AUTHORIZATION) String token
    ) {
        return ResponseEntity.ok("orderId = " +
                orderService.completeOrder(completeOrderDto, token));
    }

}