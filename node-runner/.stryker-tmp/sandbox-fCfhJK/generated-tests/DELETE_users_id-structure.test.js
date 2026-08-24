// @ts-nocheck
const express = require('express');
const request = require('supertest');

// Définition de l'application et du routeur pour le test
const app = express();
const router = express.Router();

router.delete('/users/:id', (req, res, next) => {
  try {
    if (!req.params.id || req.params.id === 'undefined') {
      throw new Error('ID manquant');
    }
    res.status(204).send();
  } catch (error) {
    next(error);
  }
});

// Middleware de gestion d'erreur nécessaire pour attraper le throw
app.use(router);
app.use((err, req, res, next) => {
  res.status(500).json({ error: err.message });
});

describe('DELETE /users/:id', () => {
  // Cas nominal
  test('devrait retourner 204 si l\'ID est fourni', async () => {
    const response = await request(app).delete('/users/123');
    expect(response.status).toBe(204);
  });

  // Cas où la condition if est vraie (ID manquant)
  // Note: Express traite '/users/' comme une route différente de '/users/:id', 
  // on simule donc une absence d'ID via un paramètre invalide ou en forçant le comportement
  test('devrait retourner 500 si l\'ID est manquant', async () => {
    // Dans Express, si on appelle /users/, le handler ne correspond pas. 
    // Pour tester le throw, on force le paramètre à une valeur déclenchant l'erreur
    const response = await request(app).delete('/users/undefined');
    expect(response.status).toBe(500);
    expect(response.body.error).toBe('ID manquant');
  });

  // Cas explicite où l'erreur est levée (throw)
  test('devrait lever une erreur explicite', async () => {
    const response = await request(app).delete('/users/undefined');
    expect(response.status).toBe(500);
    expect(response.body.error).toBe('ID manquant');
  });
});