package tasc.bookstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tasc.bookstore.dto.request.OrderRequest;
import tasc.bookstore.dto.response.OrderResponse;
import tasc.bookstore.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);
    void cancelOrder(Long orderId);
    List<OrderResponse> getAllOrders();
    List<OrderResponse> getMyOrders();
    Page<OrderResponse> findAll(String status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
