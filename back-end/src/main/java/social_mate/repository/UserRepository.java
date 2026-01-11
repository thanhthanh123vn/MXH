package social_mate.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import social_mate.dto.response.ProfileResponseDto;
import social_mate.entity.User;
import social_mate.entity.enums.AuthProvider;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);
	Optional<User> findByProviderId(String providerId);
	Optional<User> findByEmailAndAuthProvider(String email, AuthProvider authProvider);
	List<User> findByUsernameContainingIgnoreCase(String username);
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
	@Query("""
        SELECT new social_mate.dto.response.ProfileResponseDto(
            u.id,
            u.username,
            u.email,
            u.avatar,
            d.bio,
            d.jobTitle,
            d.coverPhoto,
            d.address,
            0,
            0,
            social_mate.entity.enums.FriendViewStatus.NONE   
        )
        FROM User u
        LEFT JOIN UserDetail d ON d.user.id = u.id
        WHERE u.id = :userId
    """)
	Optional<ProfileResponseDto> findProfileByUserId(@Param("userId") Long userId);

}
