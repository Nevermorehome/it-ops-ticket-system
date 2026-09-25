package com.itops.modules.base.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itops.common.exception.BusinessException;
import com.itops.common.util.TreeUtils;
import com.itops.modules.base.entity.BizCategory;
import com.itops.modules.base.mapper.BizCategoryMapper;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class BizCategoryService extends ServiceImpl<BizCategoryMapper, BizCategory> {

    public List<BizCategory> listAll(String name, String status) {
        return list(Wrappers.<BizCategory>lambdaQuery()
                .like(name != null && !name.isBlank(), BizCategory::getCategoryName, name)
                .eq(status != null && !status.isBlank(), BizCategory::getStatus, status)
                .orderByAsc(BizCategory::getParentId).orderByAsc(BizCategory::getOrderNum));
    }

    public List<BizCategory> tree(String name, String status) {
        return TreeUtils.build(listAll(name, status),
                BizCategory::getCategoryId, BizCategory::getParentId,
                BizCategory::getChildren, BizCategory::setChildren,
                Comparator.comparing(BizCategory::getOrderNum));
    }

    public void create(BizCategory category) {
        fillAncestors(category);
        save(category);
    }

    public void update(BizCategory category) {
        if (category.getCategoryId().equals(category.getParentId())) {
            throw BusinessException.of("上级分类不能选择自身");
        }
        fillAncestors(category);
        updateById(category);
    }

    public void remove(Long id) {
        long children = count(Wrappers.<BizCategory>lambdaQuery().eq(BizCategory::getParentId, id));
        if (children > 0) {
            throw BusinessException.of("存在子分类，不允许删除");
        }
        removeById(id);
    }

    private void fillAncestors(BizCategory category) {
        Long parentId = category.getParentId() == null ? 0L : category.getParentId();
        category.setParentId(parentId);
        if (parentId == 0L) {
            category.setAncestors("0");
        } else {
            BizCategory parent = getById(parentId);
            if (parent == null) {
                throw BusinessException.of("上级分类不存在");
            }
            category.setAncestors(TreeUtils.joinAncestors(parent.getAncestors(), parentId));
        }
    }
}
