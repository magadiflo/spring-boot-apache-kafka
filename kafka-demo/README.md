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
  intenta leer desde un offset (posición) que ya no está disponible o **si es la primera vez que el consumidor se
  conecta a un tema** `(topic)`. `Earliest` indica que si el offset no es válido o es un nuevo consumidor, **comenzará a
  leer desde el comienzo del topic**, es decir, el primer mensaje disponible. A continuación, veremos más detalles
  conceptuales de esta configuración junto con otra muy relacionada.

#### [Configuración de la gestión de Offset](https://docs.confluent.io/platform/current/clients/consumer.html#offset-management-configuration)

Hay dos configuraciones principales para la gestión de `offset`; si el `auto-commit` está habilitado y la política
de reinicio del `offset`.

- `enable.auto.commit`, esta configuración habilita el `auto-commit (que es el valor predeterminado)`, lo que significa
  que **el consumidor automáticamente confirma los offsets periódicamente** según el intervalo establecido por
  `auto.commit.interval.ms`. El intervalo predeterminado es de 5 segundos.<br><br>
  En nuestro caso, podríamos haber configurado en nuestro consumidor de Spring Boot
  el `spring.kafka.consumer.enable-auto-commit=true`, pero **por defecto ya viene habilitado (true)**. Si está en true,
  el consumidor automáticamente confirma los offsets en Kafka. Esto significa que **Kafka puede recordar dónde el
  consumidor dejó de leer.** Si es false, debes gestionar manualmente cuándo se confirma el offset.


- `auto.offset.reset`, define el comportamiento del consumidor cuando no hay una posición confirmada **(lo cual ocurre
  cuando el grupo se inicializa por primera vez)** o cuando un offset está fuera de rango. Puedes elegir entre
  restablecer la posición al offset `earliest` (más temprano: comienza a leer desde el primer registro disponible en el
  topic) o al offset `latest` (más reciente: comienza a leer desde el último registro) **(el valor
  predeterminado)**. También puedes seleccionar `none` (ninguno: levanta un error si el offset no se encuentra) si
  prefieres establecer el offset inicial por ti mismo y estás dispuesto a manejar manualmente los errores de fuera de
  rango.

Considero que es importante colocar aquí un ejemplo para ver en ejecución la configuración de la gestión de Offset.
Únicamente enfoquémonos en el comportamiento, más adelante veremos cómo es que vamos a construir estos ejemplos
mostrados, aquí únicamente me interesa comprender cómo es que el consumidor está leyendo los registros.

**EL ESCENARIO:** Tenemos una aplicación en consola que será nuestro `producer`, esta aplicación está ejecutándose,
mienstras que nuestra aplicación en Spring Boot, que será nuestro `consumer` aún no se ha ejecutado. Utilizando el
`producer` enviamos los primeros 3 mensajes: 1. Probando el inicio.., 2. Un saludo... 3. hola papi. A continuación
levantamos el `consumer`, quien leerá los tres mensajes enviados por el `producer` (esos tres mensajes leídos no los
estoy mostrando, pero sí los ha consumido). Ahora, dejamos el `producer` levantado, mientras que el `consumer` lo
detenemos. Teniendo ambas aplicaciones en esos estados, utilizamos el `producer` para enviar cinco mensajes más.

````bash
C:\kafka_2.13-3.7.0

$ .\bin\windows\kafka-console-producer.bat --topic wikimedia-stream --bootstrap-server localhost:9092
>1. Probando el inicio de los mensajes!
>2. Un saludo para el peru
>3. hola papi
>4. mi vida mi mundo eres tu
>5. mi agua que calma mi sed
>6. de dia tu, calor seras
>7. de noche tu, la luz seras
>8. iluminando mi camino
>
````

