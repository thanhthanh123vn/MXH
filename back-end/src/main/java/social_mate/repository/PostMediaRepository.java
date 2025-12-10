package social_mate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import social_mate.entity.post.PostMedia;
@Repository
public interface PostMediaRepository extends JpaRepository<PostMedia, Long> {
}
