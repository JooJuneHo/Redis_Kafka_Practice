# 스프린트 2 스토리: Redis로 동시성 해결

> 상태: **확정** (2026-10-16)

## 결정 사항
- 방식별 엔드포인트를 모두 남긴다: `/issue`(보호 없음), `/issue/lock`(분산락), `/issue/redis`(INCR+SADD). k6는 `-e MODE=`로 선택.
- Redisson은 `redisson-spring-boot-starter:4.8.0` 사용 (Boot 4.1.0 기준 빌드 확인). `RedissonClient`는 자동 등록되므로 빈 직접 생성 불필요.
- `/issue/redis`에서는 DB의 `coupon.issued_quantity`를 갱신하지 않는다. Redis 카운터가 기준이며, 발급 건수는 `coupon_issue` 행 수로 확인.

## S2-1. Redis 연결
- **스토리**: 학습자로서 Spring 앱에서 Redis 명령을 실행하고 싶다.
- **수용 기준**
  - `docker compose up -d redis`로 Redis 기동.
  - `spring-boot-starter-data-redis` 추가, `StringRedisTemplate`로 값 쓰기/읽기 확인.
- **의존성**: 없음

## S2-2. 분산락으로 해결
- **스토리**: 학습자로서 Redisson 락으로 발급 로직을 한 번에 하나씩만 실행해 초과 발급을 막고 싶다.
- **수용 기준**
  - `POST /coupons/{id}/issue/lock?userId=` — 쿠폰 단위 락 `lock:coupon:{id}`.
  - 락은 **트랜잭션 바깥**: 락 획득 → 트랜잭션 시작 → 커밋 → 락 해제. (반대로 하면 커밋 전에 락이 풀려 버그가 남는 것을 확인)
  - k6: 200이 **정확히 100**, `issued_quantity` 100, 행 수 100.
  - 1단계 대비 느려진 것(`http_req_duration`)을 확인.
- **의존성**: S2-1

## S2-3. INCR + SADD로 해결 (락 없음)
- **스토리**: 학습자로서 락 없이 Redis 원자 연산만으로 초과·중복 발급을 막고 싶다.
- **수용 기준**
  - `POST /coupons/{id}/issue/redis?userId=`
  - `SADD coupon:{id}:users {userId}` 결과 0 → 409 (중복).
  - `INCR coupon:{id}:count` 결과 > 총수량 → 409 (소진).
  - 통과한 요청만 `coupon_issue`에 저장. `issued_quantity`는 갱신하지 않음.
  - k6: 200이 **정확히 100**, 행 수 100. 분산락보다 빠른지 비교.
- **의존성**: S2-1
