package tasc.bookstore.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tasc.bookstore.dto.response.OrderItemResponse;
import tasc.bookstore.dto.response.OrderResponse;
import tasc.bookstore.entity.Order;
import tasc.bookstore.entity.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(source = "items", target = "items")
    OrderResponse toOrderResponse(Order order);

    @Mapping(source = "product.name", target = "productName") // chỉ map tên product
    OrderItemResponse toOrderItemResponse(OrderItem item);
}
