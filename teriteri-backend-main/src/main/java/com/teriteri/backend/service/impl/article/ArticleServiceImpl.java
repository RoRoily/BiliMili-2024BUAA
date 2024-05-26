package com.teriteri.backend.service.impl.article;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teriteri.backend.mapper.ArticleStatsMapper;
import com.teriteri.backend.pojo.*;
import com.teriteri.backend.service.article.ArticleService;
import com.teriteri.backend.service.article.ArticleStatsService;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import com.teriteri.backend.utils.RedisUtil;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.teriteri.backend.mapper.VideoMapper;
import com.teriteri.backend.mapper.VideoStatsMapper;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.Video;
import com.teriteri.backend.mapper.ArticleMapper;
import com.teriteri.backend.service.category.CategoryService;
import com.teriteri.backend.service.user.UserService;
import com.teriteri.backend.service.utils.CurrentUser;
import com.teriteri.backend.service.video.VideoService;
import com.teriteri.backend.service.video.VideoStatsService;
import com.teriteri.backend.utils.ESUtil;
import com.teriteri.backend.utils.OssUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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

    @Override
    public CustomResponse updateVideoStatus(Integer aid, Integer status) throws IOException {
        return null;
    }
}
