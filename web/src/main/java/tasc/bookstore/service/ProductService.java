package tasc.bookstore.service;

import tasc.bookstore.dto.request.ProductCreationRequest;
import tasc.bookstore.dto.request.ProductUpdateRequest;
import tasc.bookstore.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductCreationRequest request);
    ProductResponse updateProduct(Long id, ProductUpdateRequest request);
    ProductResponse getProduct(Long id);
    List<ProductResponse> getAllProducts();
    List<ProductResponse> getAllProductsByCategory(Long categoryId);
    void deleteProduct(Long id);
}
