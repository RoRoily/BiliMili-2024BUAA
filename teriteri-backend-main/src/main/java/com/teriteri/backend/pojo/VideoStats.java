package com.teriteri.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoStats {
    @TableId
    private Integer vid;
    private Integer play;
    //private Integer oldPlay;
    private Integer danmu;
    //private Integer oldDanmu;
    private Integer good;
    //private Integer oldGood;
    private Integer bad;
    //private Integer oldBad;
    private Integer coin;
    //private Integer oldCoin;
    private Integer collect;
    //private Integer oldCollect;
    private Integer share;
    //private Integer oldShare;
    private Integer comment;
    //private Integer oldComment;
}
