package social_mate.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import social_mate.entity.Friend;
import social_mate.entity.User;
import social_mate.entity.enums.FriendshipStatus;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    @Query("""
        SELECT u FROM Friend f
        JOIN User u
            ON (u.id = f.senderId AND f.receiverId = :userId)
            OR (u.id = f.receiverId AND f.senderId = :userId)
        WHERE f.status = 'ACCEPTED'
    """)
    List<User> getFriends(@Param("userId") Long userId);

    @Query("""
        SELECT u
        FROM Friend f
        JOIN User u ON u.id = f.senderId
        WHERE f.receiverId = :userId
          AND f.status = 'PENDING'
    """)
    List<User> findFriendRequests(@Param("userId") Long userId);

    @Query("""
        SELECT f FROM Friend f
        WHERE (f.senderId = :user1 AND f.receiverId = :user2)
           OR (f.senderId = :user2 AND f.receiverId = :user1)
    """)
    Optional<Friend> findFriendBetweenUsers(
            @Param("user1") Long user1,
            @Param("user2") Long user2
    );

    // chấp nhận hoặc  từ chối lời mời
    Optional<Friend> findBySenderIdAndReceiverIdAndStatus(
            long senderId,
            long receiverId,
            FriendshipStatus status
    );
// tổng bạn bè
    @Query("""
        SELECT COUNT(f) FROM Friend f
        WHERE f.status = 'ACCEPTED'
        AND (f.senderId = :userId OR f.receiverId = :userId)
    """)
    int countFriends(Long userId);

}