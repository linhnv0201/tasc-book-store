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
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    String author;

    String language;

    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(nullable = false)
    BigDecimal price;

    BigDecimal cost;

    @Column(nullable = false)
    Integer stock;

    @Builder.Default
    Integer soldQuantity = 0;

    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt = LocalDateTime.now();
}
