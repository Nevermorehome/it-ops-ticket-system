package com.itops;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 信息部内部 IT 运维工单记录系统 - 启动类
 */
@EnableAsync
@SpringBootApplication
@MapperScan("com.itops.modules.**.mapper")
public class ItopsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItopsApplication.class, args);
        System.out.println("""

                ====================================================
                  信息部 IT 运维工单系统后端启动成功
                  接口文档: http://localhost:8080/doc.html
                ====================================================
                """);
    }
}
