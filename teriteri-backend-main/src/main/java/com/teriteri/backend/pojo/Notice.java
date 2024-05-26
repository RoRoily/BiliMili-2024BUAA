package com.teriteri.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notice {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String descr;
    private Integer userId;
    private ArrayList<Integer> anotherId;
    private String content;//推送内容

}
