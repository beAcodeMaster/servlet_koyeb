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

# ✅ Render가 Tomcat을 찾을 수 있도록 환경 변수 설정
ENV CATALINA_OPTS="-Djava.security.egd=file:/dev/./urandom"
ENV PORT=8080

# ✅ Tomcat이 8080 포트를 노출하도록 설정
EXPOSE 8080

# ✅ Tomcat 실행 명령 추가
CMD ["catalina.sh", "run"]
