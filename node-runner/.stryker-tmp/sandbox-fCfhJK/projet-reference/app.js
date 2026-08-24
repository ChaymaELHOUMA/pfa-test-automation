// @ts-nocheck
function stryNS_9fa48() {
  var g = typeof globalThis === 'object' && globalThis && globalThis.Math === Math && globalThis || new Function("return this")();
  var ns = g.__stryker__ || (g.__stryker__ = {});
  if (ns.activeMutant === undefined && g.process && g.process.env && g.process.env.__STRYKER_ACTIVE_MUTANT__) {
    ns.activeMutant = g.process.env.__STRYKER_ACTIVE_MUTANT__;
  }
  function retrieveNS() {
    return ns;
  }
  stryNS_9fa48 = retrieveNS;
  return retrieveNS();
}
stryNS_9fa48();
function stryCov_9fa48() {
  var ns = stryNS_9fa48();
  var cov = ns.mutantCoverage || (ns.mutantCoverage = {
    static: {},
    perTest: {}
  });
  function cover() {
    var c = cov.static;
    if (ns.currentTestId) {
      c = cov.perTest[ns.currentTestId] = cov.perTest[ns.currentTestId] || {};
    }
    var a = arguments;
    for (var i = 0; i < a.length; i++) {
      c[a[i]] = (c[a[i]] || 0) + 1;
    }
  }
  stryCov_9fa48 = cover;
  cover.apply(null, arguments);
}
function stryMutAct_9fa48(id) {
  var ns = stryNS_9fa48();
  function isActive(id) {
    if (ns.activeMutant === id) {
      if (ns.hitCount !== void 0 && ++ns.hitCount > ns.hitLimit) {
        throw new Error('Stryker: Hit count limit reached (' + ns.hitCount + ')');
      }
      return true;
    }
    return false;
  }
  stryMutAct_9fa48 = isActive;
  return isActive(id);
}
const express = require('express');
const router = require('./routes-utilisateurs');
const app = express();
app.use(express.json());

// Montage du routeur principal
app.use(router);

// Middleware de gestion des routes 404 non trouvees
app.use((req, res) => {
  if (stryMutAct_9fa48("0")) {
    {}
  } else {
    stryCov_9fa48("0");
    res.status(404).json(stryMutAct_9fa48("1") ? {} : (stryCov_9fa48("1"), {
      error: stryMutAct_9fa48("2") ? "" : (stryCov_9fa48("2"), 'Ressource introuvable')
    }));
  }
});

// Middleware de gestion d'erreur global (500) pour intercepter les exceptions 'throw'
app.use((err, req, res, next) => {
  if (stryMutAct_9fa48("3")) {
    {}
  } else {
    stryCov_9fa48("3");
    res.status(500).json(stryMutAct_9fa48("4") ? {} : (stryCov_9fa48("4"), {
      error: stryMutAct_9fa48("7") ? err.message && 'Erreur interne du serveur' : stryMutAct_9fa48("6") ? false : stryMutAct_9fa48("5") ? true : (stryCov_9fa48("5", "6", "7"), err.message || (stryMutAct_9fa48("8") ? "" : (stryCov_9fa48("8"), 'Erreur interne du serveur')))
    }));
  }
});
module.exports = app;