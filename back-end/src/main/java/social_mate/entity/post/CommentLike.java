package social_mate.entity.post;

import jakarta.persistence.*;
import lombok.*;
import social_mate.entity.AbstractEntity;
import social_mate.entity.User;

@Entity
@Table(name = "comment_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "comment_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentLike extends AbstractEntity<CommentLike> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;
}