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

  test('devrait gérer zéro', () => {
    expect(addition(0, 5)).toBe(5);
  });
});