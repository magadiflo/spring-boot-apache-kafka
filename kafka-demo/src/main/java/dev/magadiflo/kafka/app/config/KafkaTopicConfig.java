package dev.magadiflo.kafka.app.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    // El bean NewTopic hace que se cree el topic en el broker; no es necesario si el topic ya existe.
    @Bean
    public NewTopic createTopic() {
        return TopicBuilder.name("kafka-demo").build();
    }
}
