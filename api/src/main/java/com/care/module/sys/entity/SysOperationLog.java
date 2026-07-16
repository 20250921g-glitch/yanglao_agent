package com.care.module.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class SysOperationLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String userName;
    private String module;       // 模块（来自控制器@RequestMapping）
    private String operation;    // 操作描述（由请求路径推断）
    private String method;       // HTTP 方法
    private String requestUri;   // 请求URI
    private String ip;           // 操作IP
    private String params;       // 请求参数（脱敏后，可选）
    private Integer status;      // 1成功 0失败
    private String errorMsg;     // 失败原因
    private LocalDateTime createTime;
}
