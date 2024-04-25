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

## Creando el API Rest

Para poder enviar mensajes a kafka, crearemos un restController que hará uso de la clase KafkaProducer.

````java

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/messages")
public class MessageController {

    private final KafkaProducer kafkaProducer;

    @PostMapping
    public ResponseEntity<String> sendMessage(@RequestBody String message) {
        this.kafkaProducer.sendMessage(message);
        return ResponseEntity.ok("Mensaje agregado al topic exitosamente!");
    }
}
````

## Enviando primer mensaje

En esta sección ejecutamos nuestra aplicación:

````bash
Connected to the target VM, address: '127.0.0.1:64627', transport: 'socket'

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.5)

2024-04-25T10:52:46.297-05:00  INFO 1872 --- [kafka-demo] [           main] d.m.kafka.app.KafkaDemoApplication       : Starting KafkaDemoApplication using Java 21.0.1 with PID 1872 (M:\PROGRAMACION\DESARROLLO_JAVA_SPRING\02.youtube\18.bouali_ali\03.sprint_boot_3_apache_kafka\spring-boot-apache-kafka\kafka-demo\target\classes started by USUARIO in M:\PROGRAMACION\DESARROLLO_JAVA_SPRING\02.youtube\18.bouali_ali\03.sprint_boot_3_apache_kafka\spring-boot-apache-kafka)
...
2024-04-25T10:52:50.416-05:00  INFO 1872 --- [kafka-demo] [           main] o.a.k.clients.admin.AdminClientConfig    : AdminClientConfig values: 
	auto.include.jmx.reporter = true
	bootstrap.servers = [localhost:9092]
...
	ssl.truststore.password = null
	ssl.truststore.type = JKS

2024-04-25T10:52:50.868-05:00  INFO 1872 --- [kafka-demo] [           main] o.a.kafka.common.utils.AppInfoParser     : Kafka version: 3.6.2
2024-04-25T10:52:50.870-05:00  INFO 1872 --- [kafka-demo] [           main] o.a.kafka.common.utils.AppInfoParser     : Kafka commitId: c4deed513057c94e
2024-04-25T10:52:50.870-05:00  INFO 1872 --- [kafka-demo] [           main] o.a.kafka.common.utils.AppInfoParser     : Kafka startTimeMs: 1714060370864
2024-04-25T10:52:52.015-05:00  INFO 1872 --- [kafka-demo] [| adminclient-1] o.a.kafka.common.utils.AppInfoParser     : App info kafka.admin.client for adminclient-1 unregistered
2024-04-25T10:52:52.024-05:00  INFO 1872 --- [kafka-demo] [| adminclient-1] o.apache.kafka.common.metrics.Metrics    : Metrics scheduler closed
2024-04-25T10:52:52.025-05:00  INFO 1872 --- [kafka-demo] [| adminclient-1] o.apache.kafka.common.metrics.Metrics    : Closing reporter org.apache.kafka.common.metrics.JmxReporter
2024-04-25T10:52:52.025-05:00  INFO 1872 --- [kafka-demo] [| adminclient-1] o.apache.kafka.common.metrics.Metrics    : Metrics reporters closed
2024-04-25T10:52:52.063-05:00  INFO 1872 --- [kafka-demo] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path ''
2024-04-25T10:52:52.080-05:00  INFO 1872 --- [kafka-demo] [           main] d.m.kafka.app.KafkaDemoApplication       : Started KafkaDemoApplication in 6.952 seconds (process running for 9.376)
````

Si abrimos una línea de comando y nos posicionamos en el directorio de instalación de kafka ejecutando el siguiente
comando, veremos que aparece el topic `kafka-demo` que configuramos en la aplicación:

````bash
C:\kafka_2.13-3.7.0

$ .\bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092
__consumer_offsets
kafka-demo
quickstart-events
````

Ahora, probaremos en enviar mensajes a nuestro topic `kafka-demo` utilizando nuestra api rest. Como aún no hemos
creado el cliente java que consumirá los mensajes utilizaremos el cliente de consola que viene en apache kafka, tan
solo lo haremos para observar que nuestro mensaje fue enviado correctamente y está siendo leído sin problemas.

Enviando mensaje desde nuestro api rest:

````bash
$  curl -v -X POST -H "Content-Type: text/plain" -d "Enviando mi segundo mensaje a Apache Kafka" http://localhost:8080/api/v1/messages
>
< HTTP/1.1 200
< Content-Type: text/plain;charset=UTF-8
< Content-Length: 39
< Date: Thu, 25 Apr 2024 16:10:59 GMT
<
Mensaje agregado al topic exitosamente!* Connection
````

Nuestro cliente consumer de consola está suscriba al topic `kafka-demo` desde donde está escuchando todos los mensajes
que se envíen a él:

````bash
C:\kafka_2.13-3.7.0

$ .\bin\windows\kafka-console-consumer.bat --topic kafka-demo --from-beginning --bootstrap-server localhost:9092
Enviando mi primer mensaje a Apache Kafka
Enviando mi segundo mensaje a Apache Kafka
````