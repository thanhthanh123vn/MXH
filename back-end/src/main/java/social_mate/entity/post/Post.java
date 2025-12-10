package social_mate.entity.post;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import social_mate.entity.AbstractEntity;
import social_mate.entity.User;

import java.util.List;

@Entity
@Table(name = "Posts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Post extends AbstractEntity<Post> {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "content")
    private String content;

    @Column(name = "original_post_id")
    private Long originalPostId;

    @Column(name = "privacy_mode")
    private Integer privacyMode;

    @Column(name = "is_deleted")
    private Boolean deleted;

    @OneToMany(mappedBy = "post")
    private List<PostMedia> media;
//
//    @OneToMany(mappedBy = "post")
//    private List<Comment> comments;
//
//    @OneToMany(mappedBy = "post")
//    private List<Like> likes;






}
