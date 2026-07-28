function addition(a, b) {
  return a + b;
}

describe('addition', () => {
  test('devrait additionner correctement deux nombres', () => {
    expect(addition(2, 3)).toBe(5);
  });

  test('devrait gérer les nombres négatifs', () => {
    expect(addition(-1, -1)).toBe(-2);
  });

  test('devrait gérer le zéro', () => {
    expect(addition(5, 0)).toBe(5);
  });
});