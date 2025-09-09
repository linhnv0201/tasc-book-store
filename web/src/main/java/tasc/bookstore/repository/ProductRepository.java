package tasc.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tasc.bookstore.entity.Product;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);
    //JPQL
    @Query("SELECT p FROM Product p JOIN p.categories c where c.id = :categoryId")
    List<Product> findByCategories(Long categoryId);
}
