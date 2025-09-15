package tasc.bookstore.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tasc.bookstore.dto.request.UserCreationRequest;
import tasc.bookstore.dto.request.UserPasswordUpdateRequest;
import tasc.bookstore.dto.request.UserUpdateRequest;
import tasc.bookstore.dto.response.ApiResponse;
import tasc.bookstore.dto.response.UserResponse;
import tasc.bookstore.service.UserService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> createUser(@RequestBody UserCreationRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.createUser(request));
        return apiResponse;
    }

    // Update User
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UserUpdateRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.updateUser(id, request));
        apiResponse.setMessage("Successfully updated user");
        return apiResponse;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    ApiResponse<List<UserResponse>> getUsers(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("Email: {}", authentication.getName());
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));

        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }

    // Get user by id
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getUser(id));
        return apiResponse;
    }

    // Delete user
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);

        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("User deleted successfully");
        return apiResponse;
    }

    //có danh sách role
    @GetMapping("/myInfo")
    ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @GetMapping("/myInfoJDBC")
    public ApiResponse<Map<String, Object>> getMyInfoJDBC() {
        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getMyInfoJDBC());
        return apiResponse;
    }

    @GetMapping("/fullname/{fullname}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ApiResponse<List<UserResponse>> getUsersByFullname(@PathVariable String fullname) {
        ApiResponse<List<UserResponse>> response = new ApiResponse<>();
        response.setMessage("Successfully retrieved users by fullname");
        response.setResult(userService.getUsersByFullname(fullname));
        return response;
    }

    // Update User
    @PutMapping("/myInfo")
    public ApiResponse<UserResponse> updateMyInfo(@RequestBody @Valid UserUpdateRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.updateMyInfo(request));
        apiResponse.setMessage("Successfully updated your info");
        return apiResponse;
    }

    @PutMapping("/update-password")
    public ApiResponse<UserResponse> updatePassword(@RequestBody @Valid UserPasswordUpdateRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.updateMyPassword(request));
        apiResponse.setMessage("Successfully updated your password");
        return apiResponse;
    }
}
