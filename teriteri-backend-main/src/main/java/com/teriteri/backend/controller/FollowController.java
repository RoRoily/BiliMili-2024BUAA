package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.user.FollowService;
import com.teriteri.backend.service.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
public class FollowController {
    @Autowired
    private FollowService followService;
    @Autowired
    private CurrentUser currentUser;
    /**
     * 站内用户请求某个用户的关注列表（需要jwt鉴权）
     * @param uid   被查看的用户ID
     * @return  包含关注列表的响应对象
     */
    @GetMapping("/following/get-all/user")
    public CustomResponse getAllFollowingForUser(@RequestParam("uid") Integer uid) {
        Integer loginUid = currentUser.getUserId();
        CustomResponse customResponse = new CustomResponse();
        if (Objects.equals(loginUid, uid)) {
            customResponse.setData(followService.getUidFollow(uid, true));
        } else {
            customResponse.setData(followService.getUidFollow(uid, false));
        }
        return customResponse;
    }
    /**
     * 站内用户请求某个用户的粉丝列表（需要jwt鉴权）
     * @param uid   被查看的用户ID
     * @return  包含关注列表的响应对象
     */
    @GetMapping("/followed/get-all/user")
    public CustomResponse getAllFollowedForUser(@RequestParam("uid") Integer uid) {
        Integer loginUid = currentUser.getUserId();
        CustomResponse customResponse = new CustomResponse();
        if (Objects.equals(loginUid, uid)) {
            customResponse.setData(followService.getUidFans(uid, true));
        } else {
            customResponse.setData(followService.getUidFans(uid, false));
        }
        return customResponse;
    }
    /**
     * 站内用户请求关注某个用户
     * @param uidFollow   被关注者ID
     * @return  包含关注列表的响应对象
     */
    @PostMapping("/follow/follow-one/")
    public CustomResponse addFollowing(@RequestParam("uidFollow") Integer uidFollow) {
        Integer loginUid = currentUser.getUserId();
        CustomResponse customResponse = new CustomResponse();
        followService.addFollow(uidFollow, loginUid);
        customResponse.setMessage("关注成功");
        return customResponse;
    }
    /**
     * 站内用户请求取关某个用户
     * @param uidFollow   被关注者ID
     * @return  包含关注列表的响应对象
     */
    @PostMapping("/follow/delFollow-one/")
    public CustomResponse delFollowing(@RequestParam("uidFollow") Integer uidFollow) {
        Integer loginUid = currentUser.getUserId();
        CustomResponse customResponse = new CustomResponse();
        followService.delFollow(uidFollow, loginUid);
        customResponse.setMessage("取关成功");
        return customResponse;
    }
    /**
     * 站内用户请求修改可见权限
     * @param visible 权限
     * @return  包含关注列表的响应对象
     */
    @PostMapping("/follow/updateVisible/")
    public CustomResponse updateUserVisible(@RequestParam("visible") Integer visible){
        CustomResponse customResponse = new CustomResponse();
        Integer loginUid = currentUser.getUserId();
        followService.updateVisible(loginUid, visible);
        customResponse.setMessage("更新权限成功");
        return customResponse;
    }
}
