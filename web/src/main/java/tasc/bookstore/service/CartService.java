package tasc.bookstore.service;

import tasc.bookstore.entity.Cart;
import tasc.bookstore.entity.User;

public interface CartService {

    Cart getCart();
    void addToCart( Long productId, int quantity);
    void removeFromCart(Long productId);
    void updateCartItem(Long productId, int quantity);
}
