package tasc.bookstore.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import tasc.bookstore.dto.request.ProductCreationRequest;
import tasc.bookstore.dto.request.ProductUpdateRequest;
import tasc.bookstore.dto.request.SupplierCreationRequest;
import tasc.bookstore.dto.request.SupplierUpdateRequest;
import tasc.bookstore.dto.response.ProductResponse;
import tasc.bookstore.dto.response.SupplierResponse;
import tasc.bookstore.entity.Product;
import tasc.bookstore.entity.Supplier;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toCreateProduct(ProductCreationRequest request);
    ProductResponse toProductResponse(Product product);
    void toUpdateProduct(@MappingTarget Product product, ProductUpdateRequest request);

}

