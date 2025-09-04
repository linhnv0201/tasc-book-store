package tasc.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    String email;

    @Column(nullable = false)
    String password;

    String fullname;

    String phone;

    @Column(columnDefinition = "TEXT")
    String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Role role;

    public enum Role {
        CUSTOMER, EMPLOYEE, ADMIN
    }
}
