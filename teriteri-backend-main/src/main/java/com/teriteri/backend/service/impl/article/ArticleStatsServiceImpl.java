package com.teriteri.backend.service.impl.article;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.teriteri.backend.mapper.ArticleMapper;
import com.teriteri.backend.mapper.ArticleStatsMapper;
import com.teriteri.backend.mapper.VideoMapper;
import com.teriteri.backend.mapper.VideoStatsMapper;
import com.teriteri.backend.pojo.Article;
import com.teriteri.backend.pojo.ArticleStats;
import com.teriteri.backend.pojo.VideoStats;
import com.teriteri.backend.service.article.ArticleService;
import com.teriteri.backend.service.article.ArticleStatsService;
import com.teriteri.backend.service.utils.CurrentUser;
import com.teriteri.backend.service.video.VideoStatsService;
import com.teriteri.backend.utils.ESUtil;
import com.teriteri.backend.utils.OssUtil;
import com.teriteri.backend.utils.RedisUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;
@Service
public class ArticleStatsServiceImpl implements ArticleStatsService {

    @Value("${directory.cover}")
    private String COVER_DIRECTORY;   // 投稿封面存储目录
    @Value("${directory.video}")
    private String VIDEO_DIRECTORY;   // 投稿视频存储目录
    @Value("${directory.chunk}")
    private String CHUNK_DIRECTORY;   // 分片存储目录

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleStatsMapper articleStatsMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OssUtil ossUtil;

    @Autowired
    private ESUtil esUtil;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Override
    public ArticleStats getArticleStatsById(Integer aid) {
        ArticleStats articleStats = redisUtil.getObject("videoStats:" + aid, ArticleStats.class);
        if (articleStats == null) {
            articleStats = articleStatsMapper.selectById(aid);
            if (articleStats != null) {
                ArticleStats finalArticalstats = articleStats;
                //VideoStats finalVideoStats = videoStats;
                CompletableFuture.runAsync(() -> {
                    redisUtil.setExObjectValue("articleStats:" + aid, finalArticalstats);    // 异步更新到redis
                }, taskExecutor);
            } else {
                return null;
            }
        }
        // 多线程查redis反而更慢了，所以干脆直接查数据库
        return articleStats;
    }

    @Override
    public void updateStats(Integer aid, String column, boolean increase, Integer count) {

    }

    @Override
    public void updateGoodAndBad(Integer aid, boolean addGood) {

    }
}
