package tasc.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tasc.bookstore.entity.PurchasePayment;
import java.util.List;

public interface PurchasePaymentRepository extends JpaRepository<PurchasePayment, Long> {
    List<PurchasePayment> findByPurchaseOrderId(Long purchaseOrderId);
}
