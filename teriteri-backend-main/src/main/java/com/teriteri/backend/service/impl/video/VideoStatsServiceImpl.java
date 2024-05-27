package com.teriteri.backend.service.impl.video;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.teriteri.backend.mapper.VideoMapper;
import com.teriteri.backend.mapper.VideoStatsMapper;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.UserRecord;
import com.teriteri.backend.pojo.Video;
import com.teriteri.backend.pojo.VideoStats;
import com.teriteri.backend.service.video.VideoStatsService;
import com.teriteri.backend.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.*;

@Service
public class VideoStatsServiceImpl implements VideoStatsService {
    @Autowired
    private VideoStatsMapper videoStatsMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Autowired
    private VideoMapper videoMapper;

    /**
     * 根据视频ID查询视频常变数据
     * @param vid 视频ID
     * @return 视频数据统计
     */
    @Override
    public VideoStats getVideoStatsById(Integer vid) {
        VideoStats videoStats = redisUtil.getObject("videoStats:" + vid, VideoStats.class);
        if (videoStats == null) {
            videoStats = videoStatsMapper.selectById(vid);
            if (videoStats != null) {
                VideoStats finalVideoStats = videoStats;
                CompletableFuture.runAsync(() -> {
                    redisUtil.setExObjectValue("videoStats:" + vid, finalVideoStats);    // 异步更新到redis
                }, taskExecutor);
            } else {
                return null;
            }
        }
        // 多线程查redis反而更慢了，所以干脆直接查数据库
        return videoStats;
    }

    /**
     * 更新指定字段
     * @param vid   视频ID
     * @param column    对应数据库的列名
     * @param increase  是否增加，true则增加 false则减少
     * @param count 增减数量 一般是1，只有投币可以加2
     */
    @Override
    public void updateStats(Integer vid, String column, boolean increase, Integer count) {
        UpdateWrapper<VideoStats> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("vid", vid);
        if (increase) {
            updateWrapper.setSql(column + " = " + column + " + " + count);
        } else {
            // 更新后的字段不能小于0
            updateWrapper.setSql(column + " = CASE WHEN " + column + " - " + count + " < 0 THEN 0 ELSE " + column + " - " + count + " END");
        }
        videoStatsMapper.update(null, updateWrapper);
        redisUtil.delValue("videoStats:" + vid);
        //更新视频作者的播放数据
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", vid);
        Integer uid = videoMapper.selectOne(queryWrapper).getUid();
        String key = "userRecord"+uid;
        try{
            UserRecord userRecord = (UserRecord) redisUtil.zRange(key,0,0).iterator().next();
            redisUtil.zsetDelMember(key,userRecord);
            if(column.equals("play")){
                userRecord.setPlayNew(userRecord.getPlayNew()+1);
            }
            redisUtil.zset(key,userRecord);
        }catch (Exception e){
            e.printStackTrace();
            new CustomResponse(404,"未找到视频作者的记录",null);
        }
    }

    /**
     * 同时更新点赞和点踩
     * @param vid   视频ID
     * @param addGood   是否点赞，true则good+1&bad-1，false则good-1&bad+1
     */
    @Override
    public void updateGoodAndBad(Integer vid, boolean addGood) {
        UpdateWrapper<VideoStats> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("vid", vid);
        if (addGood) {
            updateWrapper.setSql("good = good + 1");
            updateWrapper.setSql("bad = CASE WHEN bad - 1 < 0 THEN 0 ELSE bad - 1 END");
        } else {
            updateWrapper.setSql("bad = bad + 1");
            updateWrapper.setSql("good = CASE WHEN good - 1 < 0 THEN 0 ELSE good - 1 END");
        }
        videoStatsMapper.update(null, updateWrapper);
        redisUtil.delValue("videoStats:" + vid);
        //更新视频作者的点赞数据
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", vid);
        Integer uid = videoMapper.selectOne(queryWrapper).getUid();
        String key = "userRecord"+uid;
        try{
            UserRecord userRecord = (UserRecord) redisUtil.zRange(key,0,0).iterator().next();
            redisUtil.zsetDelMember(key,userRecord);
            if(addGood)userRecord.setLoveNew(userRecord.getLoveNew()+1);
            else userRecord.setLoveNew(Math.max(userRecord.getLoveNew()-1,0));
            redisUtil.zset(key,userRecord);
        }catch (Exception e){
            e.printStackTrace();
            new CustomResponse(404,"未找到视频作者的记录",null);
        }
    }
    /**
     * 更新视频的硬币数
     * @param vid   视频ID
     * @param coin   硬币个数
     */
    @Override
    public void updateCoin(Integer vid, Integer coin) {

    }
    /**
     * 更新新数据时，将原数据存入旧数据
     * @param vid   视频ID
     * 标记：该函数可能因为数据库字段名不对应导致出错。
     */
    @Override
    public void updateOld(Integer vid){
        UpdateWrapper<VideoStats> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("vid", vid);
        updateWrapper.setSql("oldPlay = play");
        updateWrapper.setSql("oldGood = good");
        updateWrapper.setSql("oldCoin = coin");
        updateWrapper.setSql("oldCollect = collect");
        updateWrapper.setSql("oldShare = share");
        updateWrapper.setSql("oldComment = comment");
        videoStatsMapper.update(null, updateWrapper);
        redisUtil.delValue("videoStats:" + vid);
    }

    // 计算距离下一个十二点的时间间隔
    private static long computeInitialDelay(Calendar now) {
        // 获取当前时间的小时和分钟
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        // 计算距离下一个十二点的时间间隔（毫秒数）
        long millisInADay = 24 * 60 * 60 * 1000;
        long millisUntilNextMidnight = millisInADay - (hour * 60 * 60 + minute * 60) * 1000;

        return millisUntilNextMidnight;
    }

    /**
     * 定时更新旧值
     * @param vid   视频ID
     */
    @Override
    public void updateOldTiming(Integer vid){
        // 创建一个ScheduledExecutorService对象
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        // 获取当前时间
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Beijing"));
        // 计算距离下一个十二点的时间间隔
        long initialDelay = computeInitialDelay(now);
        // 每天十二点执行一次
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                updateOld(vid);
            }
        }, initialDelay, 24 * 60 * 60, TimeUnit.SECONDS);
    }

    /**
     * 更新用户之间的关注，要求用户1关注用户2
     * @param uid1   用户1ID
     * @param uid2   硬币个数
     */
    @Override
    public void updateFollow(Integer uid1, Integer uid2){

    }
}
