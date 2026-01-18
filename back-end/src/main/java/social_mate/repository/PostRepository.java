package social_mate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import social_mate.dto.request.PostRequestDto;
import social_mate.entity.User;
import social_mate.entity.post.Post;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    //     @Query("SELECT p FROM Post p WHERE p.deleted = false")
//     List<Post> findAllByUserOrderByCreatedAtDesc(User user);
    // ổng bài post
    int countByUserId(Long userId);

    @Query("""
    SELECT p
    FROM Post p
    WHERE p.user.id = :userId
        AND p.deleted = false
    ORDER BY p.createdAt DESC
    """)
    List<Post> findActivePostsByUserId(@Param("userId") Long userId);

    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);
    @Query("SELECT p FROM Post p WHERE p.user = :user AND p.deleted = false ORDER BY COALESCE(p.updatedAt, p.createdAt) DESC")
    List<Post> findAllByUserOrderByDateDesc(@Param("user") User user);

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.id = :id")
    Optional<Post> findByIdWithUser(@Param("id") Long id);

    List<Post> findByUserInAndDeletedFalseOrderByCreatedAtDesc(List<User> users);

    // 2. Lấy danh sách bài viết của 1 user (theo Entity User) và chưa bị xóa
    // Dùng cho hàm getMyPosts trong PostService
    List<Post> findAllByUserAndDeletedFalseOrderByCreatedAtDesc(User user);

    // 3. Lấy danh sách bài viết theo UserId và chưa bị xóa
    // Dùng cho ProfilePostService (để tránh hiện bài đã xóa)
    List<Post> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);

    int countByUserIdAndDeletedFalse(Long userId);
}