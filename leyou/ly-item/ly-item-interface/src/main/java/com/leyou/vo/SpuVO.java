package com.leyou.vo;

import lombok.Data;

import java.util.Date;

/**
 * 该类是专门为了返回页面数据封装的类
 */

@Data
public class SpuVO {
    private Long id;
    private String bname;//品牌名
    private String cname;// 类目名
    private String title;// 标题
    private String subTitle;// 子标题
    private Date createTime;// 创建时间
    private Long cid1;// 1级分类id
    private Long cid2;// 2级分类id
    private Long cid3;// 3级类目
    private Long brandId;
	// 省略getter和setter
}