# API Starter Kit

Spring Boot 백엔드 API 서버를 새로 시작할 때 반복해서 만들게 되는 것들(CRUD, 페이징, 인증, 공통 응답/예외 처리, 문서화, Docker, 테스트)을 미리 갖춰둔 재사용용 스타터킷입니다.

## 기술 스택

- Java 21, Spring Boot 3.5.4, Gradle(Groovy DSL)
- Spring Data JPA + MySQL(로컬/운영), H2(테스트)
- Spring Security + JWT(Access/Refresh) + OAuth2 소셜 로그인(Google)
- Redis (Refresh Token 저장, 로그아웃 시 Access Token 블랙리스트)
- springdoc-openapi(Swagger UI), Docker/docker-compose
- JUnit5, Mockito, MockMvc, Testcontainers

## 패키지 구조

```
com.example.apistarterkit
├── global   # config, exception, response, security(jwt/oauth2), entity, logging, file
└── domain
    ├── member  # 회원가입/로그인/재발급/로그아웃, 내 정보
    └── post    # CRUD + 페이징 대표 예제
```

## 로컬 실행

1. 환경변수 파일 준비
   ```bash
   cp .env.example .env
   # JWT_SECRET, (선택) GOOGLE_CLIENT_ID/SECRET 값을 채워주세요
   ```
2. 애플리케이션 실행 (MySQL/Redis는 `spring-boot-docker-compose`가 `docker-compose.yml`을 감지해 자동으로 기동합니다)
   ```bash
   ./gradlew bootRun
   ```
3. Swagger UI: http://localhost:8080/swagger-ui.html
4. 헬스체크: http://localhost:8080/actuator/health

Docker가 없다면 MySQL/Redis를 직접 띄우고 `application-local.yml`의 접속 정보를 맞춰주세요.

## 전체 스택을 컨테이너로 실행

```bash
docker compose --profile full up -d --build
```

기본 `docker compose up`(로컬 개발용, mysql/redis만 기동)과 달리 `--profile full`을 주면 앱 컨테이너까지 함께 빌드/실행합니다.

## 주요 API

| 분류 | Method | Path | 설명 |
|---|---|---|---|
| Auth | POST | `/api/auth/signup` | 회원가입 |
| Auth | POST | `/api/auth/login` | 로그인 (JWT 발급) |
| Auth | POST | `/api/auth/reissue` | Access/Refresh Token 재발급 |
| Auth | POST | `/api/auth/logout` | 로그아웃 |
| Auth | GET | `/oauth2/authorization/google` | Google 소셜 로그인 시작 |
| Member | GET | `/api/members/me` | 내 정보 조회 |
| Post | POST | `/api/posts` | 게시글 작성 |
| Post | GET | `/api/posts?page=0&size=10` | 게시글 목록(페이징) |
| Post | GET | `/api/posts/{id}` | 게시글 상세 |
| Post | PUT | `/api/posts/{id}` | 게시글 수정(작성자만) |
| Post | DELETE | `/api/posts/{id}` | 게시글 삭제(작성자만) |
| File | POST | `/api/files` | 파일 업로드 |
| File | GET | `/api/files/{fileName}` | 파일 다운로드 |

모든 응답은 `{ success, data, error }` 형태(`ApiResponse<T>`)로 통일되어 있습니다.

## 테스트

```bash
./gradlew test                        # 전체 (통합 테스트는 Docker 필요)
./gradlew test --tests "*ServiceTest"  # 단위 테스트만
```

## 확장 포인트 (스타터킷 범위 밖으로 의도적으로 제외한 것들)

- **DB 마이그레이션**: 지금은 `ddl-auto: update`. 운영에서는 Flyway/Liquibase 도입을 권장합니다.
- **파일 스토리지**: 현재 로컬 디스크(`LocalFileStorageService`). S3 등으로 바꾸려면 `FileStorageService` 인터페이스를 구현해 교체하세요.
- **분산 트레이싱**: MDC traceId만 로그에 남깁니다. Zipkin/OpenTelemetry 연동은 포함하지 않았습니다.
