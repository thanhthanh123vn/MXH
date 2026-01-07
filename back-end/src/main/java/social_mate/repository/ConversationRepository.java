package social_mate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import social_mate.entity.Conversation;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT DISTINCT c FROM Conversation c " +
            "JOIN FETCH c.participants p " +
            "JOIN FETCH p.user " +
            "WHERE c IN (SELECT p2.conversation FROM Participant p2 WHERE p2.user.id = :userId) " +
            "ORDER BY c.updatedAt DESC")
    List<Conversation> findConversationsByUserId(@Param("userId") Long userId);

    @Query("Select c from Conversation c join c.participants p where c.id= :conversationId and p.user.id= :userId")
    Optional<Conversation> findByIdAndUserId(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    @Query("SELECT c FROM Conversation c " +
            "JOIN c.participants p1 " +
            "JOIN c.participants p2 " +
            "WHERE c.conversationType = 'DIRECT' " +
            "AND p1.user.id = :userId1 " +
            "AND p2.user.id = :userId2")
    Optional<Conversation> findExistingDirectConversation(@Param("userId1") Long userId1,
                                                          @Param("userId2") Long userId2);

}
