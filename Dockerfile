# 1단계: Maven 빌드를 통해 WAR 파일 생성
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app

# pom.xml 먼저 복사하여 의존성 레이어 캐싱
COPY pom.xml .
# 의존성만 먼저 해결 (캐싱 효과)
RUN mvn dependency:go-offline

# 소스 코드 복사
COPY src/ src/
# .env 파일이 필요 없음 - Koyeb에서 환경 변수 사용

# 빌드 디버그 정보 출력
RUN ls -la && \
    # resources 디렉토리가 없을 경우 생성
    mkdir -p src/main/resources && \
    # Maven 빌드 실행 (디버그 모드)
    mvn clean package -DskipTests -X

# 2단계: Tomcat에 WAR 파일 배포
FROM tomcat:10.1-jdk17

# build 단계에서 생성된 WAR 파일을 Tomcat의 webapps 디렉토리에 복사
COPY --from=build /app/target/ex10.war /usr/local/tomcat/webapps/

# 8080 포트 노출
EXPOSE 8080

# Tomcat 실행
CMD ["catalina.sh", "run"]