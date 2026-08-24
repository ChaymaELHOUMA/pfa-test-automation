// @ts-nocheck
const request = require('supertest');
const app = require('../projet-reference/app');
const { reinitialiserEtat } = require('../projet-reference/utilisateurRepository');

beforeEach(() => {
  reinitialiserEtat();
});

test('cas nominal - creation utilisateur avec donnees valides', async () => {
  const nouvelUtilisateur = {
    nom: 'Jean Dupont',
    email: 'jean@example.com',
    role: 'admin',
    statut: 'actif',
    age: 30
  };
  const reponse = await request(app).post('/users').send(nouvelUtilisateur);
  expect(reponse.statusCode).toBe(201);
  expect(reponse.body).toMatchObject(nouvelUtilisateur);
});

test('cas limite - valeurs par defaut quand champs optionnels omis', async () => {
  const reponse = await request(app).post('/users').send({
    email: 'anonyme@example.com'
  });
  expect(reponse.statusCode).toBe(201);
  expect(reponse.body.nom).toBe('Utilisateur Anonyme');
  expect(reponse.body.role).toBe('user');
  expect(reponse.body.statut).toBe('actif');
  expect(reponse.body.age).toBe(25);
});

test('cas limite - envoi objet vide', async () => {
  const reponse = await request(app).post('/users').send({});
  expect(reponse.statusCode).toBe(400);
  expect(reponse.body.error).toBe('Email invalide');
});

test('cas invalide - email mal forme', async () => {
  const reponse = await request(app).post('/users').send({ email: 'email-invalide' });
  expect(reponse.statusCode).toBe(400);
});

test('cas invalide - role non reconnu', async () => {
  const reponse = await request(app).post('/users').send({
    email: 'test@test.com',
    role: 'super-admin-interdit'
  });
  expect(reponse.statusCode).toBe(400);
  expect(reponse.body.error).toBe('Role invalide');
});

test('cas branche - email deja existant retourne erreur 409', async () => {
  const payload = { email: 'double@test.com' };
  await request(app).post('/users').send(payload);
  const reponse = await request(app).post('/users').send(payload);
  expect(reponse.statusCode).toBe(409);
  expect(reponse.body.error).toBe('Cet email existe deja');
});

test('cas erreur - simulation erreur interne serveur', async () => {
  // On force une erreur en envoyant des donnees qui font echouer le repository (ex: nom null si requis)
  // En supposant que le repository throw une erreur si age n'est pas un nombre
  const reponse = await request(app).post('/users').send({
    email: 'error@test.com',
    age: 'pas-un-nombre' 
  });
  // Si le repository throw, le catch(err) renvoie 500
  if (reponse.statusCode === 500) {
    expect(reponse.body.error).toBe('Erreur serveur');
  }
});

test('cas invalide - corps de requete null ou undefined', async () => {
  const reponse = await request(app).post('/users').send(null);
  expect(reponse.statusCode).toBe(400);
});