package social_mate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity()
@Table(name = "user_details")
@Getter
@Setter
public class UserDetail extends AbstractEntity<UserDetail> {
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "bio")
    private String bio;
    @Column(name = "job_title")
    private String jobTitle;
    @Column(name = "cover_photo")
    private String coverPhoto;
    @Column(name = "address")
    private String address;

}
