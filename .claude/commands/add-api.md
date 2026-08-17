---
description: '자유 형식 설명을 받아 프로젝트 컨벤션에 맞춰 새 API 엔드포인트(Entity~Controller)를 추가합니다'
argument-hint: [엔드포인트에 대한 자연어 설명 (예: "댓글 작성 API, POST /api/comments")]
---

# Claude 명령어: Add API

## 사용법

```
/add-api 댓글 작성 API 추가해줘, POST /api/comments, 게시글에 내용(content)을 남기는 기능
/add-api 게시글 좋아요 취소, DELETE /api/posts/{postId}/likes
/add-api 회원 프로필 조회 API, GET /api/members/{memberId}/profile
```

## 입력

`$ARGUMENTS`: $ARGUMENTS

사용자가 자연어로 설명한 내용에서 다음을 추론한다:
- **도메인명**: 어떤 도메인(member, post 등)에 속하는지. 기존 도메인이면 재사용하고, 명확히 새로운 개념이면 신규 도메인 패키지를 만든다.
- **HTTP 메서드 / 경로**: 명시되어 있으면 그대로 따르고, 없으면 REST 컨벤션(`/api/{도메인복수형}`, 리소스 생성은 POST, 조회는 GET 등)에 맞춰 정한다.
- **요청/응답 필드**: 설명에 등장하는 명사(내용, 제목, 상태 등)를 기반으로 최소한의 필드를 구성한다. 애매하면 기존 유사 도메인의 필드 구성을 참고한다.
- **인증 필요 여부**: 설명에 "내 게시글", "로그인한 사용자" 등 사용자 맥락이 있으면 인증이 필요한 엔드포인트로 판단한다.

## 프로세스

1. **도메인 확인**: `src/main/java/com/example/apistarterkit/domain/{도메인}` 디렉토리가 이미 있는지 확인한다. 있으면 기존 구조를 그대로 재사용하고, 없으면 `controller/service/repository/entity/dto/{request,response}` 하위 패키지를 새로 만든다.
2. **Entity**: `BaseEntity`를 상속하고, `@NoArgsConstructor(access = AccessLevel.PROTECTED)` + `@Builder`가 붙은 private 생성자, 그리고 정적 팩토리 메서드(`create` 등)로만 인스턴스를 생성하도록 작성한다. setter는 두지 않고 상태 변경은 의도가 드러나는 도메인 메서드로 처리한다. 기존 엔티티에 필드/메서드만 추가하는 경우도 이 규칙을 따른다.
3. **Repository**: `JpaRepository<Entity, Long>`을 상속하는 인터페이스를 작성하고, 필요한 조회 조건은 쿼리 메서드로 추가한다.
4. **ErrorCode**: `global/exception/ErrorCode.java`에 이 엔드포인트에서 발생할 수 있는 예외를 추가한다. 도메인별 코드 프리픽스(공통 C0xx, 인증 A0xx, 회원 M0xx, 게시글 P0xx, 파일 F0xx)를 따르고, 새 도메인이면 다음 알파벳 프리픽스를 정해 일관되게 사용한다.
5. **Request DTO**: `dto/request`에 Java record로 작성하고, Bean Validation 애너테이션(`@NotBlank`, `@Size` 등)으로 필수 필드를 검증한다.
6. **Response DTO**: `dto/response`에 Java record로 작성하고, 별도 Mapper 클래스를 만들지 않고 `static XxxResponse from(Entity entity)` 정적 팩토리로 엔티티→DTO 변환을 처리한다.
7. **Service**: `@Service @RequiredArgsConstructor @Transactional(readOnly = true)`를 클래스에 붙이고, 쓰기 메서드에는 개별적으로 `@Transactional`을 붙인다. 조회 실패는 `orElseThrow(() -> new CustomException(ErrorCode.XXX))`로 처리하고, 중복되는 조회/검증 로직은 private 헬퍼 메서드로 뺀다.
8. **Controller**: `@Tag(name = "...", description = "...") @RestController @RequestMapping("/api/{도메인복수형}") @RequiredArgsConstructor`를 붙인다. 요청 바디는 `@Valid @RequestBody`로 받고, 응답은 `ApiResponse<T>`로 감싼다(생성처럼 상태 코드를 명시해야 하면 `ResponseEntity<ApiResponse<T>>` + `ResponseEntity.status(...)`, 단순 200 OK면 `ApiResponse<T>` 직접 반환). 인증이 필요하면 `SecurityUtil.getCurrentMemberId()`로 현재 사용자 ID를 얻는다. 컨트롤러 안에서 try-catch를 하지 않는다 — 예외는 `GlobalExceptionHandler`가 전역 처리한다.
9. **SecurityConfig**: 이 엔드포인트가 인증 없이 접근 가능해야 하거나 별도 인가 규칙이 필요하면 `global/config/SecurityConfig.java`를 갱신한다.

## 준수해야 할 컨벤션

- 모든 성공 응답은 `ApiResponse<T>`(`global/response/ApiResponse.java`)로 감싼다. 목록/페이징 응답은 `PageResponse<T>`를 사용한다.
- 모든 예외는 `CustomException` + `ErrorCode`로 던지고, 직접 예외를 잡아 응답을 만들지 않는다. `GlobalExceptionHandler`가 일관된 에러 JSON을 만들어준다.
- 엔티티에 setter를 두지 않는다. 상태 변경은 도메인 메서드로 표현한다.
- Entity ↔ DTO 변환은 별도 Mapper 클래스 대신 Response record의 `from()` 정적 팩토리로 처리한다.
- 기존 도메인(member, post)의 동일한 파일들을 참고해서 네이밍과 코드 스타일을 맞춘다.

## 완료 후

변경/생성된 파일 목록을 요약해서 알려주고, `./gradlew compileJava`로 컴파일이 되는지 확인한다.
