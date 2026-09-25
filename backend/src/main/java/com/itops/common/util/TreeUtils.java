package com.itops.common.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 树形结构构建工具
 */
public class TreeUtils {

    /**
     * @param list            平铺节点列表
     * @param idGetter        节点ID
     * @param parentIdGetter  父节点ID
     * @param childGetter     子集合读取
     * @param childSetter     子集合初始化写入
     */
    public static <T> List<T> build(List<T> list,
                                    Function<T, Long> idGetter,
                                    Function<T, Long> parentIdGetter,
                                    Function<T, List<T>> childGetter,
                                    BiConsumer<T, List<T>> childSetter,
                                    Comparator<T> comparator) {
        List<T> roots = new ArrayList<>();
        for (T node : list) {
            Long parentId = parentIdGetter.apply(node);
            if (parentId == null || parentId == 0L) {
                roots.add(node);
                continue;
            }
            T parent = list.stream()
                    .filter(p -> idGetter.apply(p).equals(parentId))
                    .findFirst()
                    .orElse(null);
            if (parent == null) {
                roots.add(node);
                continue;
            }
            List<T> children = childGetter.apply(parent);
            if (children == null) {
                children = new ArrayList<>();
                childSetter.accept(parent, children);
            }
            children.add(node);
        }
        if (comparator != null) {
            sortRecursive(roots, childGetter, comparator);
        }
        return roots;
    }

    private static <T> void sortRecursive(List<T> nodes,
                                          Function<T, List<T>> childGetter,
                                          Comparator<T> comparator) {
        nodes.sort(comparator);
        for (T node : nodes) {
            List<T> children = childGetter.apply(node);
            if (children != null && !children.isEmpty()) {
                sortRecursive(children, childGetter, comparator);
            }
        }
    }

    /**
     * 收集某节点的全部后代ID(含自身), ancestors 格式 "0,1,5"
     */
    public static String joinAncestors(String parentAncestors, Long parentId) {
        if (parentAncestors == null || parentAncestors.isBlank()) {
            return "0," + parentId;
        }
        return parentAncestors + "," + parentId;
    }
}
