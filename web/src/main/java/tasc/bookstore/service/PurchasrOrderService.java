package tasc.bookstore.service;

import tasc.bookstore.dto.request.PurchaseOrderCreationRequest;
import tasc.bookstore.dto.response.PurchaseOrderResponse;

import java.util.List;
import java.util.Map;

public interface PurchasrOrderService {
    PurchaseOrderResponse createPurchaseOrder(PurchaseOrderCreationRequest request);
    List<PurchaseOrderResponse> getAll();
    PurchaseOrderResponse getById(Long id);

}
