package com.panda.pay.config.redis;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Title:RedissonAutoConfiguration @Copyright: Copyright (c) 2016 @Description: <br>
 * @Company: panda-fintech @Created on 2018/6/24下午2:18
 *
 * @miaoxuehui@panda-fintech.com
 */
@Configuration
@ConditionalOnClass(Config.class)
public class RedissonAutoConfiguration {
  @Autowired private RedissonProperties redssionProperties;

  /**
   * 哨兵模式自动装配
   *
   * @return
   */
  @Bean
  @ConditionalOnProperty(name = "redisson.master-name")
  RedissonClient redissonSentinel() {
    Config config = new Config();
    SentinelServersConfig serverConfig =
        config
            .useSentinelServers()
            .addSentinelAddress(redssionProperties.getSentinelAddresses())
            .setMasterName(redssionProperties.getMasterName())
            .setTimeout(redssionProperties.getTimeout())
            .setMasterConnectionPoolSize(redssionProperties.getMasterConnectionPoolSize())
            .setSlaveConnectionPoolSize(redssionProperties.getSlaveConnectionPoolSize());

    if (StringUtils.isNotBlank(redssionProperties.getPassword())) {
      serverConfig.setPassword(redssionProperties.getPassword());
    }
    return Redisson.create(config);
  }

  /**
   * 单机模式自动装配
   *
   * @return
   */
  @Bean(name = "redissonClient")
  @ConditionalOnProperty(name = "redisson.address")
  RedissonClient redissonSingle() {
    Config config = new Config();
    SingleServerConfig serverConfig =
        config
            .useSingleServer()
            .setAddress(redssionProperties.getAddress())
            .setTimeout(redssionProperties.getTimeout())
            .setConnectionPoolSize(redssionProperties.getConnectionPoolSize())
            .setConnectionMinimumIdleSize(redssionProperties.getConnectionMinimumIdleSize());

    if (StringUtils.isNotBlank(redssionProperties.getPassword())) {
      serverConfig.setPassword(redssionProperties.getPassword());
    }

    return Redisson.create(config);
  }
}
