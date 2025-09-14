package tasc.bookstore.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import tasc.bookstore.dto.request.ProductCreationRequest;
import tasc.bookstore.dto.request.ProductUpdateRequest;
import tasc.bookstore.dto.response.CategoryResponse;
import tasc.bookstore.dto.response.ProductResponse;
import tasc.bookstore.entity.Product;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toCreateProduct(ProductCreationRequest request);
    void toUpdateProduct(@MappingTarget Product product, ProductUpdateRequest request);
//    ProductResponse toProductResponse(Product product);

    // Custom method map Product -> ProductResponse
    // dùng default (hoặc static) cho phép viết logic mapping tùy chỉnh ngay trong interface Mapper
    // MapStruct vẫn tự generate các method abstract khác
    // Không cần tạo class Mapper riêng, vẫn dùng @Mapper(componentModel="spring") → Spring inject trực tiếp
    default ProductResponse toProductResponse(Product product) {
        if (product == null) return null;

        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setAuthor(product.getAuthor());
        response.setLanguage(product.getLanguage());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setCost(product.getCost());
        response.setStock(product.getStock());
        response.setSoldQuantity(product.getSoldQuantity());
        response.setCreatedAt(product.getCreatedAt());
        response.setIsVisible(product.getIsVisible());

        // Map categories -> CategoryResponse
        if (product.getCategories() != null) {
            Set<CategoryResponse> categoryResponses = product.getCategories()
                    .stream()
                    .map(c -> new CategoryResponse(c.getName()))
                    .collect(Collectors.toSet());
            response.setCategories(categoryResponses);
        } else {
            response.setCategories(Collections.emptySet());
        }

        return response;
    }

}

