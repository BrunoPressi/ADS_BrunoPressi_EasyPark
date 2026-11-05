# =============================================================================
# Stage 1: Build da aplicação Quarkus (Maven + Java 21)
# =============================================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /build

# Limita o uso de memória do Maven para rodar com segurança em instâncias EC2 t2.micro/t3.micro
ENV MAVEN_OPTS="-Xmx512m"

# Cache das dependências do Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compilação do pacote Quarkus (fast-jar)
COPY src ./src
RUN mvn package -DskipTests -B

# =============================================================================
# Stage 2: Imagem final enxuta de execução (JRE 21 Alpine)
# =============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /deployments

# Configura fuso horário padrão (importante para agendamentos e datas de permanência/parcelas)
ENV TZ=America/Sao_Paulo
ENV LANG=pt_BR.UTF-8
ENV JAVA_OPTS="-Xms128m -Xmx400m -XX:+ExitOnOutOfMemoryError"

# Copia as camadas do fast-jar gerado pelo Quarkus
COPY --from=build /build/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build /build/target/quarkus-app/*.jar /deployments/
COPY --from=build /build/target/quarkus-app/app/ /deployments/app/
COPY --from=build /build/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager -jar /deployments/quarkus-run.jar"]
