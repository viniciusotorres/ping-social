# Etapa 1: build da aplicação
FROM ubuntu:latest as build

# Atualiza pacotes e instala o JDK 17 e Maven
RUN apt-get update && apt-get install -y openjdk-17-jdk maven

# Define variáveis de ambiente (opcional durante build)
ENV PGHOST=localhost
ENV PGPORT=5432
ENV PGDATABASE=ping_db
ENV PGUSER=postgres
ENV PGPASSWORD=1234

# Copia os arquivos da aplicação
COPY . .

# Build do projeto (pula os testes)
RUN mvn clean install -DskipTests

# Etapa 2: imagem final leve com o JDK para rodar o app
FROM openjdk:17-jdk-slim

# Define porta da aplicação
EXPOSE 8080

# Copia o .jar gerado na etapa anterior
COPY --from=build /target/pingsocial-0.0.1-SNAPSHOT.jar app.jar

# Comando de inicialização
ENTRYPOINT ["java", "-jar", "/app.jar"]
