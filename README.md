# [Apache Kafka Tutorial with Spring Boot Reactive & WebFlux | Kafka Tutorial](https://www.youtube.com/watch?v=KQDTtvZMS9c)

Tutorial tomado del canal de **youtube Bouali Ali**

---

## ¿Qué es un Message Broker?

Un `message broker` **(intermediario de mensajes o broker de mensajes)** es un componente de software intermediario
responsable de facilitar la comunicación y el intercambio de datos entre diferentes aplicaciones o sistemas, por lo que
su función principal es desacoplar a los `Producers`, que son las aplicaciones que envían datos al `broker`
(intermediario), de los `Consumers`, que son las aplicaciones que reciben los datos; por lo que el intermediario de
mensajes actúa como mediador asegurando que los mensajes se entreguen de manera eficiente y confiable desde
el `Producer` al `Consumer`.

La característica clave de los intermediarios de mensajes son, primero, tenemos el desacoplamiento, por lo tanto, el
intermediario de mensajes permite un acoplamiento flexible entre las aplicaciones al permitirles comunicarse sin la
necesidad de estar al tanto de la existencia de cada uno.

Como Message Broker tenemos a: `Apache Kafka, Amazon SQS o RabbitMQ`.

![01.message_broker.png](assets/01.message_broker.png)