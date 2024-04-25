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

## Kafka Broker

Los brokers de Kafka son servidores con tareas especiales: gestionar el equilibrio de carga, la replicación y el
desacoplamiento de flujos dentro del clúster de Kafka. Cómo hacen su trabajo? Bueno, en primer lugar, para iniciar un
clúster kafka, el desarrollador se autentica en un servidor boostrap (o unos cuantos). Estos son los primeros servidores
del clúster. Luego, los brokers también equilibran la carga y se encargan de la replicación, y esas dos características
son clave para la velocidad, escalabilidad y estabilidad de kafka.

Por lo que, un `Kafka Broker es un servidor Kafka individual` que almacena datos y atiende las solicitudes de los
clientes. Los `Brokers` dentro de un cluster se comunican entre sí para garantizar la replicación de datos y mantener
los metadatos del clúster y a cada broker en un clúster de kafka se le asigna un identificador único.

![04.kafka_broker.png](assets/04.kafka_broker.png)

## Kafka Producer

El `Producer` es una aplicación cliente que publica o escribe eventos en un clúster de Kafka, por lo que el producer
puede ser una aplicación java, php, .net o cualquier otro tipo de aplicación, incluso una línea de comandos y
simplemente escribe o envía un evento a un clúster de Kafka.

![05.kafka_producer.png](assets/05.kafka_producer.png)

## Kafka Consumer

El `consumer` es la aplicación o el sistema que consume o se suscribe al `topic` o al clúster de kafka para consumir los
eventos. Los consumidores pueden ser parte de un grupo de consumidores que les permite paralelizar el procesamiento de
mensajes.

![06.kafka_consumer.png](assets/06.kafka_consumer.png)

## Kafka Topic

Un `kafka topic` es un canal lógico o una categoría de alimentación en la que **los productores publican registros
(mensajes) y los consumidores consumen registros.** Los `topics` sirven para organizar y categorizar el flujo de
mensajes dentro del sistema de mensajería kafka.

![07.kafka_topic.png](assets/07.kafka_topic.png)

## Kafka Partitions

En Apache Kafka, una partición es una unidad básica de paralelismo y escalabilidad. Es una forma de dividir
horizontalmente un topic en múltiples unidades gestionadas independientemente. Cada partición es una secuencia de
registros estrictamente ordenada e inmutable, y desempeña un papel crucial en la distribución, el procesamiento paralelo
y la tolerancia a fallos de los datos dentro de un clúster de kafka.

Las `partitions` permiten el escalado horizontal y el procesamiento paralelo de datos dentro de un tema. Cada partición
puede considerarse como un flujo independiente de mensajes para que los productores puedan escribir y los consumidores
puedan leer desde diferentes particiones simultáneamente, lo que le permite a Kafka manejar un mayor volumen de datos
al distribuir la carga de trabajo entre múltiples particiones.

![08.kafka_partitions.png](assets/08.kafka_partitions.png)

## Offsets

`Offsets` es una secuencia de identificadores que se asignan a los mensajes a medida que llegan a una partición.
Una vez asignado el offset, nunca se cambiará. El primer mensaje recibe un `offset cero (0)`. El siguiente mensaje
recibe un `offset uno (1)`, y así sucesivamente.

Un `Offset` es un identificador único asignado a cada mensaje dentro de una partición de un topic de kafka, por lo que
representa la posición o ubicación de un mensaje en el registro de la partición. Los `offsets` se usan para rastrear
el proceso de los consumidores y permitirles reanudar el consumo desde un punto específico, incluso en caso de falla
o reinicio.

![09.offsets.png](assets/09.offsets.png)

## Consumer Groups

Un grupo de consumidores es una agrupación lógica de consumidores de Kafka que trabajan juntos para consumir y procesar
mensajes de una o más particiones de un topic. Para que **cada partición de un topic pueda asignarse como máximo a un
consumidor dentro de un grupo de consumidores**. Como puede ver, **esto garantiza que cada mensaje dentro de una
partición sea procesada solo por un consumer a la vez.**

Un grupo de consumidores contiene uno o más consumidores que trabajan juntos para procesar mensajes.

![10.consumer_groups.png](assets/10.consumer_groups.png)

---

# [Installing and exploring Kafka](https://kafka.apache.org/quickstart)

---

