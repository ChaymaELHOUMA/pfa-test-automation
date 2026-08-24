// @ts-nocheck
function validerEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

describe('validerEmail', () => {
  test('devrait retourner true pour un email au format nominal (test@example.com)', () => {
    const email = 'test@example.com';
    expect(validerEmail(email)).toBe(true);
  });
});