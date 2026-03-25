# 로컬 실행·테스트 — Gateway / Proxy / usage-service (사용량 이벤트까지)

버전: 1.0  

OpenAI 등 API 키를 두고 **게이트웨이 → 프록시 → Provider** 호출 시 **`UsageRecordedEvent`가 RabbitMQ로 발행**되고 **`usage-service`가 DB에 저장**하는 흐름을 로컬에서 재현·검증하는 절차입니다.  
관련 설계 문서: [`contracts/gateway-proxy.md`](contracts/gateway-proxy.md), [`event-consumer-flow.md`](event-consumer-flow.md), [`usage-analytics-relationship.md`](usage-analytics-relationship.md) §3.1(패턴 A).

---

## 전제

- **인증·로그인 서비스는 사용하지 않는다**고 가정(캡스톤/개발 모드). Gateway는 `GATEWAY_DEV_MODE=true`(기본에 가깝게)일 때 **`X-User-Id` 헤더**만으로 `/api/v1/ai/**` 진입 가능.
- 실제 OpenAI 호출을 하려면 **`PROXY_OPENAI_TEST_API_KEY`**(또는 동등 설정)에 유효한 키가 필요하다. 키는 **커밋하지 말고** `.env` 등 로컬에만 둔다(`.env.example` 참고).

---

## 1. 인프라·앱 기동 순서

### 1.1 Docker Compose (PostgreSQL, RabbitMQ, Redis, 선택: proxy·gateway)

저장소 루트에서:

```bash
docker compose up -d
```

- Postgres: 보통 `localhost:5432`, DB/사용자는 `.env` 또는 `.env.example`의 `POSTGRES_*` 와 맞출 것.
- RabbitMQ: AMQP `5672`, 관리 UI `15672`(guest/guest).
- `docker-compose.yml`에 **proxy-service**, **api-gateway-service**가 포함되어 있으면 함께 기동된다.

### 1.2 OpenAI 키를 proxy에 전달

`proxy-service`는 `proxy.key-service.mock-key` ← 환경 변수 **`PROXY_OPENAI_TEST_API_KEY`** 로 테스트용 upstream 키를 읽는다. 키는 **절대 커밋하지 말고**, 저장소 루트의 **`.env`**에만 둔다(`.gitignore`에 포함됨).

**권장(Compose로 proxy 기동):**

1. 저장소 **루트**에서 `.env.example`을 복사해 `.env`를 만든다.  
   - 예: `copy .env.example .env`(cmd), `Copy-Item .env.example .env`(PowerShell).
2. `.env` 안에서 **`PROXY_OPENAI_TEST_API_KEY=`** 줄을 찾아(없으면 추가) 등호 뒤에 실제 키를 붙인다. `.env.example`에는 주석으로만 안내되어 있을 수 있으니, 동일 이름의 변수 한 줄을 `.env`에 두면 된다.
3. 루트에서 **`docker compose up`**을 실행한다. Compose는 **같은 디렉터리의 `.env`**를 읽어 `${PROXY_OPENAI_TEST_API_KEY}`를 치환하고, `docker-compose.yml`의 **`proxy-service`** `environment`로 컨테이너에 전달한다.

**프록시를 호스트에서** `gradlew bootRun`만 하는 경우: 루트 `.env`는 Spring Boot/Gradle이 자동으로 읽지 않으므로, IDE 실행 구성이나 셸에서 동일 이름으로 설정한다(예: PowerShell `$env:PROXY_OPENAI_TEST_API_KEY = "sk-..."`).

### 1.3 usage-service (호스트에서 실행)

Compose의 Postgres·Rabbit과 **같은 호스트·포트**로 붙인다.

```bash
cd services/usage-service
# Windows PowerShell 예시
$env:POSTGRES_HOST = "localhost"
$env:SPRING_RABBITMQ_HOST = "localhost"   # application.yml 기본과 동일하면 생략 가능
.\gradlew.bat bootRun
```

- 기본 HTTP 포트: **`8092`** (`USAGE_SERVICE_PORT`로 변경 가능).
- 상세 설정: `services/usage-service/src/main/resources/application.yml`.

### 1.4 실행 순서 요약

