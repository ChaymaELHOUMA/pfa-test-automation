const express = require('express');
const request = require('supertest');

// Définition de l'application et de la route
const app = express();
app.get('/users', (req, res) => {
  res.json({ utilisateurs: [] });
});

// Suite de tests Jest
describe('GET /users', () => {
  test('cas nominal : devrait retourner une liste d\'utilisateurs vide', async () => {
    const response = await request(app).get('/users');
    
    expect(response.status).toBe(200);
    expect(response.body).toEqual({ utilisateurs: [] });
    expect(response.headers['content-type']).toMatch(/json/);
  });
});