# 1. 베이스 이미지 설정 (JDK 환경)
FROM openjdk:17-jdk-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 빌드된 jar 파일을 컨테이너로 복사
# (build/libs/ 안에 있는 .jar 파일을 app.jar로 복사)
COPY build/libs/*.jar app.jar

# 4. 앱 실행
ENTRYPOINT ["java", "-jar", "app.jar"]