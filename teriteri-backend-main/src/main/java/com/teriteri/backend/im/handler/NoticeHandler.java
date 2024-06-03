package com.teriteri.backend.im.handler;

import com.alibaba.fastjson2.JSONObject;
import com.teriteri.backend.im.IMServer;
import com.teriteri.backend.mapper.ChatDetailedMapper;
import com.teriteri.backend.pojo.Article;
import com.teriteri.backend.pojo.ChatDetailed;
import com.teriteri.backend.pojo.IMResponse;
import com.teriteri.backend.pojo.dto.UserDTO;
import com.teriteri.backend.service.message.ChatService;
import com.teriteri.backend.service.user.FollowService;
import com.teriteri.backend.service.user.UserService;
import com.teriteri.backend.utils.RedisUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


@Slf4j
@Component
public class NoticeHandler {
    private static ChatService chatService;
    private static ChatDetailedMapper chatDetailedMapper;
    private static UserService userService;
    private static RedisUtil redisUtil;
    private static Executor taskExecutor;

    private static FollowService followService;

    @Autowired
    private void setDependencies(ChatService chatService,
                                 ChatDetailedMapper chatDetailedMapper,
                                 UserService userService,
                                 RedisUtil redisUtil,
                                 FollowService followService,
                                 @Qualifier("taskExecutor") Executor taskExecutor) {
        NoticeHandler.chatService = chatService;
        NoticeHandler.chatDetailedMapper = chatDetailedMapper;
        NoticeHandler.userService = userService;
        NoticeHandler.redisUtil = redisUtil;
        NoticeHandler.taskExecutor = taskExecutor;
        NoticeHandler.followService =  followService;
    }
    /**
     * 接收aid，向目前所有的粉丝返回文章的contentUrl,标题，coverUrl
     * 以下是传输参数
     * @param ctx   对应视频ID
     * @param article   查询获得的文章实体
     * @return  文件
     *
     *
     * */

    //发送消息时，ctx只作为获取发送方及处理异常项的作用



    /**
     * 发送消息
     * @param ctx
     * @param tx
     */
    public static void send(ChannelHandlerContext ctx, TextWebSocketFrame tx) {
        try {
            // 从channel中获取当前用户id 封装写库
            Integer user_id = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
            System.out.println(user_id);
            List<Integer> fans = followService.getUidFans(user_id,true);
            System.out.println(fans);
            for(Integer fan:fans) {
                ChatDetailed chatDetailed = JSONObject.parseObject(tx.text(), ChatDetailed.class);
                System.out.println("发送方id：" + user_id + "接收方Id:" + fan);
                user_id = 24;
                chatDetailed.setAnotherId(fan);
                chatDetailed.setUserId(user_id);
                chatDetailed.setUserDel(0);
                chatDetailed.setAnotherDel(0);
                chatDetailed.setWithdraw(0);
                chatDetailed.setTime(new Date());
                System.out.println("接收到聊天消息：" + chatDetailed);
                chatDetailedMapper.insert(chatDetailed);
                // "chat_detailed_zset:对方:自己"
                redisUtil.zset("chat_detailed_zset:" + user_id + ":" + chatDetailed.getAnotherId(), chatDetailed.getId());
                redisUtil.zset("chat_detailed_zset:" + chatDetailed.getAnotherId() + ":" + user_id, chatDetailed.getId());
                boolean online = chatService.updateChat(user_id, chatDetailed.getAnotherId());

                // 转发到发送者和接收者的全部channel
                Map<String, Object> map = new HashMap<>();
                map.put("type", "接收");
                map.put("online", online);  // 对方是否在窗口
                map.put("detail", chatDetailed);
                Integer finalUser_id = user_id;
                CompletableFuture<Void> chatFuture = CompletableFuture.runAsync(() -> {
                    map.put("chat", chatService.getChat(finalUser_id, chatDetailed.getAnotherId()));
                }, taskExecutor);
                CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                    map.put("user", userService.getUserById(finalUser_id));
                }, taskExecutor);
                chatFuture.join();
                userFuture.join();

                // 发给自己的全部channel
                Set<Channel> from = IMServer.userChannel.get(user_id);
                if (from != null) {
                    for (Channel channel : from) {
                        channel.writeAndFlush(IMResponse.message("whisper", map));
                    }
                }
                // 发给对方的全部channel
                Set<Channel> to = IMServer.userChannel.get(chatDetailed.getAnotherId());
                if (to != null) {
                    for (Channel channel : to) {
                        channel.writeAndFlush(IMResponse.message("whisper", map));
                    }
                }
            }
        } catch (Exception e) {
            log.error("发送聊天信息时出错了：" + e);
            ctx.channel().writeAndFlush(IMResponse.error("发送消息时出错了 Σ(ﾟдﾟ;)"));
        }
    }

    /*public static void send(ChannelHandlerContext ctx, Article article){
        try{
            //Notice notice =  JSONObject.parseObject(tx.text(), Notice.class);
            //获取发送方的id
            Integer user_id = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
            //notice.setUserId(user_id);
            //缩略图？

            List<Integer> fans = getFollowed(user_id,true);
            for(Integer fan:fans){
                UserDTO fan_user  = userService.getUserById(fan);
                // boolean online = NoticeService.updateCha

                //需要发送到前端的格式
                Map<String,Object> map = new HashMap<>();
                map.put("type","接收");
                map.put("cover",article.getCoverUrl());
                map.put("title",article.getTitle());
                map.put("aid",article.getAid());
                //?需要这么写吗？
                //发送方
                map.put("user", userService.getUserById(user_id));
                Set<Channel> to = IMServer.userChannel.get(fan_user.getUid());
                if (to != null) {
                    for (Channel channel : to) {
                        //需要添加notice类
                        channel.writeAndFlush(IMResponse.message("notice", map)); //实时发送并写入
                    }
                }
            }
        }catch (Exception e) {
            log.error("发送通知信息时出错了：" + e);
            ctx.channel().writeAndFlush(IMResponse.error("发送通知信息时出错了 Σ(ﾟдﾟ;)"));
        }
    }*/
}

