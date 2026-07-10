/**
 * STRESS TEST
 * -----------
 * Keeps ramping up users until the system breaks (errors spike, latency explodes).
 * The point where thresholds first breach IS your system's limit.
 * Goal: find the breaking point.
 *
 * Run: k6 run scenarios/stress.js
 * Warning: this WILL make the app struggle. Run on a non-production environment.
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, ADMIN_CREDENTIALS } from '../config.js';
import { login, authHeaders } from '../auth.js';

export const options = {
  stages: [
    { duration: '2m', target: 50 },   // normal load
    { duration: '2m', target: 100 },  // above normal
    { duration: '2m', target: 200 },  // high load
    { duration: '2m', target: 300 },  // pushing limits
    { duration: '2m', target: 400 },  // breaking point territory
    { duration: '3m', target: 0 },    // recovery — watch if it recovers cleanly
  ],
  // Stress test intentionally loosens thresholds so it doesn't abort early —
  // we want to SEE the degradation, not stop the test
  thresholds: {
    http_req_failed: ['rate<0.10'],    // allow up to 10% failure before aborting
    http_req_duration: ['p(95)<5000'], // allow up to 5s p95 before aborting
  },
};

export function setup() {
  return { token: login(ADMIN_CREDENTIALS) };
}

export default function (data) {
  const headers = authHeaders(data.token);

  // Hit the most LDAP-heavy endpoints to stress both Spring Boot and OpenLDAP
  const responses = http.batch([
    ['GET', `${BASE_URL}/api/users?pageSize=20`,
      null, { ...headers, tags: { endpoint: 'list_users' } }],
    ['GET', `${BASE_URL}/api/groups`,
      null, { ...headers, tags: { endpoint: 'list_groups' } }],
    ['GET', `${BASE_URL}/api/organisations/tree`,
      null, { ...headers, tags: { endpoint: 'org_tree' } }],
  ]);

  check(responses[0], { 'users: not 500': (r) => r.status !== 500 });
  check(responses[1], { 'groups: not 500': (r) => r.status !== 500 });
  check(responses[2], { 'orgs: not 500': (r) => r.status !== 500 });

  sleep(1);
}
