package social_mate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import social_mate.dto.request.PostRequestDto;
import social_mate.dto.response.PostResponseDto;
import social_mate.entity.User;
import social_mate.entity.UserPrincipal;
import social_mate.entity.post.Post;
import social_mate.entity.post.PostMedia;
import social_mate.mapper.PostMapper;
import social_mate.repository.PostMediaRepository;
import social_mate.repository.PostRepository;
import social_mate.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMediaRepository mediaRepository;
    private final PostMapper postMapper;



    public PostResponseDto createPost(PostRequestDto dto, UserPrincipal userPrincipal) {

        if (userPrincipal == null) {
            // Điều này không nên xảy ra nếu được gọi từ controller đã check
            // Hoặc bạn có thể ném một exception cụ thể
            throw new IllegalStateException("UserPrincipal không được null");
        }

        // Lấy User entity từ principal
        User userCurrent=userPrincipal.getUser();




        Post newPost=postMapper.toPost(dto);
        newPost.setUser(userCurrent);

        Post savedPost = postRepository.save(newPost);


        if (dto.getMediaUrls() != null) {
            for (String url : dto.getMediaUrls()) {
                PostMedia media = new PostMedia();
                media.setPost(savedPost);
                media.setUrl(url);
                media.setType("image");
                mediaRepository.save(media);
            }
        }
    return postMapper.toPostResponseDto(savedPost);
    }


}
