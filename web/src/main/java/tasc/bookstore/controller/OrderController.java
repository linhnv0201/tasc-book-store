package tasc.bookstore.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tasc.bookstore.dto.request.OrderRequest;
import tasc.bookstore.dto.response.ApiResponse;
import tasc.bookstore.dto.response.OrderResponse;
import tasc.bookstore.service.OrderService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {
    OrderService orderService;

    @GetMapping
    public ApiResponse<List<OrderResponse>> getAllOrders() {
        ApiResponse<List<OrderResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("All orders found");
        apiResponse.setResult(orderService.getAllOrders());
        return apiResponse;
    }

    @GetMapping("/mine")
    public ApiResponse<List<OrderResponse>> getMyOrders(){
        ApiResponse<List<OrderResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("All my orders");
        apiResponse.setResult(orderService.getMyOrders());
        return apiResponse;
    }

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
        ApiResponse<OrderResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(orderService.createOrder(orderRequest));
        return apiResponse;
    }
}
