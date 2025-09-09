package tasc.bookstore.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tasc.bookstore.entity.Cart;
import tasc.bookstore.entity.CartItem;
import tasc.bookstore.entity.Product;
import tasc.bookstore.entity.User;
import tasc.bookstore.exception.AppException;
import tasc.bookstore.exception.ErrorCode;
import tasc.bookstore.repository.CartRepository;
import tasc.bookstore.repository.ProductRepository;
import tasc.bookstore.repository.UserRepository;
import tasc.bookstore.service.CartService;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartServiceImpl implements CartService {
    UserRepository userRepository;
    CartRepository cartRepository;
    ProductRepository productRepository;


    @Override
    //traánh tạo 1 cart trùng cho nhiều user
    @Transactional
    public Cart getCart() {
        // Lấy user đang login
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Lấy cart của user
        return cartRepository.findByCustomerId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(user);
                    return cartRepository.save(newCart);
                });
    }


    @Override
    @Transactional
    public void addToCart(Long productId, int quantity) {
        Cart cart = getCart(); // lấy cart của user đang login

        // Kiểm tra xem sản phẩm đã có trong cart chưa
        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (item != null) {
            // Nếu có rồi thì tăng số lượng
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            // Nếu chưa có thì tạo mới CartItem
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            cart.getItems().add(item);
        }

        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void updateCartItem(Long productId, int quantity) {
        Cart cart = getCart();

        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (quantity <= 0) {
            // nếu số lượng <= 0 thì xóa item
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }

        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void removeFromCart(Long productId) {
        Cart cart = getCart();
        cart.getItems().removeIf(ci -> ci.getProduct().getId().equals(productId));
        cartRepository.save(cart);
    }

}
