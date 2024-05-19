package com.ershi.bibackend.manager;

import com.ershi.bibackend.common.ErrorCode;
import com.ershi.bibackend.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 专门提供 RedisLimiter 限流基础服务（提供通用能力）
 *
 * @author Ershi
 * @date 2024/05/19
 */
@Service
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;


    /**
     * （比如每秒每个用户只能访问一次，需要给每个用户分配一个限流器，用 key 区分)
     *
     * @param key 区分不同的限流器，与 userId 对应
     */
    public void doRateLimit(String key) {
        // 配置限流规则
        RRateLimiter rateLimiter = redissonClient.getRateLimiter("rateLimiter:" + key);
        // 每秒发放5个令牌 => 每秒可执行5次操作
        rateLimiter.trySetRate(RateType.OVERALL, 5, 1, RateIntervalUnit.SECONDS);
        // 每次操作来了后，请求一个令牌
        boolean canProceed = rateLimiter.tryAcquire(1);

        if (!canProceed){
            throw new BusinessException(ErrorCode.TO_MANY_REQUEST);
        }
    }

}
