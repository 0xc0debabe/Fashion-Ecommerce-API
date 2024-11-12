package hmw.ecommerce.controller;

import hmw.ecommerce.entity.dto.order.CreateOrderDto;
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
            @RequestHeader(name = Const.AUTHORIZATION) String token,
            @RequestBody(required = false) CreateOrderDto request
    ) {
        return ResponseEntity.ok(orderService.createOrder(token, request));
    }

}
