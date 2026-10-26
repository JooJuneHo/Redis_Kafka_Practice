# PRD: 선착순 쿠폰 발급 시스템 (Redis·Kafka 학습용)

> 상태: **확정** (2026-10-10)

## 1. 한 줄 요약
선착순 쿠폰 발급 API를 단계별로 고쳐 나가며, **Redis와 Kafka가 왜 필요한지 직접 겪고 해결해 보는** 학습 프로젝트.

## 2. 목표
- 동시 요청으로 쿠폰이 초과 발급되는 버그를 재현하고, Redis(분산락 → 원자 연산)로 해결한다.
- Redis Sorted Set으로 선착순 대기열을 만든다.
- DB 저장을 Kafka로 비동기 처리하고, 컨슈머 장애 시 유실·중복을 다뤄 본다.
- 수준: **개념 이해**. 성능 수치 리포트·문서화는 하지 않는다. 기간은 최대한 짧게.

## 3. 사용자
- 본인 1명. 요청은 k6 부하 테스트와 curl로만 보낸다.

## 4. 기술 스택
- Spring Boot 4.1.1 / Java 21 (Azul, `~/.jdks/azul-21.0.11`) / Gradle / Lombok
- 프로젝트: `바탕화면/쿠폰 발급/Coupon`, 패키지 `com.coupon.coupon`
- PostgreSQL, Redis, Kafka — 모두 Docker Compose
- Redisson(분산락), spring-kafka
- k6 — Docker로 실행 (설치 불필요)

## 5. 기능

### API
- `POST /coupons` — 쿠폰 생성 (이름, 총수량)
- `POST /coupons/{id}/issue?userId=` — 쿠폰 발급

### 데이터
- `coupon`(id, name, total_quantity, issued_quantity)
- `coupon_issue`(id, coupon_id, user_id, created_at) — `(coupon_id, user_id)` unique

### 단계
1. **DB만** — 단순 구현 → 수량 100개 쿠폰에 동시 1000건 → 초과 발급 재현
2. **Redis 분산락** — Redisson 락으로 발급 로직을 감싸 해결. 느려지는 것 체감
3. **Redis 원자 연산** — `INCR`로 수량 카운트 + `SADD`로 1인 1매. 락 없이 해결 → 2단계와 비교
4. **Kafka 비동기 저장** — API는 Redis 검증 후 이벤트만 발행, 컨슈머가 DB 저장
5. **Kafka 장애 실습** — 컨슈머 강제 종료/재시작 → offset 확인, 중복 수신 멱등 처리(unique 제약), 실패 메시지 DLQ
6. **Redis 대기열** — `ZADD`(score=요청 시각)로 줄 세우기, 스케줄러가 앞에서 N명씩 꺼내 발급, 사용자는 자기 순번 조회

## 6. 범위 밖
- 수정·삭제·목록 API, 회원/인증, 화면
- 파티션 확장·멀티 컨슈머 튜닝, 성능 수치 리포트

## 7. 성공 기준
- 1단계에서 초과 발급이 재현된다.
- 2·3단계에서 동시 1000건에도 발급이 정확히 100건.
- 4단계에서 DB에 최종 100건 저장.
- 5단계에서 컨슈머가 죽었다 살아나도 DB 100건, 중복 없음.
- 6단계에서 요청 순서대로 발급되고, 순번 조회가 된다.
