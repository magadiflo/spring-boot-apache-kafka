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

Agregamos las configuraciones correspondientes al `producer` y `consumer` de esta aplicación:

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
  a un tema (topic). `Earliest` indica que si el offset no es válido o es un nuevo consumidor, comenzará a leer desde el
  comienzo del topic, es decir, el primer mensaje disponible. Digamos, que este atributo es similar a la bandera que
  usamos cuando ejecutamos el consumer en consola `--from-beginning`.
- 