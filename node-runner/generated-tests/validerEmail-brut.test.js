function validerEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

describe('validerEmail', () => {
  test('devrait retourner true pour une adresse email valide (cas nominal)', () => {
    expect(validerEmail('test@exemple.com')).toBe(true);
  });

  test('devrait retourner false pour une adresse sans arobase', () => {
    expect(validerEmail('testexemple.com')).toBe(false);
  });

  test('devrait retourner false pour une adresse sans domaine', () => {
    expect(validerEmail('test@')).toBe(false);
  });

  test('devrait retourner false pour une adresse contenant des espaces', () => {
    expect(validerEmail('test @exemple.com')).toBe(false);
  });
});