package social_mate.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import social_mate.entity.Message;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {


    @Query("select m from Message m join fetch m.sender where m.conversation.id = :conversationId ")
    List<Message> findByConversationId(@Param("conversationId") Long conversationId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
            "AND m.sender.id != :userId " +
            "AND m.messageStatus != 'SEEN'")
    List<Message> findUnseenMessagesInConversation(@Param("conversationId") Long conversationId,
                                                   @Param("userId") Long userId);
    @Query("SELECT m FROM Message m " +
            "JOIN m.conversation c " +
            "JOIN c.participants p " +
            "WHERE p.user.id = :userId " +
            "AND m.sender.id != :userId " +
            "AND m.messageStatus = 'SENT'")
    List<Message> findPendingMessagesForUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM Message m " +
            "WHERE m.conversation.id = :conversationId " +
            "AND m.sender.id != :userId " +
            "AND m.messageStatus != 'SEEN'")
    Long countUnseenMessages(@Param("conversationId") Long conversationId,
                             @Param("userId") Long userId);


}
