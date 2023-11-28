package dev.magadiflo.kafka.app.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaProducer {

    /**
     * KafkaTemplate<String, String>, ambos son String ya que fue lo que definimos en el archivo de configuración
     * application.yml en la sección de producer:
     * key-serializer: org.apache.kafka.common.serialization.StringSerializer
     * value-serializer: org.apache.kafka.common.serialization.StringSerializer
     */
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String message) {
        log.info("Enviando mensaje al topic magadiflo: {}", message);
        this.kafkaTemplate.send("magadiflo", message);
    }
}
