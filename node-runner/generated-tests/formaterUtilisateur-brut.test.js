const formaterUtilisateur = (utilisateur) => {
  return { id: utilisateur.id, nom: utilisateur.nom };
};

describe('formaterUtilisateur', () => {
  test('devrait retourner un objet avec uniquement id et nom', () => {
    const utilisateurEntree = {
      id: 1,
      nom: 'Jean Dupont',
      email: 'jean@exemple.com',
      age: 30
    };

    const resultatAttendu = {
      id: 1,
      nom: 'Jean Dupont'
    };

    expect(formaterUtilisateur(utilisateurEntree)).toEqual(resultatAttendu);
  });
});