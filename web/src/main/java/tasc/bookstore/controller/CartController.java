package tasc.bookstore.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tasc.bookstore.entity.Cart;
import tasc.bookstore.service.CartService;

@Slf4j
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartController {
    CartService cartService;

    @GetMapping
    public Cart getCart() {
        return cartService.getCart();
    }

    @PostMapping("/add")
    public void addToCart( Long productId, int quantity){
        cartService.addToCart(productId, quantity);
    };

    @PostMapping("/remove")
    public void removeFromCart( Long productId){
        cartService.removeFromCart(productId);
    }

    @PostMapping("/update")
    public void updateCartItem( Long productId, int quantity){
        cartService.updateCartItem(productId, quantity);
    }
}
