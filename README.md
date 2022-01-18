# Middleware Engineering "*Message Oriented Middleware*"

## Ausführung

Zuerst müssen die Docker Images erstellt werden. Dafür werden folgende Befehle ausgeführt:
```shell
cd Windpark/TimingStation/
gradle bootImageBuild
```
```shell
cd Windpark/ControllingStation/
gradle bootImageBuild
```
Anschließend kann im `WindPark` directory das Docker Compose file ausgeführt werden. Dafür wird folgender Befehl benötigt.
```shell
docker-compose up -d
```

## Aufgabenstellung
Die detaillierte [Aufgabenstellung](TASK.md) beschreibt die notwendigen Schritte zur Realisierung.

## Fragestellung

1. Nennen Sie mindestens 4 Eigenschaften der Message Oriented Middleware?
    - Message queue
    - Async/Sync Persistent communication
    - Intermediate storage capacity for messages in the communication network
    - Communication may take minutes (not ms)
    - Basic idea: insert message into queue
2. Was versteht man unter einer transienten und synchronen Kommunikation?
    - Synchronuous: Sender is blocked until
        - Buffered at recieving host
        - deliver or reciever
        - reciever has processed the message
    - Asynchronous: Sender continues immediately after submission
    - Persistent communication: Message is stored in the communication system
    - Transient communication: Message is stored only as long as sender and reciever are executing
3. Beschreiben Sie die Funktionsweise einer JMS Queue?
    - Point to point
        - If no reciever listens to the queue, messages are kept until they can be deliveres or expire
    - Publish subscribe
        - Ordinary subscribers only receive messages published while the subscriber is active.
        - Messages for durable subscribers published while the subscriber is inactive will be delivered to  the subscriber the next time it becomes active
        - Messages are only kept for a durable subscriber if
            - the topic is durable (allows to store messages) and
            - the message is persistent.
4. JMS Overview - Beschreiben Sie die wichtigsten JMS Klassen und deren Zusammenhang?
    - ConnectionFactory(Creates Connection)
    - Connection(Creates session)
    - Session(creates Message, MessageConsumer, MessageProducer)
    - MessageProducer
    - MessageConsumer
5. Beschreiben Sie die Funktionsweise eines JMS Topic?
    - Implementiert eine publish and subscribe mechanik
6. Was versteht man unter einem lose gekoppelten verteilten System? Nennen Sie ein Beispiel dazu. Warum spricht man hier von lose?
    - http://discoveration.de/it/loose-kopplung-von-systemen/371/
    - Man kann ein System härter Koppeln oder loser. Je nachdem wie stark es gekoppelt ist, sind die Unterschiedlichen teile mehr voneinander Abhängig.
    - Das bedeutet beispielsweise, dass wenn Systeme nur bei der Interaktion miteinander abhängig sind, es relativ lose ist. Umso mehr die Systeme voneinander abgekapselt sind,
    - desto loser sind Sie auch.

## Implementierung

Zuerst habe ich die Timingstation Klassen von der vorherigen Aufgaben in meine neue Timingstation kopiert. 
Dann habe ich bei dem Party Model einen default Konstruktor ohne Parameter hinzugefügt um das Parsen zu ermöglichen.

Anschließend habe ich mir überlegt wie die Aufgabe am Sinnvollsten zu lösen ist. Nach eineigen Recherche habe ich mich für ein docker-compose File entschieden.
Dies hat den Vorteil, dass das ActiveMQ nicht lokal installiert werden muss und die Verbindung und das aufsetzten automatisiert gelöst werden kann.

```dockerfile
version: '3.7'
services:
  activemq:
    image: webcenter/activemq:latest
    container_name: activemq
    ports:
      - "8161:8161"
      - "61616:61616"
    environment:
      ACTIVEMQ_ADMIN_LOGIN: admin
      ACTIVEMQ_ADMIN_PASSWORD: admin
      ACTIVEMQ_USER_LOGIN: user
      ACTIVEMQ_USER_PASSWORD: user
      ACTIVEMQ_ENABLED_SCHEDULER: 'true'
  timingstation-1:
    image: timingstation:latest
    container_name: timingstation-1
    ports:
      - "8081:8080"
    environment:
      STATION_ID: 1
      SECTION_ID: 1
    depends_on:
      - controllingstation-1
      - activemq
  timingstation-2:
    image: timingstation:latest
    container_name: timingstation-2
    ports:
      - "8082:8080"
    environment:
      STATION_ID: 2
      SECTION_ID: 1
    depends_on:
      - controllingstation-1
      - activemq
  timingstation-3:
    image: timingstation:latest
    container_name: timingstation-3
    ports:
      - "8083:8080"
    environment:
      STATION_ID: 3
      SECTION_ID: 1
    depends_on:
      - controllingstation-1
      - activemq
  timingstation-4:
    image: timingstation:latest
    container_name: timingstation-4
    ports:
      - "8084:8080"
    environment:
      STATION_ID: 4
      SECTION_ID: 1
    depends_on:
      - controllingstation-1
      - activemq
  controllingstation-1:
    image: controllingstation:latest
    container_name: controllingstation-1
    ports:
      - "8080:8080"
    environment:
      STATION_ID: 1
      SECTION_ID: 1
    depends_on:
      - activemq
```

Anschließend habe ich die ActiveMQ Config erstellt. 
Die Daten der Timingstation wie broker-url und queue wurden als ENV Variable übergeben, und mittels @Value im Programm verwendet.

Den Spring Controller, habe ich so abgeändert, dass nun keine timingstation mehr angegeben wird. In der Controllingstation wurden sowohl zugriff auf einzelne Timingstations realisiert, als auch den gesamten Datensatz anzuzeigen.
In meinem Fall werden die Daten alle 30000 ms also 30s gesendet. Das wurde mittels einem Scheduler realisiert. Der Listener fängt die Daten dann ab und parsed sie zurück in TimingstationData.

Im Timingstation Service wurde eine commitData() methode hinzugefügt, welche die Daten in die message Queue sendet.

Beim committen und receiven wird jeweils eine Log Nachricht ausgegeben, dass von der jeweiligen Timingstation etwas versendet, bzw. empfangen wurde.
Anschließend wurde noche eine logback.xml config erstellt, welche das Log zu einem File loggen sollte. Das habe ich dann aber nichtmehr getestet.

## Quellen
- https://springframework.guru/logback-configuration-using-xml/, am 17.01.2022
- https://howtodoinjava.com/spring-boot2/logging/logging-with-lombok/, am 17.01.2022
- https://codenotfound.com/spring-jms-message-converter-example.html, am 17.01.2022
- https://www.concretepage.com/spring-5/spring-jmslistener, am 17.01.2022
