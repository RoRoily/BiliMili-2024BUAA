package com.teriteri.backend.service.record;

import java.util.List;

public interface UserRecordService {

    /**
     * 获取近七天播放量增长量
     * @param uid 用户uid
     * @return    一个增长list
     */
    List<Integer> getPlayRecordByUid(Integer uid);
    void setPlayRecordByUid(Integer uid);
    /**
     * 获取近七天点赞量增长量
     * @param uid 用户uid
     * @return    一个增长list
     */
    List<Integer> getLoveRecordByUid(Integer uid);
    void setLoveRecordByUid(Integer uid);
    /**
     * 获取近七天收藏量增长量
     * @param uid 用户uid
     * @return    一个增长list
     */
    List<Integer> getCollectRecordByUid(Integer uid);
    void setCollectRecordByUid(Integer uid);
    /**
     * 获取近七天关注量增长量
     * @param uid 用户uid
     * @return    一个增长list
     */
    List<Integer> getFansRecordByUid(Integer uid);
    void setFansRecordByUid(Integer uid);

    /**
     * 更新某个用户的record
     * @param uid 用户uid
     */
    void updateRecordByUid(Integer uid);
    /**
     * 实现定时更新，暂定于UTM+8的0点
     */
    void updateRecord();
}
