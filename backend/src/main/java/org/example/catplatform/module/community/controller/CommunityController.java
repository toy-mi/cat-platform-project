package org.example.catplatform.module.community.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.catplatform.common.result.Result;
import org.example.catplatform.module.community.dto.CommentDTO;
import org.example.catplatform.module.community.dto.PostDTO;
import org.example.catplatform.module.user.dto.Comment;
import org.example.catplatform.module.community.entity.Post;
import org.example.catplatform.module.community.entity.PostLike;
import org.example.catplatform.module.community.service.CommentService;
import org.example.catplatform.module.community.service.PostLikeService;
import org.example.catplatform.module.community.service.PostService;
import org.example.catplatform.module.user.entity.User;
import org.example.catplatform.module.user.service.UserService;
import org.example.catplatform.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.example.catplatform.module.notification.entity.Notification;
import org.example.catplatform.module.notification.service.NotificationService;


import java.util.stream.Collectors;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.io.File;
import org.springframework.beans.BeanUtils;


@RestController
@RequestMapping("/api/community")
public class CommunityController {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostLikeService likeService;

    @Autowired
    private UserService userService;

    @Value("${file.upload.community}")
    private String uploadPath;

    @Value("${file.access.prefix.community:/uploads/community/}")
    private String accessPrefix;

    @Autowired
    private NotificationService notificationService;  // 注入

    // 评论 VO 类
    @lombok.Data
    static class CommentVO {
        private Long id;
        private Long postId;
        private Long userId;
        private String content;
        private LocalDateTime createTime;
        private String postTitle;
        private String userName;
        private String userAvatar;
    }

    // ========== 发送评论通知 ==========
    private void sendCommentNotification(Long postUserId, Long postId, Long commentUserId, String commentContent) {
        User commentUser = userService.getById(commentUserId);
        String userName = commentUser != null ? commentUser.getNickname() : "某用户";
        Notification notification = new Notification();
        notification.setUserId(postUserId);
        notification.setType("POST_COMMENT");
        notification.setTitle("您的动态收到新评论");
        notification.setContent(userName + " 评论了您的动态：" + commentContent);
        notification.setRelatedId(postId);
        notification.setIsRead(0);
        notificationService.save(notification);
    }

    // ========== 发送点赞通知 ==========
    private void sendLikeNotification(Long postUserId, Long postId, Long likeUserId) {
        User likeUser = userService.getById(likeUserId);
        String userName = likeUser != null ? likeUser.getNickname() : "某用户";
        Notification notification = new Notification();
        notification.setUserId(postUserId);
        notification.setType("POST_LIKE");
        notification.setTitle("您的动态收到点赞");
        notification.setContent(userName + " 点赞了您的动态");
        notification.setRelatedId(postId);
        notification.setIsRead(0);
        notificationService.save(notification);
    }