Ahora, volvemos a levantar la aplicación de spring boot `consumer` y vemos que ahora **únicamente está consumiendo
los mensajes nuevos, es decir, mensajes posteriores a los mensajes que ya había consumido** y eso es producto de que
tenemos la configuración por defecto (implícito) `spring.kafka.consumer.enable-auto-commit=true`.

**Esto sugiere que Kafka está recordando los offsets y, al reiniciar el consumidor, este comienza desde el último offset
confirmado, evitando así la relectura de mensajes previamente consumidos.** Si el auto-commit está habilitado, el
consumidor confirmará automáticamente los offsets en intervalos regulares, evitando que se reconsuman mensajes al
reiniciar la aplicación. Esta configuración es común y facilita el manejo de offsets porque Kafka lleva el seguimiento
por ti.

````bash
mcat.TomcatWebServer  : Tomcat started on port 8081 (http) with context path ''
mer.ConsumerConfig    : ConsumerConfig values: 
...
.ConsumerCoordinator  : [Consumer clientId=consumer-myGroup-1, groupId=myGroup] Adding newly assigned partitions: wikimedia-stream-0
.ConsumerCoordinator  : [Consumer clientId=consumer-myGroup-1, groupId=myGroup] Setting offset for partition wikimedia-stream-0 to the committed offset FetchPosition{offset=617, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[DESKTOP-EGDL8Q6:9092 (id: 0 rack: null)], epoch=0}}
eListenerContainer    : myGroup: partitions assigned: [wikimedia-stream-0]
WikimediaConsumer     : Consumiendo mensaje desde el topic wikimedia-stream: 4. mi vida mi mundo eres tu
WikimediaConsumer     : Consumiendo mensaje desde el topic wikimedia-stream: 5. mi agua que calma mi sed
WikimediaConsumer     : Consumiendo mensaje desde el topic wikimedia-stream: 6. de dia tu, calor seras
WikimediaConsumer     : Consumiendo mensaje desde el topic wikimedia-stream: 7. de noche tu, la luz seras
WikimediaConsumer     : Consumiendo mensaje desde el topic wikimedia-stream: 8. iluminando mi camino
````

Notemos que cuando reiniciamos el `consumer`, el valor del `auto-offset-reset` no se aplica en
este caso, ya que **este parámetro se usa solo cuando el offset del consumer no es válido o no está disponible**, por
ejemplo, `porque es un nuevo grupo` o porque los offsets anteriores han expirado o han sido eliminados.

Finalmente, **¿qué pasa si a nuestro grupo de consumidores le agregamos un nuevo consumidor?**, en ese escenario si
el grupo de consumidores ya tiene offsets `confirmados (commit)` para las particiones asignadas, el nuevo consumidor
debería continuar desde esos offsets y **no re-consumir mensajes previamente confirmados.** Si hay un offset confirmado
para la partición, **Kafka usará ese como el punto de partida para el nuevo consumidor.**

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

## Create a Kafka Consumer

Ahora, crearemos nuestro cliente java que se comportará como un `consumer`. Este consumidor se suscribirá al
topic `kafka-demo` para que esté pendiente de él a medida que se envíen mensajes a dicho topic.

````java

@Slf4j
@Service
public class KafkaConsumer {

    @KafkaListener(topics = "kafka-demo", groupId = "myGroup")
    public void consumeMessage(String message) {
        log.info("Mensaje recibido desde el topic kafka-demo: {}", message);
    }
}
````

**DONDE**

- `@KafkaListener`, anotación que marca un método como destino de un listener de mensajes de kafka sobre
  los `topics` especificados.
- `topics = "kafka-demo"`, topic al que nos vamos a suscribir y estar pendientes de los mensajes que le lleguen.
- `groupId = "myGroup"`, nombre que utilizaremos para unirnos a un grupo de consumidores. Es importante que
  este `groupId` sea el mismo que definimos en el `application.yml` en la sección de `consumer`.

## Ejecutando aplicación Producer y Consumer

Si ejecutamos la aplicación de Spring Boot veremos en consola lo siguiente:

