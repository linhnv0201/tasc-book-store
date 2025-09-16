package tasc.bookstore.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
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
import tasc.bookstore.repository.ProductJDBCRepository;
import tasc.bookstore.repository.ProductRepository;
import tasc.bookstore.service.ProductService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static tasc.bookstore.specification.ProductSpecification.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {
    ProductRepository productRepository;
    ProductJDBCRepository productJDBCRepository;
    ProductMapper productMapper;
    CategoryRepository categoryRepository;
//    NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public ProductResponse createProduct(ProductCreationRequest request) {
        if (productRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.PRODUCT_EXISTED);
        }
        // Chuyển request thành Product entity (không set categories trước)
        Product product = productMapper.toCreateProduct(request);
        product.setCreatedAt(LocalDateTime.now());
        product.setCost(BigDecimal.ZERO);

        // Lấy danh sách Category entity từ categoryIds
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
        return filterProductForNonAdmin(productMapper.toProductResponse(product));
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toProductResponse)
                .map(this::filterProductForNonAdmin)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)  // ← cần để giữ connection mở, dùng với khai báo StoreProcedure
    public List<ProductResponse> getAllProductsByCategory(Long category) {
        return productRepository.GetProductsByCategoryId(category).stream()
                .map(productMapper::toProductResponse)
                .map(this::filterProductForNonAdmin)
                .toList();
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        productRepository.delete(product);
    }

    @Override
    public Map<String, Object> getProductByIdNamedJDBC(Long id) {
        List<Map<String, Object>> rows = productJDBCRepository.findProductWithCategoriesById(id);

        if (rows.isEmpty()) {
            return null; // hoặc throw new AppException(...)
        }

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> firstRow = rows.get(0);
        result.put("id", firstRow.get("product_id"));
        result.put("name", firstRow.get("product_name"));
        result.put("price", firstRow.get("price"));
        result.put("cost", firstRow.get("cost"));

        // Gom danh sách category
        List<String> categories = rows.stream()
                .map(r -> (String) r.get("category_name"))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        result.put("categories", categories);

        return result;
    }

    @Override
    public List<Map<String, Object>> getProductsByCategoryOrderByPriceDesc(Long categoryId) {
        return productJDBCRepository.findProductsByCategoryOrderByPriceDesc(categoryId);
    }

    @Override
    public List<Map<String, Object>> getProductsByAuthor(String author) {
        List<Object[]> rows = productRepository.findProductsByAuthor(author);

        // Map<ProductId, ProductInfo + Set<CategoryName>>
        Map<Long, Map<String, Object>> productMap = new LinkedHashMap<>();

        for (Object[] row : rows) {
            Long productId = ((Number) row[0]).longValue();

            Map<String, Object> productEntry = productMap.get(productId);
            if (productEntry == null) {
                productEntry = new HashMap<>();
                productEntry.put("product_id", productId);
                productEntry.put("product_name", row[1]);
                productEntry.put("author", row[2]);
                productEntry.put("description", row[3]);
                productEntry.put("price", row[4]);
                productEntry.put("stock", row[5]);
                productEntry.put("categories", new HashSet<String>());
                productMap.put(productId, productEntry);
            }

            String catName = (String) row[6];
            if (catName != null) {
                ((Set<String>) productEntry.get("categories")).add(catName);
            }
        }

        return new ArrayList<>(productMap.values());
    }


    // test Specification + paging
    @Override
    public Page<ProductResponse> fullSearch(String name, String author, String language, List<Long> categoriesId
            , BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findAll(
                hasName(name)
                        .and(hasAuthor(author))
                        .and(hasLanguage(language))
                        .and(hasCategories(categoriesId))
                        .and(hasPriceBetween(minPrice, maxPrice)),
                pageable
        ).map(productMapper::toProductResponse);
    }


    private boolean isAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getName())
                && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private ProductResponse filterProductForNonAdmin(ProductResponse productResponse) {
        if (!isAdmin()) {
            productResponse.setCost(null);
        }
        return productResponse;
    }

    @Override
    public List<Map<String, Object>> getTopSoldProducts(LocalDate startDate, LocalDate endDate){
        return productJDBCRepository.getTopSoldProducts(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getPurchaseOrderItemBySupplierId(Long supplierId, LocalDate startDate, LocalDate endDate) {
        return productJDBCRepository.getPurchaseOrderItemBySupplierId(supplierId, startDate, endDate);
    }



}
