package tasc.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tasc.bookstore.entity.Message;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationId(Long conversationId);
}
