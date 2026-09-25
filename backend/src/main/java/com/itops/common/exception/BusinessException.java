package com.itops.common.exception;

import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    /**
     * 默认按 400(客户端请求错误) 处理:
     * 参数校验、状态流转冲突、重复数据等业务规则违反都属于调用方错误
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public static BusinessException of(String message) {
        return new BusinessException(message);
    }

    /** 服务端内部错误(磁盘IO、编号生成失败等), 对客户端隐藏细节 */
    public static BusinessException server(String message) {
        return new BusinessException(500, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }
}
