package hmw.ecommerce.controller;

import hmw.ecommerce.entity.dto.order.CancelOrderDto;
import hmw.ecommerce.entity.dto.order.CreateOrderDto;
import hmw.ecommerce.entity.dto.order.GetOrdersDto;
import hmw.ecommerce.entity.vo.Const;
import hmw.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static hmw.ecommerce.entity.vo.Const.HAS_ROLE_MEMBER;

@RequiredArgsConstructor
@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    @PreAuthorize(HAS_ROLE_MEMBER)
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestBody(required = false) CreateOrderDto request,
            @RequestHeader(name = Const.AUTHORIZATION) String token
    ) {
        return ResponseEntity.ok("orderId = " + orderService.createOrder(token, request));
    }

    @PreAuthorize(HAS_ROLE_MEMBER)
    @GetMapping
    public ResponseEntity<?> getOrders(
            @RequestBody GetOrdersDto.Request dtoRequest,
            @RequestHeader(name = Const.AUTHORIZATION) String token
    ) {

        return ResponseEntity.ok(orderService.getOrders(token, dtoRequest));
    }

    @PreAuthorize(HAS_ROLE_MEMBER)
    @DeleteMapping
    public ResponseEntity<?> cancelOrder(
            @RequestBody CancelOrderDto cancelOrderDto,
            @RequestHeader(name = Const.AUTHORIZATION) String token
    ) {
        return ResponseEntity.ok("orderId = " + orderService.cancelOrder(token, cancelOrderDto));
    }

}
