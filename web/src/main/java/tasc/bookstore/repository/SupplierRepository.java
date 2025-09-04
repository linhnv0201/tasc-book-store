package tasc.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tasc.bookstore.entity.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
