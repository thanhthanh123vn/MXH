package social_mate.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;
import social_mate.entity.enums.AuthProvider;

@Entity()
@Table(name = "users")
@Getter
@Setter

public class User extends AbstractEntity<User> {

	@Column(name = "username")
	private String username;

	@Column(name = "email")
	private String email;

	@Column(name = "password")
	private String password;

	@Column(name = "avatar")
	private String avatar="https://res.cloudinary.com/dgroxcuap/image/upload/v1763231600/avatar-blank_da7xpf.jpg";

	@Column(name = "auth_provider")
	@Enumerated(EnumType.STRING)
	private AuthProvider authProvider;


	@Column(name = "provider_id")
	private String providerId;

}