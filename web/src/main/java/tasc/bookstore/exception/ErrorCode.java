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
    INVALID_PASSWORD(999, "At least 4 characters"),
    WRONG_PASSWORD(999, "Wrong password"),
    UNAUTHENTICATED(1001, "Unauthenticated"),
    INVALID_ROLE(1002, "Invalid role"),
    SUPPLIER_EXISTED(1003, "Supplier already existed"),
    CATEGORY_EXISTED(1003, "Category already existed"),
    SUPPLIER_NOT_FOUND(1003, "Supplier not found"),
    CATEGORY_NOT_FOUND(1003, "Category not found"),
    PRODUCT_EXISTED(1003, "Product already existed"),
    PRODUCT_NOT_EXIST(1003, "Product not existed"),
    PRODUCT_NOT_FOUND(1003, "Product not found"),
    ;
    int code;
    String message;
}
