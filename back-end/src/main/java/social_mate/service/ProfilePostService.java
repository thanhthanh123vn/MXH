package social_mate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import social_mate.dto.response.ProfilePostResponseDto;
import social_mate.entity.post.Post;
import social_mate.mapper.PostMapper;
import social_mate.mapper.UserMapper;
import social_mate.repository.PostLikeRepository;
import social_mate.repository.PostMediaRepository;
import social_mate.repository.PostRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfilePostService {
    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostMapper profilePostMapper;
    private final UserMapper userMapper;
    public List<ProfilePostResponseDto> getProfilePosts(Long userId) {

        List<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(userId);


        return posts.stream()
                .map(this::mapToProfileDto)
                .toList();
    }
    private ProfilePostResponseDto mapToProfileDto(Post post) {

        ProfilePostResponseDto dto = profilePostMapper.toDto(post);


        dto.setMediaUrls(postMediaRepository.findMediaUrlsByPostId(post.getId()));

        dto.setTotalLikes((int) postLikeRepository.countByPostId(post.getId()));


         dto.setUser(userMapper.toUserResponseDto(post.getUser()));


        if (post.getOriginalPostId() != null) {

            Post originalPost = postRepository.findById(post.getOriginalPostId())
                    .orElse(null);

            if (originalPost != null) {

                ProfilePostResponseDto originalDto = mapToProfileDto(originalPost);

                // Gán vào DTO cha
                dto.setOriginalPost(originalDto);
            }
        }

        return dto;
    }
}
