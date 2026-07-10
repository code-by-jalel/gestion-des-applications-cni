/**
 * AUTH STRESS TEST
 * ----------------
 * Hammers the login endpoint specifically.
 * Each virtual user logs in repeatedly — this directly stresses OpenLDAP's
 * bind operation, which is the most expensive single LDAP call in your system.
 * Goal: find how many concurrent logins LDAP can handle before it degrades.
 *
 * Run: k6 run scenarios/auth_stress.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, ADMIN_CREDENTIALS } from '../config.js';

export const options = {
  stages: [
    { duration: '1m', target: 20 },
    { duration: '2m', target: 50 },
    { duration: '2m', target: 100 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    // LDAP bind is inherently slower than a DB password check —
    // 1 second p95 is a reasonable target, 3 seconds is the hard fail
    'http_req_duration{endpoint:login}': ['p(95)<1000', 'p(99)<3000'],
    http_req_failed: ['rate<0.01'],
  },
};

export default function () {
  const res = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify(ADMIN_CREDENTIALS),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { endpoint: 'login' },
    }
  );

  check(res, {
    'login: status 200': (r) => r.status === 200,
    'login: has token': (r) => !!r.json('token'),
    'login: under 1s': (r) => r.timings.duration < 1000,
  });

  // simulate the user staying logged in for a bit before logging in again
  sleep(Math.random() * 5 + 2);
}