/*

    public static void send(ChannelHandlerContext ctx, TextWebSocketFrame tx) {
        try {
            ChatDetailed chatDetailed = JSONObject.parseObject(tx.text(), ChatDetailed.class);
//            System.out.println("接收到聊天消息：" + chatDetailed);

            // 从channel中获取当前用户id 封装写库
            Integer user_id = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
            chatDetailed.setUserId(user_id);
            chatDetailed.setUserDel(0);
            chatDetailed.setAnotherDel(0);
            chatDetailed.setWithdraw(0);
            chatDetailed.setTime(new Date());
            chatDetailedMapper.insert(chatDetailed);
            // "chat_detailed_zset:对方:自己"
            redisUtil.zset("chat_detailed_zset:" + user_id + ":" + chatDetailed.getAnotherId(), chatDetailed.getId());
            redisUtil.zset("chat_detailed_zset:" + chatDetailed.getAnotherId() + ":" + user_id, chatDetailed.getId());
            boolean online = chatService.updateChat(user_id, chatDetailed.getAnotherId());

            // 转发到发送者和接收者的全部channel
            Map<String, Object> map = new HashMap<>();
            map.put("type", "接收");
            map.put("online", online);  // 对方是否在窗口
            map.put("detail", chatDetailed);
            CompletableFuture<Void> chatFuture = CompletableFuture.runAsync(() -> {
                map.put("chat", chatService.getChat(user_id, chatDetailed.getAnotherId()));
            }, taskExecutor);
            CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                map.put("user", userService.getUserById(user_id));
            }, taskExecutor);
            chatFuture.join();
            userFuture.join();

            // 发给自己的全部channel
            Set<Channel> from = IMServer.userChannel.get(user_id);
            if (from != null) {
                for (Channel channel : from) {
                    channel.writeAndFlush(IMResponse.message("whisper", map));
                }
            }
            // 发给对方的全部channel
            Set<Channel> to = IMServer.userChannel.get(chatDetailed.getAnotherId());
            if (to != null) {
                for (Channel channel : to) {
                    channel.writeAndFlush(IMResponse.message("whisper", map));
                }
            }

        } catch (Exception e) {
            log.error("发送聊天信息时出错了：" + e);
            ctx.channel().writeAndFlush(IMResponse.error("发送消息时出错了 Σ(ﾟдﾟ;)"));
        }
    }*/
