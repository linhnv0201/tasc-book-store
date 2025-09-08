package tasc.bookstore.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import tasc.bookstore.dto.request.SupplierCreationRequest;
import tasc.bookstore.dto.request.SupplierUpdateRequest;
import tasc.bookstore.dto.response.SupplierResponse;
import tasc.bookstore.entity.Supplier;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    Supplier toCreateSupplier(SupplierCreationRequest request);
    SupplierResponse toSupplierResponse(Supplier supplier);
    void toUpdateSupplier(@MappingTarget Supplier supplier, SupplierUpdateRequest request);

}
