# 1단계: Maven을 이용해 WAR 파일 자동 빌드
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app
# pom.xml과 소스코드를 컨테이너에 복사
COPY pom.xml .
COPY src/ src/
# 테스트를 건너뛰고 패키징하여 WAR 파일 생성 (target/ex10.war)
RUN mvn clean package -DskipTests

# 2단계: Tomcat 컨테이너에 빌드된 WAR 파일 배포
FROM tomcat:10.1-jdk17
# Tomcat의 기본 webapps 디렉토리에 WAR 파일 복사
COPY --from=build /app/target/ex10.war /usr/local/tomcat/webapps/
