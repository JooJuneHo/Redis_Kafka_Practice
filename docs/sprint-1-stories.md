# 스프린트 1 스토리: 환경 + 버그 재현

> 상태: **확정** (2026-10-10)

## S1-1. 인프라 띄우기
- **스토리**: 학습자로서 명령 하나로 PostgreSQL·Redis·Kafka를 띄우고 싶다. 환경 세팅에 시간을 쓰지 않기 위해.
- **수용 기준**
  - `docker compose up -d`로 세 컨테이너가 모두 뜬다.
  - 포트: PostgreSQL 5432, Redis 6379, Kafka 9092. Kafka는 KRaft 모드(Zookeeper 없음).
- **의존성**: 없음

## S1-2. 프로젝트에 DB 연결
- **스토리**: 학습자로서 기존 Spring Boot 프로젝트가 PostgreSQL에 연결되어 실행되길 원한다.
- **수용 기준**
  - `build.gradle`에 Data JPA, PostgreSQL 드라이버 추가.
  - 앱이 기동되고 DB에 연결된다. 테이블은 `ddl-auto`로 생성.
- **의존성**: S1-1

## S1-3. 쿠폰 생성 API
- **스토리**: 학습자로서 테스트할 쿠폰을 만들고 싶다.
- **수용 기준**
  - `POST /coupons {name, totalQuantity}` → 저장 후 id 반환.
- **의존성**: S1-2

## S1-4. 쿠폰 발급 API (보호 장치 없음)
- **스토리**: 학습자로서 일부러 동시성 보호가 없는 발급 API를 만들어 버그를 재현하고 싶다.
- **수용 기준**
  - `POST /coupons/{id}/issue?userId=` → 쿠폰 조회 → `issued < total`이면 `issued+1` → `coupon_issue` 저장.
  - 수량 소진 시 409, 같은 user 재요청 시 409 (존재 여부 조회 + unique 제약).
  - 락·격리수준 조정·원자적 update를 **쓰지 않는다** (일부러).
- **의존성**: S1-3

## S1-5. k6로 버그 재현
- **스토리**: 학습자로서 동시 1000건을 보내 초과 발급을 눈으로 확인하고 싶다.
- **수용 기준**
  - k6를 Docker로 실행, VU 100명이 userId 1~1000으로 총 1000건 요청.
  - 실행 후 쿼리 한 줄로 `coupon_issue` 행 수와 `coupon.issued_quantity` 확인.
  - 수량 100개 쿠폰에서 **`coupon_issue`가 100건 초과** (또는 `issued_quantity` ≠ 실제 행 수).
- **의존성**: S1-4
