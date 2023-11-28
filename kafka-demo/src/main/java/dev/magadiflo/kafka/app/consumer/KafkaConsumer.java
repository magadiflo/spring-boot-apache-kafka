package dev.magadiflo.kafka.app.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumer {

    //groupId = "myGroup", el que configuramos en el application.yml
    @KafkaListener(topics = "magadiflo", groupId = "myGroup")
    public void consumeMessage(String message) {
      log.info("Consumiendo mensaje desde el topic magadiflo: {}", message);
    }

}
