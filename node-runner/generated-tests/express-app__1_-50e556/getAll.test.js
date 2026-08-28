/**
 * Définition de la fonction getAll
 * @param {Array} data - La source de données
 * @returns {Array} - La liste complète
 */
function getAll(data) {
  if (!Array.isArray(data)) {
    throw new Error('Invalid input: data must be an array');
  }
  return data;
}

// Tests unitaires Jest
test('cas nominal - retourne le tableau complet avec des éléments', () => {
  const data = [1, 2, 3];
  expect(getAll(data)).toEqual([1, 2, 3]);
  expect(getAll(data)).toHaveLength(3);
});

test('cas limite - retourne un tableau vide', () => {
  const data = [];
  expect(getAll(data)).toEqual([]);
  expect(getAll(data)).toHaveLength(0);
});

test('cas limite - accepte un tableau contenant des objets ou chaînes vides', () => {
  expect(getAll([{}, ""])).toEqual([{}, ""]);
});

test('cas invalide - lève une erreur si l\'entrée est un objet au lieu d\'un tableau', () => {
  expect(() => getAll({})).toThrow('Invalid input: data must be an array');
});

test('cas invalide - lève une erreur si l\'entrée est une chaîne de caractères', () => {
  expect(() => getAll("test")).toThrow('Invalid input: data must be an array');
});

test('cas invalide - lève une erreur si l\'entrée est null ou undefined', () => {
  expect(() => getAll(null)).toThrow('Invalid input: data must be an array');
  expect(() => getAll(undefined)).toThrow('Invalid input: data must be an array');
});

test('cas erreur - lève une erreur si l\'entrée est un nombre', () => {
  expect(() => getAll(0)).toThrow('Invalid input: data must be an array');
  expect(() => getAll(123)).toThrow('Invalid input: data must be an array');
});