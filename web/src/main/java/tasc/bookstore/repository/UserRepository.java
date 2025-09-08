package tasc.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tasc.bookstore.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);
    //Optional là wrapper, cho biết kết quả có thể có hoặc không.
    //Nếu email tồn tại trong DB → Optional<User> chứa user.
    //Nếu không tồn tại → Optional.empty().
    //👉 Điều này giúp bạn tránh lỗi NullPointerException vì không cần trả về null.
    Optional<User> findByEmail(String email);


}
