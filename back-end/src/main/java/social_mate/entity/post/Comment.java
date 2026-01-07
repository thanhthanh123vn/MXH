package social_mate.entity.post;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import social_mate.entity.AbstractEntity;
import social_mate.entity.User;

@Entity
@Table(name = "Comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Comment extends AbstractEntity<Comment> {

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    private Post post;
}