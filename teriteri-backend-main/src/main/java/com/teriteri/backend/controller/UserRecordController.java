package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.record.UserRecordService;
import com.teriteri.backend.service.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class UserRecordController {
    @Autowired
    private UserRecordService userRecordService;
    @Autowired
    private CurrentUser currentUser;
    /**
     * 查询用户7天数据
     * @param uid 用户uid
     */
    @PostMapping("/user_record")
    public CustomResponse getUserRecord(@RequestParam("uid") Integer uid) {
        CustomResponse customResponse = new CustomResponse();
        try{
            Map<String,List<Integer>> map = new HashMap<>();
            List<Integer> play = userRecordService.getPlayRecordByUid(uid);
            List<Integer> love = userRecordService.getLoveRecordByUid(uid);
            List<Integer> collect = userRecordService.getCollectRecordByUid(uid);
            List<Integer> fans = userRecordService.getFansRecordByUid(uid);
            if(play != null && !play.isEmpty()){map.put("play",play);}
            if(love != null && !love.isEmpty()){map.put("love",love);}
            if(collect != null && !collect.isEmpty()){map.put("collect",collect);}
            if(fans != null && !fans.isEmpty()){map.put("fans",fans);}
            if(map.size()==4)customResponse.setData(map);
        }catch (Exception e){
            e.printStackTrace();
            customResponse.setCode(500);
            customResponse.setMessage("获取用户近七天数据失败");
        }
        return customResponse;
    }
}
