package tasc.bookstore.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tasc.bookstore.dto.request.CartItemRequest;
import tasc.bookstore.dto.response.ApiResponse;
import tasc.bookstore.dto.response.CartResponse;
import tasc.bookstore.exception.AppException;
import tasc.bookstore.exception.ErrorCode;
import tasc.bookstore.service.CartService;

@Slf4j
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartController {
    CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getCart() {
        ApiResponse<CartResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(cartService.getCart());
        return apiResponse;
    }

    @PostMapping("/add")
    public ApiResponse<CartResponse> addToCart(@RequestBody CartItemRequest request) {
        if (request.getProductId() == null) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new AppException(ErrorCode.NEGATIVE_QUANTITY);
        }

        ApiResponse<CartResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Add to cart successful");
        apiResponse.setResult(cartService.addToCart(request));
        return apiResponse;
    }

    @PutMapping("/update")
    public ApiResponse<CartResponse> updateCartItem(@RequestBody CartItemRequest request){
        if (request.getProductId() == null) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new AppException(ErrorCode.NEGATIVE_QUANTITY);
        }
        ApiResponse<CartResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Update cart successful");
        apiResponse.setResult(cartService.updateCartItem(request));
        return apiResponse;
    }

    @DeleteMapping("/remove/{productId}")
    public ApiResponse<Void> removeFromCart(@PathVariable Long productId){
        cartService.removeFromCart(productId);
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Remove from cart successful");
        return apiResponse;
    }
}
