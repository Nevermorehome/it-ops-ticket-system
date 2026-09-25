package com.itops.modules.base.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.itops.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_location")
public class BizLocation extends BaseEntity {

    @TableId
    private Long locationId;
    private String locationName;
    private String building;
    private String floor;
    private String room;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String status;
    private String remark;
}
