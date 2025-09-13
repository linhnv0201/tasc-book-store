package tasc.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tasc.bookstore.entity.Order;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByCode(String code);
    List<Order> findByCustomerId(Long customerId);
}
