package social_mate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import social_mate.entity.post.Post;
import social_mate.entity.post.PostMedia;

import java.util.List;

@Repository
public interface PostMediaRepository extends JpaRepository<PostMedia, Long> {
    void deleteAllByPost(Post post);

    @Query("SELECT pm.url FROM PostMedia pm WHERE pm.post.id = :postId")
    List<String> findMediaUrlsByPostId(@Param("postId") Long postId);
}

