/**
 * SPIKE TEST
 * ----------
 * Simulates a sudden burst of users (e.g. everyone logs in at 9am).
 * Goes from 0 to 200 users in 10 seconds, holds briefly, drops back.
 * Goal: does the system recover after a spike, or does it stay broken?
 *
 * Run: k6 run scenarios/spike.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, ADMIN_CREDENTIALS, THRESHOLDS } from '../config.js';
import { login, authHeaders } from '../auth.js';

export const options = {
  stages: [
    { duration: '10s', target: 5 },    // baseline
    { duration: '10s', target: 200 },  // spike — 200 users in 10 seconds
    { duration: '1m',  target: 200 },  // stay at spike level
    { duration: '10s', target: 5 },    // drop back
    { duration: '2m',  target: 5 },    // recovery period — does latency return to normal?
  ],
  thresholds: {
    ...THRESHOLDS,
    // During spike we allow higher failure rates
    http_req_failed: ['rate<0.05'],
  },
};

export function setup() {
  return { token: login(ADMIN_CREDENTIALS) };
}

export default function (data) {
  const headers = authHeaders(data.token);

  // Spike scenario: everyone loads the users list simultaneously
  const res = http.get(
    `${BASE_URL}/api/users?pageSize=20`,
    { ...headers, tags: { endpoint: 'list_users' } }
  );

  check(res, {
    'spike: status not 5xx': (r) => r.status < 500,
    'spike: responded in time': (r) => r.timings.duration < 3000,
  });

  sleep(1);
}
