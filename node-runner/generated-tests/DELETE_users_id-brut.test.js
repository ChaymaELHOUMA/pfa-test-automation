const express = require('express');
const request = require('supertest');

// Définition du handler (extrait de votre code)
const deleteUserHandler = (req, res) => {
  if (!req.params.id) {
    throw new Error('ID manquant');
  }
  res.status(204).send();
};

// Configuration de l'application de test
const app = express();
app.delete('/users/:id', deleteUserHandler);

describe('DELETE /users/:id', () => {
  it('devrait retourner 204 pour une suppression réussie avec un ID valide', async () => {
    const response = await request(app).delete('/users/123');
    
    expect(response.status).toBe(204);
  });

  it('devrait échouer si aucun ID n\'est fourni (test de sécurité)', async () => {
    // Note : Express routeur matchera rarement sans paramètre si la route est définie explicitement avec :id
    // Mais nous testons ici le comportement attendu du handler
    const req = { params: {} };
    const res = {
      status: jest.fn().mockReturnThis(),
      send: jest.fn()
    };

    expect(() => {
      deleteUserHandler(req, res);
    }).toThrow('ID manquant');
  });
});