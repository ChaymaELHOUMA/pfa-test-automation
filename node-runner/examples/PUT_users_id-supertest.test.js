const request = require('supertest');
const app = require('../projet-reference/app');

test('cas nominal - changement de statut vers actif', async () => {
  const response = await request(app).put('/users/1').send({ statut: 'actif' });
  expect(response.statusCode).toBe(200);
  expect(response.body).toEqual({ statut: 'actif' });
});

test('cas erreur - statut inconnu retourne 400', async () => {
  const response = await request(app).put('/users/1').send({ statut: 'inconnu' });
  expect(response.statusCode).toBe(400);
  expect(response.body).toEqual({ error: 'Statut inconnu' });
});