Vamos a la siguiente dirección [kafka.apache.org/quickstart](https://kafka.apache.org/quickstart) y seguimos la
secuencia de la imagen:

![kafka installation](./assets/11.kafka-installation.png)

Luego de descargar el archivo `kafka_2.13-3.7.0.tgz` **lo descomprimiremos en la raíz del disco** `C:\\`.
En mi caso quedaría de la siguiente manera `C:\kafka_2.13-3.7.0`.

Por defecto, estos serán los directorios y archivos que vienen en el paquete de instalación:

![12.default-directories.png](./assets/12.default-directories.png)

## Configurando Kafka para windows

Realizamos la siguiente configuración **de manera manual** para que **Kafka funcione en Windows**, ya que por defecto
está configurado para que funcione con servidores **Linux/Mac**.

Abrimos el archivo `server.properties` ubicado en `C:\kafka_2.13-3.7.0\config` y cambiamos el directorio linux por
nuestro directorio de windows donde está nuestro servidor de kafka:

````properties
############################# Log Basics #############################
# A comma separated list of directories under which to store log files
#log.dirs=/tmp/kafka-logs (por defecto)
log.dirs=C:/kafka_2.13-3.7.0/kafka-logs
````

También debemos modificar el archivo `zookeeper.properties` ubicado en `C:\kafka_2.13-3.6.0\config`:

````properties
# the directory where the snapshot is stored.
#dataDir=/tmp/zookeeper (por defecto)
dataDir=C:/kafka_2.13-3.7.0/zookeeper
````

## Iniciando servidor Zookeeper y Kafka

Nos ubicamos mediante la terminal en nuestro directorio de instalación de kafka `C:/kafka_2.13-3.7.0` y procedemos a
ejecutar los comandos en el siguiente orden:

1. **Iniciando servidor ZooKeeper**

````bash
C:\kafka_2.13-3.7.0
$ .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties

[2024-04-24 22:30:55,023] INFO Reading configuration from: .\config\zookeeper.properties (org.apache.zookeeper.server.quorum.QuorumPeerConfig)
[2024-04-24 22:30:55,032] INFO clientPortAddress is 0.0.0.0:2181 (org.apache.zookeeper.server.quorum.QuorumPeerConfig)
...
[2024-04-24 22:30:55,110] INFO  (org.apache.zookeeper.server.ZooKeeperServer)
[2024-04-24 22:30:55,110] INFO   ______                  _                                           (org.apache.zookeeper.server.ZooKeeperServer)
[2024-04-24 22:30:55,111] INFO  |___  /                 | |                                          (org.apache.zookeeper.server.ZooKeeperServer)
[2024-04-24 22:30:55,111] INFO     / /    ___     ___   | | __   ___    ___   _ __     ___   _ __    (org.apache.zookeeper.server.ZooKeeperServer)
[2024-04-24 22:30:55,113] INFO    / /    / _ \   / _ \  | |/ /  / _ \  / _ \ | '_ \   / _ \ | '__| (org.apache.zookeeper.server.ZooKeeperServer)
[2024-04-24 22:30:55,113] INFO   / /__  | (_) | | (_) | |   <  |  __/ |  __/ | |_) | |  __/ | |     (org.apache.zookeeper.server.ZooKeeperServer)
[2024-04-24 22:30:55,114] INFO  /_____|  \___/   \___/  |_|\_\  \___|  \___| | .__/   \___| |_| (org.apache.zookeeper.server.ZooKeeperServer)
[2024-04-24 22:30:55,115] INFO                                               | |                      (org.apache.zookeeper.server.ZooKeeperServer)
[2024-04-24 22:30:55,115] INFO                                               |_|                      (org.apache.zookeeper.server.ZooKeeperServer)
[2024-04-24 22:30:55,115] INFO  (org.apache.zookeeper.server.ZooKeeperServer)
[2024-04-24 22:30:55,124] INFO Server environment:zookeeper.version=3.8.3-6ad6d364c7c0bcf0de452d54ebefa3058098ab56, built on 2023-10-05 10:34 UTC (org.apache.zookeeper.server.ZooKeeperServer)
...
[2024-04-24 22:30:55,171] INFO Created server with tickTime 3000 ms minSessionTimeout 6000 ms maxSessionTimeout 60000 ms clientPortListenBacklog -1 datadir C:\kafka_2.13-3.7.0\zookeeper\version-2 snapdir C:\kafka_2.13-3.7.0\zookeeper\version-2 (org.apache.zookeeper.server.ZooKeeperServer)
[2024-04-24 22:30:55,187] INFO Using org.apache.zookeeper.server.NIOServerCnxnFactory as server connection factory (org.apache.zookeeper.server.ServerCnxnFactory)
...
[2024-04-24 22:30:55,289] INFO binding to port 0.0.0.0/0.0.0.0:2181 (org.apache.zookeeper.server.NIOServerCnxnFactory)
...
[2024-04-24 22:30:55,382] INFO PrepRequestProcessor (sid:0) started, reconfigEnabled=false (org.apache.zookeeper.server.PrepRequestProcessor)
[2024-04-24 22:30:55,382] INFO zookeeper.request_throttler.shutdownTimeout = 10000 ms (org.apache.zookeeper.server.RequestThrottler)
[2024-04-24 22:30:55,418] INFO Using checkIntervalMs=60000 maxPerMinute=10000 maxNeverUsedIntervalMs=0 (org.apache.zookeeper.server.ContainerManager)
[2024-04-24 22:30:55,419] INFO ZooKeeper audit is disabled. (org.apache.zookeeper.audit.ZKAuditProvider)
````

2. **Iniciando el servidor de Kafka**