1. `docker compose up -d` (DB·큐·필요 시 gateway·proxy)
2. (호스트에서) **`usage-service` `bootRun`**
3. Gateway·Proxy가 이미 Compose에 있지 않다면 각각 `services/`에서 `bootRun` (Gateway `8080`, Proxy `8081`).

---

## 2. AI 호출 (Gateway 경로)

개발 모드에서는 **JWT 없이** `X-User-Id`만 보낸다.

```http
POST http://localhost:8080/api/v1/ai/openai/v1/chat/completions
Content-Type: application/json
X-User-Id: test-user-1

{
  "model": "gpt-4o-mini",
  "messages": [{ "role": "user", "content": "hello" }]
}
```

- 응답이 200이고 본문이 오면, 프록시는 upstream 호출 후 응답에서 usage를 파싱해 이벤트를 발행한다.

---

## 3. 로그로 확인하기

### 3.1 proxy-service

현재 구현에서는 **usage 발행 성공 전용 `INFO` 로그가 거의 없다.** 실패는 upstream/API 키에서 드러난다.

### 3.2 usage-service

성공 시 **`DEBUG`** 레벨 로그만 남는다:

- `Stored usage event eventId=… userId=…`
- 중복 `eventId`: `Skipping duplicate usage event …`

실패 시 **`ERROR`**:

- `Failed to deserialize or persist UsageRecordedEvent`

`application.yml` 또는 환경 변수로 로그 레벨을 올린다:

```yaml
logging:
  level:
    com.eevee.usageservice: DEBUG
```

(Spring Boot 속성: `logging.level.com.eevee.usageservice=DEBUG`)

---

## 4. RabbitMQ 관리 UI로 확인

1. 브라우저: `http://localhost:15672` (guest/guest).
2. Exchange **`usage.events`**, 라우팅 키 **`usage.recorded`**, 큐 **`usage-service.queue`** 를 확인한다.
3. AI 호출 전후 **message rates / ready messages** 변화를 본다(소비가 빠르면 큐에 안 쌓일 수 있음).

---

## 5. PostgreSQL로 최종 확인 (권장)

`usage-service` 테이블 **`usage_recorded_log`** 에 행이 들어가면 소비·저장까지 성공한 것이다.

```bash
docker exec -it <postgres_컨테이너_이름> psql -U app -d app -c "SELECT event_id, user_id, provider, model, prompt_tokens, completion_tokens, total_tokens, occurred_at, persisted_at FROM usage_recorded_log ORDER BY persisted_at DESC LIMIT 10;"
```

(`POSTGRES_USER` / `POSTGRES_DB` 는 팀 `.env`에 맞게 조정.)

---

## 6. 자동 테스트 (코드 검증)

저장소에 **Wire 형식·멱등·Testcontainers 통합** 테스트가 있다:

```bash
cd services/usage-service
./gradlew test
# Windows: gradlew.bat test
```

- **통합 테스트**(`UsageRecordedEventPipelineIntegrationTest`)는 **Docker(Testcontainers)** 가 필요하다.

---

## 7. 자주 나는 문제

| 증상 | 점검 |
|------|------|
| Proxy 401/키 오류 | `PROXY_OPENAI_TEST_API_KEY` 미설정 또는 Compose에 미전달 |
| usage-service DB 연결 실패 | `POSTGRES_HOST`·포트·DB명이 Compose와 일치하는지 |
| usage-service Rabbit 연결 실패 | `localhost:5672`, guest 계정, Rabbit 기동 여부 |
| Gateway 401 | 개발 모드가 꺼져 있으면 JWT 필요. `GATEWAY_DEV_MODE`·`X-User-Id` 확인 |
| 로그에 저장 흔적 없음 | usage-service에 `com.eevee.usageservice` **DEBUG** 활성화 |

---

## 8. 관련 문서

- Gateway ↔ Proxy 경로·헤더: [`contracts/gateway-proxy.md`](contracts/gateway-proxy.md)
- 이벤트 팬아웃: [`event-consumer-flow.md`](event-consumer-flow.md)
- usage / analytics 역할: [`usage-analytics-relationship.md`](usage-analytics-relationship.md)
