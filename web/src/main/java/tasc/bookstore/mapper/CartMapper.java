package tasc.bookstore.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tasc.bookstore.dto.response.CartItemResponse;
import tasc.bookstore.dto.response.CartResponse;
import tasc.bookstore.entity.Cart;
import tasc.bookstore.entity.CartItem;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(source = "customer.fullname", target = "customerName")
    CartResponse toCartResponse(Cart cart);
    CartItemResponse toCartItemResponse(CartItem cartItem);
}
