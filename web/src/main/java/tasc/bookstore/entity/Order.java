package tasc.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    String code;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    User customer;

    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Status status;

    BigDecimal totalAmount;

//    @Column(columnDefinition = "TEXT")
//    String shippingAddress;

    @Column(columnDefinition = "TEXT")
    String note;

//    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
//    List<OrderItem> items;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>(); // <-- quan trọng

    public enum Status {
        PENDING, PAID, SHIPPED, CANCELLED
    }
}
