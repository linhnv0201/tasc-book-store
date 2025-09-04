package tasc.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id")
    Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    User sender;

    @Column(columnDefinition = "TEXT")
    String message;

    @Column(nullable = false, updatable = false)
    LocalDateTime sentAt = LocalDateTime.now();

    @Builder.Default
    Boolean isRead = false;
}
