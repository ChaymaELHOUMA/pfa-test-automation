// @ts-nocheck
const express = require('express');
const request = require('supertest');

// Définition de l'application et de la route directement dans le fichier
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

// Tests avec Jest et Supertest
describe('PUT /users/:id', () => {
  
  test('devrait retourner le statut actif (cas switch actif)', async () => {
    const response = await request(app)
      .put('/users/1')
      .send({ statut: 'actif' });
    
    expect(response.status).toBe(200);
    expect(response.body).toEqual({ statut: 'actif' });
  });

  test('devrait retourner le statut inactif (cas switch inactif)', async () => {
    const response = await request(app)
      .put('/users/1')
      .send({ statut: 'inactif' });
    
    expect(response.status).toBe(200);
    expect(response.body).toEqual({ statut: 'inactif' });
  });

  test('devrait retourner une erreur 400 pour un statut inconnu (cas default)', async () => {
    const response = await request(app)
      .put('/users/1')
      .send({ statut: 'en-attente' });
    
    expect(response.status).toBe(400);
    expect(response.body).toEqual({ error: 'Statut inconnu' });
  });

  test('devrait retourner une erreur 400 si le champ statut est absent (cas default)', async () => {
    const response = await request(app)
      .put('/users/1')
      .send({});
    
    expect(response.status).toBe(400);
    expect(response.body).toEqual({ error: 'Statut inconnu' });
  });
});