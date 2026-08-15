const express = require('express');
const router = require('./routes-utilisateurs');

const app = express();
app.use(express.json());
app.use(router);

module.exports = app;
