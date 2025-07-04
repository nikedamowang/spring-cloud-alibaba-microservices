package com.cloudDemo.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext
                .<String, Object>newSerializationContext()
                .key(new StringRedisSerializer())
                .hashKey(new StringRedisSerializer())
                .value(new GenericJackson2JsonRedisSerializer())
                .hashValue(new GenericJackson2JsonRedisSerializer())
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}
