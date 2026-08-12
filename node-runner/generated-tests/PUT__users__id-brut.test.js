const express = require('express');
const request = require('supertest');

// Définition du handler (extrait du router)
const updateStatusHandler = (req, res) => {
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
};

// Configuration de l'application de test
const app = express();
app.use(express.json());
app.put('/users/:id', updateStatusHandler);

// Tests
describe('PUT /users/:id', () => {
  it('devrait retourner 200 et le statut actif quand le corps est valide', async () => {
    const response = await request(app)
      .put('/users/1')
      .send({ statut: 'actif' });

    expect(response.status).toBe(200);
    expect(response.body).toEqual({ statut: 'actif' });
  });

  it('devrait retourner 200 et le statut inactif quand le corps est valide', async () => {
    const response = await request(app)
      .put('/users/1')
      .send({ statut: 'inactif' });

    expect(response.status).toBe(200);
    expect(response.body).toEqual({ statut: 'inactif' });
  });

  it('devrait retourner 400 si le statut est inconnu', async () => {
    const response = await request(app)
      .put('/users/1')
      .send({ statut: 'banni' });

    expect(response.status).toBe(400);
    expect(response.body).toEqual({ error: 'Statut inconnu' });
  });
});