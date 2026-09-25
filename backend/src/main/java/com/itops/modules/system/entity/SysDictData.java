package com.itops.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.itops.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

    @TableId
    private Long dictCode;
    private String dictType;
    private String dictLabel;
    private String dictValue;
    private String listClass;
    private Integer orderNum;
    private String status;
    private String remark;
}
