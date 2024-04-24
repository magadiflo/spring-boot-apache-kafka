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

## Apache Kafka Global Overview

`Apache Kafka` es una plataforma de procesamiento de flujo e intermediario de mensajes distribuido, tolerante a fallas y
altamente escalable. `Apache Kafka` está diseñado para manejar grandes volúmenes de flujos de datos en tiempo real y de
manera tolerante a fallas.

![02.kafka_overview.png](assets/02.kafka_overview.png)

## Kafka Cluster

Kafka es diseñado para operar como un sistema distribuido y un cluster le permite escalar horizontalmente, proporciona
tolerancia y maneja grandes volúmenes de datos a través de múltiples nodos.

En el contexto de `Kafka`, un cluster es un grupo de `brokers/servidores` que trabajan juntos por tres
razones: `velocidad (baja latencia)`, `durabilidad` y `escalabilidad`.

Varios flujos de datos pueden ser procesados por servidores separados, lo que disminuye la latencia de la entrega de
datos. Los datos se replican en varios servidores, de modo que si uno falla, otro servidor tiene la copia de seguridad
de los datos, lo que garantiza la estabilidad, es decir, la durabilidad y disponibilidad de los datos. Kafka también
equilibra la carga entre varios servidores para ofrecer escalabilidad.

![03.kafka_cluster.png](assets/03.kafka_cluster.png)