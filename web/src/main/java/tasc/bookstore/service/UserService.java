package tasc.bookstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tasc.bookstore.dto.request.UserCreationRequest;
import tasc.bookstore.dto.request.UserPasswordUpdateRequest;
import tasc.bookstore.dto.request.UserUpdateRequest;
import tasc.bookstore.dto.response.UserResponse;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserResponse createUser(UserCreationRequest request);
    UserResponse registerCustomer(UserCreationRequest request);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    UserResponse updateMyInfo(UserUpdateRequest request);
    UserResponse updateMyPassword(UserPasswordUpdateRequest request);
    List<UserResponse> getUsers();
    UserResponse getUser(Long id);
    void deleteUser(Long id);
    UserResponse getMyInfo();
//    Map<String, Object> getMyInfoJDBC();
    UserResponse getMyInfoJDBC();
//    List<UserResponse> getUsersByRole(String roleName);
    List<UserResponse> getUsersByFullname(String fullname);
    Page<UserResponse> searchUsers(String email, String fullname, String role, Pageable pageable);
}
