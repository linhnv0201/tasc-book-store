package tasc.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tasc.bookstore.entity.Conversation;
import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByCustomerId(Long customerId);
    List<Conversation> findByEmployeeId(Long employeeId);
}
