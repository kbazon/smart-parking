@echo off
cd /d put_do_projekta

start cmd /k mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
start cmd /k mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=8082

echo Spring Boot instances started.
pause