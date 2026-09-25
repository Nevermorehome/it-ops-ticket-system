package com.itops.modules.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itops.common.exception.BusinessException;
import com.itops.modules.system.entity.SysDictData;
import com.itops.modules.system.entity.SysDictType;
import com.itops.modules.system.mapper.SysDictDataMapper;
import com.itops.modules.system.mapper.SysDictTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDictService {

    private final SysDictTypeMapper typeMapper;
    private final SysDictDataMapper dataMapper;

    // ---------------- 字典类型 ----------------

    public List<SysDictType> typeList(String dictName, String dictType) {
        return typeMapper.selectList(Wrappers.<SysDictType>lambdaQuery()
                .like(dictName != null && !dictName.isBlank(), SysDictType::getDictName, dictName)
                .like(dictType != null && !dictType.isBlank(), SysDictType::getDictType, dictType)
                .orderByDesc(SysDictType::getDictId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void createType(SysDictType type) {
        verifyType(type);
        typeMapper.insert(type);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateType(SysDictType type) {
        verifyType(type);
        SysDictType old = typeMapper.selectById(type.getDictId());
        if (old != null && !old.getDictType().equals(type.getDictType())) {
            // 同步修改字典数据的类型编码
            SysDictData update = new SysDictData();
            update.setDictType(type.getDictType());
            dataMapper.update(update, Wrappers.<SysDictData>lambdaUpdate()
                    .eq(SysDictData::getDictType, old.getDictType()));
        }
        typeMapper.updateById(type);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeType(Long dictId) {
        SysDictType type = typeMapper.selectById(dictId);
        if (type != null) {
            dataMapper.delete(Wrappers.<SysDictData>lambdaQuery().eq(SysDictData::getDictType, type.getDictType()));
        }
        typeMapper.deleteById(dictId);
    }

    private void verifyType(SysDictType type) {
        if (type.getDictType() == null || type.getDictType().isBlank()) {
            throw BusinessException.of("字典类型编码不能为空");
        }
        Long count = typeMapper.selectCount(Wrappers.<SysDictType>lambdaQuery()
                .eq(SysDictType::getDictType, type.getDictType())
                .ne(type.getDictId() != null, SysDictType::getDictId, type.getDictId()));
        if (count > 0) {
            throw BusinessException.of("字典类型编码已存在");
        }
    }

    // ---------------- 字典数据 ----------------

    public List<SysDictData> dataList(String dictType, String dictLabel, String status) {
        return dataMapper.selectList(Wrappers.<SysDictData>lambdaQuery()
                .eq(dictType != null && !dictType.isBlank(), SysDictData::getDictType, dictType)
                .like(dictLabel != null && !dictLabel.isBlank(), SysDictData::getDictLabel, dictLabel)
                .eq(status != null && !status.isBlank(), SysDictData::getStatus, status)
                .orderByAsc(SysDictData::getOrderNum));
    }

    public void createData(SysDictData data) {
        dataMapper.insert(data);
    }

    public void updateData(SysDictData data) {
        dataMapper.updateById(data);
    }

    public void removeData(Long dictCode) {
        dataMapper.deleteById(dictCode);
    }
}
