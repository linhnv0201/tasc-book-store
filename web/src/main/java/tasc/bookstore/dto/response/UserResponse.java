package tasc.bookstore.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import tasc.bookstore.enums.Role;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String email;
    String password;
    String fullname;
    String phone;
    String address;
    Set<Role> role;
}