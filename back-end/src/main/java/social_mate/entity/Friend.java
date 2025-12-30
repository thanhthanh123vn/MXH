package social_mate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import social_mate.entity.enums.FriendshipStatus;

@Entity()
@Table(name = "friendships")
@Getter
@Setter
public class Friend extends AbstractEntity<Friend> {

    @Column(name = "sender_id")
    private long senderId;

    @Column(name = "receiver_id")
    private long receiverId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;


}
