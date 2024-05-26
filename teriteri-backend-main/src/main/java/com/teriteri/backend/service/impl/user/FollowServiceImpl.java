package com.teriteri.backend.service.impl.user;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.teriteri.backend.mapper.FollowMapper;
import com.teriteri.backend.pojo.Favorite;
import com.teriteri.backend.pojo.Follow;
import com.teriteri.backend.service.user.FollowService;
import com.teriteri.backend.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl implements FollowService {
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;//同步或异步执行器
    /**
     * 根据是否用户本人获取全部可见的关注列表
     * @param uid   用户ID
     * @param isOwner  是否用户本人
     * @return  关注列表
     */
    @Override
    public List<Integer> getUidFollow(Integer uid, boolean isOwner){
        String key = "fans:" + uid;   // uid用户的关注列表，所以uid用户是粉丝
        Set<Object>following = redisUtil.zRange(key, 0, -1);
        List<Integer> list = following.stream().map(obj -> Integer.parseInt(obj.toString())).collect(Collectors.toList());
        if (!list.isEmpty())   {
            if (!isOwner) {
                List<Integer> list1 = new ArrayList<>();
                for (Integer follow : list) {
                    if(true/** 以后实现拉黑用**/){
                        list1.add(follow);
                    }
                }
                return list1;
            }
            return list;
        }
        list = followMapper.getUidFollowByUid(uid);

        List<Integer> finalList = list;
        if(list!=null&& !list.isEmpty()){
            CompletableFuture.runAsync(() -> {
                redisUtil.setExObjectValue(key, finalList);
            }, taskExecutor);
            return list;
        }
        return Collections.emptyList();
    }
    /**
     * 根据是否用户本人获取全部可见的粉丝列表
     * @param uid   用户ID
     * @param isOwner  是否用户本人
     * @return  关注列表
     */
    @Override
    public List<Integer> getUidFans(Integer uid, boolean isOwner){
        String key = "follow:" + uid;   // uid用户的粉丝列表，uid是up
        Set<Object>fans = redisUtil.zRange(key, 0, -1);
        List<Integer> list = fans.stream().map(obj -> Integer.parseInt(obj.toString())).collect(Collectors.toList());
        if (!list.isEmpty())   {
            /*待实现是否根据用户本人来看*/
            return list;
        }
        list = followMapper.getUidFansByUid(uid);
        List<Integer> finalList = list;
        if(list!=null&& !list.isEmpty()){
            CompletableFuture.runAsync(() -> {
                redisUtil.setExObjectValue(key, finalList);
            }, taskExecutor);
            return list;
        }
        return Collections.emptyList();
    }
    /**
     * 关注用户
     * @param uidFollow   关注者ID
     * @param uidFans   粉丝ID
     * 关注者id对应的用户，有一个粉丝ID
     * 粉丝id对应的用户，有一个关注ID
     */
    @Override
    public void addFollow(Integer uidFollow, Integer uidFans){
        Follow newFollow = new Follow(uidFollow,uidFans,1);
        followMapper.insert(newFollow);
        String key = "follow:" + uidFollow;
        redisUtil.zset(key,uidFans);
        String key2 = "fans:" + uidFans;
        redisUtil.zset(key2,uidFollow);
    }
    /**
     * 取关用户
     * @param uidFollow   关注者ID
     * @param uidFans   被关注者ID
     */
    @Override
    public void delFollow(Integer uidFollow, Integer uidFans){
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uidFollow", uidFollow).eq("uidFans", uidFans);
        Follow follow = followMapper.selectOne(queryWrapper);
        if(follow==null) return;
        else followMapper.delete(queryWrapper);
        String key = "follow:" + uidFollow;
        redisUtil.delValue(key);
        String key2 = "fans:" + uidFans;
        redisUtil.delValue(key2);
    }
    /**
     * 更新其他人是否可以查看关注列表
     * @param uid   自己的ID
     * @param visible   能否查看,1可以，0不可以
     */
    @Override
    public void updateVisible(Integer uid, Integer visible){
        UpdateWrapper<Follow> followUpdateWrapper = new UpdateWrapper<>();
        followUpdateWrapper.eq("uid", uid);
        followUpdateWrapper.set("visible", visible);
    }
}
