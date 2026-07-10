/**
 * SMOKE TEST
 * ----------
 * 1-2 virtual users, runs for 30 seconds.
 * Goal: confirm every endpoint is reachable and returns correct status.
 * Run this first before any heavier test.
 *
 * Run: k6 run scenarios/smoke.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, ADMIN_CREDENTIALS, THRESHOLDS } from '../config.js';
import { login, authHeaders } from '../auth.js';

export const options = {
  vus: 2,
  duration: '30s',
  thresholds: THRESHOLDS,
};

// setup() runs ONCE before virtual users start — perfect for login
export function setup() {
  const token = login(ADMIN_CREDENTIALS);
  return { token };
}

// default() runs repeatedly for each virtual user
export default function (data) {
  const headers = authHeaders(data.token);

  // 1 — list users
  const usersRes = http.get(
    `${BASE_URL}/api/users?pageSize=20`,
    { ...headers, tags: { endpoint: 'list_users' } }
  );
  check(usersRes, {
    'GET /api/users: status 200': (r) => r.status === 200,
    'GET /api/users: returns items array': (r) => Array.isArray(r.json('items')),
  });

  sleep(0.5);

  // 2 — list groups
  const groupsRes = http.get(
    `${BASE_URL}/api/groups`,
    { ...headers, tags: { endpoint: 'list_groups' } }
  );
  check(groupsRes, {
    'GET /api/groups: status 200': (r) => r.status === 200,
    'GET /api/groups: returns array': (r) => Array.isArray(r.json()),
  });

  sleep(0.5);

  // 3 — org tree
  const treeRes = http.get(
    `${BASE_URL}/api/organisation/tree`,
    { ...headers, tags: { endpoint: 'org_tree' } }
  );
  check(treeRes, {
    'GET /api/organisation/tree: status 200': (r) => r.status === 200,
  });

  sleep(1);
}
