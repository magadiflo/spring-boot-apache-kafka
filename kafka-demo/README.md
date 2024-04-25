# Kafka Demo

Este proyecto `kafka-demo` será al mismo tiempo un `productor` y un `consumidor`.

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

## Configure Kafka producer and consumer

Como esta aplicación se comportará al mismo tiempo como un `producer` y un `consumer`, entonces necesitamos agregar en
el `application.yml` las configuraciones para ambas formas:

````yml
spring:
  application:
    name: kafka-demo

  kafka:
    consumer:
      bootstrap-servers: localhost:9092
      group-id: myGroup
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer

    producer:
      bootstrap-servers: localhost:9092
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
````

**DONDE**

- `bootstrap-servers`, uno o más brokers de kafka para conectarse al iniciar.
- `group-id`, un nombre que se utiliza para unirse a un grupo de consumidores.
- `auto-offset-reset: earliest`, se refiere a la configuración de Kafka para determinar qué hacer cuando un consumidor
  intenta leer desde un offset (posición) que ya no está disponible o si es la primera vez que el consumidor se conecta
  a un tema `(topic)`. `Earliest` indica que si el offset no es válido o es un nuevo consumidor, comenzará a leer desde
  el comienzo del topic, es decir, el primer mensaje disponible. Digamos, que este atributo es similar a la bandera que
  usamos cuando ejecutamos el consumer en consola `--from-beginning`.

**IMPORTANTE**
> Debemos tener en cuenta en el `producer` el tipo de la clase para serializar tanto la key como el value. Ese mismo
> tipo de clase usaremos en el consumer, obviamente para `Deserealizar`. En nuestro caso, por ejemplo, estamos usando
> como un `key-serializer` para el `producer` la clase `StringSerializer`, eso significa que en el `key-deserializer`
> del `consumer` usaremos el `StringDeserializer`.

## Configure Kafka Topic

Dentro del paquete `/config` creamos nuestra clase de configuración `KafkaTopicConfig` donde definiremos nuestro `@Bean`
que se encargará de crear un `topic` llamado `kafka-demo`.

**El bean `NewTopic` hace que se cree el topic en el broker; `no es necesario si el topic ya existe`.**

````java

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic createTopic() {
        return TopicBuilder.name("kafka-demo").build();
    }
}
````

## Create a Kafka Producer

Crearemos una clase de componente que definirá un método e inyectará la clase `KafkaTemplate` para enviar mensajes
al topic `kafka-demo`.

````java

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String message) {
        log.info("Enviando mensaje al topic kafka-demo: {}", message);
        kafkaTemplate.send("kafka-demo", message);
    }
}
````

**DONDE**

- `KafkaTemplate<String, String>`, el primer String representa el tipo de dato de la clave y el segundo el tipo de dato
  del valor. En nuestro caso, esa definición es coherente con lo que hemos definido en la sección `producer`
  del `application.yml`, donde hemos definido los siguientes
  atributos `key-serializer: org.apache.kafka.common.serialization.StringSerializer` y
  el `value-serializer: org.apache.kafka.common.serialization.StringSerializer`