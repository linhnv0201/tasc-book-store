package tasc.bookstore.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import tasc.bookstore.dto.request.UserCreationRequest;
import tasc.bookstore.dto.request.UserUpdateRequest;
import tasc.bookstore.dto.response.UserResponse;
import tasc.bookstore.entity.User;

//Báo cho MapStruct biết đây là mapper interface.
//componentModel = "spring" → MapStruct sẽ generate 1 implementation class và annotate nó
//  với @Component, để Spring quản lý bean này.
//Nhờ đó, bạn có thể @Autowired hoặc @RequiredArgsConstructor để inject UserMapper vào service.
@Mapper(componentModel = "spring")
public interface UserMapper {
    User toCreateUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);
    void toUpdateUser(@MappingTarget User user, UserUpdateRequest request);
    //Bình thường, MapStruct sẽ tạo mới object khi map từ DTO → Entity.
    //Nhưng khi bạn muốn cập nhật một object đã có sẵn (ví dụ User lấy từ database), bạn cần nói cho MapStruct biết là:
    //👉 “Hãy map các field từ DTO vào object này, đừng tạo mới.”
    //Annotation @MappingTarget chính là để làm việc đó.
}
