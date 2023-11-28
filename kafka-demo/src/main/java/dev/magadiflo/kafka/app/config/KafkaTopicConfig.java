package dev.magadiflo.kafka.app.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic generateTopic() {
        // Creamos un nuevo topic, dejamos las configuraciones predeterminadas al de Kafka
        return TopicBuilder.name("magadiflo").build();
    }

}
