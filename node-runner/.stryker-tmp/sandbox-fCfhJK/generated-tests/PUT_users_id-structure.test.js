// @ts-nocheck
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

test('cas nominal - devrait retourner le statut actif pour un input valide', async () => {
  const response = await request(app).put('/users/1').send({ statut: 'actif' });
  expect(response.status).toBe(200);
  expect(response.body).toEqual({ statut: 'actif' });
});

test('cas limite - devrait retourner erreur pour une chaine vide', async () => {
  const response = await request(app).put('/users/1').send({ statut: '' });
  expect(response.status).toBe(400);
});

test('cas limite - devrait retourner erreur pour un objet vide', async () => {
  const response = await request(app).put('/users/1').send({});
  expect(response.status).toBe(400);
});

test('cas invalide - devrait retourner erreur pour null', async () => {
  const response = await request(app).put('/users/1').send({ statut: null });
  expect(response.status).toBe(400);
});

test('cas branche - devrait executer le cas actif du switch', async () => {
  const response = await request(app).put('/users/1').send({ statut: 'actif' });
  expect(response.status).toBe(200);
  expect(response.body.statut).toBe('actif');
});

test('cas branche - devrait executer le cas inactif du switch', async () => {
  const response = await request(app).put('/users/1').send({ statut: 'inactif' });
  expect(response.status).toBe(200);
  expect(response.body.statut).toBe('inactif');
});

test('cas branche - devrait executer le cas default du switch', async () => {
  const response = await request(app).put('/users/1').send({ statut: 'suspendu' });
  expect(response.status).toBe(400);
  expect(response.body.error).toBe('Statut inconnu');
});

test('cas erreur - devrait retourner 400 pour un statut inconnu', async () => {
  const response = await request(app).put('/users/1').send({ statut: 'inconnu_xyz' });
  expect(response.status).toBe(400);
});