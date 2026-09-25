package com.itops.modules.base.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.itops.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_asset")
public class BizAsset extends BaseEntity {

    @TableId
    private Long assetId;
    private String assetNo;
    private String assetName;
    private String category;
    private String brand;
    private String model;
    private Long userId;
    private String userName;
    private Long deptId;
    private String locationText;
    /** 0在用 1闲置 2维修 3报废 */
    private String status;
    private LocalDate buyDate;
    private String remark;
}
