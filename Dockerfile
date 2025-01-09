FROM mysql:8.0

ENV MYSQL_ROOT_PASSWORD=root
ENV MYSQL_DATABASE=uozap

COPY src/main/resources/db_init.sql /docker-entrypoint-initdb.d/

EXPOSE 3306