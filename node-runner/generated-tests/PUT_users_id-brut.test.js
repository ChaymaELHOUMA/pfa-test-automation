const express = require('express');
const request = require('supertest');

// Définition du handler
const updateUserStatus = (req, res) => {
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
app.put('/users/:id', updateUserStatus);

// Tests
describe('PUT /users/:id', () => {
  it('devrait retourner le statut actif pour une requête valide', async () => {
    const response = await request(app)
      .put('/users/1')
      .send({ statut: 'actif' });
    
    expect(response.status).toBe(200);
    expect(response.body).toEqual({ statut: 'actif' });
  });

  it('devrait retourner le statut inactif pour une requête valide', async () => {
    const response = await request(app)
      .put('/users/1')
      .send({ statut: 'inactif' });
    
    expect(response.status).toBe(200);
    expect(response.body).toEqual({ statut: 'inactif' });
  });

  it('devrait retourner une erreur 400 pour un statut invalide', async () => {
    const response = await request(app)
      .put('/users/1')
      .send({ statut: 'suspendu' });
    
    expect(response.status).toBe(400);
    expect(response.body).toEqual({ error: 'Statut inconnu' });
  });
});