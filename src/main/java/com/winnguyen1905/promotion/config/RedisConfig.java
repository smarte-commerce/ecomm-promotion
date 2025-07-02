package com.winnguyen1905.promotion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
class RedisConfig {

  /**
   * Creates a {@link LettuceConnectionFactory} which is the default connection
   * factory used by Spring Data Redis in Spring Boot 3.x. Host, port and other
   * settings can be customized via standard <code>spring.data.redis.*</code>
   * properties in <code>application.yaml</code>.
   */
  @Bean
  RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(); // will pick up host/port from properties
  }

  /**
   * Configures a {@link RedisTemplate} with String serializers for keys and
   * values. This is suitable for locking because we store simple String keys
   * and values ("discount:lock:uuid" -&gt; "thread:timestamp:uuid").
   */
  @Bean
  RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    // Use String serialization for all keys/hashes to keep data human-readable
    StringRedisSerializer serializer = new StringRedisSerializer();
    template.setKeySerializer(serializer);
    template.setValueSerializer(serializer);
    template.setHashKeySerializer(serializer);
    template.setHashValueSerializer(serializer);

    template.afterPropertiesSet();
    return template;
  }
}
