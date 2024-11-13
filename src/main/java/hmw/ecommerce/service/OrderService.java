package hmw.ecommerce.service;

import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.Member;
import hmw.ecommerce.entity.Order;
import hmw.ecommerce.entity.OrderItem;
import hmw.ecommerce.entity.dto.cart.AddToCartDto;
import hmw.ecommerce.entity.dto.order.CancelOrderDto;
import hmw.ecommerce.entity.dto.order.CreateOrderDto;
import hmw.ecommerce.entity.dto.order.GetOrdersDto;
import hmw.ecommerce.entity.vo.OrderStatus;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.exception.exceptions.ItemException;
import hmw.ecommerce.exception.exceptions.MemberException;
import hmw.ecommerce.exception.exceptions.OrderException;
import hmw.ecommerce.jwt.JWTUtil;
import hmw.ecommerce.repository.OrderItemRepository;
import hmw.ecommerce.repository.OrderRepository;
import hmw.ecommerce.repository.entity.ItemRepository;
import hmw.ecommerce.repository.entity.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static hmw.ecommerce.entity.vo.Const.CART_ITEMS;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    // 쇼핑몰 사이트에서 구매하기 눌려보니 장바구니와 아이템디테일에서 주문하기랑 다르다는 걸 화긴
    public Long createOrder(String token, CreateOrderDto orderDto) {
        // 아이디 찾는 로직에서 토큰 예외 검사
        String loginId = jwtUtil.extractLoginIdFromToken(token);
        Member findMember = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_LOGIN_ID));
        if (orderFromCart(orderDto)) {
            return createOrderFromCart(findMember, loginId);
        }

        if (orderDto.getCount() == 0) {
            throw new OrderException(ErrorCode.ORDER_NOT_ALLOWED);
        }

        return orderFromItemDetail(orderDto, findMember);
    }

    public List<GetOrdersDto.Response> getOrders(String token, GetOrdersDto.Request dtoRequest) {
        String loginId = jwtUtil.extractLoginIdFromToken(token);
        int page = dtoRequest.getPage();
        int size = dtoRequest.getSize();

        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        Page<OrderItem> orderItems = orderItemRepository.findOrderItemsByLoginId(loginId, pageable);
        return orderItems
                .stream()
                .map(GetOrdersDto.Response::fromEntity).
                collect(Collectors.toList());
    }

    public Long cancelOrder(String token, CancelOrderDto cancelOrderDto) {
        String loginId = jwtUtil.extractLoginIdFromToken(token);

        OrderItem orderItem = orderItemRepository
                .findOrderItemByLoginId(
                        loginId,
                        cancelOrderDto.getItemId(),
                        cancelOrderDto.getOrderId()
                ).orElseThrow(() -> new OrderException(ErrorCode.NOT_FOUND_ORDER));

        if (!orderItem.getOrder().getMember().getLoginId().equals(loginId)) {
            throw new OrderException(ErrorCode.CAN_NOT_ORDER_CANCEL);
        }

        orderItem.getOrder().cancel(orderItem);
        orderItem.getItem().increaseStock(orderItem.getUnitCount());

        return orderItem.getOrder().getId();
    }

    private Long orderFromItemDetail(CreateOrderDto orderDto, Member findMember) {
        int count = orderDto.getCount();
        Long itemId = orderDto.getItemId();

        Item findItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.NOT_EXISTS_ITEM));

        if (!findItem.isStockAvailability(count)) {
            throw new OrderException(ErrorCode.OUT_OF_STOCK);
        }

        Order savedOrder = orderRepository.save(
                Order.createOrder(
                        findMember,
                        count,
                        findItem.getPrice() * count,
                        OrderStatus.ORDERED));

        orderItemRepository.save(
                OrderItem.toEntity(savedOrder, findItem, count, count * findItem.getPrice(), findMember.getLoginId())
        );

        findItem.decreaseStock(count);

        return savedOrder.getId();
    }


    private Long createOrderFromCart(Member findMember, String loginId) {
        // 개별 주문이 아닐때(장바구니 주문일때)
        HashOperations<String, String, Set<AddToCartDto.Response>> hashOperations = redisTemplate.opsForHash();
        Set<AddToCartDto.Response> cartItems = hashOperations.get(CART_ITEMS, loginId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new OrderException(ErrorCode.ORDER_NOT_ALLOWED);
        }

        // 한번에 끌어오지 않으면 장바구니 개수당 repository 접근하므로 이렇게 작성함
        Set<Long> itemIds = cartItems.stream()
                .map(AddToCartDto.Response::getItemId)
                .collect(Collectors.toSet());
        List<Item> items = itemRepository.findAllById(itemIds);
        List<Long> stockErrorList = new ArrayList<>();
        int totalCount = 0;
        int totalPrice = 0;

        for (AddToCartDto.Response cartItem : cartItems) {
            Long itemId = cartItem.getItemId();
            int count = cartItem.getCount();

            Item item = items.stream()
                    .filter(i -> i.getId().equals(itemId))
                    .findFirst()
                    .orElseThrow(() -> new OrderException(ErrorCode.NOT_EXISTS_ITEM));

            if (!item.isStockAvailability(count)) {
                stockErrorList.add(item.getId());
            } else {
                totalCount += cartItem.getCount();
                totalPrice += cartItem.getPrice() * cartItem.getCount();
                item.decreaseStock(cartItem.getCount());
            }
        }

        // 재고부족 담아서 예외 던짐
        if (!stockErrorList.isEmpty()) {
            throw new OrderException(ErrorCode.OUT_OF_STOCK, stockErrorList);
        }

        Order savedOrder = orderRepository.save(
                Order.createOrder(
                        findMember,
                        totalCount,
                        totalPrice,
                        OrderStatus.ORDERED)
        );


        for (AddToCartDto.Response cartItem : cartItems) {
            Long itemId = cartItem.getItemId();
            int count = cartItem.getCount();

            Item item = items.stream()
                    .filter(i -> i.getId().equals(itemId))
                    .findFirst()
                    .orElseThrow(() -> new OrderException(ErrorCode.NOT_EXISTS_ITEM));


            orderItemRepository.save(OrderItem.toEntity(
                    savedOrder, item, count, cartItem.getPrice() * count, loginId
            ));
        }

        hashOperations.delete(CART_ITEMS, loginId);

        return savedOrder.getId();
    }

    private boolean orderFromCart(CreateOrderDto request) {
        return request.getItemId() == null;
    }


}
