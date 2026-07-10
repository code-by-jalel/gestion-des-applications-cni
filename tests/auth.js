import http from 'k6/http';
import { check } from 'k6';
import { BASE_URL } from './config.js';

/**
 * Logs in and returns a JWT token.
 * Call this in setup() so login only happens once per test run,
 * not once per virtual user.
 */
export function login(credentials) {
  const res = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify(credentials),
    { headers: { 'Content-Type': 'application/json' }, tags: { endpoint: 'login' } }
  );

  check(res, {
    'login: status 200': (r) => r.status === 200,
    'login: has token': (r) => r.json('token') !== undefined,
  });

  if (res.status !== 200) {
    console.error(`Login failed: ${res.status} ${res.body}`);
    return null;
  }

  return res.json('token');
}

/**
 * Returns headers with the Authorization bearer token.
 */
export function authHeaders(token) {
  return {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
  };
}
