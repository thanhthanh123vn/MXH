package social_mate.entity.post;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import social_mate.entity.AbstractEntity;

@Entity
@Table(name = "PostMedia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostMedia extends AbstractEntity<PostMedia> {
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    @Column(name="type")
    private String type;


    @Column(name = "url")
    private String url;
    @Column(name = "file_name")
    private String fileName;
}
