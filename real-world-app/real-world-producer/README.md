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

## Creando Wikimedia Controller

Vamos a crear un controlador llamado `WikimediaController` con un endpoint. Básicamente, lo creamos para tener una
manera de activar el `producer`, es decir, podríamos haber colocado el
`this.wikimediaStreamConsumer.consumeStreamAndPublish()` al iniciar la aplicación en el bean `CommandLineRunner`, eso
haría que apenas se inicie la aplicación se empiece a ejecutar nuestro proyecto; pero en nuestro caso, creamos un
endpoint para activar el proceso cuando hagamos la llamada en el momento en el que queramos.

````java

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
````

## Probando Wikimedia Producer

Primero ejecutamos nuestra aplicación `real-world-producer`, luego levantaremos un cliente consumer de kafka para ver
los mensajes que van llegando al topic `wikimedia-stream`. Ahora, realizamos una petición al endpoint del controlador:

````bash
$ curl -v http://localhost:8080/api/v1/wikimedia | jq
>
< HTTP/1.1 200 OK
````

Vemos que el consumer de la consola va recibiendo los mensajes, tal como lo esperábamos.

**NOTA** Los mensajes recibidos son flujos constantes, no acaban, así que aquí solo pongo una parte.

````bash
C:\kafka_2.13-3.7.0

$ .\bin\windows\kafka-console-consumer.bat --topic wikimedia-stream --from-beginning --bootstrap-server localhost:9092
{"$schema":"/mediawiki/recentchange/1.0.0","meta":{"uri":"https://be.wikipedia.org/wiki/1948","request_id":"7e936910-96ee-418d-9858-541a8687c775","id":"b36a0621-4e62-44de-8f96-57b5dbfdfec0","dt":"2024-04-25T23:26:57Z","domain":"be.wikipedia.org","stream":"mediawiki.recentchange","topic":"eqiad.mediawiki.recentchange","partition":0,"offset":5050392750},"id":58757985,"type":"edit","namespace":0,"title":"1948","title_url":"https://be.wikipedia.org/wiki/1948","comment":"/* ðƒð░ð┤ðÀðÁÐû */","timestamp":1714087617,"user":"JerzyKundrat","bot":false,"notify_url":"https://be.wikipedia.org/w/index.php?diff=4719016&oldid=4707608","minor":false,"length":{"old":6274,"new":6615},"revision":{"old":4707608,"new":4719016},"server_url":"https://be.wikipedia.org","server_name":"be.wikipedia.org","server_script_path":"/w","wiki":"bewiki","parsedcomment":"<span dir=\"auto\"><span class=\"autocomment\"><a href=\"/wiki/1948#ðƒð░ð┤ðÀðÁÐû\" title=\"1948\">ÔåÆÔÇÄðƒð░ð┤ðÀðÁÐû</a></span></span>"}
````

