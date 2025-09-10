package tasc.bookstore.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tasc.bookstore.dto.request.ProductCreationRequest;
import tasc.bookstore.dto.request.ProductUpdateRequest;
import tasc.bookstore.dto.response.ProductResponse;
import tasc.bookstore.entity.Category;
import tasc.bookstore.entity.Product;
import tasc.bookstore.exception.AppException;
import tasc.bookstore.exception.ErrorCode;
import tasc.bookstore.mapper.ProductMapper;
import tasc.bookstore.repository.CategoryRepository;
import tasc.bookstore.repository.ProductRepository;
import tasc.bookstore.service.ProductService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {
    ProductRepository productRepository;
    ProductMapper productMapper;
    CategoryRepository categoryRepository;

    @Override
    public ProductResponse createProduct(ProductCreationRequest request) {
        if (productRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.PRODUCT_EXISTED);
        }
        // Chuyển request thành Product entity (không set categories trước)
        Product product = productMapper.toCreateProduct(request);
        product.setCreatedAt(LocalDateTime.now());

        // Lấy danh sách Category entity từ categoryIds
//        Set<Category> categoryEntities = categoryRepository.findAllById(request.getCategoryIds())
//                .stream()
//                .collect(Collectors.toSet());
        Set<Category> categoryEntities = new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));

        // Gán category entity cho product
        product.setCategories(categoryEntities);

        // Lưu product, Hibernate sẽ tự động cập nhật bảng product_categories
        product = productRepository.save(product);

        return productMapper.toProductResponse(product);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // Kiểm tra tên mới
        if (request.getName() != null && !product.getName().equals(request.getName())
                && productRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.PRODUCT_EXISTED);
        }

        // Cập nhật product bằng mapper
        productMapper.toUpdateProduct(product, request);

        product = productRepository.save(product);

        return productMapper.toProductResponse(product);
    }

    @Override
    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return productMapper.toProductResponse(product);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)  // ← cần để giữ connection mở, dùng với khai báo StoreProcedure
    public List<ProductResponse> getAllProductsByCategory(Long category) {
        return productRepository.GetProductsByCategoryId(category).stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        productRepository.delete(product);
    }
}
