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