import http from 'k6/http';
import { check } from 'k6';
import exec from 'k6/execution';

export const options = {
  scenarios: {
    issue: {
      executor: 'shared-iterations', // VU들이 총 횟수를 나눠서 실행
      vus: 100,                      // 동시 사용자 100명
      iterations: 1000,              // 총 1000건
    },
  },
};

const COUPON_ID = __ENV.COUPON_ID || 1;

export default function () {
  const userId = exec.scenario.iterationInTest + 1; // 1~1000, 요청마다 다른 사용자
  const res = http.post(
    `http://host.docker.internal:8080/coupons/${COUPON_ID}/issue?userId=${userId}`
  );
  check(res, {
    '발급 성공(200)': (r) => r.status === 200,
    '소진(409)': (r) => r.status === 409,
  });
}
