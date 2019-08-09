package com.panda.pay.config.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @Title:RedisConfig @Copyright: Copyright (c) 2018 @Description: <br>
 * @Company: panda-fintech @Created on 2018/8/15下午8:47
 *
 * @miaoxuehui@panda-fintech.com
 */
@Configuration
public class RedisConfig {
  @Autowired private RedisConnectionFactory redisConnectionFactory;

  @Bean(name = "stringRedisTemplate")
  public RedisTemplate<String, String> customStringRedisTemplate() {
    RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
    initStringRedisTemplate(redisTemplate, redisConnectionFactory);
    return redisTemplate;
  }

  @Bean(name = "objectRedisTemplate")
  public RedisTemplate<String, Object> customObjectRedisTemplate() {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    initObjectRedisTemplate(redisTemplate, redisConnectionFactory);
    return redisTemplate;
  }

  private void initStringRedisTemplate(
      RedisTemplate<String, String> redisTemplate, RedisConnectionFactory factory) {
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new StringRedisSerializer());
    redisTemplate.setConnectionFactory(factory);
  }

  private void initObjectRedisTemplate(
      RedisTemplate<String, Object> redisTemplate, RedisConnectionFactory factory) {
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setHashKeySerializer(new StringRedisSerializer());
    redisTemplate.setHashValueSerializer(new JdkSerializationRedisSerializer());
    redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
    redisTemplate.setConnectionFactory(factory);
  }

  /**
   * 实例化 HashOperations 对象,可以使用 Hash 类型操作
   *
   * @param redisTemplate
   * @return
   */
  @Bean
  public HashOperations<String, String, Object> hashOperations(
      RedisTemplate<String, Object> redisTemplate) {
    return redisTemplate.opsForHash();
  }

  /**
   * 实例化 ValueOperations 对象,可以使用 String 操作
   *
   * @param redisTemplate
   * @return
   */
  @Bean
  public ValueOperations<String, Object> valueOperations(
      RedisTemplate<String, Object> redisTemplate) {
    return redisTemplate.opsForValue();
  }

  /**
   * 实例化 ListOperations 对象,可以使用 List 操作
   *
   * @param redisTemplate
   * @return
   */
  @Bean
  public ListOperations<String, Object> listOperations(
      RedisTemplate<String, Object> redisTemplate) {
    return redisTemplate.opsForList();
  }

  /**
   * 实例化 SetOperations 对象,可以使用 Set 操作
   *
   * @param redisTemplate
   * @return
   */
  @Bean
  public SetOperations<String, Object> setOperations(RedisTemplate<String, Object> redisTemplate) {
    return redisTemplate.opsForSet();
  }

  /**
   * 实例化 ZSetOperations 对象,可以使用 ZSet 操作
   *
   * @param redisTemplate
   * @return
   */
  @Bean
  public ZSetOperations<String, Object> zSetOperations(
      RedisTemplate<String, Object> redisTemplate) {
    return redisTemplate.opsForZSet();
  }
}
