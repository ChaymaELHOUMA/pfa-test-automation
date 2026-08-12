function addition(a, b) {
  return a + b;
}

describe('addition', () => {
  test('additionne correctement deux nombres', () => {
    expect(addition(1, 2)).toBe(3);
  });

  test('additionne correctement des nombres négatifs', () => {
    expect(addition(-1, -1)).toBe(-2);
  });

  test('additionne correctement avec zéro', () => {
    expect(addition(5, 0)).toBe(5);
  });
});