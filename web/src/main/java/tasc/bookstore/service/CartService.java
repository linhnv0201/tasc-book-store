package tasc.bookstore.service;

import tasc.bookstore.dto.request.CartItemRequest;
import tasc.bookstore.dto.response.CartResponse;
import tasc.bookstore.entity.Cart;
import tasc.bookstore.entity.User;

public interface CartService {

    CartResponse getCart();
    CartResponse addToCart(CartItemRequest request);
    CartResponse updateCartItem(CartItemRequest request);
    void removeFromCart(Long productId);
}
