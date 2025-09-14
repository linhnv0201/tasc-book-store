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
public class PurchaseOrderResponse {
    String code;
    String supplierName;
    String createdBy;
    BigDecimal totalAmount;
    String note;
    List<PurchaseOrderItemResponse> items;
}
