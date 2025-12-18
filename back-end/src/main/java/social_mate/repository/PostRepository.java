package social_mate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import social_mate.dto.request.PostRequestDto;
import social_mate.entity.User;
import social_mate.entity.post.Post;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.deleted = false")
    List<Post> findAllByUserOrderByCreatedAtDesc(User user);

}
