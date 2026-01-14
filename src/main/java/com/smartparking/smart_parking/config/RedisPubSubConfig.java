package com.smartparking.smart_parking.config;

import com.smartparking.smart_parking.service.ParkingEventConsumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisPubSubConfig {

    @Bean
    ChannelTopic parkingEventsTopic() {
        return new ChannelTopic("parking-events");
    }

    @Bean
    @ConditionalOnProperty(name = "parking.consumer.enabled", havingValue = "true")
    MessageListenerAdapter parkingEventsListener(ParkingEventConsumer consumer) {
        return new MessageListenerAdapter(consumer, "handleMessage");
    }

    @Bean
    @ConditionalOnProperty(name = "parking.consumer.enabled", havingValue = "true")
    RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter parkingEventsListener,
            ChannelTopic parkingEventsTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(parkingEventsListener, parkingEventsTopic);
        return container;
    }
}
