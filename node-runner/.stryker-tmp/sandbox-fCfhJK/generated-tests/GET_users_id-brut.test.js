// @ts-nocheck
const express = require('express');
const request = require('supertest');

// Définition du handler (adapté pour être testable)
const getUserHandler = (req, res) => {
  // Simulation de recherche : on renvoie un user si l'id est '1', sinon null
  const utilisateur = req.params.id === '1' ? { id: '1', name: 'John Doe' } : null;
  
  if (!utilisateur) {
    return res.status(404).json({ error: 'Utilisateur introuvable' });
  }
  res.json(utilisateur);
};

// Configuration de l'application de test
const app = express();
app.get('/users/:id', getUserHandler);

describe('GET /users/:id', () => {
  it('devrait retourner 200 et l\'utilisateur si celui-ci existe (cas nominal)', async () => {
    const response = await request(app).get('/users/1');
    
    expect(response.status).toBe(200);
    expect(response.body).toEqual({ id: '1', name: 'John Doe' });
  });

  it('devrait retourner 404 si l\'utilisateur est introuvable', async () => {
    const response = await request(app).get('/users/999');
    
    expect(response.status).toBe(404);
    expect(response.body).toEqual({ error: 'Utilisateur introuvable' });
  });
});