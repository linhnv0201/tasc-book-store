package tasc.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tasc.bookstore.dto.response.UserResponse;
import tasc.bookstore.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    //Optional là wrapper, cho biết kết quả có thể có hoặc không.
    //Nếu email tồn tại trong DB → Optional<User> chứa user.
    //Nếu không tồn tại → Optional.empty().
    //👉 Điều này giúp bạn tránh lỗi NullPointerException vì không cần trả về null.
    Optional<User> findByEmail(String email);

    @Query("SELECT new tasc.bookstore.dto.response.UserResponse(" +
            "u.email, u.password, u.fullname, u.phone, u.address, u.role) " +
            "FROM User u " +
            "WHERE u.fullname = :fullname")
    List<UserResponse> findUsersByFullname(@Param("fullname") String fullname);
}
