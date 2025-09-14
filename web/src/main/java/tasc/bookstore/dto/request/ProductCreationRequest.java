package tasc.bookstore.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreationRequest {

    @NotBlank(message = "Product name is required")
    String name;

    String author;

    String language;

    @NotEmpty(message = "At least one category is required")
    Set<@NotNull Long> categoryIds;

    String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true, message = "Cost must be >= 0")
    BigDecimal cost;

}
