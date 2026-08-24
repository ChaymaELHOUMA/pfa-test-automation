// @ts-nocheck
const express = require('express');
const request = require('supertest');

// --- Définition du handler (simulé pour le test) ---
const app = express();

app.get('/users/:id', (req, res) => {
  // Pour les besoins du test, on simule la présence ou l'absence selon l'ID
  const utilisateur = req.params.id === '123' ? { id: '123', name: 'John Doe' } : null;
  
  if (!utilisateur) {
    return res.status(404).json({ error: 'Utilisateur introuvable' });
  }
  res.json(utilisateur);
});

// --- Tests ---
describe('GET /users/:id', () => {
  
  // Chemin nominal : l'utilisateur existe
  test('devrait retourner 200 et les données de l\'utilisateur si trouvé', async () => {
    const response = await request(app).get('/users/123');
    expect(response.status).toBe(200);
    expect(response.body).toEqual({ id: '123', name: 'John Doe' });
  });

  // Chemin condition if vraie et erreur 404
  test('devrait retourner 404 si l\'utilisateur est introuvable', async () => {
    const response = await request(app).get('/users/999');
    expect(response.status).toBe(404);
    expect(response.body).toEqual({ error: 'Utilisateur introuvable' });
  });
});