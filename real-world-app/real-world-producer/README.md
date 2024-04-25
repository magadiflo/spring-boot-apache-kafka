# Producer

---

## Dependencias

````xml
<!--Spring Boot 3.2.5-->
<!--Java 21-->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>

    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.projectreactor</groupId>
        <artifactId>reactor-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
````

## Configurando Producer

En el `application.yml` agregamos las configuraciones corresponientes a un producer de kafka:

````yml
spring:
  application:
    name: real-world-producer

  kafka:
    producer:
      bootstrap-servers: localhost:9092
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
````

Notar que en el `value-serializer` estamos usando el serializador
`org.apache.kafka.common.serialization.StringSerializer`, dado que no queremos perder el tiempo creando una clase que
se adapte a los datos que vienen, es decir, los datos que recibiremos los trataremos como `Strings` y no como objetos
`JSON` si es que utilizáramos el serializador de `JSON`.

## Creando el topic a enviar los registros

Podemos crear una clase de configuración y agregar el bean que nos permitirá crear un topic. A ese topic le llamaremos
`wikimedia-stream`.

````java

@Configuration
public class WikimediaTopicConfig {
    @Bean
    public NewTopic createTopic() {
        return TopicBuilder.name("wikimedia-stream").build();
    }
}
````

**IMPORTANTE**
> **El bean `NewTopic` hace que se cree el topic en el broker; `no es necesario si el topic ya existe`.**

## Configurando WebClient (WebFlux)

Ahora configuraremos un `WebClient` de `WebFlux` para hacer la llamada al endPoint
reactivo `https://stream.wikimedia.org/v2/stream/recentchange` desde donde obtendremos flujos de datos.

````java

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
````

## Creando el Wikimedia Kafka Producer

Creamos una clase de servicio que será nuestro productor con el que enviaremos mensajes a kafka:

````java

@RequiredArgsConstructor
@Service
public class WikimediaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String message) {
        this.kafkaTemplate.send("wikimedia-stream", message);
    }
}
````

## Creando el Wikimedia Stream Consumer

Crearemos la clase de servicio que llamará a la url `https://stream.wikimedia.org/v2/stream/recentchange` desde donde
se obtendrá los datos usando WebClient de WebFlux.

````java

@Slf4j
@Service
public class WikimediaStreamConsumer {

    private final WebClient webClient;
    private final WikimediaProducer wikimediaProducer;

    public WikimediaStreamConsumer(WebClient.Builder webClientBuilder, WikimediaProducer wikimediaProducer) {
        this.webClient = webClientBuilder
                .baseUrl("https://stream.wikimedia.org/v2")
                .build();
        this.wikimediaProducer = wikimediaProducer;
    }

    public void consumeStreamAndPublish() {
        this.webClient.get()
                .uri("/stream/recentchange")
                .retrieve()
                .bodyToFlux(String.class)
                .subscribe(
                        next -> {
                            log.info("{}", next);
                            this.wikimediaProducer.sendMessage(next);
                        },
                        error -> log.error("error: {}", error.getMessage()),
                        () -> log.info("¡Flux completado!")
                );
    }
}
````

**DONDE**

- `Flux`, es un publisher, por lo tanto, un observable. Emite una secuencia asíncrona de 0 a N elementos (onNext) y
  termina con una señal (onComplete). También puede terminar con una seña (onError).
- En el código anterior estamos creando un Flux de Strings.
- No sucederá nada hasta que nos subscribamos `.subscribe()`.
- Cuando hacemos el `.subscribe()` empezamos a observar, por lo tanto, se trata de un consumidor, un `Observer` que
  consume cada elemento que emite el `Observable`. En nuestro caso, la fuente observable es el endpoint reactivo, dado
  que nos está enviando constantemente datos. Ahora, con el `.subscribe()` no solo puede consumir, sino también puede
  manejar cualquier tipo de error que pueda ocurrir. Finalmente, si la emisión finaliza, se ejecuta el tercer parámetro
  que corresponde al evento `onComplete`.