````bash
C:\kafka_2.13-3.7.0
$ .\bin\windows\kafka-server-start.bat .\config\server.properties

...
[2024-04-24 22:34:07,870] INFO Connecting to zookeeper on localhost:2181 (kafka.server.KafkaServer)
[2024-04-24 22:34:07,897] INFO [ZooKeeperClient Kafka server] Initializing a new session to localhost:2181. (kafka.zookeeper.ZooKeeperClient)
...
[2024-04-24 22:34:10,859] INFO Kafka version: 3.7.0 (org.apache.kafka.common.utils.AppInfoParser)
[2024-04-24 22:34:10,859] INFO Kafka commitId: 2ae524ed625438c5 (org.apache.kafka.common.utils.AppInfoParser)
[2024-04-24 22:34:10,860] INFO Kafka startTimeMs: 1714016050851 (org.apache.kafka.common.utils.AppInfoParser)
[2024-04-24 22:34:10,863] INFO [KafkaServer id=0] started (kafka.server.KafkaServer)
[2024-04-24 22:34:11,482] INFO [zk-broker-0-to-controller-forwarding-channel-manager]: Recorded new controller, from now on will use node DESKTOP-EGDL8Q6:9092 (id: 0 rack: null) (kafka.server.NodeToControllerRequestThread)
[2024-04-24 22:34:11,482] INFO [zk-broker-0-to-controller-alter-partition-channel-manager]: Recorded new controller, from now on will use node DESKTOP-EGDL8Q6:9092 (id: 0 rack: null) (kafka.server.NodeToControllerRequestThread)
````

Después de iniciar los servidores y crear nuestro primer `tópic` (la creación del topic se ve a continuación) vemos que
se han agregado nuevos directorios al directorio de instalación de kafka:

![after-run-kafka](./assets/13.after-run-kafka-directories.png)

## Explorando Kafka

Kafka es una plataforma distribuida de transmisión de eventos que le permite leer, escribir, almacenar y procesar
eventos (también llamados registros o mensajes en la documentación) en muchas máquinas.

Eventos de ejemplo son transacciones de pago, actualizaciones de geolocalización desde teléfonos móviles, pedidos de
envío, mediciones de sensores desde dispositivos IoT o equipos médicos, y mucho más. Estos eventos están organizados y
almacenados en `topics`. De manera muy simplificada, un `topic` es similar a una carpeta en un sistema de archivos y los
eventos son los archivos en esa carpeta.

### Creando un Topic

Entonces, antes de que puedas escribir tus primeros eventos, debes crear un `topic`. Abra otra sesión de terminal y
ejecute:

````bash
C:\kafka_2.13-3.7.0
$ .\bin\windows\kafka-topics.bat --create --topic quickstart-events --bootstrap-server localhost:9092
Created topic quickstart-events.
````

**DONDE**

- `quickstart-events`, nombre que le damos al topic que estamos creando.
- `localhost:9092`, servidor de arranque que es la dirección de nuestro servidor de Apache Kafka (broker).

### Describe un topic

Podemos ver los detalles del topic `quickstart-events` creado anteriormente:

````bash
C:\kafka_2.13-3.7.0
$ .\bin\windows\kafka-topics.bat --describe --topic quickstart-events --bootstrap-server localhost:9092
Topic: quickstart-events        TopicId: TjsU60PyTRaeiR95VXknFQ PartitionCount: 1       ReplicationFactor: 1    Configs:
        Topic: quickstart-events        Partition: 0    Leader: 0       Replicas: 0     Isr: 0
````

### Listando topics

Listamos todos los topics que existen dentro del broker de kafka:

````bash
C:\kafka_2.13-3.7.0
$ .\bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092
quickstart-events
````

### Levantando un producer de kafka

Dentro de las instalaciones de Kafka, existe una aplicación en consola que actúa como un productor. Utilizaremos dicha
aplicación de consola para poder enviar mensajes a nuestro topic `quickstart-events` tal como se muestra a continuación:

````bash
C:\kafka_2.13-3.7.0
$ .\bin\windows\kafka-console-producer.bat --topic quickstart-events --bootstrap-server localhost:9092
>Hola, es mi primer mensaje!
>Este seria mi segundo mensaje!
>
````

### Levantando un consumer de kafka

Además del productor de consola, también encontramos dentro de las instalaciones de Kafka una aplicación en consola que
hace de `consumer`. Utilizaremos dicha aplicación para poder subscribirnos al topic `quickstart-events` y poder
leer los mensajes que se estén enviando a ese topic.

````bash
C:\kafka_2.13-3.7.0
$ .\bin\windows\kafka-console-consumer.bat --topic quickstart-events --from-beginning --bootstrap-server localhost:9092
Hola, es mi primer mensaje!
Este seria mi segundo mensaje!
````

La bandera `--from-beginning` indica que esta aplicación leerá los mensajes desde el inicio. Si no colocamos dicha
bandera, empezará a monitorear los mensajes desde el momento en que se ejecutó la aplicación consumer.