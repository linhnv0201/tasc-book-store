package tasc.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tasc.bookstore.entity.InvalidatedToken;
@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
}
