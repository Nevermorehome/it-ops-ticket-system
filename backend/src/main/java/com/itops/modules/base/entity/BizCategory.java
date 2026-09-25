package com.itops.modules.base.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.itops.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_category")
public class BizCategory extends BaseEntity {

    @TableId
    private Long categoryId;
    private Long parentId;
    private String ancestors;
    private String categoryName;
    private Integer orderNum;
    private String status;

    @TableField(exist = false)
    private List<BizCategory> children;
}
