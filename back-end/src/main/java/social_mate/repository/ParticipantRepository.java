package social_mate.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import social_mate.entity.Participant;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

	@Query("SELECT p FROM Participant p " + "JOIN FETCH p.user " + "WHERE p.conversation.id = :conversationId")
	List<Participant> findByConversationId(@Param("conversationId") Long conversationId);

	Optional<Participant> findByConversationIdAndUserId(Long conversationId, Long userId);

}
