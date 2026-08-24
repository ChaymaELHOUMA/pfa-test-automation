// @ts-nocheck
function validerEmail(email) { return /^[^@]+@[^@]+\.[^@]+$/.test(email); }

describe('validerEmail', () => {
  // Cas nominaux
  test('cas nominal - email valide standard', () => {
    expect(validerEmail('test@exemple.com')).toBe(true);
  });

  test('cas nominal - email avec sous-domaine', () => {
    expect(validerEmail('user@mail.domaine.fr')).toBe(true);
  });

  // Cas limites
  test('cas limite - chaine vide', () => {
    expect(validerEmail('')).toBe(false);
  });

  test('cas limite - nombre 0', () => {
    expect(validerEmail(0)).toBe(false);
  });

  test('cas limite - tableau vide', () => {
    expect(validerEmail([])).toBe(false);
  });

  test('cas limite - objet vide', () => {
    expect(validerEmail({})).toBe(false);
  });

  // Cas invalides
  test('cas invalide - sans arobase', () => {
    expect(validerEmail('testexemple.com')).toBe(false);
  });

  test('cas invalide - sans point après arobase', () => {
    expect(validerEmail('test@exemple')).toBe(false);
  });

  test('cas invalide - avec plusieurs arobases', () => {
    expect(validerEmail('test@@exemple.com')).toBe(false);
  });

  test('cas invalide - sans domaine', () => {
    expect(validerEmail('test@.com')).toBe(false);
  });

  // Cas erreurs
  test('cas erreur - passage de null', () => {
    expect(validerEmail(null)).toBe(false);
  });

  test('cas erreur - passage de undefined', () => {
    expect(validerEmail(undefined)).toBe(false);
  });
});