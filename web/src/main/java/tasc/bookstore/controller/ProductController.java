package tasc.bookstore.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tasc.bookstore.dto.request.ProductCreationRequest;
import tasc.bookstore.dto.request.ProductUpdateRequest;
import tasc.bookstore.dto.response.ApiResponse;
import tasc.bookstore.dto.response.ProductResponse;
import tasc.bookstore.service.ProductService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    ProductService productService;

    @PostMapping
    public ApiResponse<ProductResponse> createProduct(@RequestBody ProductCreationRequest request) {
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully created product");
        apiResponse.setResult(productService.createProduct(request));
        return apiResponse;
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> updateProduct(@PathVariable Long id, @RequestBody ProductUpdateRequest request ) {
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully updated product");
        apiResponse.setResult(productService.updateProduct(id, request));
        return apiResponse;
    }

    @DeleteMapping("/{id}")
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

    @GetMapping("/category/{id}")
    public ApiResponse<List<ProductResponse>> getProductsByCategory(@PathVariable Long id) {
        ApiResponse<List<ProductResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully retrieved products");
        apiResponse.setResult(productService.getAllProductsByCategory(id));
        return apiResponse;
    }
}
