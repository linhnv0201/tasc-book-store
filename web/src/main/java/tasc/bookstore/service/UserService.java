package tasc.bookstore.service;

import tasc.bookstore.dto.request.UserCreationRequest;
import tasc.bookstore.dto.request.UserUpdateRequest;
import tasc.bookstore.entity.User;

import java.util.List;

public interface UserService {
    User createUser(UserCreationRequest request);
    User updateUser(Long id, UserUpdateRequest request);
    List<User> getUsers();
    User getUser(Long id);
    void deleteUser(Long id);
}