````bash
"C:\Program Files\Java\jdk-21.0.1\bin\java.exe" -agentlib:jdwp=transport=dt_socket,address=127.0.0.1:65406,suspend=y,server=n -javaagent:C:\Users\USUARIO\AppData\Local\JetBrains\IdeaIC2024.1\captureAgent\debugger-agent.jar=file:/C:/Users/USUARIO/AppData/Local/Temp/capture.props -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath "M:\PROGRAMACION\DESARROLLO_JAVA_SPRING\02.youtube\18.bouali_ali\03.sprint_boot_3_apache_kafka\spring-boot-apache-kafka\kafka-demo\target\classes;C:\Users\USUARIO\.m2\repository\org\springframework\boot\spring-boot-starter-web\3.2.5\spring-boot-starter-web-3.2.5.jar;C:\Users\USUARIO\.m2\repository\org\springframework\boot\spring-boot-starter\3.2.5\spring-boot-starter-3.2.5.jar;C:\Users\USUARIO\.m2\repository\org\springframework\boot\spring-boot\3.2.5\spring-boot-3.2.5.jar;C:\Users\USUARIO\.m2\repository\org\springframework\boot\spring-boot-autoconfigure\3.2.5\spring-boot-autoconfigure-3.2.5.jar;C:\Users\USUARIO\.m2\repository\org\springframework\boot\spring-boot-starter-logging\3.2.5\spring-boot-starter-logging-3.2.5.jar;C:\Users\USUARIO\.m2\repository\org\apache\logging\log4j\log4j-to-slf4j\2.21.1\log4j-to-slf4j-2.21.1.jar;C:\Users\USUARIO\.m2\repository\org\apache\logging\log4j\log4j-api\2.21.1\log4j-api-2.21.1.jar;C:\Users\USUARIO\.m2\repository\org\slf4j\jul-to-slf4j\2.0.13\jul-to-slf4j-2.0.13.jar;C:\Users\USUARIO\.m2\repository\jakarta\annotation\jakarta.annotation-api\2.1.1\jakarta.annotation-api-2.1.1.jar;C:\Users\USUARIO\.m2\repository\org\yaml\snakeyaml\2.2\snakeyaml-2.2.jar;C:\Users\USUARIO\.m2\repository\org\springframework\boot\spring-boot-starter-json\3.2.5\spring-boot-starter-json-3.2.5.jar;C:\Users\USUARIO\.m2\repository\com\fasterxml\jackson\core\jackson-databind\2.15.4\jackson-databind-2.15.4.jar;C:\Users\USUARIO\.m2\repository\com\fasterxml\jackson\core\jackson-annotations\2.15.4\jackson-annotations-2.15.4.jar;C:\Users\USUARIO\.m2\repository\com\fasterxml\jackson\core\jackson-core\2.15.4\jackson-core-2.15.4.jar;C:\Users\USUARIO\.m2\repository\com\fasterxml\jackson\datatype\jackson-datatype-jdk8\2.15.4\jackson-datatype-jdk8-2.15.4.jar;C:\Users\USUARIO\.m2\repository\com\fasterxml\jackson\datatype\jackson-datatype-jsr310\2.15.4\jackson-datatype-jsr310-2.15.4.jar;C:\Users\USUARIO\.m2\repository\com\fasterxml\jackson\module\jackson-module-parameter-names\2.15.4\jackson-module-parameter-names-2.15.4.jar;C:\Users\USUARIO\.m2\repository\org\springframework\boot\spring-boot-starter-tomcat\3.2.5\spring-boot-starter-tomcat-3.2.5.jar;C:\Users\USUARIO\.m2\repository\org\apache\tomcat\embed\tomcat-embed-core\10.1.20\tomcat-embed-core-10.1.20.jar;C:\Users\USUARIO\.m2\repository\org\apache\tomcat\embed\tomcat-embed-el\10.1.20\tomcat-embed-el-10.1.20.jar;C:\Users\USUARIO\.m2\repository\org\apache\tomcat\embed\tomcat-embed-websocket\10.1.20\tomcat-embed-websocket-10.1.20.jar;C:\Users\USUARIO\.m2\repository\org\springframework\spring-web\6.1.6\spring-web-6.1.6.jar;C:\Users\USUARIO\.m2\repository\org\springframework\spring-beans\6.1.6\spring-beans-6.1.6.jar;C:\Users\USUARIO\.m2\repository\org\springframework\spring-webmvc\6.1.6\spring-webmvc-6.1.6.jar;C:\Users\USUARIO\.m2\repository\org\springframework\spring-aop\6.1.6\spring-aop-6.1.6.jar;C:\Users\USUARIO\.m2\repository\org\springframework\spring-expression\6.1.6\spring-expression-6.1.6.jar;C:\Users\USUARIO\.m2\repository\org\springframework\kafka\spring-kafka\3.1.4\spring-kafka-3.1.4.jar;C:\Users\USUARIO\.m2\repository\org\springframework\spring-context\6.1.6\spring-context-6.1.6.jar;C:\Users\USUARIO\.m2\repository\org\springframework\spring-messaging\6.1.6\spring-messaging-6.1.6.jar;C:\Users\USUARIO\.m2\repository\org\springframework\spring-tx\6.1.6\spring-tx-6.1.6.jar;C:\Users\USUARIO\.m2\repository\org\springframework\retry\spring-retry\2.0.5\spring-retry-2.0.5.jar;C:\Users\USUARIO\.m2\repository\org\apache\kafka\kafka-clients\3.6.2\kafka-clients-3.6.2.jar;C:\Users\USUARIO\.m2\repository\com\github\luben\zstd-jni\1.5.5-1\zstd-jni-1.5.5-1.jar;C:\Users\USUARIO\.m2\repository\org\lz4\lz4-java\1.8.0\lz4-java-1.8.0.jar;C:\Users\USUARIO\.m2\repository\org\xerial\snappy\snappy-java\1.1.10.5\snappy-java-1.1.10.5.jar;C:\Users\USUARIO\.m2\repository\org\slf4j\slf4j-api\2.0.13\slf4j-api-2.0.13.jar;C:\Users\USUARIO\.m2\repository\io\micrometer\micrometer-observation\1.12.5\micrometer-observation-1.12.5.jar;C:\Users\USUARIO\.m2\repository\io\micrometer\micrometer-commons\1.12.5\micrometer-commons-1.12.5.jar;C:\Users\USUARIO\.m2\repository\com\google\code\findbugs\jsr305\3.0.2\jsr305-3.0.2.jar;C:\Users\USUARIO\.m2\repository\org\projectlombok\lombok\1.18.32\lombok-1.18.32.jar;C:\Users\USUARIO\.m2\repository\org\springframework\spring-core\6.1.6\spring-core-6.1.6.jar;C:\Users\USUARIO\.m2\repository\org\springframework\spring-jcl\6.1.6\spring-jcl-6.1.6.jar;C:\Users\USUARIO\.m2\repository\ch\qos\logback\logback-core\1.4.14\logback-core-1.4.14.jar;C:\Users\USUARIO\.m2\repository\ch\qos\logback\logback-classic\1.4.14\logback-classic-1.4.14.jar;C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2023.3\lib\idea_rt.jar" dev.magadiflo.kafka.app.KafkaDemoApplication
Connected to the target VM, address: '127.0.0.1:65406', transport: 'socket'

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.5)

