package tasc.bookstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import tasc.bookstore.entity.Product;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsByName(String name);

//Các cách truy vấn lấy list product by categoryId
//    Cách 1: JPQL
//    @Query("SELECT p FROM Product p JOIN p.categories c where c.id = :categoryId")
//    List<Product> GetProductsByCategoryId(Long categoryId);

//    Cách 2: native query, dùng Param trong trường họp có nhiều parameter
//    @Query(value = "SELECT p.* " +
//            "FROM products p " +
//            "JOIN product_categories pc ON p.id = pc.product_id " +
//            "WHERE pc.category_id = :categoryId",
//            nativeQuery = true)
//    List<Product> GetProductsByCategoryId(@Param("categoryId") Long categoryId);

    //    Cách 3: native query, dùng ?1 khi chỉ có 1 parameter duy nhất
//    @Query(value = "SELECT p.* " +
//            "FROM products p " +
//            "JOIN product_categories pc ON p.id = pc.product_id " +
//            "WHERE pc.category_id = ?1",
//            nativeQuery = true)
//    List<Product> GetProductsByCategoryId(Long categoryId);

//    Cách 4: Dùng store procedure
    @Procedure(name = "GetProductsByCategoryId")
    List<Product> GetProductsByCategoryId(@Param("categoryId") Long categoryId);

    //JPQL
        //Query đơn giản, theo entity
        //Muốn portable giữa DB
        //Muốn tận dụng cache của JPA
    //Native Query:
        //Query phức tạp, join nhiều bảng, subquery, function DB-specific
        //Khi JPQL không hỗ trợ
        //Muốn tối ưu SQL để performance cao

     Page<Product> findAll(Specification<Product> spec, Pageable pageable);

}
