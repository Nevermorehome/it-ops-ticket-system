package com.itops.common.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页查询基类
 */
@Data
public class PageQuery {

    @Min(value = 1, message = "页码不能小于1")
    private long pageNum = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 500, message = "每页条数不能超过500")
    private long pageSize = 10;

    /** 排序字段 */
    private String orderByColumn;

    /** asc/desc */
    private String isAsc = "desc";
}
