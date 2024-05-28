package com.teriteri.backend.service.impl.article;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teriteri.backend.mapper.ArticleStatsMapper;
import com.teriteri.backend.pojo.*;
import com.teriteri.backend.service.article.ArticleService;
import com.teriteri.backend.service.article.ArticleStatsService;
import com.teriteri.backend.utils.OssUtil;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import com.teriteri.backend.utils.RedisUtil;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.Video;
import com.teriteri.backend.mapper.ArticleMapper;
import com.teriteri.backend.service.category.CategoryService;
import com.teriteri.backend.service.user.UserService;
import com.teriteri.backend.service.utils.CurrentUser;
import com.teriteri.backend.utils.ESUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.concurrent.Executor;


@Slf4j
@Service
public class ArticleServiceImpl implements ArticleService  {


    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CategoryService categoryService;


    @Autowired
    private ArticleStatsMapper articleStatsMapper;

    @Autowired
    private ArticleStatsService articleStatsService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private ESUtil esUtil;
    @Autowired
    private OssUtil ossUtil;
    @Override
    public List<Map<String, Object>> getArticlesWithDataByIds(Set<Object> set, Integer index, Integer quantity) {
        return null;
    }

    @Override
    public List<Map<String, Object>> getArticlesWithDataByIdsOrderByDesc(List<Integer> idList, @Nullable String column, Integer page, Integer quantity) {
        return null;
    }



