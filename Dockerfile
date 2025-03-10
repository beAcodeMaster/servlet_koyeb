# 1단계: Maven을 이용해 프로젝트 빌드
FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /app

# 프로젝트 파일을 복사
COPY pom.xml .
RUN mvn dependency:go-offline  # 필요한 의존성 미리 다운로드

COPY src ./src
RUN mvn clean package -DskipTests  # 테스트를 건너뛰고 빌드

# 2단계: Tomcat 컨테이너에서 실행
FROM tomcat:10.1.0-jdk17-temurin
WORKDIR /usr/local/tomcat/webapps/

# 빌드된 WAR 파일을 Tomcat에 복사
COPY --from=build /app/target/ex10.war ROOT.war

# Tomcat 실행
EXPOSE 8080
CMD ["catalina.sh", "run"]