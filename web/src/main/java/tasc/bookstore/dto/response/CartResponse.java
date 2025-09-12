package tasc.bookstore.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartResponse {
    Long id;
    String customerName;
    List<CartItemResponse> items;
    BigDecimal totalPrice;    // tính toán dựa trên giá hiện tại của product
}
