package tasc.bookstore.service;

import tasc.bookstore.dto.request.OrderRequest;
import tasc.bookstore.dto.response.OrderResponse;
import tasc.bookstore.entity.User;

import java.util.List;
import java.util.Map;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);
    List<OrderResponse> getAllOrders();
    List<OrderResponse> getMyOrders();
}
