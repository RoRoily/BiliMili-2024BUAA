package com.teriteri.backend.service.impl.record;

import com.teriteri.backend.mapper.UserMapper;
import com.teriteri.backend.pojo.UserRecord;
import com.teriteri.backend.service.record.UserRecordService;
import com.teriteri.backend.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;


import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserRecordServiceImpl implements UserRecordService {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserMapper userMapper;

    /**
     * 获取近七天播放量增长量
     *
     * @param uid 用户uid
     * @return 一个增长list
     */
    @Override
    public List<Integer> getPlayRecordByUid(Integer uid) {
        String key = "userRecord" + uid;
        UserRecord userRecord = (UserRecord) redisUtil.zRange(key,0,0).iterator().next();
        return userRecord.getPlay();
    }

    @Override
    public void setPlayRecordByUid(Integer uid) {
        String key = "userRecord" + uid;
        UserRecord userRecord = (UserRecord) redisUtil.zRange(key,0,0).iterator().next();
        redisUtil.zsetDelMember(key,userRecord);
        int delta = userRecord.getPlayNew()-userRecord.getPlayOld();
        List<Integer> deltaWeek = new ArrayList<>();
        for(int i=1;i<7;++i){
            deltaWeek.add(userRecord.getPlay().get(i));
        }
        deltaWeek.add(delta);
        userRecord.setPlay(deltaWeek);
        userRecord.setPlayOld(userRecord.getPlayNew());
        userRecord.setPlayNew(0);
        redisUtil.zset(key,userRecord);
    }

    /**
     * 获取近七天点赞量增长量
     *
     * @param uid 用户uid
     * @return 一个增长list
     */
    @Override
    public List<Integer> getLoveRecordByUid(Integer uid) {
        String key = "userRecord" + uid;
        UserRecord userRecord = (UserRecord) redisUtil.zRange(key,0,0).iterator().next();
        return userRecord.getLove();
    }

    @Override
    public void setLoveRecordByUid(Integer uid) {
        String key = "userRecord" + uid;
        UserRecord userRecord = (UserRecord) redisUtil.zRange(key,0,0).iterator().next();
        int delta = userRecord.getLoveNew()-userRecord.getLoveOld();
        List<Integer> deltaWeek = new ArrayList<>();
        for(int i=1;i<7;++i){
            deltaWeek.add(userRecord.getLove().get(i));
        }
        deltaWeek.add(delta);
        userRecord.setLove(deltaWeek);
        userRecord.setLoveOld(userRecord.getLoveNew());
        userRecord.setLoveNew(0);
        redisUtil.zset(key,userRecord);
    }

    /**
     * 获取近七天收藏量增长量
     *
     * @param uid 用户uid
     * @return 一个增长list
     */
    @Override
    public List<Integer> getCollectRecordByUid(Integer uid) {
        String key = "userRecord" + uid;
        UserRecord userRecord = (UserRecord) redisUtil.zRange(key,0,0).iterator().next();
        return userRecord.getCollect();
    }

    @Override
    public void setCollectRecordByUid(Integer uid) {
        String key = "userRecord" + uid;
        UserRecord userRecord = (UserRecord) redisUtil.zRange(key,0,0).iterator().next();
        int delta = userRecord.getCollectNew()-userRecord.getCollectOld();
        List<Integer> deltaWeek = new ArrayList<>();
        for(int i=1;i<7;++i){
            deltaWeek.add(userRecord.getCollect().get(i));
        }
        deltaWeek.add(delta);
        userRecord.setCollect(deltaWeek);
        userRecord.setCollectOld(userRecord.getCollectNew());
        userRecord.setCollectNew(0);
        redisUtil.zset(key,userRecord);
    }

    /**
     * 获取近七天关注量增长量
     *
     * @param uid 用户uid
     * @return 一个增长list
     */
    @Override
    public List<Integer> getFansRecordByUid(Integer uid) {
        String key = "userRecord" + uid;
        UserRecord userRecord = (UserRecord) redisUtil.zRange(key,0,0).iterator().next();
        return userRecord.getFans();
    }

    @Override
    public void setFansRecordByUid(Integer uid) {
        String key = "userRecord" + uid;
        UserRecord userRecord = (UserRecord) redisUtil.zRange(key,0,0).iterator().next();
        int delta = userRecord.getFansNew()-userRecord.getFansOld();
        List<Integer> deltaWeek = new ArrayList<>();
        for(int i=1;i<7;++i){
            deltaWeek.add(userRecord.getFans().get(i));
        }
        deltaWeek.add(delta);
        userRecord.setFans(deltaWeek);
        userRecord.setFansOld(userRecord.getFansNew());
        userRecord.setFansNew(0);
        redisUtil.zset(key,userRecord);
    }

    /**
     * 更新某个用户的record
     * uid 用户uid
     *
     * @param uid   用户uid
     */
    @Override
    public void updateRecordByUid(Integer uid) {
        setPlayRecordByUid(uid);
        setLoveRecordByUid(uid);
        setCollectRecordByUid(uid);
        setFansRecordByUid(uid);
    }

    /**
     * 实现更新，暂定于UTM+8的0点
     */
    @Override
    // 指定在每天的中国时间0:00运行
    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Shanghai")
    public void updateRecord() {
        List<Integer> userIds = userMapper.getAllUserIds();
        for(Integer uid:userIds){
            updateRecordByUid(uid);
        }
    }
}
