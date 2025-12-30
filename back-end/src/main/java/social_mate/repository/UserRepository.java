package social_mate.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import social_mate.entity.User;
import social_mate.entity.enums.AuthProvider;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);
	Optional<User> findByProviderId(String providerId);
	Optional<User> findByEmailAndAuthProvider(String email, AuthProvider authProvider);

	@Query("""
    SELECT u FROM User u
    WHERE u.id <> :currentUserId
      AND (
        LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        
      )
	""")
	List<User> searchUsers(
			@Param("keyword") String keyword,
			@Param("currentUserId") Long currentUserId
	);



}
