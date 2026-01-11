package social_mate.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import social_mate.entity.User;
import social_mate.entity.post.Post;
import social_mate.entity.post.PostLike;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    // Tìm like theo user và post
    Optional<PostLike> findByUserAndPost(User user, Post post);

    // Kiểm tra user đã like bài viết chưa
    boolean existsByUserAndPost(User user, Post post);

    // Đếm tổng số like của bài viết
    long countByPost(Post post);

    // Xóa like theo post (dùng khi xóa bài viết)
    void deleteAllByPost(Post post);
// điếm lượt like theo id post
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.id = :postId")
    int countByPostId(@Param("postId") Long postId);
}