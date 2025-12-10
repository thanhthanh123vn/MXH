package social_mate.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import social_mate.entity.User;
import social_mate.entity.enums.AuthProvider;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);
	Optional<User> findByProviderId(String providerId);
	Optional<User> findByEmailAndAuthProvider(String email, AuthProvider authProvider);

}
