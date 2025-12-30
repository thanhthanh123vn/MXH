package social_mate.entity.post;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import social_mate.entity.AbstractEntity;
import social_mate.entity.User;

@Entity
@Table(name = "post_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "post_id"}) // Đảm bảo 1 user chỉ like 1 bài 1 lần
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostLike extends AbstractEntity<PostLike> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}