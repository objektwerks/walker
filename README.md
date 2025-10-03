Walker
------
>Walker app using ScalaFx, ScalikeJdbc, Jsoniter, JoddMail, PostgreSql, HikariCP, Helidon, Ox and Scala 3.

Model
-----
>A session represents a walk.
* Account 1 ---> * Walker
* Walker 1 ---> * Session

Calculations
------------
1. [Calories Burned Walking](https://captaincalculator.com/health/calorie/calories-burned-walking-calculator/)
   
Build
-----
1. sbt clean compile

Test
----
1. sbt clean test

Server Run
----------
1. sbt server/run

Client Run
----------
1. sbt client/run

Package Server
--------------
1. sbt server/universal:packageBin
2. see server/target/universal

Client Assembly
---------------
1. sbt clean test assembly copyAssemblyJar

Execute Client
--------------
1. java -jar .assembly/walker-$version.jar ( or double-click executable jar )

Postgresql
----------
1. install: You may have to edit your .bash(rc) or .zprofile to include:
   1. export POSTGRESQL_BIN="/opt/homebrew/Cellar/postgresql@18/18.0/bin" to
      1. export PATH=/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin:$JAVA_HOME/bin:$SCALA_HOME/bin:$GRAAL_VM_HOME/bin:$GRADLE_HOME/bin:$MAVEN_HOME/bin:$VSCODE_BIN:$COURSIER_BIN:$POSTGRESQL_BIN:$PATH
2. config:
    1. on osx intel: /usr/local/var/postgres/postgresql.config : listen_addresses = ‘localhost’, port = 5432
    2. on osx m1: /opt/homebrew/var/postgres/postgresql.config : listen_addresses = ‘localhost’, port = 5432
3. run:
    1. brew services start postgresql@18
4. logs:
    1. on osx intel: /usr/local/var/log/postgres.log
    2. on m1: /opt/homebrew/var/log/postgres.log

Database
--------
>Example database url: postgresql://localhost:5432/walker?user=mycomputername&password=walker"
1. psql postgres
2. CREATE DATABASE walker OWNER [your computer name];
3. GRANT ALL PRIVILEGES ON DATABASE walker TO [your computer name];
4. \l
5. \q
6. psql walker
7. \i ddl.sql
8. \q

DDL
---
>Alternatively run: psql -d walker -f ddl.sql
1. psql walker
2. \i ddl.sql
3. \q

Drop
----
1. psql postgres
2. drop database walker;
3. \q

Environment
-----------
>The following environment variables must be defined:
```
export WALKER_HOST="127.0.0.1"
export WALKER_PORT=7070
export WALKER_ENDPOINT="/command"

export WALKER_CACHE_INITIAL_SIZE=4
export WALKER_CACHE_MAX_SIZE=10
export WALKER_CACHE_EXPIRE_AFTER=24

export WALKER_POSTGRESQL_URL="jdbc:postgresql://localhost:5432/walker"
export WALKER_POSTGRESQL_USER="yourusername"
export WALKER_POSTGRESQL_PASSWORD="walker"
export WALKER_POSTGRESQL_DRIVER="org.postgresql.ds.PGSimpleDataSource"
export WALKER_POSTGRESQL_DB_NAME="walker"
export WALKER_POSTGRESQL_HOST="127.0.0.1"
export WALKER_POSTGRESQL_PORT=5432
export WALKER_POSTGRESQL_POOL_INITIAL_SIZE=9
export WALKER_POSTGRESQL_POOL_MAX_SIZE=32
export WALKER_POSTGRESQL_POOL_CONNECTION_TIMEOUT_MILLIS=30000

export WALKER_EMAIL_HOST="your-email.provider.com"
export WALKER_EMAIL_ADDRESS="your-email@provider.com"
export WALKER_EMAIL_PASSWORD="your-email-password"
```

Resources
---------
* [JavaFX](https://openjfx.io/index.html)
* [JavaFX Tutorial](https://jenkov.com/tutorials/javafx/index.html)
* [ScalaFX](http://www.scalafx.org/)
* [ScalikeJdbc](http://scalikejdbc.org/)

License
-------
>Copyright (c) [2023 - 2025] [Objektwerks]

>Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    * http://www.apache.org/licenses/LICENSE-2.0

>Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
