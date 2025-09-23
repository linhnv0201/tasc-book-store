package tasc.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tasc.bookstore.entity.CustomerPayment;
import tasc.bookstore.entity.Order;

import java.util.List;

public interface CustomerPaymentRepository extends JpaRepository<CustomerPayment, Long> {
    List<CustomerPayment> findByOrderId(Long orderId);
    boolean existsByOrder(Order order);
}
