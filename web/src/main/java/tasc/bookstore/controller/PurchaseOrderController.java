package tasc.bookstore.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tasc.bookstore.dto.request.PurchaseOrderCreationRequest;
import tasc.bookstore.dto.response.ApiResponse;
import tasc.bookstore.dto.response.PurchaseOrderResponse;
import tasc.bookstore.service.PurchasrOrderService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/purchase-orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PurchaseOrderController {

    PurchasrOrderService purchaseOrderService;

    @PostMapping
    public ApiResponse<PurchaseOrderResponse> createPurchaseOrder(
            @RequestBody PurchaseOrderCreationRequest request) {
        ApiResponse<PurchaseOrderResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(purchaseOrderService.createPurchaseOrder(request));
        return apiResponse;
    }

    @GetMapping
    public ApiResponse<List<PurchaseOrderResponse>> getAllOrders() {
        ApiResponse<List<PurchaseOrderResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(purchaseOrderService.getAll());
        return apiResponse;
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseOrderResponse> getOrderById(@PathVariable Long id) {
        ApiResponse<PurchaseOrderResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(purchaseOrderService.getById(id));
        return apiResponse;
    }
}
