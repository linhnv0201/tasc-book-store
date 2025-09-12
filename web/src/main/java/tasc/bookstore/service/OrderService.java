package tasc.bookstore.service;

import tasc.bookstore.dto.request.OrderRequest;
import tasc.bookstore.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);
    List<OrderResponse> getAllOrders();
}
