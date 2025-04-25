package com.hltj.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hltj.api.common.JwtTokenUtil;
import com.hltj.api.dto.ContentDTO;
import com.hltj.api.dto.PageResult;
import com.hltj.api.dto.VideoDTO;
import com.hltj.api.mapper.ArticleMapper;
import com.hltj.api.mapper.CollectMapper;
import com.hltj.api.mapper.FollowMapper;
import com.hltj.api.mapper.LikeMapper;
import com.hltj.api.mapper.MomentMapper;
import com.hltj.api.mapper.UserMapper;
import com.hltj.api.mapper.VideoMapper;
import com.hltj.api.model.Article;
import com.hltj.api.model.Collect;
import com.hltj.api.model.Like;
import com.hltj.api.model.Moment;
import com.hltj.api.model.User;
import com.hltj.api.model.Video;
import com.hltj.api.service.ContentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private MomentMapper momentMapper;

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private CollectMapper collectMapper;

    @Autowired
    private FollowMapper followMapper;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    public PageResult<ContentDTO> getFollowShareList(Integer page, Integer pageSize, String token) {
        String jwcode = jwtTokenUtil.extractUsername(token);

        // Get users that the current user follows
        List<String> followingJwcodes = followMapper.findFollowingJwcodes(jwcode);
        if (followingJwcodes.isEmpty()) {
            return PageResult.of(0L, new ArrayList<>());
        }

        // Create pages for different content types
        Page<Article> articlePage = new Page<>(page, pageSize);
        Page<Video> videoPage = new Page<>(page, pageSize);
        Page<Moment> momentPage = new Page<>(page, pageSize);

        // Fetch articles
        LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
        articleWrapper.in(Article::getJwcode, followingJwcodes)
                .orderByDesc(Article::getCreateTime)
                .select(Article::getId, Article::getJwcode, Article::getTitle,
                       Article::getContent, Article::getImage, Article::getCreateTime,
                       Article::getLikeCount, Article::getCommentCount, Article::getCollectCount);
        Page<Article> articles = articleMapper.selectPage(articlePage, articleWrapper);

        // Fetch videos
        LambdaQueryWrapper<Video> videoWrapper = new LambdaQueryWrapper<>();
        videoWrapper.in(Video::getJwcode, followingJwcodes)
                .orderByDesc(Video::getCreateTime);
        Page<Video> videos = videoMapper.selectPage(videoPage, videoWrapper);

        // Fetch moments
        LambdaQueryWrapper<Moment> momentWrapper = new LambdaQueryWrapper<>();
        momentWrapper.in(Moment::getJwcode, followingJwcodes)
                    .orderByDesc(Moment::getCreateTime)
                    .select(Moment::getId, Moment::getJwcode, Moment::getContent,
                           Moment::getImages, Moment::getCreateTime, Moment::getLikeCount,
                           Moment::getCommentCount, Moment::getCollectCount, Moment::getType);
        Page<Moment> moments = momentMapper.selectPage(momentPage, momentWrapper);

        // Combine content
        List<ContentDTO> contentList = new ArrayList<>();

        // Convert articles to ContentDTO
        for (Article article : articles.getRecords()) {
            ContentDTO dto = convertArticleToContentDTO(article, jwcode);
            contentList.add(dto);
        }

        // Convert videos to ContentDTO
        for (Video video : videos.getRecords()) {
            ContentDTO dto = convertVideoToContentDTO(video, jwcode);
            contentList.add(dto);
        }

        // Convert moments to ContentDTO
        for (Moment moment : moments.getRecords()) {
            ContentDTO dto = convertMomentToContentDTO(moment, jwcode);
            contentList.add(dto);
        }

        // Sort by create time (newest first)
        contentList.sort((a, b) -> b.getTime().compareTo(a.getTime()));

        // Paginate
        long total = articles.getTotal() + videos.getTotal() + moments.getTotal();
        return PageResult.of(total, contentList);
    }

    @Override
    public PageResult<VideoDTO> getRecommendVideos(Integer page, Integer pageSize, String token) {
        Page<Video> videoPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Video::getLikeCount);
        Page<Video> videos = videoMapper.selectPage(videoPage, wrapper);

        String jwcode = jwtTokenUtil.extractUsername(token);

        List<VideoDTO> videoDTOs = videos.getRecords().stream()
                .map(video -> convertToVideoDTO(video, jwcode))
                .collect(Collectors.toList());

        return PageResult.of(videos.getTotal(), videoDTOs);
    }

    @Override
    public PageResult<VideoDTO> getVideoList(Integer page, Integer pageSize, String token, Long id) {
        // Fetch the video with the given id
        Video currentVideo = videoMapper.selectById(id);
        if (currentVideo == null) {
            return PageResult.of(0L, new ArrayList<>());
        }

        // Fetch related videos
        Page<Video> videoPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Video::getId, id) // Not the current video
                .orderByDesc(Video::getCreateTime);
        Page<Video> videos = videoMapper.selectPage(videoPage, wrapper);

        String jwcode = jwtTokenUtil.extractUsername(token);

        List<VideoDTO> videoDTOs = new ArrayList<>();

        // Add current video as first item
        videoDTOs.add(convertToVideoDTO(currentVideo, jwcode));

        // Add related videos
        videoDTOs.addAll(videos.getRecords().stream()
                .map(video -> convertToVideoDTO(video, jwcode))
                .collect(Collectors.toList()));

        return PageResult.of(videos.getTotal() + 1, videoDTOs);
    }

    @Override
    @Transactional
    public Boolean likeContent(Long id, Integer status, String token) {
        String jwcode = jwtTokenUtil.extractUsername(token);

        // Determine content type
        Integer contentType = determineContentType(id);
        if (contentType == null) {
            return false;
        }

        if (status == 1) { // Like
            // Check if already liked
            Integer likeCount = likeMapper.checkUserLikeStatus(id, contentType, jwcode);
            if (likeCount == 0) {
                Like like = new Like();
                like.setContentId(id);
                like.setContentType(contentType);
                like.setJwcode(jwcode);
                like.setCreateTime(new Date());
                likeMapper.insert(like);

                // Increase like count in content
                increaseLikeCount(id, contentType);
            }
        } else { // Unlike
            LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Like::getContentId, id)
                    .eq(Like::getContentType, contentType)
                    .eq(Like::getJwcode, jwcode);
            likeMapper.delete(wrapper);

            // Decrease like count in content
            decreaseLikeCount(id, contentType);
        }

        return true;
    }

    @Override
    public PageResult<ContentDTO> getLikeList(Integer page, Integer pageSize, String token) {
        String jwcode = jwtTokenUtil.extractUsername(token);

        Page<Like> likePage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getJwcode, jwcode)
                .orderByDesc(Like::getCreateTime);
        Page<Like> likes = likeMapper.selectPage(likePage, wrapper);

        List<ContentDTO> contentList = new ArrayList<>();

        for (Like like : likes.getRecords()) {
            Integer contentType = like.getContentType();
            Long contentId = like.getContentId();

            ContentDTO dto = null;
            if (contentType == 1) { // Article
                // 使用不包含author字段的查询方式
                LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
                articleWrapper.eq(Article::getId, contentId)
                             .select(Article::getId, Article::getJwcode, Article::getTitle,
                                   Article::getContent, Article::getImage, Article::getCreateTime,
                                   Article::getLikeCount, Article::getCommentCount, Article::getCollectCount);
                Article article = articleMapper.selectOne(articleWrapper);
                if (article != null) {
                    dto = convertArticleToContentDTO(article, jwcode);
                }
            } else if (contentType == 2) { // Video
                Video video = videoMapper.selectById(contentId);
                if (video != null) {
                    dto = convertVideoToContentDTO(video, jwcode);
                }
            } else if (contentType == 3) { // Moment
                // 使用不包含author字段的查询方式
                LambdaQueryWrapper<Moment> momentWrapper = new LambdaQueryWrapper<>();
                momentWrapper.eq(Moment::getId, contentId);
                Moment moment = momentMapper.selectOne(momentWrapper);

                if (moment != null) {
                    dto = convertMomentToContentDTO(moment, jwcode);
                }
            }

            if (dto != null) {
                contentList.add(dto);
            }
        }

        return PageResult.of(likes.getTotal(), contentList);
    }

    @Override
    @Transactional
    public Boolean collectContent(Long id, Integer status, String token) {
        String jwcode = jwtTokenUtil.extractUsername(token);

        // Determine content type
        Integer contentType = determineContentType(id);
        if (contentType == null) {
            return false;
        }

        if (status == 1) { // Collect
            // Check if already collected
            Integer collectCount = collectMapper.checkUserCollectStatus(id, contentType, jwcode);
            if (collectCount == 0) {
                Collect collect = new Collect();
                collect.setContentId(id);
                collect.setContentType(contentType);
                collect.setJwcode(jwcode);
                collect.setCreateTime(new Date());
                collectMapper.insert(collect);

                // Increase collect count in content
                increaseCollectCount(id, contentType);
            }
        } else { // Uncollect
            LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Collect::getContentId, id)
                    .eq(Collect::getContentType, contentType)
                    .eq(Collect::getJwcode, jwcode);
            collectMapper.delete(wrapper);

            // Decrease collect count in content
            decreaseCollectCount(id, contentType);
        }

        return true;
    }

    @Override
    public PageResult<ContentDTO> getCollectList(Integer page, Integer pageSize, String token) {
        String jwcode = jwtTokenUtil.extractUsername(token);

        Page<Collect> collectPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getJwcode, jwcode)
                .orderByDesc(Collect::getCreateTime);
        Page<Collect> collects = collectMapper.selectPage(collectPage, wrapper);

        List<ContentDTO> contentList = new ArrayList<>();

        for (Collect collect : collects.getRecords()) {
            Integer contentType = collect.getContentType();
            Long contentId = collect.getContentId();

            ContentDTO dto = null;
            if (contentType == 1) { // Article
                // 使用不包含author字段的查询方式
                LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
                articleWrapper.eq(Article::getId, contentId)
                             .select(Article::getId, Article::getJwcode, Article::getTitle,
                                   Article::getContent, Article::getImage, Article::getCreateTime,
                                   Article::getLikeCount, Article::getCommentCount, Article::getCollectCount);
                Article article = articleMapper.selectOne(articleWrapper);
                if (article != null) {
                    dto = convertArticleToContentDTO(article, jwcode);
                }
            } else if (contentType == 2) { // Video
                Video video = videoMapper.selectById(contentId);
                if (video != null) {
                    dto = convertVideoToContentDTO(video, jwcode);
                }
            } else if (contentType == 3) { // Moment
                // 使用不包含author字段的查询方式
                LambdaQueryWrapper<Moment> momentWrapper = new LambdaQueryWrapper<>();
                momentWrapper.eq(Moment::getId, contentId);
                Moment moment = momentMapper.selectOne(momentWrapper);

                if (moment != null) {
                    dto = convertMomentToContentDTO(moment, jwcode);
                }
            }

            if (dto != null) {
                contentList.add(dto);
            }
        }

        return PageResult.of(collects.getTotal(), contentList);
    }

    @Override
    public PageResult<ContentDTO> getArticles(Integer page, Integer pageSize, String token) {
        Page<Article> articlePage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Article::getCreateTime)
              .select(Article::getId, Article::getJwcode, Article::getTitle,
                      Article::getContent, Article::getImage, Article::getCreateTime,
                      Article::getLikeCount, Article::getCommentCount, Article::getCollectCount);
        Page<Article> articles = articleMapper.selectPage(articlePage, wrapper);

        String jwcode = jwtTokenUtil.extractUsername(token);

        List<ContentDTO> contentList = articles.getRecords().stream()
                .map(article -> convertArticleToContentDTO(article, jwcode))
                .collect(Collectors.toList());

        return PageResult.of(articles.getTotal(), contentList);
    }

    @Override
    public ContentDTO getArticleDetail(String jwcode, Long id, String token) {
        // 使用不包含author字段的查询方式
        LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
        articleWrapper.eq(Article::getId, id)
                     .select(Article::getId, Article::getJwcode, Article::getTitle,
                           Article::getContent, Article::getImage, Article::getCreateTime,
                           Article::getLikeCount, Article::getCommentCount, Article::getCollectCount);
        Article article = articleMapper.selectOne(articleWrapper);
        if (article == null) {
            return null;
        }

        String currentJwcode = jwtTokenUtil.extractUsername(token);

        ContentDTO dto = convertArticleToContentDTO(article, currentJwcode);

        // Add follow status
        Integer followStatus = followMapper.checkFollowStatus(currentJwcode, jwcode);
        dto.getUser().setUserIdentity(followStatus > 0 ? 1 : 0);

        return dto;
    }

    @Override
    public ContentDTO getMomentDetail(Long id, String token) {
        // 使用不包含author字段的查询方式
        LambdaQueryWrapper<Moment> momentWrapper = new LambdaQueryWrapper<>();
        momentWrapper.eq(Moment::getId, id);
        Moment moment = momentMapper.selectOne(momentWrapper);

        if (moment == null) {
            return null;
        }

        String jwcode = jwtTokenUtil.extractUsername(token);

        return convertMomentToContentDTO(moment, jwcode);
    }

    @Override
    public PageResult<ContentDTO> getUserMomentList(String jwcode, Integer page, Integer pageSize, String token) {
        // Get user identity
        User user = userMapper.findByJwcode(jwcode);
        if (user == null) {
            return PageResult.of(0L, new ArrayList<>());
        }

        String currentJwcode = jwtTokenUtil.extractUsername(token);

        List<ContentDTO> contentList = new ArrayList<>();
        long total = 0;

        // If teacher, get articles and videos
        if (user.getUserIdentity() == 1) {
            // Get articles
            Page<Article> articlePage = new Page<>(page, pageSize);
            LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
            articleWrapper.eq(Article::getJwcode, jwcode)
                    .orderByDesc(Article::getCreateTime)
                    .select(Article::getId, Article::getJwcode, Article::getTitle,
                           Article::getContent, Article::getImage, Article::getCreateTime,
                           Article::getLikeCount, Article::getCommentCount, Article::getCollectCount);
            Page<Article> articles = articleMapper.selectPage(articlePage, articleWrapper);

            contentList.addAll(articles.getRecords().stream()
                    .map(article -> convertArticleToContentDTO(article, currentJwcode))
                    .collect(Collectors.toList()));

            total += articles.getTotal();

            // Get videos
            Page<Video> videoPage = new Page<>(page, pageSize);
            LambdaQueryWrapper<Video> videoWrapper = new LambdaQueryWrapper<>();
            videoWrapper.eq(Video::getJwcode, jwcode)
                    .orderByDesc(Video::getCreateTime);
            Page<Video> videos = videoMapper.selectPage(videoPage, videoWrapper);

            contentList.addAll(videos.getRecords().stream()
                    .map(video -> convertVideoToContentDTO(video, currentJwcode))
                    .collect(Collectors.toList()));

            total += videos.getTotal();
        }

        // Get moments for all users
        Page<Moment> momentPage = new Page<>(page, pageSize);

        // 使用LambdaQueryWrapper自定义查询字段，避免查询不存在的author字段
        LambdaQueryWrapper<Moment> momentWrapper = new LambdaQueryWrapper<>();
        momentWrapper.eq(Moment::getJwcode, jwcode)
                    .orderByDesc(Moment::getCreateTime)
                    .select(Moment::getId, Moment::getJwcode, Moment::getContent,
                           Moment::getImages, Moment::getCreateTime, Moment::getLikeCount,
                           Moment::getCommentCount, Moment::getCollectCount, Moment::getType);

        Page<Moment> moments = momentMapper.selectPage(momentPage, momentWrapper);

        contentList.addAll(moments.getRecords().stream()
                .map(moment -> convertMomentToContentDTO(moment, currentJwcode))
                .collect(Collectors.toList()));

        total += moments.getTotal();

        // Sort by create time (newest first)
        contentList.sort((a, b) -> b.getTime().compareTo(a.getTime()));

        return PageResult.of(total, contentList);
    }

    @Override
    @Transactional
    public Long publishMoment(String content, String images, String token) {
        String jwcode = jwtTokenUtil.extractUsername(token);

        Moment moment = new Moment();
        moment.setJwcode(jwcode);
        moment.setContent(content);
        moment.setImages(images);
        moment.setCreateTime(new Date());
        moment.setLikeCount(0);
        moment.setCommentCount(0);
        moment.setCollectCount(0);
        moment.setType(3); // Moment type

        momentMapper.insert(moment);

        return moment.getId();
    }

    @Override
    @Transactional
    public Long publishArticle(String title, String content,String url, String token) {
        String jwcode = jwtTokenUtil.extractUsername(token);

        // Check if user is a teacher
        User user = userMapper.findByJwcode(jwcode);
        if (user == null ) {
            return null; // Only teachers can publish articles
        }

        Article article = new Article();
        article.setJwcode(jwcode);
        article.setTitle(title);
        article.setContent(content);
        article.setCreateTime(new Date());
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setCollectCount(0);
        article.setImage(url);

        articleMapper.insert(article);

        return article.getId();
    }

    @Override
    @Transactional
    public Long publishVideo(String title, String content, String video, String url, String token) {
        String jwcode = jwtTokenUtil.extractUsername(token);

        // Check if user is a teacher
        User user = userMapper.findByJwcode(jwcode);
        if (user == null ) {
            return null; // Only teachers can publish videos
        }

        Video videoObj = new Video();
        videoObj.setJwcode(jwcode);
        videoObj.setTitle(title);
        videoObj.setContent(content);
        videoObj.setVideo(video);
        videoObj.setCover(url);
        videoObj.setCreateTime(new Date());
        videoObj.setLikeCount(0);
        videoObj.setCommentCount(0);
        videoObj.setCollectCount(0);

        videoMapper.insert(videoObj);

        return videoObj.getId();
    }

    @Override
    @Transactional
    public Boolean deleteArticle(Long id, String token) {
        String jwcode = jwtTokenUtil.extractUsername(token);

        // 查询文章，使用不包含author字段的查询方式
        LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
        articleWrapper.eq(Article::getId, id)
                     .select(Article::getId, Article::getJwcode);
        Article article = articleMapper.selectOne(articleWrapper);
        if (article == null) {
            return false;
        }

        // 验证权限（只有文章作者才能删除）
        if (!article.getJwcode().equals(jwcode)) {
            return false;
        }

        // 删除相关的点赞记录
        LambdaQueryWrapper<Like> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(Like::getContentId, id)
                .eq(Like::getContentType, 1); // 1表示文章
        likeMapper.delete(likeWrapper);

        // 删除相关的收藏记录
        LambdaQueryWrapper<Collect> collectWrapper = new LambdaQueryWrapper<>();
        collectWrapper.eq(Collect::getContentId, id)
                .eq(Collect::getContentType, 1); // 1表示文章
        collectMapper.delete(collectWrapper);

        // 删除文章
        articleMapper.deleteById(id);

        return true;
    }

    @Override
    @Transactional
    public Boolean deleteVideo(Long id, String token) {
        String jwcode = jwtTokenUtil.extractUsername(token);

        // 查询视频
        Video video = videoMapper.selectById(id);
        if (video == null) {
            return false;
        }

        // 验证权限（只有视频作者才能删除）
        if (!video.getJwcode().equals(jwcode)) {
            return false;
        }

        // 删除相关的点赞记录
        LambdaQueryWrapper<Like> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(Like::getContentId, id)
                .eq(Like::getContentType, 2); // 2表示视频
        likeMapper.delete(likeWrapper);

        // 删除相关的收藏记录
        LambdaQueryWrapper<Collect> collectWrapper = new LambdaQueryWrapper<>();
        collectWrapper.eq(Collect::getContentId, id)
                .eq(Collect::getContentType, 2); // 2表示视频
        collectMapper.delete(collectWrapper);

        // 删除视频
        videoMapper.deleteById(id);

        return true;
    }

    // Helper methods

    private ContentDTO convertArticleToContentDTO(Article article, String currentJwcode) {
        ContentDTO dto = new ContentDTO();
        dto.setId(article.getId());
        dto.setType(1); // Article type
        dto.setTime(dateFormat.format(article.getCreateTime()));
        dto.setTitle(article.getTitle());
        dto.setContent(article.getContent());

        ContentDTO.UserContentDTO userContent = new ContentDTO.UserContentDTO();

        // Get user details
        User user = userMapper.findByJwcode(article.getJwcode());
        if (user != null) {
            userContent.setName(user.getName());
            userContent.setAvatar(user.getAvatar());
            userContent.setJwcode(user.getJwcode());
            userContent.setUserIdentity(user.getUserIdentity());
        }

        userContent.setLikeCount(article.getLikeCount());
        userContent.setCommentCount(article.getCommentCount());
        userContent.setFavoriteCount(article.getCollectCount());
        userContent.setImage(article.getImage());

        // Check if current user has liked/favorited this article
        userContent.setLiked(likeMapper.checkUserLikeStatus(article.getId(), 1, currentJwcode));
        userContent.setFavorited(collectMapper.checkUserCollectStatus(article.getId(), 1, currentJwcode));

        dto.setUser(userContent);

        return dto;
    }

    private ContentDTO convertVideoToContentDTO(Video video, String currentJwcode) {
        ContentDTO dto = new ContentDTO();
        dto.setId(video.getId());
        dto.setType(2); // Video type
        dto.setTime(dateFormat.format(video.getCreateTime()));
        dto.setTitle(video.getTitle());
        dto.setContent(video.getContent());

        ContentDTO.UserContentDTO userContent = new ContentDTO.UserContentDTO();

        // Get user details
        User user = userMapper.findByJwcode(video.getJwcode());
        if (user != null) {
            userContent.setName(user.getName());
            userContent.setAvatar(user.getAvatar());
            userContent.setJwcode(user.getJwcode());
            userContent.setUserIdentity(user.getUserIdentity());
        }

        userContent.setVideo(video.getVideo());
        userContent.setLikeCount(video.getLikeCount());
        userContent.setCommentCount(video.getCommentCount());
        userContent.setFavoriteCount(video.getCollectCount());
        userContent.setImage(video.getCover());

        // Check if current user has liked/favorited this video
        userContent.setLiked(likeMapper.checkUserLikeStatus(video.getId(), 2, currentJwcode));
        userContent.setFavorited(collectMapper.checkUserCollectStatus(video.getId(), 2, currentJwcode));

        dto.setUser(userContent);

        return dto;
    }

    private ContentDTO convertMomentToContentDTO(Moment moment, String currentJwcode) {
        ContentDTO dto = new ContentDTO();
        dto.setId(moment.getId());
        dto.setType(3); // Moment type
        dto.setTime(dateFormat.format(moment.getCreateTime()));
        dto.setContent(moment.getContent());

        ContentDTO.UserContentDTO userContent = new ContentDTO.UserContentDTO();

        // Get user details
        User user = userMapper.findByJwcode(moment.getJwcode());
        if (user != null) {
            userContent.setName(user.getName());
            userContent.setAvatar(user.getAvatar());
            userContent.setJwcode(user.getJwcode());
            userContent.setUserIdentity(user.getUserIdentity());
        }

        userContent.setLikeCount(moment.getLikeCount());
        userContent.setCommentCount(moment.getCommentCount());
        userContent.setFavoriteCount(moment.getCollectCount());

        // Parse images
        if (moment.getImages() != null && !moment.getImages().isEmpty()) {
            userContent.setImages(moment.getImages().split(","));
        }

        // Check if current user has liked/favorited this moment
        userContent.setLiked(likeMapper.checkUserLikeStatus(moment.getId(), 3, currentJwcode));
        userContent.setFavorited(collectMapper.checkUserCollectStatus(moment.getId(), 3, currentJwcode));

        dto.setUser(userContent);

        return dto;
    }

    private VideoDTO convertToVideoDTO(Video video, String currentJwcode) {
        VideoDTO dto = new VideoDTO();
        dto.setId(video.getId());
        dto.setCover(video.getCover());
        dto.setTitle(video.getTitle());
        dto.setContent(video.getContent());
        dto.setVideo(video.getVideo());
        dto.setCreatetime(dateFormat.format(video.getCreateTime()));
        dto.setLikeCount(video.getLikeCount());
        dto.setCommentCount(video.getCommentCount());
        dto.setCollectCount(video.getCollectCount());
        dto.setJwcode(video.getJwcode());

        // Check if user has liked/collected
        dto.setLiked(likeMapper.checkUserLikeStatus(video.getId(), 2, currentJwcode));
        dto.setCollect(collectMapper.checkUserCollectStatus(video.getId(), 2, currentJwcode));

        // Get user details
        User user = userMapper.findByJwcode(video.getJwcode());
        if (user != null) {
            dto.setName(user.getName());
            dto.setAvatar(user.getAvatar());
        }

        return dto;
    }

    private Integer determineContentType(Long id) {
        // Try to find in articles，使用不包含author字段的查询方式
        LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
        articleWrapper.eq(Article::getId, id)
                     .select(Article::getId);
        Article article = articleMapper.selectOne(articleWrapper);
        if (article != null) {
            return 1;
        }

        // Try to find in videos
        Video video = videoMapper.selectById(id);
        if (video != null) {
            return 2;
        }

        // Try to find in moments，使用不包含author字段的查询方式
        LambdaQueryWrapper<Moment> momentWrapper = new LambdaQueryWrapper<>();
        momentWrapper.eq(Moment::getId, id);
        Moment moment = momentMapper.selectOne(momentWrapper);

        if (moment != null) {
            return 3;
        }

        return null;
    }

    private void increaseLikeCount(Long id, Integer contentType) {
        if (contentType == 1) {
            articleMapper.increaseLikeCount(id);
        } else if (contentType == 2) {
            videoMapper.increaseLikeCount(id);
        } else if (contentType == 3) {
            momentMapper.increaseLikeCount(id);
        }
    }

    private void decreaseLikeCount(Long id, Integer contentType) {
        if (contentType == 1) {
            articleMapper.decreaseLikeCount(id);
        } else if (contentType == 2) {
            videoMapper.decreaseLikeCount(id);
        } else if (contentType == 3) {
            momentMapper.decreaseLikeCount(id);
        }
    }

    private void increaseCollectCount(Long id, Integer contentType) {
        if (contentType == 1) {
            articleMapper.increaseCollectCount(id);
        } else if (contentType == 2) {
            videoMapper.increaseCollectCount(id);
        } else if (contentType == 3) {
            momentMapper.increaseCollectCount(id);
        }
    }

    private void decreaseCollectCount(Long id, Integer contentType) {
        if (contentType == 1) {
            articleMapper.decreaseCollectCount(id);
        } else if (contentType == 2) {
            videoMapper.decreaseCollectCount(id);
        } else if (contentType == 3) {
            momentMapper.decreaseCollectCount(id);
        }
    }
}