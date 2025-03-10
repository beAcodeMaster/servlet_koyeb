# 1단계: Maven 빌드를 통해 WAR 파일 생성
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app
# 필요한 파일 복사 (pom.xml, .env, 소스코드)
COPY pom.xml .
COPY src/ src/
COPY .env .
# 테스트를 건너뛰고 패키징: ex10.war 파일이 target 폴더에 생성됨
RUN mvn clean package -DskipTests

# 2단계: Tomcat에 WAR 파일 배포
FROM tomcat:10.1-jdk17
# 기본 Tomcat 설정 변경이 필요하면 server.xml 등 추가 파일 복사 가능
# build 단계에서 생성된 WAR 파일을 Tomcat의 webapps 디렉토리에 복사
COPY --from=build /app/target/ex10.war /usr/local/tomcat/webapps/
