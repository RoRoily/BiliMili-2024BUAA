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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;
@Data
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
        UpdateWrapper<ArticleStats> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("aid", aid);
        if (increase) {
            updateWrapper.setSql(column + " = " + column + " + " + count);
        } else {
            // 更新后的字段不能小于0
            updateWrapper.setSql(column + " = CASE WHEN " + column + " - " + count + " < 0 THEN 0 ELSE " + column + " - " + count + " END");
        }
        articleStatsMapper.update(null, updateWrapper);
        redisUtil.delValue("articleStats:" + aid);
    }

    @Override
    public void updateGoodAndBad(Integer aid, boolean addGood) {
        UpdateWrapper<ArticleStats> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("aid", aid);
        if (addGood) {
            updateWrapper.setSql("good = good + 1");
            updateWrapper.setSql("bad = CASE WHEN bad - 1 < 0 THEN 0 ELSE bad - 1 END");
        } else {
            updateWrapper.setSql("bad = bad + 1");
            updateWrapper.setSql("good = CASE WHEN good - 1 < 0 THEN 0 ELSE good - 1 END");
        }
        articleStatsMapper.update(null, updateWrapper);
        redisUtil.delValue("articleStats:" + aid);
    }
}
