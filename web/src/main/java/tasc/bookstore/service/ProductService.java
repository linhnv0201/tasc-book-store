package tasc.bookstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tasc.bookstore.dto.request.ProductCreationRequest;
import tasc.bookstore.dto.request.ProductUpdateRequest;
import tasc.bookstore.dto.response.ProductResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ProductService {
    ProductResponse createProduct(ProductCreationRequest request);
    ProductResponse updateProduct(Long id, ProductUpdateRequest request);
    ProductResponse getProduct(Long id);
    List<ProductResponse> getAllProducts();
    List<ProductResponse> getAllProductsByCategory(Long categoryId);
    void deleteProduct(Long id);
    Map<String, Object> getProductByIdNamedJDBC(Long id);
    List<Map<String, Object>> getProductsByCategoryOrderByPriceDesc(Long categoryId);
    List<Map<String, Object>> getProductsByAuthor(String author);
    Page<ProductResponse> fullSearch(String name, String author, String language, List<Long> categoryIds
            , BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    List<Map<String, Object>> getTopSoldProducts(LocalDate startDate, LocalDate endDate);


}