    /**
     * 根据vid查询单个视频信息，包含用户信息和分区信息
     * @param aid 视频ID
     * @return 包含用户信息、分区信息、视频信息的map
     */
    @Override
    public Map<String, Object> getArticleWithDataById(Integer aid) {
        Map<String, Object> map = new HashMap<>();
        // 先查询 redis
        Article article = redisUtil.getObject("video:" + aid, Article.class);
        if (article == null) {
            // redis 查不到再查数据库
            QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("aid", aid).ne("status", 3);
            article = articleMapper.selectOne(queryWrapper);
            if (article != null) {
                Article finalArtical1 = article;
                CompletableFuture.runAsync(() -> {
                    redisUtil.setExObjectValue("video:" + aid, finalArtical1);    // 异步更新到redis
                }, taskExecutor);
            } else  {
                return null;
            }
        }

        // 多线程异步并行查询用户信息和分区信息并封装
        Article finalArticle = article;
        CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
            map.put("user", userService.getUserById(finalArticle.getAid()));
            map.put("stats", articleStatsService.getArticleStatsById(finalArticle.getAid()));
        }, taskExecutor);
        CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
            map.put("category", categoryService.getCategoryById(finalArticle.getMcId(), finalArticle.getScId()));
        }, taskExecutor);
        map.put("video", article);
        // 使用join()等待userFuture和categoryFuture任务完成
        userFuture.join();
        categoryFuture.join();

        return map;
    }

    @Override
    public List<Map<String, Object>> getArticlesWithDataByIdList(List<Integer> list) {
        return null;
    }

    /**
     * 更新专栏状态，包括过审、不通过、删除，其中审核相关需要管理员权限，删除可以是管理员或者投稿用户
     *
     * @param aid    文章ID
     * @param status 要修改的状态，1通过 2不通过 3删除
     * @return 无data返回，仅返回响应信息
     */
    @Override
    public CustomResponse updateArticleStatus(Integer aid, Integer status) throws IOException {
        CustomResponse customResponse = new CustomResponse();
        Integer userId = currentUser.getUserId();
        if (status == 1 || status == 2) {
            if (!currentUser.isAdmin()) {
                customResponse.setCode(403);
                customResponse.setMessage("您不是管理员，无权访问");
                return customResponse;
            }
            if (status == 1) {
                QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("aid", aid).ne("status", 3);
                Article article = articleMapper.selectOne(queryWrapper);
                if (article == null) {
                    customResponse.setCode(404);
                    customResponse.setMessage("专栏不见了QAQ");
                    return customResponse;
                }
                Integer lastStatus = article.getStatus();
                article.setStatus(1);
                UpdateWrapper<Article> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("aid", aid).set("status", 1).set("upload_date", new Date());     // 更新专栏状态审核通过
                int flag = articleMapper.update(null, updateWrapper);
                if (flag > 0) {
                    // 更新成功
                    esUtil.updateArticle(article);  // 更新ES专栏文档
                    redisUtil.delMember("article_status:" + lastStatus, aid);     // 从旧状态移除
                    redisUtil.addMember("article_status:1", aid);     // 加入新状态
                    redisUtil.zset("user_article_upload:" + article.getUid(), article.getAid());
                    redisUtil.delValue("article:" + aid);     // 删除旧的专栏信息
                    return customResponse;
                } else {
                    // 更新失败，处理错误情况
                    customResponse.setCode(500);
                    customResponse.setMessage("更新状态失败");
                    return customResponse;
                }
            }
            else {
                // 目前逻辑跟上面一样的，但是可能以后要做一些如 记录不通过原因 等操作，所以就分开写了
                QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("aid", aid).ne("status", 3);
                Article article = articleMapper.selectOne(queryWrapper);
                if (article == null) {
                    customResponse.setCode(404);
                    customResponse.setMessage("专栏不见了QAQ");
                    return customResponse;
                }
                Integer lastStatus = article.getStatus();
                article.setStatus(2);
                UpdateWrapper<Article> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("aid", aid).set("status", 2);     // 更新专栏状态审核不通过
                int flag = articleMapper.update(null, updateWrapper);
                if (flag > 0) {
                    // 更新成功
                    esUtil.updateArticle(article);  // 更新ES视频文档
                    redisUtil.delMember("article_status:" + lastStatus, aid);     // 从旧状态移除
                    redisUtil.addMember("article_status:2", aid);     // 加入新状态
                    redisUtil.zsetDelMember("user_article_upload:" + article.getUid(), article.getAid());
                    redisUtil.delValue("article:" + aid);     // 删除旧的专栏信息
                    return customResponse;
                } else {
                    // 更新失败，处理错误情况
                    customResponse.setCode(500);
                    customResponse.setMessage("更新状态失败");
                    return customResponse;
                }
            }
        } else if (status == 3) {
            QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("aid", aid).ne("status", 3);
            Article article = articleMapper.selectOne(queryWrapper);
            if (article == null) {
                customResponse.setCode(404);
                customResponse.setMessage("视频不见了QAQ");
                return customResponse;
            }
            if (Objects.equals(userId, article.getUid()) || currentUser.isAdmin()) {
                String articleUrl = article.getArticleUrl();
                String articlePrefix = articleUrl.split("aliyuncs.com/")[1];  // OSS视频文件名
                String coverUrl = article.getCoverUrl();
                String coverPrefix = coverUrl.split("aliyuncs.com/")[1];  // OSS封面文件名
                Integer lastStatus = article.getStatus();
                UpdateWrapper<Article> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("aid", aid).set("status", 3).set("delete_date", new Date());     // 更新专栏状态已删除
                int flag = articleMapper.update(null, updateWrapper);
                if (flag > 0) {
                    // 更新成功
                    esUtil.deleteVideo(aid);
                    redisUtil.delMember("article_status:" + lastStatus, aid);     // 从旧状态移除
                    redisUtil.delValue("article:" + aid);     // 删除旧的专栏信息
                    redisUtil.delValue("danmu_idset:" + aid);   // 删除该视频的弹幕
                    redisUtil.zsetDelMember("user_article_upload:" + article.getUid(), article.getAid());
                    // 搞个异步线程去删除OSS的源文件
                    CompletableFuture.runAsync(() -> ossUtil.deleteFiles(articlePrefix), taskExecutor);
                    CompletableFuture.runAsync(() -> ossUtil.deleteFiles(coverPrefix), taskExecutor);
                    // 批量删除该专栏下的全部评论缓存
                    CompletableFuture.runAsync(() -> {
                        Set<Object> set = redisUtil.zReverange("comment_article:" + aid, 0, -1);
                        List<String> list = new ArrayList<>();
                        set.forEach(id -> list.add("comment_article_reply:" + id));
                        list.add("comment_article:" + aid);
                        redisUtil.delValues(list);
                    }, taskExecutor);
                    return customResponse;
                } else {
                    // 更新失败，处理错误情况
                    customResponse.setCode(500);
                    customResponse.setMessage("更新状态失败");
                    return customResponse;
                }
            } else {
                customResponse.setCode(403);
                customResponse.setMessage("您没有权限删除专栏");
                return customResponse;
            }
        }
        customResponse.setCode(500);
        customResponse.setMessage("更新状态失败");
        return customResponse;
    }
}
