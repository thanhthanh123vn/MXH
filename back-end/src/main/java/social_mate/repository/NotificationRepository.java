package social_mate.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import social_mate.dto.response.NotificationResponseDto;
import social_mate.entity.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {


    List<Notification> findTop10ByOwnerIdOrderByIdDesc(long ownerId);


    long countByOwnerIdAndIsReadFalse(long ownerId);


    List<Notification> findByOwnerIdAndIsReadFalse(long ownerId);

}