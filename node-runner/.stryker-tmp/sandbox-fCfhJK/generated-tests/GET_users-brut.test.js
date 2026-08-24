// @ts-nocheck
const express = require('express');
const request = require('supertest');

// Définition du handler (inclus dans le fichier pour l'autonomie)
const getUsersHandler = (req, res) => {
  res.json({ utilisateurs: [] });
};

// Configuration de l'application de test
const app = express();
app.get('/users', getUsersHandler);

describe('GET /users', () => {
  it('devrait retourner une liste vide d\'utilisateurs avec un statut 200', async () => {
    const response = await request(app).get('/users');
    
    expect(response.status).toBe(200);
    expect(response.body).toEqual({ utilisateurs: [] });
    expect(response.headers['content-type']).toMatch(/json/);
  });
});