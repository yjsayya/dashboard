package com.example.dashboard.service;

import com.example.dashboard.domain.Article;
import com.example.dashboard.domain.ArticleComment;
import com.example.dashboard.domain.UserAccount;
import com.example.dashboard.dto.ArticleCommentDto;
import com.example.dashboard.repository.ArticleCommentRepository;
import com.example.dashboard.repository.ArticleRepository;
import com.example.dashboard.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ArticleCommentService {

    private final ArticleRepository articleRepository;
    private final ArticleCommentRepository articleCommentRepository;
    private final UserAccountRepository userAccountRepository;

    @Transactional(readOnly = true)
    public List<ArticleCommentDto> searchArticleComments(Long articleId) {
        return articleCommentRepository.findByArticle_Id(articleId)
                .stream()
                .map(ArticleCommentDto::from)
                .toList();
    }

    public void saveArticleComment(ArticleCommentDto dto) {
        try {
            // 1. article 찾기
            Article article = articleRepository.getReferenceById(dto.articleId());
            // 2. 유저 정보 찾기
            UserAccount userAccount = userAccountRepository.getReferenceById(dto.userAccountDto().userId());
            // 3. 찾은 article과 userAccount를 넣어서 받아온 dto로 entity 만들기
            ArticleComment articleComment = dto.toEntity(article, userAccount);

            // 4. 저장 진행시켜
            if (dto.parentCommentId() != null) {
                ArticleComment parentComment = articleCommentRepository.getReferenceById(dto.parentCommentId());
                parentComment.addChildComment(articleComment);
            } else {
                articleCommentRepository.save(articleComment);
            }
        } catch (EntityNotFoundException e) {
            log.warn("댓글 저장 실패. 댓글 작성에 필요한 정보를 찾을 수 없습니다 - {}", e.getLocalizedMessage());
        }
    }

    public void deleteArticleComment(Long articleCommentId, String userId) {
        articleCommentRepository.deleteByIdAndUserAccount_UserId(articleCommentId, userId);
    }}