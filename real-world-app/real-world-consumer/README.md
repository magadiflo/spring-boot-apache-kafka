# Consumer

---

## Dependencias

````xml
<!--Spring Boot 3.2.5-->
<!--Java 21-->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
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
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
````

## Configurando application.yml

El archivo de configuración contendrá las configuraciones necesarias para nuestra aplicación del tipo `consumer`.
Notar que el `value-deserializer` que estamos usando es `org.apache.kafka.common.serialization.StringDeserializer`,
similar a lo que el `producer` nos envía.

Ahora, dado que esta aplicación `consumer` usará el deserializador `StringDeserializer` no es necesario usar la
configuración `spring.json.trusted.packages: '*'` que usamos en las primeras aplicaciones, ya que no estamos
usando el deserializador `JSON`.

````yaml
spring:
  application:
    name: real-world-consumer

  kafka:
    consumer:
      bootstrap-servers: localhost:9092
      group-id: myGroup
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
````

## Agrega topic

Creamos una clase de configuración que nos permitirá crear el topic `wikimedia-stream`. Ahora, esto podríamos omitirlo,
ya que en la aplicación `real-world-producer` ya lo habíamos agregado y además ejecutado, por lo que ese topic ya
se encuentra creado en kafka. Según la documentación de Spring Boot **el bean `NewTopic` hace que se cree el topic en el
broker; `no es necesario si el topic ya existe`.**

````java

@Configuration
public class WikimediaTopicConfig {
    @Bean
    public NewTopic createTopic() {
        return TopicBuilder.name("wikimedia-stream").build();
    }
}
````

## Agrega Consumer

Crearemos la clase de servicio que se encargará de estar pendiente del topic `wikimedia-strem`.

````java

@Slf4j
@Service
public class WikimediaConsumer {

    @KafkaListener(topics = "wikimedia-stream", groupId = "myGroup")
    public void consumeMessage(String message) {
        log.info("Consumiendo mensaje desde el topic wikimedia-stream: {}", message);
    }
}
````

**IMPORTANTE**

El `groupId = "myGroup"`, es el nombre que utilizaremos para unirnos a un grupo de consumidores. Es importante que
este `groupId` sea el mismo que definimos en el `application.yml` en la sección de `consumer`.