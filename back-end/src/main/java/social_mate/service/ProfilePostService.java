package social_mate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import social_mate.dto.response.ProfilePostResponseDto;
import social_mate.entity.post.Post;
import social_mate.mapper.PostMapper;
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

    public List<ProfilePostResponseDto> getProfilePosts(Long userId) {

        List<Post> posts =
                postRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return posts.stream()
                .map(post -> {
                    // 1. Map cơ bản
                    ProfilePostResponseDto dto =
                            profilePostMapper.toDto(post);
                    // 2. Bổ sung media
                    dto.setMediaUrls(
                            postMediaRepository.findMediaUrlsByPostId(post.getId())
                    );
                    // 3. Bổ sung total like
                    dto.setTotalLikes(
                            postLikeRepository.countByPostId(post.getId())
                    );
                    return dto;
                })
                .toList();
    }
}
