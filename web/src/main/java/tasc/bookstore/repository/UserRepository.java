package tasc.bookstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tasc.bookstore.dto.response.UserResponse;
import tasc.bookstore.entity.Product;
import tasc.bookstore.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Page<User> findAll(Specification<User> spec, Pageable pageable);

    boolean existsByEmail(String email);
    //Optional là wrapper, cho biết kết quả có thể có hoặc không.
    //Nếu email tồn tại trong DB → Optional<User> chứa user.
    //Nếu không tồn tại → Optional.empty().
    //👉 Điều này giúp bạn tránh lỗi NullPointerException vì không cần trả về null.
    Optional<User> findByEmail(String email);

    //Đây là class-based projection.
    //JPA sẽ khởi tạo trực tiếp object UserResponse cho mỗi row trả về từ DB.
    //Các giá trị u.email, u.password, ... được truyền vào constructor của UserResponse.
    //triu vấn này dùng jpql (dùng tên class và field của entity chứ ko phải của db)
    @Query("SELECT new tasc.bookstore.dto.response.UserResponse(" +
            "u.email, u.password, u.fullname, u.phone, u.address, u.role) " +
            "FROM User u " +
            "WHERE u.fullname = :fullname")
    List<UserResponse> findUsersByFullname(@Param("fullname") String fullname);

}
