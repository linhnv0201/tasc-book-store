package tasc.bookstore.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tasc.bookstore.dto.response.PurchaseOrderItemResponse;
import tasc.bookstore.dto.response.PurchaseOrderResponse;
import tasc.bookstore.entity.PurchaseOrder;
import tasc.bookstore.entity.PurchaseOrderItem;

@Mapper(componentModel = "spring")
public interface PurchaseOrderMapper {
    @Mapping(source = "items", target = "items")
    @Mapping(source = "createdBy.fullname", target = "createdBy") // map sang field trong DTO
    PurchaseOrderResponse toResponse(PurchaseOrder purchaseOrder);

    @Mapping(source = "product.name", target = "productName") // chỉ map tên product
    PurchaseOrderItemResponse toItemResponse(PurchaseOrderItem item);
}
