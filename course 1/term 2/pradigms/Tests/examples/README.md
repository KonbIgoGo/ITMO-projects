# Простой VPN-сервер

На самом деле успели сделать только проброс портов, но он вполне работает.


## Структура

Приложение состоит из четырёх модулей:

* vpn — модуль-агрегатор для остальных модулей;
* [vpn-proto](vpn-proto) — протокол и сетевой взаимодействие;
* [vpn-server](vpn-server) — сервер с базой данных и web UI;
* [vpn-client](vpn-client) — клиент с интерфейсом командной строки.


## Сборка {#build}

Для сборки требуется Java 17 и Apache Maven ([скачать](https://maven.apache.org/download.cgi)).

Команды для сборки надо выполнять в корне проекта. К ним может быть добавлена опция
`-Dmaven.test.skip=true` что бы не запуска тесты.
 * Сборка проекта: `mvn package -Pproduction`.
   В результате сборки в каждом модуле появляется каталог `target`, содержащий собранный модуль.
* Запуск сервера через
  [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.devtools):
  `mvn --projects vpn-server spring-boot:run`.
 * Удаление сгенерированных файлов: `mvn clean`.

## Запуск

Перед запуском клиент и сервер должны быть [собраны](#build).

Сервер запускается из каталога `vpn-server/target` командой
`java -jar vpn-server-1.0.0-SNAPSHOT.jar`.
Он использует SSL-ключ `server.jks`, создаваемый в процессе сборки,
и создаёт файл базы данных `vpn.h2.mv.db`.

По умолчанию для администрирования используется порт 8889
(пользователь: `admin`, пароль `adminadmin`), а для VPN-сервера порт 8887.
Эти опции настраиваются в
[application.properties](vpn-server/src/main/resources/application.properties).

Сервер запускается из каталога `vpn-client/target` командой
`java -jar vpn-client-1.0.0-SNAPSHOT-full.jar опции`, например,
`java -jar vpn-client-1.0.0-SNAPSHOT-full.jar -u geo -p ghbdtnvbh -v localhost:8887 -t nerc.itmo.ru:80 -l 8885`.
Он использует SSL-сертификат `client.crt`, создаваемый в процессе сборки.

Перед использованием клиента не забудьте создать пользователя в админке.


## Исходные файлы

Модуль `vpn`
 * [pom.xml](pom.xml) — конфигурация модуля;
 * [README.md](README.md) — этот файл.
 * `server.jks` — SSL-ключ для сервера (создаётся при сборке);
 * `client.crt` — SSL-сертификат для клиента (создаётся при сборке).

Модуль `vpn-proto`
 * [pom.xml](vpn-proto/pom.xml) — конфигурация модуля;
 * [VpnProto.java](vpn-proto/src/main/java/info/kgeorgiy/vpn/proto/VpnProto.java)
   — базовый класс для `VpnServer` и `VpnClient`, определяющий протокол взаимодействия;
 * [VpnClient.java](vpn-proto/src/main/java/info/kgeorgiy/vpn/proto/VpnClient.java)
   — реализация клиента.
 * [VpnServer.java](vpn-proto/src/main/java/info/kgeorgiy/vpn/proto/VpnServer.java)
   — базовый класс для реализации сервера.

Модуль `vpn-client`
 * [pom.xml](vpn-client/pom.xml) — конфигурация модуля;
 * [Main.java](vpn-client/src/main/java/info/kgeorgiy/vpn/client/Main.java)
   — реализация клиента (интерфейс командной строки);
 * [logback.xml](vpn-client/src/main/resources/logback.xml)
   — конфигурация логгера.

Модуль `vpn-server`
 * [pom.xml](vpn-client/pom.xml) — конфигурация модуля;
 * [application.properties](vpn-server/src/main/resources/application.properties)
   — конфигурация сервера;
 * [VpnServerApplication.java](vpn-server/src/main/java/info/kgeorgiy/vpn/server/VpnServerApplication.java)
   — класс для запуска приложения;
 * [SecurityConfig.java](vpn-server/src/main/java/info/kgeorgiy/vpn/server/SecurityConfig.java)
   — конфигурация Spring Security;
 * [HelloView.java](vpn-server/src/main/java/info/kgeorgiy/vpn/server/HelloView.java)
   — реализация страницы `/hello`;
 * [UsersView.java](vpn-server/src/main/java/info/kgeorgiy/vpn/server/UsersView.java)
   — реализация страницы пользователей;
 * [User.java](vpn-server/src/main/java/info/kgeorgiy/vpn/server/User.java)
   — класс с информацией о пользователи, сохраняемой в базе данных;
 * [UserRepository.java](vpn-server/src/main/java/info/kgeorgiy/vpn/server/UserRepository.java)
   — интерфейс доступа к таблице пользователей;
 * [VpnServerBean.java](vpn-server/src/main/java/info/kgeorgiy/vpn/server/VpnServerBean.java)
   — реализация VPN-сервера с авторизацией пользователей.


## Используемые технологии

### Сборка и управление зависимостями (все модули)

Для сборки и управления зависимостями используется [Apache Maven](https://maven.apache.org/)
([скачать](https://maven.apache.org/download.cgi),
[краткий туториал](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)
[руководства](https://maven.apache.org/guides/index.html)).

Для описания модулей Maven использует `pom.xml` (сокращение от Project Object Model,
[справочник](https://maven.apache.org/pom.html))
в формате [XML](https://www.w3.org/TR/xml/) ([туториал](https://www.w3schools.com/xml/default.asp)).

Для каждого модуля обязательно указываются _координаты_:
 * `groupId` — проект или группа (набор связанных модулей);
 * `artifactId` — идентификатор модуля в группе;
 * `version` — версия модуля.

Дополнительно можно указать:
 * `packaging` — способ сборки (по умолчанию `jar`, для агрегаторов `pom`);
 * `name` — человеко-читаемое название;
 * `description` — описание модуля/проекта;
 * `url` — ссылка на страницу модуля/проекта;
 * `parent` — родительский модуль, от которого наследуются настройки;
 * `properties` — набор пар ключ-значение, доступные для плагинов и в других частях `pom.xml`
   с использованием синтаксиса `${ключ}`.
 * `dependencies`, `dependencyManagement` — описание зависимостей.
    Для каждой зависимости указываются координаты, использование и вид использования
    (`scope`): `compile` (по умолчанию), `test`, `runtime` и другие;
 * `build` — настройки сборки, обычно набор плагинов (`plugin`);
 * `profiles` — настройки, зависящие от профиля сборки (например `dev` и `production`).

Собранные Maven-модули могут быть загружены в
[репозитории](https://maven.apache.org/guides/introduction/introduction-to-repositories.html).
Чаще всего используется [основной репозиторий](https://repo.maven.apache.org/maven2/),
для которого есть удобный поисковик [Maven Central](https://central.sonatype.org/).

Для сборки и управления зависимостями также могут быть использованы.
 * [Gradle Build Tool](https://gradle.org/) — для описания проектов использует
   [Groovy](https://groovy-lang.org/) или [Kotlin](https://kotlinlang.org/).
   Использует Maven-зависимости в форме `groupId:artifactId:version`.
 * [Apache Ant](https://ant.apache.org/) — для описания проектов использует XML.
   В отличие от Maven и Groovy явно описывает процесс сборки шаг-за-шагом,
   а не на основе плагинов и этапов сборки.
   Для управления Maven-зависимостями используется [Ivy](https://ant.apache.org/ivy/).


### Spring  (server)

Сервер создан на основе [Spring Boot](https://spring.io/projects/spring-boot)
([туториал](https://spring.io/guides/gs/spring-boot),
[справочник](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)) —
проекта, облегчающего создания standalone серверных приложений на основе фреймворка
[Spring](https://spring.io/)
([гайды](https://spring.io/guides),
[справочник](https://docs.spring.io/spring-framework/reference/index.html)).
Исходный модуль был создан при помощи [Spring Initializr](https://start.spring.io/).

Spring активно использует аннотации для задания связей между классами.

Для упрощения разработки используются
[Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.devtools),
автоматически перезапускающие приложения после сборки.
Примечание: если изменяется набор зависимостей, то приложение надо перезапустить с нуля,
так как DevTools это не отслеживают.

Для авторизации и хеширования/проверки паролей используется
[Spring Security](https://spring.io/projects/spring-security)
([туториал](https://spring.io/guides/gs/securing-web)).


### Базы данных (server)

Для работы с базой данных H2 используется Java Persistence API (JPA).

[H2](https://www.h2database.com/html/main.html)
([туториал](https://www.h2database.com/html/tutorial.html),
[справочник](https://www.h2database.com/html/commands.html)) —
легковесная встраиваемая (запускается внутри используемого проекта)
in-memory (загружает все данные в память) система управления базами данных (СУБД),
реализованная на Java.

Альтернативные СУБД:
 * [Apache Derby](https://db.apache.org/derby/) — встраиваемая, возможность in-memory, на Java, open source.
 * [HyperSQL database](https://hsqldb.org/) — встраиваемая, in-memory, на Java, open source.
 * [SQLite](https://www.sqlite.org/) — встраиваемая, in-memory, на C, open source.
   Очень компактная, часто используется в Android-приложениях, но поддерживает
   SQL весьма избирательно.
* [MySQL](https://www.mysql.com/) — отдельная (не встраиваемая), open source.
* [PostgreSQL](https://www.postgresql.org/) — отдельная, open source.
  Есть очень стабильное ядро, к которому подключаются множество дополнительных
  и (полу)экспериментальных возможностей: специфические системы хранения,
  индексы, репликация, балансировщики нагрузки и так далее.
* [SQL Server](https://www.mysql.com/) — отдельная, проприетарная.
  Сделана Microsoft, используется, в основном, под Windows.
* [Oracle Database](https://www.mysql.com/) — отдельная, проприетарная.
  Если у вас много данных и денег.

[Java Persistence API](https://www.oracle.com/java/technologies/persistence-jsp.html)
([туториал](https://spring.io/guides/gs/accessing-data-jpa),
[справочник](https://docs.spring.io/spring-data/jpa/reference/jpa.html)) —
API для хранения графов специально размеченных Java-объектов в реляционных базах данных
(Object-relational mapping, ORM). Основывается на кодогенерации и
[рефлексии](https://blogs.oracle.com/javamagazine/post/java-reflection-introduction),
позволяющей получать сведения о классах, полях и методах во время исполнения программы.

Альтернативные подходы:
  * [Java Data Base Connectivity](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/package-summary.html)
    (JDBC, [туториал](https://docs.oracle.com/javase/tutorial/jdbc/basics/index.html),
    [Spring-обёртка](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/core/simple/JdbcClient.html))
    — базовое API для взаимодействия с базами данных, используется в JPA и почти всеми другими.
  * [Spring Data JDBC](https://spring.io/projects/spring-data-jdbc) — аналог JPA, но без JPA.
  * [Hibernate](https://hibernate.org/) — де-факто стандартная реализация ORM для JPA,
    используется и в Spring Data JPA.
  * [JOOQ Object Oriented Querying](https://www.jooq.org/)
    — объектно-ориентированный построитель запросов, платная альтернатива
    [LINQ](https://learn.microsoft.com/en-us/dotnet/csharp/linq/).
  * [JDBI](https://jdbi.org/) — связь запросов и методов через аннотации.


### Логирование (proto, server, client)

Для логирования используется [Simple Logging Facade for Java](https://www.slf4j.org/)
(SLF4J, [руководство](https://www.slf4j.org/manual.html))
с реализацией [Logback](https://logback.qos.ch/) ([руководство](https://logback.qos.ch/manual/)).

Альтернативные фреймворки для логирования (могут работать с SLF4J).
 * [JDK Platform Logging](https://docs.oracle.com/en/java/javase/21/docs/api/java.logging/java/util/logging/package-summary.html)
   ([руководство](https://docs.oracle.com/en/java/javase/21/core/java-logging-overview.html))
   — входит в стандартную библиотеку, но появился позже альтернатив, поэтому не особо популярен.
 * [Log4j](https://logging.apache.org/log4j/2.x/) — один из старейших фреймворков для логов
   ([руководство](https://logging.apache.org/log4j/2.x/manual/index.html)).
   Умеет много всего, часто слишком много всего.
 * [Apache Commons Logging](https://commons.apache.org/proper/commons-logging/) (JCL) —
   ещё один фреймворк от Apache, часто используется для проектов, которые начинались
   в рамках проекта [Jakarta](https://jakarta.apache.org/).


### Web-интерфейс (server)

Для реализации web-интерфейса используется фреймворк
[Vaadin](https://vaadin.com/) ([туториал](https://vaadin.com/docs/latest/tutorial),
[документация](https://vaadin.com/docs/latest/overview)).

Vaadin удобен тем, что позволяет описывать весь интерфейс Java-кодом, но, при этом,
для каждого действия пользователя (для которого есть слушатель) отправляется запрос
на сервер и ожидается ответ, что может быть долго.
Кроме того, для эффективной реализации страниц, содержащих много данных
(большие списки или таблицы) приходится писать генераторы HTML,
вместо использования встроенных компонент.

Альтернативные Java-фреймворки:
 * [Jakarta (ранее Java) Server Pages](https://jakarta.ee/specifications/pages/3.1/)
   (JSP; [туториал](https://docs.oracle.com/javaee/5/tutorial/doc/bnagx.html);
   [спецификация](https://jakarta.ee/specifications/pages/3.1/jakarta-server-pages-spec-3.1))
   — PHP-style шаблонизатор;
 * [Jakarta Faces](https://jakarta.ee/specifications/faces/4.0/) (ранее Java Server Faces, JSF;
   [туториал](https://eclipse-ee4j.github.io/jakartaee-tutorial/#the-web-tier),
   [спецификация](https://jakarta.ee/specifications/faces/4.0/))
   — набор web-компонентов для JSP со встроенной поддержкой
   [AJAX](https://en.wikipedia.org/wiki/Ajax_(programming)).

Альтернативные JavaScript фреймворки.
Взаимодействие с сервером обычно производится через пересылку [JSON](https://www.json.org/).
 * [React](https://react.dev/)
   ([туториал](https://react.dev/learn/tutorial-tic-tac-toe),
   [документация](https://react.dev/reference/react))
   — фреймворк с поддержкой автоматических обновлений при изменении состояния.
 * [Angular](https://angular.io/)
   ([туториалы](https://angular.io/tutorial), [документация](https://angular.io/docs))
   — полноценный [Model-View-Controller](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller)
   (MVC) фреймворк.
 * [Vue](https://vuejs.org/)
  ([туториал](https://vuejs.org/tutorial/), [документация](https://vuejs.org/guide/introduction.html))
  — относительно простой и non-opinionated фреймворк, но, желательно знать
  [TypeScript](https://www.typescriptlang.org/)
  ([туториал](https://www.typescriptlang.org/docs/handbook/typescript-in-5-minutes-oop.html),
  [документация](https://www.typescriptlang.org/docs/)).


### Работа с сетью (proto)

Используется [встроенная](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/package-summary.html)
в Java работа с TCP.
В классе [VpnProto.java](vpn-proto/src/main/java/info/kgeorgiy/vpn/proto/VpnProto.java)
реализован простой многопоточный TCP-форвардер, который перекладывает пакеты из одного
соединения в другое.

Для организации протокола используется поддержка встроенного ввода-вывода примитивных типов
([DataIS](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/InputOutputStream.html),
[DataOS](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/DataOutputStream.html)).
При создании соединения клиент пишет в него три строки: имя пользователя, пароль, целевой хост
и одно число: целевой номер порта.


### Шифрования трафика (server, client)

Java [поддерживает](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/javax/net/ssl/package-summary.html)
Transport Layer Security (TLS; ранее было известно как Secure Sockets Layer, SSL;
[туториал](https://geekflare.com/tls-101/), [спецификация](https://datatracker.ietf.org/doc/html/rfc8446))
из коробки.

Однако инициализация
[SSLContext](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/javax/net/ssl/SSLContext.html)
далека от очевидности и простоты, поэтому в проекте используется библиотека-обёртка
[Simple SSL](https://github.com/StevenJDH/simple-ssl)
([туториал](https://github.com/StevenJDH/simple-ssl#using-the-library)),
позволяющая сконфигурировать контекст в несколько строк.

Для генерации ключей используется [Java Keytool](https://docs.oracle.com/en/java/javase/21/docs/specs/man/keytool.html),
входящий в JDK.


### Интерфейс командной строки (client)

Для создания интерфейса командной строки в проекте используется библиотека
[Picocli](https://picocli.info/) ([туториал](https://picocli.info/quick-guide.html)),
позволяющая описывать команды, аргументы и опции при помощи аннотаций.

Альтернативные подходы:
* [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/)
  ([туториал](https://commons.apache.org/proper/commons-cli/usage.html),
  [документация](https://commons.apache.org/proper/commons-cli/apidocs/org/apache/commons/cli/package-summary.html))
  — часто используется в проектах Apache Foundation;
* [Spring Shell](https://spring.io/projects/spring-shell)
  — не только разбор командной строки, но и полноценные интерактивные приложения;
* ручной — в целом, всегда можно распарсить `args` руками, но не рекомендуется для
   сколько-нибудь сложных случаев.
