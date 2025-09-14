package tasc.bookstore.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tasc.bookstore.dto.request.PurchaseOrderCreationRequest;
import tasc.bookstore.dto.request.PurchaseOrderItemRequest;
import tasc.bookstore.dto.response.PurchaseOrderResponse;
import tasc.bookstore.entity.Product;
import tasc.bookstore.entity.PurchaseOrder;
import tasc.bookstore.entity.PurchaseOrderItem;
import tasc.bookstore.entity.User;
import tasc.bookstore.exception.AppException;
import tasc.bookstore.exception.ErrorCode;
import tasc.bookstore.mapper.PurchaseOrderMapper;
import tasc.bookstore.repository.ProductRepository;
import tasc.bookstore.repository.PurchaseOrderRepository;
import tasc.bookstore.repository.SupplierRepository;
import tasc.bookstore.repository.UserRepository;
import tasc.bookstore.service.PurchasrOrderService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PurchaseOrderServiceImpl implements PurchasrOrderService {

    UserRepository userRepository;
    PurchaseOrderRepository purchaseOrderRepository;
    ProductRepository productRepository;
    SupplierRepository supplierRepository;
    PurchaseOrderMapper purchaseOrderMapper;

    @Override
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderCreationRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_ORDER);
        }

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setCreatedAt(LocalDateTime.now());
        purchaseOrder.setCreatedBy(getCurrentUser());
        purchaseOrder.setNote(request.getNote());
        purchaseOrder.setCode(generatePurchaseOrderCode());
        purchaseOrder.setSupplier(supplierRepository.findById(request.getSupplierId()).orElse(null));

        BigDecimal total = BigDecimal.ZERO;

        for (PurchaseOrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            PurchaseOrderItem orderItem = new PurchaseOrderItem();
            orderItem.setPurchaseOrder(purchaseOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setCost(itemReq.getCost());

            total = total.add(itemReq.getCost().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            purchaseOrder.getItems().add(orderItem);

            // 🟢 Update giá cost trung bình cho Product
            BigDecimal oldCost = product.getCost() != null ? product.getCost() : BigDecimal.ZERO;
            long oldStock = product.getStock() != null ? product.getStock() : 0;

            long newQuantity = itemReq.getQuantity();
            BigDecimal newCost = itemReq.getCost();

            long totalStock = oldStock + newQuantity;

            if (totalStock > 0) {
                BigDecimal newAvgCost = (oldCost.multiply(BigDecimal.valueOf(oldStock))
                        .add(newCost.multiply(BigDecimal.valueOf(newQuantity))))
                        .divide(BigDecimal.valueOf(totalStock), RoundingMode.HALF_UP);

                product.setCost(newAvgCost);
            } else {
                // trường hợp chưa có tồn kho thì set bằng giá mới
                product.setCost(newCost);
            }

            // cập nhật tồn kho
            product.setStock(Math.toIntExact(totalStock));

            productRepository.save(product);
        }


        purchaseOrder.setTotalAmount(total);
        purchaseOrderRepository.save(purchaseOrder);
        return purchaseOrderMapper.toResponse(purchaseOrder);
    }

    @Override
    public List<PurchaseOrderResponse> getAll() {
        return purchaseOrderRepository.findAll()
                .stream()
                .map(purchaseOrderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PurchaseOrderResponse getById(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return purchaseOrderMapper.toResponse(purchaseOrder);
    }


    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private String generatePurchaseOrderCode() {
        // 1. Lấy ngày hiện tại dạng YYYYMMDD
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 2. Sinh 4 chữ số ngẫu nhiên
        int randomPart = (int)(Math.random() * 10000); // 0-9999
        String randomPartStr = String.format("%04d", randomPart);

        // 3. Kết hợp ngày + random
        String code = "PO" + datePart + randomPartStr;

        // 4. Kiểm tra trùng với DB (nếu muốn thật sự an toàn)
        while (purchaseOrderRepository.existsByCode(code)) {
            randomPart = (int)(Math.random() * 10000);
            randomPartStr = String.format("%04d", randomPart);
            code = "ORDER" + datePart + randomPartStr;
        }

        return code;
    }
}
