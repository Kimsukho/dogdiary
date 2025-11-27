# 환경 변수 설정 가이드

## 개요
이 프로젝트는 보안을 위해 민감한 정보(데이터베이스 연결 정보, JWT Secret 등)를 별도의 설정 파일로 분리합니다.

## 설정 파일 구조

### 1. `application.properties`
- **용도**: 공개 가능한 기본 설정만 포함
- **Git 관리**: ✅ 커밋됨
- **내용**: 포트, 애플리케이션 이름, MyBatis 설정, Thymeleaf 설정 등

### 2. `application-local.properties`
- **용도**: 로컬 개발 환경용 민감한 정보
- **Git 관리**: ❌ 커밋되지 않음 (.gitignore에 포함)
- **생성 방법**: 아래 "로컬 개발 환경 설정" 섹션 참조

### 3. `application-prod.properties`
- **용도**: 운영 환경용 민감한 정보
- **Git 관리**: ❌ 커밋되지 않음 (.gitignore에 포함)
- **생성 방법**: `application-prod.properties.example` 파일을 복사하여 생성

## 로컬 개발 환경 설정

### 1단계: `application-local.properties` 파일 생성

`src/main/resources/` 디렉토리에 `application-local.properties` 파일을 생성하고 다음 내용을 입력하세요:

```properties
# 데이터베이스 설정
spring.datasource.url=jdbc:mysql://localhost:3306/mydatabase?serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT 설정
jwt.secret=your-local-jwt-secret

# 로깅 설정 (개발용)
logging.level.org.springframework.security=DEBUG
logging.level.com.project=DEBUG
```

### 2단계: 실제 값 입력

- `spring.datasource.url`: 로컬 MySQL 데이터베이스 URL
- `spring.datasource.username`: 데이터베이스 사용자명
- `spring.datasource.password`: 데이터베이스 비밀번호
- `jwt.secret`: JWT 토큰 서명에 사용할 Secret 키

### 3단계: 프로파일 활성화

애플리케이션을 실행할 때 `local` 프로파일을 활성화하세요:

**방법 1: IDE에서 실행**
- Run Configuration에서 VM options에 추가:
  ```
  -Dspring.profiles.active=local
  ```

**방법 2: Maven으로 실행**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**방법 3: JAR 파일 실행**
```bash
java -jar -Dspring.profiles.active=local dog-diary.jar
```

## 운영 환경 설정

### 1단계: `application-prod.properties` 파일 생성

`src/main/resources/` 디렉토리에 `application-prod.properties.example` 파일을 복사하여 `application-prod.properties`로 이름을 변경하세요.

### 2단계: 운영 환경 값 입력

```properties
# 데이터베이스 설정
spring.datasource.url=jdbc:mysql://production-db-host:3306/production_db?serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=production_username
spring.datasource.password=production_password

# JWT 설정 (강력한 Secret 키 사용 필수!)
jwt.secret=your-strong-production-jwt-secret-key

# 로깅 설정
logging.level.org.springframework.security=INFO
logging.level.com.project=INFO

# Thymeleaf 캐시 활성화
spring.thymeleaf.cache=true

# H2 콘솔 비활성화
spring.h2.console.enabled=false
```

### 3단계: 프로파일 활성화

운영 환경에서는 `prod` 프로파일을 활성화하세요:

```bash
java -jar -Dspring.profiles.active=prod dog-diary.jar
```

## 환경 변수 사용 방법 (선택사항)

환경 변수를 직접 사용하는 방법도 있습니다. 이 경우 설정 파일 대신 시스템 환경 변수나 서버 환경 변수를 사용할 수 있습니다.

### 환경 변수 설정 예시

**Linux/Mac:**
```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/mydatabase
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=1234
export JWT_SECRET=your-secret-key
```

**Windows (PowerShell):**
```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/mydatabase"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="1234"
$env:JWT_SECRET="your-secret-key"
```

### application.properties에서 환경 변수 참조

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/mydatabase}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:root}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}
jwt.secret=${JWT_SECRET:default-secret}
```

위 형식은 환경 변수가 있으면 사용하고, 없으면 기본값을 사용합니다.

## 배포 플랫폼별 설정

### AWS (Elastic Beanstalk, EC2)
- 환경 변수를 Elastic Beanstalk 콘솔에서 설정
- 또는 `.ebextensions` 디렉토리에 설정 파일 배치

### Heroku
- Heroku Config Vars 사용:
  ```bash
  heroku config:set SPRING_DATASOURCE_URL=jdbc:mysql://...
  heroku config:set SPRING_DATASOURCE_USERNAME=...
  heroku config:set SPRING_DATASOURCE_PASSWORD=...
  heroku config:set JWT_SECRET=...
  ```

### Docker
- `docker-compose.yml`에서 환경 변수 설정:
  ```yaml
  environment:
    - SPRING_DATASOURCE_URL=jdbc:mysql://...
    - SPRING_DATASOURCE_USERNAME=...
    - SPRING_DATASOURCE_PASSWORD=...
    - JWT_SECRET=...
  ```

### Kubernetes
- ConfigMap 또는 Secret 사용:
  ```yaml
  apiVersion: v1
  kind: Secret
  metadata:
    name: dog-diary-secret
  type: Opaque
  stringData:
    spring.datasource.url: jdbc:mysql://...
    spring.datasource.username: ...
    spring.datasource.password: ...
    jwt.secret: ...
  ```

## 보안 권장사항

1. **강력한 비밀번호 사용**: 데이터베이스 비밀번호와 JWT Secret은 충분히 강력하게 설정
2. **JWT Secret**: 운영 환경에서는 최소 32자 이상의 랜덤 문자열 사용 권장
3. **파일 권한**: 민감한 설정 파일은 적절한 파일 권한 설정 (예: 600)
4. **환경 변수**: 가능하면 환경 변수 사용을 권장 (설정 파일보다 더 안전)
5. **정기적인 변경**: 운영 환경의 비밀번호와 Secret은 정기적으로 변경

## 문제 해결

### 설정이 적용되지 않는 경우

1. **프로파일 확인**: `spring.profiles.active`가 올바르게 설정되었는지 확인
2. **파일 위치 확인**: 설정 파일이 `src/main/resources/` 디렉토리에 있는지 확인
3. **파일 이름 확인**: `application-{profile}.properties` 형식이 맞는지 확인
4. **로깅 확인**: 애플리케이션 시작 시 어떤 프로파일이 활성화되었는지 로그 확인

### 로그에서 프로파일 확인

애플리케이션 시작 시 다음과 같은 로그가 출력됩니다:
```
The following profiles are active: local
```

## 참고 자료

- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Spring Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)

