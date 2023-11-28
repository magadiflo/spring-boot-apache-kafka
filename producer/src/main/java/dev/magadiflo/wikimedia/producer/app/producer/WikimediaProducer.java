package dev.magadiflo.wikimedia.producer.app.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class WikimediaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String message) {
        log.info("Enviando mensaje al topic wikimedia-stream: {}", message);
        this.kafkaTemplate.send("wikimedia-stream", message);
    }
}
