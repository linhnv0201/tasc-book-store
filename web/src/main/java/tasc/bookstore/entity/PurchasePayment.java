package tasc.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "purchase_payments")
public class PurchasePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "purchase_order_id")
    PurchaseOrder purchaseOrder;

    @Column(nullable = false)
    LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentMethod paymentMethod;

    @Column(nullable = false)
    BigDecimal amount;

    @Enumerated(EnumType.STRING)
    Status status;

    @ManyToOne
    @JoinColumn(name = "created_by")
    User createdBy;

    public enum PaymentMethod {
        CASH, BANK_TRANSFER, OTHER
    }

    public enum Status {
        UNPAID, PAID
    }
}
