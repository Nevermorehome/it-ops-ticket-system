package com.itops.modules.report.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {

    /** 区间内按日新建趋势 */
    List<Map<String, Object>> trendCreated(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           @Param("dataScope") String dataScope);

    /** 区间内按日解决趋势 */
    List<Map<String, Object>> trendResolved(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end,
                                            @Param("dataScope") String dataScope);

    /** 分布统计: category / priority / source / status */
    List<Map<String, Object>> distribution(@Param("groupBy") String groupBy,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           @Param("dataScope") String dataScope);

    /** 处理人排行 */
    List<Map<String, Object>> handlerRank(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end,
                                          @Param("dataScope") String dataScope);

    /** 部门工单量 */
    List<Map<String, Object>> deptStat(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       @Param("dataScope") String dataScope);

    Double avgDuration(@Param("start") LocalDateTime start,
                       @Param("end") LocalDateTime end,
                       @Param("dataScope") String dataScope);

    /** 区间概览: 新建/解决/关闭 + 当前待受理/处理中/挂起存量 */
    Map<String, Object> overview(@Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end,
                                 @Param("dataScope") String dataScope);
}
