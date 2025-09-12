package tasc.bookstore.mapper;

import org.mapstruct.Mapper;
import tasc.bookstore.dto.response.CartItemResponse;
import tasc.bookstore.dto.response.CartResponse;
import tasc.bookstore.entity.Cart;
import tasc.bookstore.entity.CartItem;

@Mapper(componentModel = "spring")
public interface CartMapper {
    CartResponse toCartResponse(Cart cart);
    CartItemResponse toCartItemResponse(CartItem cartItem);
}

//        Supplier toCreateSupplier(SupplierCreationRequest request);
//        SupplierResponse toSupplierResponse(Supplier supplier);
//        void toUpdateSupplier(@MappingTarget Supplier supplier, SupplierUpdateRequest request);
