export const BASE_URL = 'http://localhost:8081';

// Use real LDAP credentials from your directory
export const ADMIN_CREDENTIALS = {
  username: 'jaleleddine.benromdhane@gmail.com',
  password: '1234',
};

export const REGULAR_USER_CREDENTIALS = {
  username: 'user1@example.com',
  password: 'userPassword',
};

// Thresholds — tests FAIL if these are breached
export const THRESHOLDS = {
  // 95% of requests must complete under 500ms
  http_req_duration: ['p(95)<500', 'p(99)<1500'],
  // Less than 1% of requests can fail
  http_req_failed: ['rate<0.01'],
  // 99% of login requests under 1 second (LDAP bind is slower than DB lookup)
  'http_req_duration{endpoint:login}': ['p(99)<1000'],
  // list operations under 800ms (LDAP search + join is heavier)
  'http_req_duration{endpoint:list_users}': ['p(95)<800'],
  'http_req_duration{endpoint:list_groups}': ['p(95)<800'],
  'http_req_duration{endpoint:org_tree}': ['p(95)<800'],
};
