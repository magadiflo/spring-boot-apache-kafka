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

Notar que el puerto lo hemos cambiado, ya que ejecutaremos al mismo timepo la aplicación del producer y consumer y
ambos deben tener puertos distintos.

````yaml
server:
  port: 8081

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

## Probando aplicación consumer

Ejecutamos nuestra aplicación `real-world-consumer`, al hacerlo nuestra aplicación quedará escuchando al
topic `wikimedia-stream`.

Ahora, levantamos la aplicación `real-world-producer` y hacemos una llamada a su endpoint:

````bash
$ curl -v http://localhost:8080/api/v1/wikimedia | jq
>
< HTTP/1.1 200 OK
````

Como resultado tendremos en consola de la aplicación `real-world-consumer` el log siguiente, indicándonos que 
nuestra aplicación `consumer` está, efectivamente, consumiendo los mensajes que le llegan al topic `wikimedia-stream`:

````bash
WikimediaConsumer     : Consumiendo mensaje desde el topic wikimedia-stream: {"$schema":"/mediawiki/recentchange/1.0.0","meta":{"uri":"https://www.wikidata.org/wiki/Q26427390","request_id":"386cdcc4-5289-46b3-a9a8-522c249301f5","id":"56e479de-90f9-4fd3-82eb-92a6b9ddbed8","dt":"2024-04-26T00:00:52Z","domain":"www.wikidata.org","stream":"mediawiki.recentchange","topic":"eqiad.mediawiki.recentchange","partition":0,"offset":5050435611},"id":2201565331,"type":"edit","namespace":0,"title":"Q26427390","title_url":"https://www.wikidata.org/wiki/Q26427390","comment":"/* wbsetdescription-set:1|sl */ kategorija Wikimedie, [[Wikidata:Requests for permissions/Bot/MDanielsBot|Bot task 1]]: replacing sl-language descriptions ([[:toollabs:editgroups/b/wikibase-cli/41f0a4f3ca60f/|details]])","timestamp":1714089652,"user":"MDanielsBot","bot":true,"notify_url":"https://www.wikidata.org/w/index.php?diff=2137317625&oldid=1822985428&rcid=2201565331","minor":false,"patrolled":true,"length":{"old":12399,"new":12398},"revision":{"old":1822985428,"new":2137317625},"server_url":"https://www.wikidata.org","server_name":"www.wikidata.org","server_script_path":"/w","wiki":"wikidatawiki","parsedcomment":"‎<span dir=\"auto\"><span class=\"autocomment\">Changed Slovenian description: </span></span> kategorija Wikimedie, <a href=\"/wiki/Wikidata:Requests_for_permissions/Bot/MDanielsBot\" title=\"Wikidata:Requests for permissions/Bot/MDanielsBot\">Bot task 1</a>: replacing sl-language descriptions (<a href=\"https://iw.toolforge.org/editgroups/b/wikibase-cli/41f0a4f3ca60f/\" class=\"extiw\" title=\"toollabs:editgroups/b/wikibase-cli/41f0a4f3ca60f/\">details</a>)"}
WikimediaConsumer     : Consumiendo mensaje desde el topic wikimedia-stream: {"$schema":"/mediawiki/recentchange/1.0.0","meta":{"uri":"https://en.wikipedia.org/wiki/Wikipedia:Vital_articles/Level/5/People/Writers_and_journalists","request_id":"723e650f-65ec-4f5d-b50a-5e02bf90e902","id":"af5e44a4-cd01-4024-994c-2781b20c3c4d","dt":"2024-04-26T00:00:39Z","domain":"en.wikipedia.org","stream":"mediawiki.recentchange","topic":"eqiad.mediawiki.recentchange","partition":0,"offset":5050435612},"id":1769695958,"type":"edit","namespace":4,"title":"Wikipedia:Vital articles/Level/5/People/Writers and journalists","title_url":"https://en.wikipedia.org/wiki/Wikipedia:Vital_articles/Level/5/People/Writers_and_journalists","comment":"/* Europe */ Moved from the philosophers page. (He's from Muslim-controlled Spain for most of his life.)","timestamp":1714089639,"user":"SailorGardevoir","bot":false,"notify_url":"https://en.wikipedia.org/w/index.php?diff=1220798182&oldid=1220682296","minor":false,"length":{"old":94545,"new":94577},"revision":{"old":1220682296,"new":1220798182},"server_url":"https://en.wikipedia.org","server_name":"en.wikipedia.org","server_script_path":"/w","wiki":"enwiki","parsedcomment":"<span dir=\"auto\"><span class=\"autocomment\"><a href=\"/wiki/Wikipedia:Vital_articles/Level/5/People/Writers_and_journalists#Europe\" title=\"Wikipedia:Vital articles/Level/5/People/Writers and journalists\">→‎Europe</a>: </span> Moved from the philosophers page. (He&#039;s from Muslim-controlled Spain for most of his life.)</span>"}
WikimediaConsumer     : Consumiendo mensaje desde el topic wikimedia-stream: {"$schema":"/mediawiki/recentchange/1.0.0","meta":{"uri":"https://www.wikidata.org/wiki/Q57652894","request_id":"38efff04-5c04-4fb5-a39e-69189a8966cc","id":"559195eb-db73-4b32-9876-2e6bb8db0c4f","dt":"2024-04-26T00:00:52Z","domain":"www.wikidata.org","stream":"mediawiki.recentchange","topic":"eqiad.mediawiki.recentchange","partition":0,"offset":5050435613},"id":2201565332,"type":"edit","namespace":0,"title":"Q57652894","title_url":"https://www.wikidata.org/wiki/Q57652894","comment":"/* wbsetdescription-add:1|hy */ հոդված, based on en:article","timestamp":1714089652,"user":"ԱշբոտՏՆՂ","bot":true,"notify_url":"https://www.wikidata.org/w/index.php?diff=2137317628&oldid=1948919710&rcid=2201565332","minor":false,"patrolled":true,"length":{"old":72575,"new":72646},"revision":{"old":1948919710,"new":2137317628},"server_url":"https://www.wikidata.org","server_name":"www.wikidata.org","server_script_path":"/w","wiki":"wikidatawiki","parsedcomment":"‎<span dir=\"auto\"><span class=\"autocomment\">Added [hy] description: </span></span> հոդված, based on en:article"}
WikimediaConsumer     : Consumiendo mensaje desde el topic wikimedia-stream: {"$schema":"/mediawiki/recentchange/1.0.0","meta":{"uri":"https://www.wikidata.org/wiki/Q36510645","request_id":"2b0af12b-bcfc-4304-b5b3-6206a7763200","id":"c193d055-bab3-47d5-85ba-f2e9c8df931a","dt":"2024-04-26T00:00:52Z","domain":"www.wikidata.org","stream":"mediawiki.recentchange","topic":"eqiad.mediawiki.recentchange","partition":0,"offset":5050435614},"id":2201565333,"type":"edit","namespace":0,"title":"Q36510645","title_url":"https://www.wikidata.org/wiki/Q36510645","comment":"/* wbeditentity-update-languages-short:0||fa, hy, tg, ur */ BOT - Adding descriptions (4 languages): fa, hy, tg, ur","timestamp":1714089652,"user":"Emijrpbot","bot":true,"notify_url":"https://www.wikidata.org/w/index.php?diff=2137317627&oldid=1758983594&rcid=2201565333","minor":false,"patrolled":true,"length":{"old":21641,"new":21971},"revision":{"old":1758983594,"new":2137317627},"server_url":"https://www.wikidata.org","server_name":"www.wikidata.org","server_script_path":"/w","wiki":"wikidatawiki","parsedcomment":"‎<span dir=\"auto\"><span class=\"autocomment\">Changed label, description and/or aliases in fa, hy, tg, ur: </span></span> BOT - Adding descriptions (4 languages): fa, hy, tg, ur"}
WikimediaConsumer     : Consumiendo mensaje desde el topic wikimedia-stream: {"$schema":"/mediawiki/recentchange/1.0.0","meta":{"uri":"https://pl.wiktionary.org/wiki/niem.","request_id":"50cd62f7-6162-42d3-be2a-3fbd36cdede6","id":"838eb957-5b18-41a2-a4f9-d98a1183aeca","dt":"2024-04-26T00:00:52Z","domain":"pl.wiktionary.org","stream":"mediawiki.recentchange","topic":"eqiad.mediawiki.recentchange","partition":0,"offset":5050435615},"id":9977270,"type":"edit","namespace":0,"title":"niem.","title_url":"https://pl.wiktionary.org/wiki/niem.","comment":"","timestamp":1714089652,"user":"Zan-mir","bot":false,"notify_url":"https://pl.wiktionary.org/w/index.php?diff=8380181&oldid=6748779","minor":false,"length":{"old":470,"new":470},"revision":{"old":6748779,"new":8380181},"server_url":"https://pl.wiktionary.org","server_name":"pl.wiktionary.org","server_script_path":"/w","wiki":"plwiktionary","parsedcomment":""}
WikimediaConsumer     : Consumiendo mensaje desde el topic wikimedia-stream: {"$schema":"/mediawiki/recentchange/1.0.0","meta":{"uri":"https://es.wikipedia.org/wiki/Museo_de_Transportes_El%C3%A9ctricos_del_Distrito_Federal","request_id":"b9a857ff-2741-479f-b325-555c6d1f8b5e","id":"ced75844-a7d1-4b0b-b10a-1e62054844e1","dt":"2024-04-26T00:00:52Z","domain":"es.wikipedia.org","stream":"mediawiki.recentchange","topic":"eqiad.mediawiki.recentchange","partition":0,"offset":5050435616},"id":312240309,"type":"edit","namespace":0,"title":"Museo de Transportes Eléctricos del Distrito Federal","title_url":"https://es.wikipedia.org/wiki/Museo_de_Transportes_El%C3%A9ctricos_del_Distrito_Federal","comment":"","timestamp":1714089652,"user":"187.131.36.75","bot":false,"notify_url":"https://es.wikipedia.org/w/index.php?diff=159712614&oldid=157649267","minor":false,"length":{"old":2628,"new":2630},"revision":{"old":157649267,"new":159712614},"server_url":"https://es.wikipedia.org","server_name":"es.wikipedia.org","server_script_path":"/w","wiki":"eswiki","parsedcomment":""}
WikimediaConsumer     : Consumiendo mensaje desde el topic wikimedia-stream: {"$schema":"/mediawiki/recentchange/1.0.0","meta":{"uri":"https://en.wikipedia.org/wiki/Wikipedia:Version_1.0_Editorial_Team/Alphabet_articles_by_quality_log","request_id":"5a0773af-bfce-4763-af4b-c9e6973a3a50","id":"59344d07-5b1d-417b-b578-77336f1ad26b","dt":"2024-04-26T00:00:52Z","domain":"en.wikipedia.org","stream":"mediawiki.recentchange","topic":"eqiad.mediawiki.recentchange","partition":0,"offset":5050435617},"id":1769695959,"type":"edit","namespace":4,"title":"Wikipedia:Version 1.0 Editorial Team/Alphabet articles by quality log","title_url":"https://en.wikipedia.org/wiki/Wikipedia:Version_1.0_Editorial_Team/Alphabet_articles_by_quality_log","comment":"Update logs for past 7 days","timestamp":1714089652,"user":"WP 1.0 bot","bot":true,"notify_url":"https://en.wikipedia.org/w/index.php?diff=1220798210&oldid=1220633617","minor":false,"length":{"old":110,"new":110},"revision":{"old":1220633617,"new":1220798210},"server_url":"https://en.wikipedia.org","server_name":"en.wikipedia.org","server_script_path":"/w","wiki":"enwiki","parsedcomment":"Update logs for past 7 days"}
WikimediaConsumer     : Consumiendo mensaje desde el topic wikimedia-stream: {"$schema":"/mediawiki/recentchange/1.0.0","meta":{"uri":"https://commons.wikimedia.org/wiki/File:Croquis-_clients_de_l%27h%C3%B4tel_-_Porches_-_Portugal_(8660411518).jpg","request_id":"659a9d2d-6d19-477a-9e26-ffd054ff3f27","id":"d67aa9c0-1b23-4da1-a31e-9c08111bc29e","dt":"2024-04-26T00:00:52Z","domain":"commons.wikimedia.org","stream":"mediawiki.recentchange","topic":"eqiad.mediawiki.recentchange","partition":0,"offset":5050435618},"id":2478631313,"type":"edit","namespace":6,"title":"File:Croquis- clients de l'hôtel - Porches - Portugal (8660411518).jpg","title_url":"https://commons.wikimedia.org/wiki/File:Croquis-_clients_de_l%27h%C3%B4tel_-_Porches_-_Portugal_(8660411518).jpg","comment":"/* wbeditentity-update:0| */ automatically adding [[Commons:Structured data|structured data]] based on file information","timestamp":1714089652,"user":"SchlurcherBot","bot":true,"notify_url":"https://commons.wikimedia.org/w/index.php?diff=871334732&oldid=606356845&rcid=2478631313","minor":false,"patrolled":true,"length":{"old":4962,"new":7334},"revision":{"old":606356845,"new":871334732},"server_url":"https://commons.wikimedia.org","server_name":"commons.wikimedia.org","server_script_path":"/w","wiki":"commonswiki","parsedcomment":"‎<span dir=\"auto\"><span class=\"autocomment\">Changed an entity: </span></span> automatically adding <a href=\"/wiki/Commons:Structured_data\" title=\"Commons:Structured data\">structured data</a> based on file information"}
WikimediaConsumer     : Consumiendo mensaje desde el topic wikimedia-stream: {"$schema":"/mediawiki/recentchange/1.0.0","meta":{"uri":"https://www.wikidata.org/wiki/Q26427392","request_id":"51ef792b-4e85-49e3-ba8d-92658bcafced","id":"3124ca63-142b-40c3-9daf-4ebd0f877427","dt":"2024-04-26T00:00:53Z","domain":"www.wikidata.org","stream":"mediawiki.recentchange","topic":"eqiad.mediawiki.recentchange","partition":0,"offset":5050435619},"id":2201565334,"type":"edit","namespace":0,"title":"Q26427392","title_url":"https://www.wikidata.org/wiki/Q26427392","comment":"/* wbsetdescription-set:1|sl */ kategorija Wikimedie, [[Wikidata:Requests for permissions/Bot/MDanielsBot|Bot task 1]]: replacing sl-language descriptions ([[:toollabs:editgroups/b/wikibase-cli/41f0a4f3ca60f/|details]])","timestamp":1714089653,"user":"MDanielsBot","bot":true,"notify_url":"https://www.wikidata.org/w/index.php?diff=2137317630&oldid=1717683735&rcid=2201565334","minor":false,"patrolled":true,"length":{"old":12280,"new":12279},"revision":{"old":1717683735,"new":2137317630},"server_url":"https://www.wikidata.org","server_name":"www.wikidata.org","server_script_path":"/w","wiki":"wikidatawiki","parsedcomment":"‎<span dir=\"auto\"><span class=\"autocomment\">Changed Slovenian description: </span></span> kategorija Wikimedie, <a href=\"/wiki/Wikidata:Requests_for_permissions/Bot/MDanielsBot\" title=\"Wikidata:Requests for permissions/Bot/MDanielsBot\">Bot task 1</a>: replacing sl-language descriptions (<a href=\"https://iw.toolforge.org/editgroups/b/wikibase-cli/41f0a4f3ca60f/\" class=\"extiw\" title=\"toollabs:editgroups/b/wikibase-cli/41f0a4f3ca60f/\">details</a>)"}
WikimediaConsumer     : Consumiendo mensaje desde el topic wikimedia-stream: {"$schema":"/mediawiki/recentchange/1.0.0","meta":{"uri":"https://es.wikipedia.org/wiki/Glee:_The_Music,_The_Christmas_Album","request_id":"42bfdaf8-d435-446d-8e30-5c7cbf3d55ac","id":"9b60e7ca-73a1-400f-87ca-6534918b2d95","dt":"2024-04-26T00:00:52Z","domain":"es.wikipedia.org","stream":"mediawiki.recentchange","topic":"eqiad.mediawiki.recentchange","partition":0,"offset":5050435620},"id":312240310,"type":"edit","namespace":0,"title":"Glee: The Music, The Christmas Album","title_url":"https://es.wikipedia.org/wiki/Glee:_The_Music,_The_Christmas_Album","comment":"/* Lista de canciones */","timestamp":1714089652,"user":"2800:150:102:175:38D5:56B9:2BA:5009","bot":false,"notify_url":"https://es.wikipedia.org/w/index.php?diff=159712615&oldid=157199146","minor":false,"length":{"old":20199,"new":20183},"revision":{"old":157199146,"new":159712615},"server_url":"https://es.wikipedia.org","server_name":"es.wikipedia.org","server_script_path":"/w","wiki":"eswiki","parsedcomment":"<span dir=\"auto\"><span class=\"autocomment\"><a href=\"/wiki/Glee:_The_Music,_The_Christmas_Album#Lista_de_canciones\" title=\"Glee: The Music, The Christmas Album\">→‎Lista de canciones</a></span></span>"}
WikimediaConsumer     : Consumiendo mensaje desde el topic wikimedia-stream: {"$schema":"/mediawiki/recentchange/1.0.0","meta":{"uri":"https://en.wikipedia.org/wiki/Category_talk:Replaceable_non-free_use_to_be_decided_after_25_April_2024","request_id":"4fb8ad55-a01f-4354-85e9-0676d2584ecb","id":"d5bf4701-791c-40a8-b3f6-c450f961476b","dt":"2024-04-26T00:00:53Z","domain":"en.wikipedia.org","stream":"mediawiki.recentchange","topic":"eqiad.mediawiki.recentchange","partition":0,"offset":5050435621},"id":1769695961,"type":"log","namespace":15,"title":"Category talk:Replaceable non-free use to be decided after 25 April 2024","title_url":"https://en.wikipedia.org/wiki/Category_talk:Replaceable_non-free_use_to_be_decided_after_25_April_2024","comment":"[[WP:CSD#G8|G8]]: Talk page of deleted page \"Category:Replaceable non-free use to be decided after 25 April 2024\"","timestamp":1714089653,"user":"Explicit","bot":false,"log_id":161650906,"log_type":"delete","log_action":"delete","log_params":[],"log_action_comment":"deleted &quot;[[Category talk:Replaceable non-free use to be decided after 25 April 2024]]&quot;: [[WP:CSD#G8|G8]]: Talk page of deleted page \"Category:Replaceable non-free use to be decided after 25 April 2024\"","server_url":"https://en.wikipedia.org","server_name":"en.wikipedia.org","server_script_path":"/w","wiki":"enwiki","parsedcomment":"<a href=\"/wiki/Wikipedia:CSD#G8\" class=\"mw-redirect\" title=\"Wikipedia:CSD\">G8</a>: Talk page of deleted page &quot;Category:Replaceable non-free use to be decided after 25 April 2024&quot;"}
WikimediaConsumer     : Consumiendo mensaje desde el topic wikimedia-stream: {"$schema":"/mediawiki/recentchange/1.0.0","meta":{"uri":"https://www.wikidata.org/wiki/Q125616592","request_id":"2be0247d-2800-4d36-b6d8-decdc37c43e2","id":"7d0661bb-7a28-484e-b5e9-9347c860b6d9","dt":"2024-04-26T00:00:53Z","domain":"www.wikidata.org","stream":"mediawiki.recentchange","topic":"eqiad.mediawiki.recentchange","partition":0,"offset":5050435622},"id":2201565335,"type":"edit","namespace":0,"title":"Q125616592","title_url":"https://www.wikidata.org/wiki/Q125616592","comment":"/* wbsetdescription-add:1|ar */ لعبة فيديو","timestamp":1714089653,"user":"Mr.Ibrahembot","bot":true,"notify_url":"https://www.wikidata.org/w/index.php?diff=2137317631&oldid=2137317398&rcid=2201565335","minor":false,"patrolled":true,"length":{"old":7763,"new":7841},"revision":{"old":2137317398,"new":2137317631},"server_url":"https://www.wikidata.org","server_name":"www.wikidata.org","server_script_path":"/w","wiki":"wikidatawiki","parsedcomment":"‏<span dir=\"auto\"><span class=\"autocomment\">أضاف وصفا بـ[ar]: </span></span> لعبة فيديو"}
WikimediaConsumer     : Consumiendo mensaje desde el topic wikimedia-stream: {"$schema":"/mediawiki/recentchange/1.0.0"
````