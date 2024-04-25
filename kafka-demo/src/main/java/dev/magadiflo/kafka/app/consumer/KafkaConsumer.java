package dev.magadiflo.kafka.app.consumer;

import dev.magadiflo.kafka.app.payload.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumer {

    //@KafkaListener(topics = "kafka-demo", groupId = "myGroup")
    public void consumeMessage(String message) {
        log.info("Mensaje recibido desde el topic kafka-demo: {}", message);
    }

    @KafkaListener(topics = "kafka-demo", groupId = "myGroup")
    public void consumeJsonMessage(Student student) {
        log.info("Consumiendo Student desde el topic kafka-demo: {}", student.toString());
    }
}
