package hmw.ecommerce.service;

import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.Member;
import hmw.ecommerce.entity.Order;
import hmw.ecommerce.entity.OrderItem;
import hmw.ecommerce.entity.dto.cart.AddToCartDto;
import hmw.ecommerce.entity.dto.order.*;
import hmw.ecommerce.entity.vo.OrderStatus;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.exception.exceptions.ItemException;
import hmw.ecommerce.exception.exceptions.MemberException;
import hmw.ecommerce.exception.exceptions.OrderException;
import hmw.ecommerce.jwt.JWTUtil;
import hmw.ecommerce.repository.entity.ItemRepository;
import hmw.ecommerce.repository.entity.MemberRepository;
import hmw.ecommerce.repository.entity.OrderItemRepository;
import hmw.ecommerce.repository.entity.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    /**
     * 장바구니에서 주문을 생성하거나 아이템 상세 페이지에서 주문을 생성하는 메서드.
     *
     * @param token 로그인 토큰.
     * @param orderDto 주문 생성 DTO.
     * @return 생성된 주문의 ID.
     */
    public Long createOrder(String token, CreateOrderDto orderDto) {
        String loginId = jwtUtil.extractLoginIdFromToken(token);
        Member findMember = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_LOGIN_ID));
        if (orderFromCart(orderDto)) {
            return createOrderFromCart(findMember, loginId);
        }

        if (orderDto.getCount() == null || orderDto.getItemId() == null) {
            throw new OrderException(ErrorCode.ORDER_NOT_ALLOWED);
        }

        return orderFromItemDetail(orderDto, findMember);
    }

    /**
     * 회원의 주문 목록을 조회하는 메서드.
     *
     * @param token 로그인 토큰.
     * @param dtoRequest 주문 조회 요청 DTO.
     * @return 회원의 주문 목록.
     */
    @Transactional(readOnly = true)
    public List<GetOrdersDto.Response> getOrders(String token, GetOrdersDto.Request dtoRequest) {
        String buyerId = jwtUtil.extractLoginIdFromToken(token);
        int page = dtoRequest.getPage();
        int size = dtoRequest.getSize();

        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        Page<OrderItem> orderItems = orderItemRepository.findOrderItemsByBuyerId(buyerId, pageable);
        return orderItems
                .stream()
                .map(GetOrdersDto.Response::fromEntity).
                collect(Collectors.toList());
    }

    /**
     * 주문을 취소하는 메서드.
     *
     * @param token 로그인 토큰.
     * @param cancelOrderDto 취소할 주문 정보.
     * @return 취소된 주문의 ID.
     */
    public Long cancelOrder(String token, CancelOrderDto cancelOrderDto) {
        String loginId = jwtUtil.extractLoginIdFromToken(token);

        OrderItem orderItem = orderItemRepository
                .findOrderItemByBuyerId(
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

    /**
     * 판매자의 판매 주문을 조회하는 메서드.
     *
     * @param orderDto 판매 주문 조회 DTO.
     * @param token 판매자 로그인 토큰.
     * @return 판매자의 주문 목록.
     */
    @Transactional(readOnly = true)
    public List<GetSellOrderDto.Response> getSellOrder(GetSellOrderDto.Request orderDto, String token) {
        String loginId = jwtUtil.extractLoginIdFromToken(token);
        int page = orderDto.getPage();
        int size = orderDto.getSize();
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderItem> orderItems = orderItemRepository.findSellLatestBySellerId(loginId, orderDto, pageable);
        return orderItems
                .map(GetSellOrderDto.Response::fromEntity)
                .toList();
    }

    /**
     * 주문을 완료하는 메서드.
     *
     * @param completeOrderDto 주문 완료 DTO.
     * @param token 판매자 로그인 토큰.
     * @return 완료된 주문의 ID.
     */
    public Long completeOrder(CompleteOrderDto completeOrderDto, String token) {
        String sellerId = jwtUtil.extractLoginIdFromToken(token);
        Long itemId = completeOrderDto.getItemId();
        Long orderId = completeOrderDto.getOrderId();

        OrderItem orderItem = orderItemRepository
                .findOrderItemBySellerId(sellerId, itemId, orderId)
                .orElseThrow(() -> new OrderException(ErrorCode.NOT_FOUND_ORDER));

        Order order = orderItem.getOrder();
        order.complete(orderItem);
        return order.getId();
    }

    /**
     * 아이템 상세 페이지에서 주문을 생성하는 메서드.
     *
     * @param orderDto 주문 정보 DTO.
     * @param findMember 주문자 회원.
     * @return 생성된 주문의 ID.
     */
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
                        OrderStatus.PENDING));

        orderItemRepository.save(
                OrderItem.toEntity(savedOrder, findItem, count, count * findItem.getPrice(), findMember.getLoginId())
        );

        findItem.decreaseStock(count);

        return savedOrder.getId();
    }

    /**
     * 장바구니에서 주문을 생성하는 메서드.
     *
     * @param findMember 주문자 회원.
     * @param loginId 회원의 로그인 ID.
     * @return 생성된 주문의 ID.
     */
    private Long createOrderFromCart(Member findMember, String loginId) {
        HashOperations<String, String, Set<AddToCartDto.Response>> hashOperations = redisTemplate.opsForHash();
        Set<AddToCartDto.Response> cartItems = hashOperations.get(CART_ITEMS, loginId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new OrderException(ErrorCode.ORDER_NOT_ALLOWED);
        }

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

        if (!stockErrorList.isEmpty()) {
            throw new OrderException(ErrorCode.OUT_OF_STOCK, stockErrorList);
        }

        Order savedOrder = orderRepository.save(
                Order.createOrder(
                        findMember,
                        totalCount,
                        totalPrice,
                        OrderStatus.PENDING)
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

    /**
     * 장바구니로 주문하는 지 개별주문 하는지 확인하는 메서드
     *
     * @param request CreateOrderDto의 값이 비어있으면 개별주문 없으면 장바구니 주문
     * @return
     */
    private boolean orderFromCart(CreateOrderDto request) {
        return request.getItemId() == null && request.getCount() == null;
    }

}
