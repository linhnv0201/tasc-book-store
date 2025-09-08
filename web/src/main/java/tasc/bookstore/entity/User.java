package tasc.bookstore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

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
    @Size(min = 4, message = "INVALID_PASSWORD")
    String password;

    String fullname;

    String phone;

    //tạo cột trong DB kiểu TEXT thay vì để JPA tự chọn
    @Column(columnDefinition = "TEXT")
    String address;

    Set<String> role;
}