INFO 13668 --- [kafka-demo] [           main] d.m.kafka.app.KafkaDemoApplication       : Starting KafkaDemoApplication using Java 21.0.1 with PID 13668 (M:\PROGRAMACION\DESARROLLO_JAVA_SPRING\02.youtube\18.bouali_ali\03.sprint_boot_3_apache_kafka\spring-boot-apache-kafka\kafka-demo\target\classes started by USUARIO in M:\PROGRAMACION\DESARROLLO_JAVA_SPRING\02.youtube\18.bouali_ali\03.sprint_boot_3_apache_kafka\spring-boot-apache-kafka)
INFO 13668 --- [kafka-demo] [           main] d.m.kafka.app.KafkaDemoApplication       : No active profile set, falling back to 1 default profile: "default"
...
INFO 13668 --- [kafka-demo] [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-myGroup-1, groupId=myGroup] Discovered group coordinator DESKTOP-EGDL8Q6:9092 (id: 2147483647 rack: null)
INFO 13668 --- [kafka-demo] [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-myGroup-1, groupId=myGroup] (Re-)joining group
INFO 13668 --- [kafka-demo] [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-myGroup-1, groupId=myGroup] Request joining group due to: need to re-join with the given member-id: consumer-myGroup-1-f777573a-5c50-4218-96e2-137ac4ea9035
INFO 13668 --- [kafka-demo] [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-myGroup-1, groupId=myGroup] Request joining group due to: rebalance failed due to 'The group member needs to have a valid member id before actually entering a consumer group.' (MemberIdRequiredException)
INFO 13668 --- [kafka-demo] [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-myGroup-1, groupId=myGroup] (Re-)joining group
INFO 13668 --- [kafka-demo] [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-myGroup-1, groupId=myGroup] Successfully joined group with generation Generation{generationId=1, memberId='consumer-myGroup-1-f777573a-5c50-4218-96e2-137ac4ea9035', protocol='range'}
INFO 13668 --- [kafka-demo] [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-myGroup-1, groupId=myGroup] Finished assignment for group at generation 1: {consumer-myGroup-1-f777573a-5c50-4218-96e2-137ac4ea9035=Assignment(partitions=[kafka-demo-0])}
INFO 13668 --- [kafka-demo] [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-myGroup-1, groupId=myGroup] Successfully synced group in generation Generation{generationId=1, memberId='consumer-myGroup-1-f777573a-5c50-4218-96e2-137ac4ea9035', protocol='range'}
INFO 13668 --- [kafka-demo] [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-myGroup-1, groupId=myGroup] Notifying assignor about the new Assignment(partitions=[kafka-demo-0])
INFO 13668 --- [kafka-demo] [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-myGroup-1, groupId=myGroup] Adding newly assigned partitions: kafka-demo-0
INFO 13668 --- [kafka-demo] [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-myGroup-1, groupId=myGroup] Found no committed offset for partition kafka-demo-0
INFO 13668 --- [kafka-demo] [ntainer#0-0-C-1] o.a.k.c.c.internals.SubscriptionState    : [Consumer clientId=consumer-myGroup-1, groupId=myGroup] Resetting offset for partition kafka-demo-0 to position FetchPosition{offset=0, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[DESKTOP-EGDL8Q6:9092 (id: 0 rack: null)], epoch=0}}.
INFO 13668 --- [kafka-demo] [ntainer#0-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : myGroup: partitions assigned: [kafka-demo-0]
INFO 13668 --- [kafka-demo] [ntainer#0-0-C-1] d.m.kafka.app.consumer.KafkaConsumer     : Mensaje recibido desde el topic kafka-demo: Enviando mi primer mensaje a Apache Kafka
INFO 13668 --- [kafka-demo] [ntainer#0-0-C-1] d.m.kafka.app.consumer.KafkaConsumer     : Mensaje recibido desde el topic kafka-demo: Enviando mi segundo mensaje a Apache Kafka
````

Observamos que estamos recibiendo lo dos mensajes enviados anteriormente, esto es gracias que en la configuración
del `application.yml` definimos la propiedad `auto-offset-reset: earliest`.

Ahora, usando nuestra api rest, enviamos un tercer mensaje:

````bash
$ curl -v -X POST -H "Content-Type: text/plain" -d "Enviando mi tercer mensaje a Apache Kafka" http://localhost:8080/api/v1/messages
>
< HTTP/1.1 200
< Content-Type: text/plain;charset=UTF-8
< Content-Length: 39
< Date: Thu, 25 Apr 2024 16:33:20 GMT
<
Mensaje agregado al topic exitosamente!
````

En consola del IDE veremos que el mensaje se ha recibido exitosamente:

````bash
INFO 13668 --- [kafka-demo] [nio-8080-exec-2] o.a.k.clients.producer.KafkaProducer     : [Producer clientId=producer-1] Instantiated an idempotent producer.
INFO 13668 --- [kafka-demo] [nio-8080-exec-2] o.a.kafka.common.utils.AppInfoParser     : Kafka version: 3.6.2
INFO 13668 --- [kafka-demo] [nio-8080-exec-2] o.a.kafka.common.utils.AppInfoParser     : Kafka commitId: c4deed513057c94e
INFO 13668 --- [kafka-demo] [nio-8080-exec-2] o.a.kafka.common.utils.AppInfoParser     : Kafka startTimeMs: 1714062800220
INFO 13668 --- [kafka-demo] [ad | producer-1] org.apache.kafka.clients.Metadata        : [Producer clientId=producer-1] Cluster ID: -lvYjMq4SSePgOrgi4naQQ
INFO 13668 --- [kafka-demo] [ad | producer-1] o.a.k.c.p.internals.TransactionManager   : [Producer clientId=producer-1] ProducerId set to 1001 with epoch 0
INFO 13668 --- [kafka-demo] [ntainer#0-0-C-1] d.m.kafka.app.consumer.KafkaConsumer     : Mensaje recibido desde el topic kafka-demo: Enviando mi tercer mensaje a Apache Kafka
````

---

# Kafka para Objetos JSON

---

## Configurando Kafka para objetos JSON: Serialización y Deserialización

En las secciones anteriores hemos estamos enviando y recibiendo "Strings" a nuestro Topic de kafka, pero ahora, nos
surge la necesidad de enviar objetos. **¿Cómo lo podríamos hacer?**

Bueno, primero hay que tener en cuenta que Apache Kafka almacena y transporta datos en formato de bit, por lo que hay
una gran cantidad de archivos integrados serializadores y deserializadores, pero ninguno incluye para JSON. **La razón
por la que `Apache Kafka` no proporciona un serializador JSON es para evitar imponer un formado de serialización
específico a los usuarios**, por lo que kafka está diseñado para ser independiente de los datos utilizados por
productores y consumidores, lo que permite la flexibilidad al acomodar varias estructuras de datos y formatos de
serialización.

Por otro lado, `Spring for Apache Kafka` creó el serializador: `JsonSerializer` y el `JsonDeserializer` que podemos
usar para **convertir objetos java hacia json y viceversa.**.

En ese sentido, modificaremos el archivo `application.yml` definiendo el nuevo `value-deserializer` y `value-serialize`
para el `consumer` y `producer` respectivamente:

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
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer

    producer:
      bootstrap-servers: localhost:9092
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
````

Ahora, crearemos la clase `Student` que será como nuestro payload a enviar con el producer y al mismo tiempo lo usaremos
para recibirlo con el consumer:

````java

@ToString
@Getter
@Setter
public class Student {
    private int id;
    private String firstname;
    private String lastname;
}
````

## Crea un Kafka Json Producer

Creamos una clase de servicio que será la encargada de enviar nuestros mensajes hacia kafka, pero en esta oportunidad
los mensajes será objetos java que serán convertidas a JSON:

````java

@RequiredArgsConstructor
@Service
public class KafkaJsonProducer {

    private final KafkaTemplate<String, Student> kafkaTemplate;

    public void sendMessage(Student student) {
        Message<Student> message = MessageBuilder
                .withPayload(student)
                .setHeader(KafkaHeaders.TOPIC, "kafka-demo")
                .build();
        this.kafkaTemplate.send(message);
    }
}
````

**DONDE**

- `Message`, representación de mensaje genérico con encabezados y cuerpo.
- `withPayload(student)`, crea un nuevo mensaje con el payload dado.
- `setHeader()`, establezca el valor para el nombre del encabezado dado. Si el valor proporcionado es nulo, se eliminará
  el encabezado.
- `KafkaHeaders.TOPIC`, es una constante que representa el nombre del encabezado que Kafka usa para identificar a qué
  tópico se debe enviar el mensaje. Al agregar este encabezado al mensaje, estás indicando explícitamente a qué tópico
  debe ser enviado.
- `kafka-demo`, esto significa que cuando envíes el mensaje, Kafka sabrá que debe ser publicado en el tópico llamado
  `kafka-demo`.

## Modificando el controlador MessageController

Ajustamos un nuevo endpoint en nuestro controlador que nos permitirá recibir un objeto `Student` para luego ser enviado
a la clase `KafkaJsonProducer`.

````java

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/messages")
public class MessageController {

    /* another code */
    private final KafkaJsonProducer kafkaJsonProducer;

    /* End-point that send string */

    @PostMapping(path = "/json")
    public ResponseEntity<String> sendJsonMessage(@RequestBody Student student) {
        this.kafkaJsonProducer.sendMessage(student);
        return ResponseEntity.ok("Estudiante (JSON) agregado al topic exitosamente!");
    }
}
````

## Probando clase Json Producer

Antes de ejecutar la aplicación, vamos a realizar una modificación a la clase `KafkaConsumer`, dado que esta clase
está esperando recibir mensajes del Topic pero como `String`, mientras que, la prueba que haremos requiere que los
consumidores esperen recibir un objeto JSON del tipo `Student`, así que procederemos a comentar la anotación
`//@KafkaListener(topics = "kafka-demo", groupId = "myGroup")`.

**NOTA**
> Si no comentamos dicha línea, al ejecutar nuestra aplicación y enviar mensajes al endpoint de json obtendremos muchos
> errores, así que es importante comentarlo.

````java

@Slf4j
@Service
public class KafkaConsumer {

    //@KafkaListener(topics = "kafka-demo", groupId = "myGroup")
    public void consumeMessage(String message) {
        log.info("Mensaje recibido desde el topic kafka-demo: {}", message);
    }
}
````

Enviamos las peticiones http al endpoint que admite un objeto json:

````bash
$ curl -v -X POST -H "Content-Type: application/json" -d "{\"id\": 1, \"firstname\": \"Lesly\", \"lastname\": \"Aguila\"}" http://localhost:8080/api/v1/messages/json
>
< HTTP/1.1 200
< Content-Type: text/plain;charset=UTF-8
< Content-Length: 49
< Date: Thu, 25 Apr 2024 17:43:41 GMT
<
Estudiante (JSON) agregado al topic exitosamente!
````

En consola, abrimos un cliente consumer de kafka para ver los mensajes que enviaremos.

````bash
C:\kafka_2.13-3.7.0

$ .\bin\windows\kafka-console-consumer.bat --topic kafka-demo --from-beginning --bootstrap-server localhost:9092
Enviando mi primer mensaje a Apache Kafka
Enviando mi segundo mensaje a Apache Kafka
Enviando mi tercer mensaje a Apache Kafka
{"id":1,"firstname":"Martin","lastname":"Torres"}
{"id":1,"firstname":"Milagros","lastname":"Diaz"}
{"id":1,"firstname":"Lesly","lastname":"Aguila"}
````

## Crea Kafka JSON Consumer

Anteriormente, ya habíamos creado nuestra clase `KafkaConsumer`. En este apartado, crearemos un nuevo método con la
anotación `@KafkaListener` para estar pendiente del topic `kafka-demo` esperando recibir objeto del tipo `Student`.
Recordar también que habíamos comentado el método que recibía mensajes del tipo String, esto es importante,
dado que ahora recibiremos objetos JSON.

````java

@Slf4j
@Service
public class KafkaConsumer {

    //@KafkaListener(topics = "kafka-demo", groupId = "myGroup")
    public void consumeMessage(String message) {
        log.info("Mensaje recibido desde el topic kafka-demo: {}", message);
    }

    @KafkaListener(topics = "kafka-demo", groupId = "myGroup")
    public void consumeJsonMessage(Student student) {
        log.info("Consumiendo Student desde el topic kafka-demo: {}", student.toString());
    }
}
````

## Probando Kafka JSON Consumer

Antes de probar nuestra clase, debemos realizar una configuración en el `application.yml`. Debemos agregar la
siguiente configuración: `spring.kafka.consumer.properties.spring.json.trusted.packages='*'`.

````yml
spring:
  # Other properties
  kafka:
    consumer:
      # other properties
      properties:
        spring.json.trusted.packages: '*'
    # Other properties
````

Apache Kafka tiene un mecanismo de seguridad que debemos configurar para decir que confíe en el paquete
`dev.magadiflo.kafka.app.payload` donde está nuestra clase `Student` que será convertida a un objeto JSON y que esa
clase sí es seguro para poder serializarlo. Si tenemos múltiples paquetes donde hay clases que se van a serializar
para enviarse por Kafka, entonces podemos usar `*` que indica que confíe en todos.

Para ser precisos, el error que nos podría mostrar si no agregamos dicha configuración es:

````bash
Caused by: java.lang.IllegalArgumentException: 
The class 'dev.magadiflo.kafka.app.payload.Student' is not in the trusted packages: [java.util, java.lang]. 
If you believe this class is safe to deserialize, please provide its name. 
If the serialization is only done by a trusted source, you can also enable trust all (*).
````

Ahora sí, ejecutamos la aplicación y vemos el resultado en consola:

````bash
$ curl -v -X POST -H "Content-Type: application/json" -d "{\"id\": 1, \"firstname\": \"Susana\", \"lastname\": \"Alvarado\"}" http://localhost:8080/api/v1/messages/json
>
< HTTP/1.1 200
< Content-Type: text/plain;charset=UTF-8
< Content-Length: 49
< Date: Thu, 25 Apr 2024 18:09:31 GMT
<
Estudiante (JSON) agregado al topic exitosamente!
````

Consola del Ide IntelliJ IDEA:

````bash
INFO 11436 --- [kafka-demo] [nio-8080-exec-1] o.a.k.clients.producer.KafkaProducer     : [Producer clientId=producer-1] Instantiated an idempotent producer.
INFO 11436 --- [kafka-demo] [nio-8080-exec-1] o.a.kafka.common.utils.AppInfoParser     : Kafka version: 3.6.2
INFO 11436 --- [kafka-demo] [nio-8080-exec-1] o.a.kafka.common.utils.AppInfoParser     : Kafka commitId: c4deed513057c94e
INFO 11436 --- [kafka-demo] [nio-8080-exec-1] o.a.kafka.common.utils.AppInfoParser     : Kafka startTimeMs: 1714068571390
INFO 11436 --- [kafka-demo] [ad | producer-1] org.apache.kafka.clients.Metadata        : [Producer clientId=producer-1] Cluster ID: -lvYjMq4SSePgOrgi4naQQ
INFO 11436 --- [kafka-demo] [ad | producer-1] o.a.k.c.p.internals.TransactionManager   : [Producer clientId=producer-1] ProducerId set to 1004 with epoch 0
INFO 11436 --- [kafka-demo] [ntainer#0-0-C-1] d.m.kafka.app.consumer.KafkaConsumer     : Consumiendo Student desde el topic kafka-demo: Student(id=1, firstname=Susana, lastname=Alvarado)
````
