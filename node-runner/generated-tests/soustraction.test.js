function soustraction(a, b) {
  return a - b;
}

describe('soustraction', () => {
  test('devrait soustraire correctement deux nombres positifs', () => {
    expect(soustraction(10, 5)).toBe(5);
  });

  test('devrait gérer les nombres négatifs', () => {
    expect(soustraction(-1, -1)).toBe(0);
    expect(soustraction(5, -5)).toBe(10);
  });

  test('devrait retourner zéro si les nombres sont identiques', () => {
    expect(soustraction(7, 7)).toBe(0);
  });
});