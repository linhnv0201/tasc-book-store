package tasc.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tasc.bookstore.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
}
