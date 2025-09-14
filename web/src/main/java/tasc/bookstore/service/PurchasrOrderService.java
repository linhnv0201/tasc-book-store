package tasc.bookstore.service;

import tasc.bookstore.dto.request.PurchaseOrderCreationRequest;
import tasc.bookstore.dto.response.PurchaseOrderResponse;

import java.util.List;

public interface PurchasrOrderService {
//    ProductResponse createProduct(ProductCreationRequest request);
    PurchaseOrderResponse createPurchaseOrder(PurchaseOrderCreationRequest request);
    List<PurchaseOrderResponse> getAll();
    PurchaseOrderResponse getById(Long id);

}
