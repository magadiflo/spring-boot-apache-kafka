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
