package dev.magadiflo.wikimedia.producer.app.stream;

import dev.magadiflo.wikimedia.producer.app.producer.WikimediaProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class WikimediaStreamConsumer {

    private final WebClient webClient;
    private final WikimediaProducer producer;


    public WikimediaStreamConsumer(WebClient.Builder webClientBuilder, WikimediaProducer producer) {
        this.webClient = webClientBuilder
                .baseUrl("https://stream.wikimedia.org/v2")
                .build();
        this.producer = producer;
    }

    public void consumeStreamAndPublish() {
        this.webClient.get()
                .uri("/stream/recentchange")
                .retrieve()
                .bodyToFlux(String.class)
                .subscribe(producer::sendMessage);
    }
}
