package hmw.ecommerce.controller;

import hmw.ecommerce.entity.dto.order.*;
import hmw.ecommerce.entity.vo.Const;
import hmw.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.Getter;
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

    @PreAuthorize(HAS_ROLE_MEMBER)
    @PostMapping
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody(required = false) CreateOrderDto request,
            @RequestHeader(name = Const.AUTHORIZATION) String token
    ) {
        return ResponseEntity.ok("orderId = " +
                orderService.createOrder(token, request));
    }

    @PreAuthorize(HAS_ROLE_MEMBER)
    @GetMapping
    public ResponseEntity<?> getOrders(
            @Valid @RequestBody GetOrdersDto.Request dtoRequest,
            @RequestHeader(name = Const.AUTHORIZATION) String token
    ) {

        return ResponseEntity.ok(orderService.getOrders(token, dtoRequest));
    }

    @PreAuthorize(HAS_ROLE_MEMBER)
    @DeleteMapping
    public ResponseEntity<?> cancelOrder(
            @Valid @RequestBody CancelOrderDto cancelOrderDto,
            @RequestHeader(name = Const.AUTHORIZATION) String token
    ) {
        return ResponseEntity.ok("orderId = " +
                orderService.cancelOrder(token, cancelOrderDto));
    }

    @PreAuthorize(HAS_ROLE_SELLER)
    @GetMapping("/seller")
    public ResponseEntity<?> getSellOrder(
            @Valid @RequestBody GetSellOrderDto.Request orderDto,
            @RequestHeader(name = Const.AUTHORIZATION) String token
    ) {
        return ResponseEntity.ok(orderService.getSellOrder(orderDto, token));
    }

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