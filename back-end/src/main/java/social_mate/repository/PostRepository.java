package social_mate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import social_mate.dto.request.PostRequestDto;
import social_mate.entity.post.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

}
