package tasc.bookstore.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemRequest {
    private Long productId;
    private Integer quantity;
}
