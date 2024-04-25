package dev.magadiflo.producer.app.rest;

import dev.magadiflo.producer.app.stream.WikimediaStreamConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/wikimedia")
public class WikimediaController {

    private final WikimediaStreamConsumer wikimediaStreamConsumer;

    @GetMapping
    public void startPublishing() {
        this.wikimediaStreamConsumer.consumeStreamAndPublish();
    }

}
