package tasc.bookstore.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tasc.bookstore.dto.request.OrderItemRequest;
import tasc.bookstore.dto.request.OrderRequest;
import tasc.bookstore.dto.response.OrderResponse;
import tasc.bookstore.entity.*;
import tasc.bookstore.exception.AppException;
import tasc.bookstore.exception.ErrorCode;
import tasc.bookstore.mapper.OrderMapper;
import tasc.bookstore.repository.CartRepository;
import tasc.bookstore.repository.OrderRepository;
import tasc.bookstore.repository.ProductRepository;
import tasc.bookstore.repository.UserRepository;
import tasc.bookstore.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {

    UserRepository userRepository;
    ProductRepository productRepository;
    CartRepository cartRepository;
    OrderRepository orderRepository;
    OrderMapper orderMapper;


    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_ORDER);
        }

        User user = getCurrentUser();

        Order order = new Order();
        order.setCustomer(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(Order.Status.PENDING);
        order.setCode(generateOrderCode());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            // Kiểm tra tồn kho
            if (product.getStock() < itemReq.getQuantity()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setPrice(product.getPrice());

            product.setStock(product.getStock() - orderItem.getQuantity());
            product.setSoldQuantity(product.getSoldQuantity() + orderItem.getQuantity());
            productRepository.save(product);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            order.getItems().add(orderItem);
        }

        order.setTotalAmount(total);

        // Xóa item từ cart nếu có
        Cart cart = cartRepository.findByCustomerId(user.getId()).orElse(null);
        if (cart != null) {
            List<Long> productIds = request.getItems().stream()
                    .map(OrderItemRequest::getProductId)
                    .toList();
            cart.getItems().removeIf(ci -> productIds.contains(ci.getProduct().getId()));
            cartRepository.save(cart);
        }

        orderRepository.save(order);

        return orderMapper.toOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    public List<OrderResponse> getMyOrders() {
        List<Order> orders = orderRepository.findByCustomerId(getCurrentUser().getId());
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private String generateOrderCode() {
        // 1. Lấy ngày hiện tại dạng YYYYMMDD
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 2. Sinh 4 chữ số ngẫu nhiên
        int randomPart = (int)(Math.random() * 10000); // 0-9999
        String randomPartStr = String.format("%04d", randomPart);

        // 3. Kết hợp ngày + random
        String code = "ORDER" + datePart + randomPartStr;

        // 4. Kiểm tra trùng với DB (nếu muốn thật sự an toàn)
        while (orderRepository.existsByCode(code)) {
            randomPart = (int)(Math.random() * 10000);
            randomPartStr = String.format("%04d", randomPart);
            code = "ORDER" + datePart + randomPartStr;
        }

        return code;
    }

}

