package com.itops.modules.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itops.modules.message.entity.BizMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BizMessageMapper extends BaseMapper<BizMessage> {
}
