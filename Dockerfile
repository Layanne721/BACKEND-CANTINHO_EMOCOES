# --- Estágio 1: Build (Construção) ---
# Usamos uma imagem Maven com Java 17 para compilar o projeto
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copia o pom.xml e baixa as dependências (cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código fonte e faz o build
COPY src ./src
# O comando abaixo gera o .jar e pula os testes para agilizar o deploy
RUN mvn clean package -DskipTests

# --- Estágio 2: Run (Execução) ---
# Usamos o Eclipse Temurin 17 JRE (versão padrão, melhor para bibliotecas de PDF)
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copia o .jar gerado no estágio anterior
# O nome do jar será baseado no seu artifactId e version
COPY --from=build /app/target/cantinho_emocoes-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta 8080
EXPOSE 8080

# Comando para iniciar
ENTRYPOINT ["java", "-jar", "app.jar"]