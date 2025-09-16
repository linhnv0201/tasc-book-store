package tasc.bookstore.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tasc.bookstore.dto.request.CartItemRequest;
import tasc.bookstore.dto.response.CartResponse;
import tasc.bookstore.entity.Cart;
import tasc.bookstore.entity.CartItem;
import tasc.bookstore.entity.Product;
import tasc.bookstore.entity.User;
import tasc.bookstore.exception.AppException;
import tasc.bookstore.exception.ErrorCode;
import tasc.bookstore.mapper.CartMapper;
import tasc.bookstore.repository.CartRepository;
import tasc.bookstore.repository.ProductRepository;
import tasc.bookstore.repository.UserRepository;
import tasc.bookstore.service.CartService;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartServiceImpl implements CartService {
    UserRepository userRepository;
    CartRepository cartRepository;
    ProductRepository productRepository;
    CartMapper cartMapper;

    @Override
    @Transactional // tránh tạo 1 cart trùng cho nhiều user 1 lúc
    public CartResponse getCart() {

        Cart cart = cartRepository.findByCustomerId(getCurrentUser().getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(getCurrentUser());
                    return cartRepository.save(newCart);
                });

        CartResponse response = cartMapper.toCartResponse(cart);

        // Tính totalPrice
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.setTotalPrice(total);
        response.setCustomerName(getCurrentUser().getFullname());

        return response;
    }

    @Transactional
    public Cart getCartEntity() {
        return cartRepository.findByCustomerId(getCurrentUser().getId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.CART_NOT_FOUND));
    }

    @Override
    @Transactional
    public CartResponse addToCart(CartItemRequest request) {
        Cart cart = getCartEntity();

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Optional<CartItem> itemOpt = cart.getItems().stream()
                .filter(ci -> ci.getProduct().getId().equals(request.getProductId()))
                .findFirst();

        itemOpt.ifPresentOrElse(
                item -> item.setQuantity(item.getQuantity() + request.getQuantity()),
                () -> {
                    var item = new CartItem();
                    item.setCart(cart);
                    item.setProduct(product);
                    item.setQuantity(request.getQuantity());
                    cart.getItems().add(item);
                }
        );
        cartRepository.save(cart);
        CartResponse cartResponse = cartMapper.toCartResponse(cart);
        // Tính totalPrice
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cartResponse.setTotalPrice(total);
        return cartResponse;
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(CartItemRequest request) {
        if (request.getProductId() == null) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (request.getQuantity() <= 0) {
            throw new AppException(ErrorCode.NEGATIVE_QUANTITY);
        }

        Cart cart = getCartEntity();

        // Tìm item trong cart
        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getProduct().getId().equals(request.getProductId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (request.getQuantity() <= 0) {
            cart.getItems().remove(item); // xóa nếu quantity <= 0
        } else {
            item.setQuantity(request.getQuantity()); // cập nhật quantity
        }

        cartRepository.save(cart);
        CartResponse cartResponse = cartMapper.toCartResponse(cart);
        // Tính totalPrice
        BigDecimal total = cart.getItems().stream()
                .map(item2 -> item2.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item2.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cartResponse.setTotalPrice(total);
        return cartResponse;
    }

    @Override
    @Transactional
    public void removeFromCart(Long productId) {
        Cart cart = getCartEntity();
        cart.getItems().removeIf(ci -> ci.getProduct().getId().equals(productId));
        cartRepository.save(cart);
    }

    private User getCurrentUser() {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));


    }

}
