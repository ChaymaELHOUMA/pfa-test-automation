// @ts-nocheck
const formaterUtilisateur = (utilisateur) => {
  return { id: utilisateur.id, nom: utilisateur.nom };
};

describe('formaterUtilisateur', () => {
  test('cas nominal : retourne l objet avec id et nom extraits', () => {
    const utilisateur = {
      id: 1,
      nom: 'Jean Dupont',
      email: 'jean@example.com',
      age: 30
    };

    const resultat = formaterUtilisateur(utilisateur);

    expect(resultat).toEqual({
      id: 1,
      nom: 'Jean Dupont'
    });
  });
});