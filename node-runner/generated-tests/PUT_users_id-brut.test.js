const express = require('express');
const request = require('supertest');

const app = express();
app.use(express.json());

app.put('/users/:id', (req, res) => {
  switch (req.body.statut) {
    case 'actif':
      res.json({ statut: 'actif' });
      break;
    case 'inactif':
      res.json({ statut: 'inactif' });
      break;
    default:
      res.status(400).json({ error: 'Statut inconnu' });
  }
});

test('cas nominal - changement de statut vers actif', async () => {
  const response = await request(app).put('/users/1').send({ statut: 'actif' });
  expect(response.status).toBe(200);
  expect(response.body).toEqual({ statut: 'actif' });
});

test('cas limite - envoi d\'une chaine vide comme statut', async () => {
  const response = await request(app).put('/users/1').send({ statut: '' });
  expect(response.status).toBe(400);
});

test('cas limite - envoi d\'un tableau vide comme statut', async () => {
  const response = await request(app).put('/users/1').send({ statut: [] });
  expect(response.status).toBe(400);
});

test('cas limite - envoi de la valeur 0 comme statut', async () => {
  const response = await request(app).put('/users/1').send({ statut: 0 });
  expect(response.status).toBe(400);
});

test('cas invalide - envoi d\'une valeur null', async () => {
  const response = await request(app).put('/users/1').send({ statut: null });
  expect(response.status).toBe(400);
});

test('cas invalide - corps de requete absent (undefined)', async () => {
  const response = await request(app).put('/users/1').send({});
  expect(response.status).toBe(400);
});

test('cas branche - execution du case actif', async () => {
  const response = await request(app).put('/users/1').send({ statut: 'actif' });
  expect(response.body.statut).toBe('actif');
});

test('cas branche - execution du case inactif', async () => {
  const response = await request(app).put('/users/1').send({ statut: 'inactif' });
  expect(response.body.statut).toBe('inactif');
});

test('cas branche - execution du cas default', async () => {
  const response = await request(app).put('/users/1').send({ statut: 'suspendu' });
  expect(response.status).toBe(400);
  expect(response.body.error).toBe('Statut inconnu');
});

test('cas erreur - verification explicite du code 400 sur statut invalide', async () => {
  const response = await request(app).put('/users/1').send({ statut: 'inconnu_xyz' });
  expect(response.status).toBe(400);
});