package com.leyou.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Table(name = "tb_spu")
@Data
public class Spu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long brandId;
    private Long cid1;// 1级类目
    private Long cid2;// 2级类目
    private Long cid3;// 3级类目
    private String title;// 标题
    private String subTitle;// 子标题
    private Boolean saleable;// 是否上架
    private Boolean valid;// 是否有效，逻辑删除用
    private Date createTime;// 创建时间
    @JsonIgnore //不想返回页面的字段属性可以添加该注解
    private Date lastUpdateTime;// 最后修改时间
	// 省略getter和setter

    @Transient
    private SpuDetail spuDetail;
    @Transient
    private List<Sku> skus;


/**
 * 正式开发中一般不会暴露多余属性 比如说三级目录的cid
 * 因此重新封装个专门用于返回页面的VO对象详见SpuVO类
 */
    /*
    @Transient //防止通用mapper把该属性当做数据库字段
    private String cname;//封装返回结果的类名
    @Transient
    private String bname;//封装品牌名

     */
}