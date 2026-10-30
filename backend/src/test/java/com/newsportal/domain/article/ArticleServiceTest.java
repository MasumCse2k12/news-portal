package com.newsportal.domain.article;

import com.newsportal.domain.article.dto.ArticleCreateRequest;
import com.newsportal.domain.category.Category;
import com.newsportal.domain.category.CategoryRepository;
import com.newsportal.domain.tag.TagRepository;
import com.newsportal.domain.user.Role;
import com.newsportal.domain.user.User;
import com.newsportal.domain.user.UserService;
import com.newsportal.messaging.publisher.ArticleEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock private ArticleRepository articleRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private TagRepository tagRepository;
    @Mock private UserService userService;
    @Mock private ArticleEventPublisher eventPublisher;

    @InjectMocks
    private ArticleService articleService;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("journalist1")
                .email("j1@test.com")
                .password("hashed")
                .role(Role.JOURNALIST)
                .build();

        testCategory = Category.builder()
                .id(1)
                .name("Bangladesh")
                .slug("bangladesh")
                .build();

        // Mock security context
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(testUser);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void create_shouldSaveDraftArticle() {
        ArticleCreateRequest request = mock(ArticleCreateRequest.class);
        when(request.getTitle()).thenReturn("Test Article");
        when(request.getContent()).thenReturn("Some content here");
        when(request.getCategoryId()).thenReturn(1);
        when(request.getTagSlugs()).thenReturn(null);

        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(articleRepository.findBySlug(any())).thenReturn(Optional.empty());

        Article savedArticle = Article.builder()
                .id(UUID.randomUUID())
                .title("Test Article")
                .slug("test-article")
                .content("Some content here")
                .status(ArticleStatus.DRAFT)
                .author(testUser)
                .category(testCategory)
                .build();
        when(articleRepository.save(any())).thenReturn(savedArticle);
        when(userService.toResponse(any())).thenReturn(null);

        var response = articleService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ArticleStatus.DRAFT);
        verify(articleRepository).save(any());
    }

    @Test
    void publish_shouldSetStatusAndPublishEvent() {
        UUID id = UUID.randomUUID();
        Article article = Article.builder()
                .id(id)
                .title("Test")
                .slug("test")
                .content("Content")
                .status(ArticleStatus.REVIEW)
                .author(testUser)
                .category(testCategory)
                .build();

        when(articleRepository.findById(id)).thenReturn(Optional.of(article));
        when(articleRepository.save(any())).thenReturn(article);
        when(userService.toResponse(any())).thenReturn(null);

        articleService.publish(id);

        assertThat(article.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(article.getPublishedAt()).isNotNull();
        verify(eventPublisher).publishArticlePublishedEvent(article);
    }
}
