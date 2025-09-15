package tasc.bookstore.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tasc.bookstore.dto.request.ProductCreationRequest;
import tasc.bookstore.dto.request.ProductUpdateRequest;
import tasc.bookstore.dto.response.ApiResponse;
import tasc.bookstore.dto.response.ProductResponse;
import tasc.bookstore.service.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    ProductService productService;

    @PostMapping
//    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> createProduct(@RequestBody ProductCreationRequest request) {
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully created product");
        apiResponse.setResult(productService.createProduct(request));
        return apiResponse;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> updateProduct(@PathVariable Long id, @RequestBody ProductUpdateRequest request ) {
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully updated product");
        apiResponse.setResult(productService.updateProduct(id, request));
        return apiResponse;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully deleted product");
        productService.deleteProduct(id);
        return apiResponse;
    }

    @GetMapping
    public ApiResponse<List<ProductResponse>> getAllProducts() {
        ApiResponse<List<ProductResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully retrieved products");
        apiResponse.setResult(productService.getAllProducts());
        return apiResponse;
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable Long id) {
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully retrieved product");
        apiResponse.setResult(productService.getProduct(id));
        return apiResponse;
    }

    @GetMapping("/jdbc/{id}")
    public ApiResponse<Map<String, Object>> getProductNamedJDBC(@PathVariable Long id) {
        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully retrieved product");
        apiResponse.setResult(productService.getProductByIdNamedJDBC(id));
        return apiResponse;
    }

    @GetMapping("/category/{id}")
    public ApiResponse<List<ProductResponse>> getProductsByCategory(@PathVariable Long id) {
        ApiResponse<List<ProductResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully retrieved products");
        apiResponse.setResult(productService.getAllProductsByCategory(id));
        return apiResponse;
    }

    // dùng store procedure + named jdbc
    @GetMapping("/category/{id}/by-price-desc")
    public ApiResponse<List<Map<String, Object>>> getProductsByCategoryAndSortByPrice(@PathVariable Long id) {
        ApiResponse<List<Map<String, Object>>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully retrieved products by category");
        apiResponse.setResult(productService.getProductsByCategoryOrderByPriceDesc(id));
        return apiResponse;
    }

    @GetMapping("/author/{author}")
    public ApiResponse<List<Map<String, Object>>> getProductsByAuthor(@PathVariable String author) {
        ApiResponse<List<Map<String, Object>>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully retrieved products by author");
        apiResponse.setResult(productService.getProductsByAuthor(author));
        return apiResponse;
    }

    @GetMapping("/spec/search")
    public ApiResponse<Page<ProductResponse>> search(
            @RequestParam String author,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ApiResponse<Page<ProductResponse>> response = new ApiResponse<>();
        response.setMessage("Successfully retrieved products");
        response.setResult(productService.searchByAuthorAndPriceRange(author, minPrice, maxPrice, PageRequest.of(page, size)));
        return response;
    }
}
