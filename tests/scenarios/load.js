/**
 * LOAD TEST
 * ---------
 * Simulates realistic usage: admins browsing users, groups, orgs.
 * Ramps up to 50 concurrent users, holds for 5 minutes, ramps back down.
 * Goal: confirm response times stay acceptable under normal load.
 *
 * Run: k6 run scenarios/load.js
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { BASE_URL, ADMIN_CREDENTIALS, THRESHOLDS } from '../config.js';
import { login, authHeaders } from '../auth.js';

export const options = {
  stages: [
    { duration: '1m', target: 10 },   // ramp up to 10 users
    { duration: '2m', target: 50 },   // ramp up to 50 users
    { duration: '5m', target: 50 },   // hold at 50 users
    { duration: '1m', target: 0 },    // ramp down
  ],
  thresholds: THRESHOLDS,
};

export function setup() {
  return { token: login(ADMIN_CREDENTIALS) };
}

export default function (data) {
  const headers = authHeaders(data.token);

  // Simulate a realistic admin session:
  // browse users → check a group → look at the org tree

  group('Browse users', () => {
    const res = http.get(
      `${BASE_URL}/api/users?pageSize=20`,
      { ...headers, tags: { endpoint: 'list_users' } }
    );
    check(res, {
      'list users: ok': (r) => r.status === 200,
      'list users: has items': (r) => r.json('items')?.length >= 0,
    });
    sleep(1);

    // simulate scrolling to page 2 if there is one
    const cookie = res.json('nextPageCookie');
    if (cookie) {
      const page2 = http.get(
        `${BASE_URL}/api/users?pageSize=20&cookie=${encodeURIComponent(cookie)}`,
        { ...headers, tags: { endpoint: 'list_users' } }
      );
      check(page2, { 'page 2: ok': (r) => r.status === 200 });
      sleep(1);
    }
  });

  group('Browse groups', () => {
    const res = http.get(
      `${BASE_URL}/api/groups`,
      { ...headers, tags: { endpoint: 'list_groups' } }
    );
    check(res, { 'list groups: ok': (r) => r.status === 200 });
    sleep(1);
  });

  group('Browse organisation tree', () => {
    const res = http.get(
      `${BASE_URL}/api/organisation/tree`,
      { ...headers, tags: { endpoint: 'org_tree' } }
    );
    check(res, { 'org tree: ok': (r) => r.status === 200 });
    sleep(2);
  });

  // Simulate think time between browsing sessions
  sleep(Math.random() * 3 + 1);
}
