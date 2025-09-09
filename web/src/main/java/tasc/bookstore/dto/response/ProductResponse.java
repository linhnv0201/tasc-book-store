package tasc.bookstore.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    Long id;
    String name;
    String author;
    String language;
    Set<CategoryResponse> categories;
    String description;
    BigDecimal price;
    BigDecimal cost;
    Integer stock;
    Integer soldQuantity;
    LocalDateTime createdAt;
}