    /**
     * 发布动态（需要登录）
     */
    @PostMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    public Result<Post> createPost(@RequestBody PostDTO dto) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Post post = new Post();
        post.setUserId(userDetails.getUserId());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setImages(dto.getImages());
        post.setLocationDesc(dto.getLocationDesc());
        post.setLatitude(dto.getLatitude());
        post.setLongitude(dto.getLongitude());
        post.setLikeCount(0);
        post.setCommentCount(0);
        postService.save(post);
        return Result.success("发布成功", post);
    }

    /**
     * 分页查询动态列表（按时间倒序）
     */
    @GetMapping("/posts")
    public Result<Page<Post>> pagePosts(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        @RequestParam(required = false) String keyword) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Post::getCreateTime);
        postService.page(page, wrapper);

        // 如果有关键词，对标题和内容进行模糊匹配
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Post::getTitle, keyword)
                    .or()
                    .like(Post::getContent, keyword));
        }

        wrapper.orderByDesc(Post::getCreateTime);
        postService.page(page, wrapper);

        // 填充发布者信息，并判断当前用户是否点赞
        if (!page.getRecords().isEmpty()) {
            List<Long> userIds = page.getRecords().stream().map(Post::getUserId).collect(Collectors.toList());
            Map<Long, User> userMap = userService.listByIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u));

            // 获取当前用户（可能未登录）
            Long currentUserId = null;
            try {
                CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal();
                currentUserId = userDetails.getUserId();
            } catch (Exception ignored) {}

            // 查询所有动态的点赞状态（如果用户已登录）
            Map<Long, Boolean> likedMap = null;
            if (currentUserId != null) {
                List<Long> postIds = page.getRecords().stream().map(Post::getId).collect(Collectors.toList());
                LambdaQueryWrapper<PostLike> likeWrapper = new LambdaQueryWrapper<>();
                likeWrapper.in(PostLike::getPostId, postIds)
                        .eq(PostLike::getUserId, currentUserId);
                List<PostLike> userLikes = likeService.list(likeWrapper);
                likedMap = userLikes.stream().collect(Collectors.toMap(PostLike::getPostId, like -> true));
            }

            for (Post post : page.getRecords()) {
                User user = userMap.get(post.getUserId());
                if (user != null) {
                    // 优先使用昵称，没有则用用户名
                    String displayName = user.getNickname() != null && !user.getNickname().isEmpty()
                            ? user.getNickname() : user.getUsername();
                    post.setUserName(displayName);  // ✅ 修正：使用 displayName
                    post.setUserAvatar(user.getAvatar());
                }
                if (likedMap != null) {
                    post.setLikedByCurrent(likedMap.containsKey(post.getId()));
                } else {
                    post.setLikedByCurrent(false);
                }
            }
        }
        return Result.success(page);
    }

    /**
     * 获取单条动态详情（包含评论列表）
     */
    @GetMapping("/posts/{id}")
    public Result<Map<String, Object>> getPostDetail(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null) {
            return Result.error("动态不存在");
        }
        // 填充发布者
        User user = userService.getById(post.getUserId());
        if (user != null) {
            String displayName = user.getNickname() != null && !user.getNickname().isEmpty()
                    ? user.getNickname() : user.getUsername();
            post.setUserName(displayName);  // ✅ 修正：使用 displayName
            post.setUserAvatar(user.getAvatar());
        }
        // 获取评论列表
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getPostId, id).orderByAsc(Comment::getCreateTime);
        List<Comment> comments = commentService.list(commentWrapper);
        if (!comments.isEmpty()) {
            List<Long> userIds = comments.stream().map(Comment::getUserId).collect(Collectors.toList());
            Map<Long, User> userMap = userService.listByIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
            comments.forEach(c -> {
                User u = userMap.get(c.getUserId());
                if (u != null) {
                    String displayName = u.getNickname() != null && !u.getNickname().isEmpty()
                            ? u.getNickname() : u.getUsername();
                    c.setUserName(displayName);
                    c.setUserAvatar(u.getAvatar());
                }
            });
        }

        // 当前用户是否点赞
        Long currentUserId = null;
        try {
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            currentUserId = userDetails.getUserId();
        } catch (Exception ignored) {}
        if (currentUserId != null) {
            LambdaQueryWrapper<PostLike> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(PostLike::getPostId, id).eq(PostLike::getUserId, currentUserId);
            post.setLikedByCurrent(likeService.count(likeWrapper) > 0);
        } else {
            post.setLikedByCurrent(false);
        }

        Map<String, Object> result = Map.of(
                "post", post,
                "comments", comments
        );
        return Result.success(result);
    }


    /**
     * 点赞/取消点赞
     */
    @PostMapping("/posts/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> likePost(@PathVariable Long id) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();

        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getPostId, id).eq(PostLike::getUserId, userId);
        PostLike existing = likeService.getOne(wrapper);

        Post post = postService.getById(id);
        if (post == null) {
            return Result.error("动态不存在");
        }

        if (existing == null) {
            // 点赞
            PostLike like = new PostLike();
            like.setPostId(id);
            like.setUserId(userId);
            likeService.save(like);
            post.setLikeCount(post.getLikeCount() + 1);
            postService.updateById(post);

            // 发送点赞通知（如果不是自己给自己点赞）
            if (!post.getUserId().equals(userId)) {
                sendLikeNotification(post.getUserId(), id, userId);
            }
            return Result.success("点赞成功", null);
        } else {
            // 取消点赞
            likeService.removeById(existing.getId());
            post.setLikeCount(post.getLikeCount() - 1);
            postService.updateById(post);
            return Result.success("已取消点赞", null);
        }
    }

    /**
     * 发表评论
     */
    @PostMapping("/comments")
    @PreAuthorize("isAuthenticated()")
    public Result<Comment> addComment(@RequestBody CommentDTO dto) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();

        Post post = postService.getById(dto.getPostId());
        if (post == null) {
            return Result.error("动态不存在");
        }

        Comment comment = new Comment();
        comment.setPostId(dto.getPostId());
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        comment.setCreateTime(LocalDateTime.now());
        commentService.save(comment);

        // 增加评论数
        post.setCommentCount(post.getCommentCount() + 1);
        postService.updateById(post);

        // 发送通知给动态作者（如果评论者不是作者本人）
        if (!post.getUserId().equals(userId)) {
            sendCommentNotification(post.getUserId(), post.getId(), userId, dto.getContent());
        }

        return Result.success("评论成功", comment);
    }

    /**
     * 删除评论（仅作者或管理员）
     */
    @DeleteMapping("/comments/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> deleteComment(@PathVariable Long id) {
        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String role = userDetails.getUser().getRole();
        if (!role.equals("ADMIN") && !comment.getUserId().equals(userDetails.getUserId())) {
            return Result.error("无权删除");
        }

        // 减少评论数
        Post post = postService.getById(comment.getPostId());
        if (post != null) {
            post.setCommentCount(post.getCommentCount() - 1);
            postService.updateById(post);
        }

        commentService.removeById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 删除动态（仅作者或管理员）
     */
    @DeleteMapping("/posts/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> deletePost(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null) {
            return Result.error("动态不存在");
        }

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String role = userDetails.getUser().getRole();
        if (!role.equals("ADMIN") && !post.getUserId().equals(userDetails.getUserId())) {
            return Result.error("无权删除");
        }

        // 级联删除评论和点赞（由数据库外键处理，或手动删除）
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getPostId, id);
        commentService.remove(commentWrapper);

        LambdaQueryWrapper<PostLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(PostLike::getPostId, id);
        likeService.remove(likeWrapper);

        postService.removeById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 上传动态图片（支持多图，但一次只传一张，前端可多次调用）
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = "community_" + System.currentTimeMillis() + suffix;

            // 确保目录存在
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File dest = new File(dir, fileName);
            file.transferTo(dest);

            // 构建访问URL
            String fileUrl = accessPrefix + fileName;
            return Result.success("上传成功", fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败");
        }
    }

    @GetMapping("/user/posts")
    @PreAuthorize("isAuthenticated()")
    public Result<Page<Post>> getUserPosts(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userDetails.getUserId())
                .orderByDesc(Post::getCreateTime);
        postService.page(page, wrapper);
        return Result.success(page);
    }

    @GetMapping("/user/comments")
    @PreAuthorize("isAuthenticated()")
    public Result<Page<Comment>> getUserComments(@RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getUserId, userDetails.getUserId())
                .orderByDesc(Comment::getCreateTime);
        commentService.page(page, wrapper);
        // 填充评论所属动态的标题
        if (!page.getRecords().isEmpty()) {
            List<Long> postIds = page.getRecords().stream().map(Comment::getPostId).collect(Collectors.toList());
            Map<Long, String> postTitleMap = postService.listByIds(postIds).stream()
                    .collect(Collectors.toMap(Post::getId, Post::getTitle));
            page.getRecords().forEach(c -> c.setPostTitle(postTitleMap.get(c.getPostId())));
        }
        return Result.success(page);
    }

    @GetMapping("/user/likes")
    @PreAuthorize("isAuthenticated()")
    public Result<Page<Post>> getUserLikes(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Page<PostLike> likePage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getUserId, userDetails.getUserId())
                .orderByDesc(PostLike::getCreateTime);
        likeService.page(likePage, wrapper);
        // 获取点赞对应的动态
        if (!likePage.getRecords().isEmpty()) {
            List<Long> postIds = likePage.getRecords().stream().map(PostLike::getPostId).collect(Collectors.toList());
            List<Post> posts = postService.listByIds(postIds);
            // 重新构造分页对象返回动态
            Page<Post> resultPage = new Page<>(likePage.getCurrent(), likePage.getSize(), likePage.getTotal());
            resultPage.setRecords(posts);
            return Result.success(resultPage);
        }
        return Result.success(new Page<>());
    }

    // ========== 管理员管理动态 ==========

    /**
     * 管理员分页查询所有动态（包括用户信息）
     */
    @GetMapping("/admin/posts")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<Post>> adminPagePosts(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) Long userId) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Post::getTitle, keyword).or().like(Post::getContent, keyword));
        }
        if (userId != null) {
            wrapper.eq(Post::getUserId, userId);
        }
        wrapper.orderByDesc(Post::getCreateTime);
        postService.page(page, wrapper);

        // 填充发布者信息
        if (!page.getRecords().isEmpty()) {
            List<Long> userIds = page.getRecords().stream().map(Post::getUserId).collect(Collectors.toList());
            Map<Long, User> userMap = userService.listByIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
            page.getRecords().forEach(post -> {
                User user = userMap.get(post.getUserId());
                if (user != null) {
                    post.setUserName(user.getUsername());
                }
            });
        }
        return Result.success(page);
    }

    /**
     * 管理员删除动态
     */
    @DeleteMapping("/admin/posts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> adminDeletePost(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null) {
            return Result.error("动态不存在");
        }
        // 级联删除评论和点赞（由数据库外键或手动）
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getPostId, id);
        commentService.remove(commentWrapper);

        LambdaQueryWrapper<PostLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(PostLike::getPostId, id);
        likeService.remove(likeWrapper);

        postService.removeById(id);
        return Result.success("删除成功", null);
    }

    // ========== 管理员管理评论 ==========

    /**
     * 管理员分页查询所有评论（包括动态标题、评论人）
     */
    @GetMapping("/admin/comments")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<Comment>> adminPageComments(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                                   @RequestParam(required = false) Long postId,
                                                   @RequestParam(required = false) Long userId) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (postId != null) {
            wrapper.eq(Comment::getPostId, postId);
        }
        if (userId != null) {
            wrapper.eq(Comment::getUserId, userId);
        }
        wrapper.orderByDesc(Comment::getCreateTime);
        commentService.page(page, wrapper);

        // 填充评论人姓名和关联的动态标题
        if (!page.getRecords().isEmpty()) {
            List<Long> userIds = page.getRecords().stream().map(Comment::getUserId).collect(Collectors.toList());
            Map<Long, String> userNameMap = userService.listByIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, User::getUsername));

            List<Long> postIds = page.getRecords().stream().map(Comment::getPostId).collect(Collectors.toList());
            Map<Long, String> postTitleMap = postService.listByIds(postIds).stream()
                    .collect(Collectors.toMap(Post::getId, Post::getTitle, (v1, v2) -> v1));

            page.getRecords().forEach(comment -> {
                comment.setUserName(userNameMap.get(comment.getUserId()));
                comment.setPostTitle(postTitleMap.get(comment.getPostId()));
            });
        }
        return Result.success(page);
    }

    /**
     * 管理员删除评论
     */
    @DeleteMapping("/admin/comments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> adminDeleteComment(@PathVariable Long id) {
        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        // 减少评论数
        Post post = postService.getById(comment.getPostId());
        if (post != null && post.getCommentCount() > 0) {
            post.setCommentCount(post.getCommentCount() - 1);
            postService.updateById(post);
        }
        commentService.removeById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 获取用户所有动态
     */
    @GetMapping("/user/{userId}/posts")
    public Result<Page<Post>> getUserPosts(@PathVariable Long userId,
                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId).orderByDesc(Post::getCreateTime);
        postService.page(page, wrapper);
        // 填充点赞、评论数等（同社区列表）
        return Result.success(page);
    }

    // 用户获取所有评论
    @GetMapping("/user/{userId}/comments")
    public Result<Page<CommentVO>> getUserComments(@PathVariable Long userId,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getUserId, userId).orderByDesc(Comment::getCreateTime);
        commentService.page(page, wrapper);
        // 关联动态标题
        List<CommentVO> voList = page.getRecords().stream().map(comment -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(comment, vo);
            Post post = postService.getById(comment.getPostId());
            if (post != null) vo.setPostTitle(post.getTitle());
            return vo;
        }).collect(Collectors.toList());
        Page<CommentVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(voList);
        return Result.success(resultPage);
    }
}