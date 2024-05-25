package com.ershi.bibackend.controller;

import cn.hutool.json.JSONUtil;
import com.ershi.bibackend.common.BaseResponse;
import com.ershi.bibackend.common.ResultUtils;
import com.ershi.bibackend.manager.RedisLimiterManager;
import com.ershi.bibackend.model.entity.User;
import com.ershi.bibackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.ResponseUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 用于测试自定义线程池
 *
 * @author Ershi
 * @date 2024/05/21
 */
@RestController
@RequestMapping("/queue")
@Slf4j
public class QueueController {

    @Resource
    private ThreadPoolExecutor myThreadPoolExecutor;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private UserService userService;


    /**
     * 添加任务
     *
     * @param taskName 任务名称
     */
    @GetMapping("/add")
    public void add(String taskName) {

        CompletableFuture.runAsync(() -> {
            log.info("任务执行中: " + taskName + "-执行人: " + Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, myThreadPoolExecutor);
    }


    @GetMapping("/get")
    public String get() {
        HashMap<String, Object> map = new HashMap<>();
        int size = myThreadPoolExecutor.getQueue().size();
        map.put("队列长度", size);
        long taskCount = myThreadPoolExecutor.getTaskCount();
        map.put("任务总数", taskCount);
        long completedTaskCount = myThreadPoolExecutor.getCompletedTaskCount();
        map.put("已完成任务数", completedTaskCount);
        int activeCount = myThreadPoolExecutor.getActiveCount();
        map.put("正在执行任务的线程数", activeCount);

        return JSONUtil.toJsonStr(map);
    }

    @GetMapping("/test")
    public BaseResponse<String> test(HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);

        // 限流判断
        redisLimiterManager.doRateLimit("genChartByAI-" + loginUser.getId());

        String result = "1";
        return ResultUtils.success(result);
    }

}
