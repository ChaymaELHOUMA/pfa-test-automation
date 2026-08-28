function parseNumbers(input) {
  if (!Array.isArray(input)) {
    throw new Error('Input must be an array');
  }
  return input.map((item) => {
    const parsed = Number(item);
    if (isNaN(parsed)) {
      throw new Error('Invalid number found');
    }
    return parsed;
  });
}

describe('parseNumbers', () => {
  test('cas nominal - transforme un tableau de chaines numériques en nombres', () => {
    expect(parseNumbers(['1', '2', '3'])).toEqual([1, 2, 3]);
  });

  test('cas limite - gère un tableau vide', () => {
    expect(parseNumbers([])).toEqual([]);
  });

  test('cas limite - gère le nombre zéro', () => {
    expect(parseNumbers(['0', 0])).toEqual([0, 0]);
  });

  test('cas invalide - lève une erreur si l input n est pas un tableau', () => {
    expect(() => parseNumbers("1,2,3")).toThrow('Input must be an array');
    expect(() => parseNumbers(null)).toThrow('Input must be an array');
    expect(() => parseNumbers({})).toThrow('Input must be an array');
  });

  test('cas erreur - lève une erreur si le tableau contient des valeurs non numériques', () => {
    expect(() => parseNumbers(['1', 'abc', '3'])).toThrow('Invalid number found');
  });

  test('cas erreur - lève une erreur avec une chaine vide', () => {
    expect(() => parseNumbers(['1', '', '3'])).toThrow('Invalid number found');
  });
});