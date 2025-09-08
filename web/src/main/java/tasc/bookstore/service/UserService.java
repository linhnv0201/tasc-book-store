package tasc.bookstore.service;

import tasc.bookstore.dto.request.UserCreationRequest;
import tasc.bookstore.dto.request.UserUpdateRequest;
import tasc.bookstore.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserCreationRequest request);
    UserResponse registerCustomer(UserCreationRequest request);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    UserResponse updateMyInfo(UserUpdateRequest request);
    List<UserResponse> getUsers();
    UserResponse getUser(Long id);
    void deleteUser(Long id);
    UserResponse getMyInfo();
}
