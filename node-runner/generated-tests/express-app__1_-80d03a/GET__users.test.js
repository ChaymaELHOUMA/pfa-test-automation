/**
 * Définition de la fonction GET /users
 */
const getUsers = (users) => {
  if (!Array.isArray(users)) {
    throw new Error("Invalid input: users must be an array");
  }
  return users.map(user => ({ id: user.id, name: user.name }));
};

/**
 * Tests unitaires Jest
 */
describe('GET /users', () => {
  
  test('cas nominal - retourne les utilisateurs formatés', () => {
    const input = [{ id: 1, name: 'Alice', age: 25 }, { id: 2, name: 'Bob', age: 30 }];
    const expected = [{ id: 1, name: 'Alice' }, { id: 2, name: 'Bob' }];
    expect(getUsers(input)).toEqual(expected);
  });

  test('cas limite - retourne un tableau vide pour une entrée tableau vide', () => {
    expect(getUsers([])).toEqual([]);
  });

  test('cas limite - retourne un tableau vide pour des objets utilisateurs vides', () => {
    expect(getUsers([{}, {}])).toEqual([{ id: undefined, name: undefined }]);
  });

  test('cas invalide - lève une erreur pour une entrée null', () => {
    expect(() => getUsers(null)).toThrow("Invalid input: users must be an array");
  });

  test('cas invalide - lève une erreur pour une entrée objet', () => {
    expect(() => getUsers({})).toThrow("Invalid input: users must be an array");
  });

  test('cas invalide - lève une erreur pour une entrée chaîne vide', () => {
    expect(() => getUsers("")).toThrow("Invalid input: users must be an array");
  });

  test('cas erreur - lève une erreur si l'entrée est un nombre', () => {
    expect(() => getUsers(0)).toThrow("Invalid input: users must be an array");
  });

  test('cas erreur - lève une erreur si l'entrée est undefined', () => {
    expect(() => getUsers(undefined)).toThrow("Invalid input: users must be an array");
  });
});