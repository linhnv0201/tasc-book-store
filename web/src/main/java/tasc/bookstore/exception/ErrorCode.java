package tasc.bookstore.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    USER_EXISTED(999, "User already existed"),
    USER_NOT_FOUND(999, "User not found"),
    INVALID_PASSWORD(1003, "at least 4 characters"),
    UNAUTHENTICATED(1001, "Unauthenticated"),
    INVALID_ROLE(1002, "Invalid role"),
    ;
    int code;
    String message;
}
