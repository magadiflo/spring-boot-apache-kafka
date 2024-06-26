package dev.magadiflo.kafka.app.rest;

import dev.magadiflo.kafka.app.payload.Student;
import dev.magadiflo.kafka.app.producer.KafkaJsonProducer;
import dev.magadiflo.kafka.app.producer.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/messages")
public class MessageController {

    private final KafkaProducer kafkaProducer;
    private final KafkaJsonProducer kafkaJsonProducer;

    @PostMapping
    public ResponseEntity<String> sendMessage(@RequestBody String message) {
        this.kafkaProducer.sendMessage(message);
        return ResponseEntity.ok("Mensaje agregado al topic exitosamente!");
    }

    @PostMapping(path = "/json")
    public ResponseEntity<String> sendJsonMessage(@RequestBody Student student) {
        this.kafkaJsonProducer.sendMessage(student);
        return ResponseEntity.ok("Estudiante (JSON) agregado al topic exitosamente!");
    }

}
