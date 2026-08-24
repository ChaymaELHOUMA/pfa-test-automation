// @ts-nocheck
// Repository d'utilisateurs en memoire pour le projet Express de reference
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
let donneesInitiales = stryMutAct_9fa48("309") ? [] : (stryCov_9fa48("309"), [stryMutAct_9fa48("310") ? {} : (stryCov_9fa48("310"), {
  id: 1,
  nom: stryMutAct_9fa48("311") ? "" : (stryCov_9fa48("311"), 'Alice Dupont'),
  email: stryMutAct_9fa48("312") ? "" : (stryCov_9fa48("312"), 'alice@example.com'),
  role: stryMutAct_9fa48("313") ? "" : (stryCov_9fa48("313"), 'user'),
  statut: stryMutAct_9fa48("314") ? "" : (stryCov_9fa48("314"), 'actif'),
  age: 28
}), stryMutAct_9fa48("315") ? {} : (stryCov_9fa48("315"), {
  id: 2,
  nom: stryMutAct_9fa48("316") ? "" : (stryCov_9fa48("316"), 'Bob Martin'),
  email: stryMutAct_9fa48("317") ? "" : (stryCov_9fa48("317"), 'bob@example.com'),
  role: stryMutAct_9fa48("318") ? "" : (stryCov_9fa48("318"), 'admin'),
  statut: stryMutAct_9fa48("319") ? "" : (stryCov_9fa48("319"), 'inactif'),
  age: 35
}), stryMutAct_9fa48("320") ? {} : (stryCov_9fa48("320"), {
  id: 3,
  nom: stryMutAct_9fa48("321") ? "" : (stryCov_9fa48("321"), 'Charlie Durand'),
  email: stryMutAct_9fa48("322") ? "" : (stryCov_9fa48("322"), 'charlie@example.com'),
  role: stryMutAct_9fa48("323") ? "" : (stryCov_9fa48("323"), 'user'),
  statut: stryMutAct_9fa48("324") ? "" : (stryCov_9fa48("324"), 'suspendu'),
  age: 42
})]);
let utilisateurs = JSON.parse(JSON.stringify(donneesInitiales));
let dernierId = 3;
function tous() {
  if (stryMutAct_9fa48("325")) {
    {}
  } else {
    stryCov_9fa48("325");
    return utilisateurs;
  }
}
function trouverParId(id) {
  if (stryMutAct_9fa48("326")) {
    {}
  } else {
    stryCov_9fa48("326");
    const me = utilisateurs.find(stryMutAct_9fa48("327") ? () => undefined : (stryCov_9fa48("327"), u => stryMutAct_9fa48("330") ? u.id !== Number(id) : stryMutAct_9fa48("329") ? false : stryMutAct_9fa48("328") ? true : (stryCov_9fa48("328", "329", "330"), u.id === Number(id))));
    return me ? stryMutAct_9fa48("331") ? {} : (stryCov_9fa48("331"), {
      ...me
    }) : null;
  }
}
function trouverParEmail(email) {
  if (stryMutAct_9fa48("332")) {
    {}
  } else {
    stryCov_9fa48("332");
    const me = utilisateurs.find(stryMutAct_9fa48("333") ? () => undefined : (stryCov_9fa48("333"), u => stryMutAct_9fa48("336") ? u.email !== email : stryMutAct_9fa48("335") ? false : stryMutAct_9fa48("334") ? true : (stryCov_9fa48("334", "335", "336"), u.email === email)));
    return me ? stryMutAct_9fa48("337") ? {} : (stryCov_9fa48("337"), {
      ...me
    }) : null;
  }
}
function ajouter(donnees) {
  if (stryMutAct_9fa48("338")) {
    {}
  } else {
    stryCov_9fa48("338");
    stryMutAct_9fa48("339") ? dernierId -= 1 : (stryCov_9fa48("339"), dernierId += 1);
    const nouvelUtilisateur = stryMutAct_9fa48("340") ? {} : (stryCov_9fa48("340"), {
      id: dernierId,
      nom: donnees.nom,
      email: donnees.email,
      role: stryMutAct_9fa48("343") ? donnees.role && 'user' : stryMutAct_9fa48("342") ? false : stryMutAct_9fa48("341") ? true : (stryCov_9fa48("341", "342", "343"), donnees.role || (stryMutAct_9fa48("344") ? "" : (stryCov_9fa48("344"), 'user'))),
      statut: stryMutAct_9fa48("347") ? donnees.statut && 'actif' : stryMutAct_9fa48("346") ? false : stryMutAct_9fa48("345") ? true : (stryCov_9fa48("345", "346", "347"), donnees.statut || (stryMutAct_9fa48("348") ? "" : (stryCov_9fa48("348"), 'actif'))),
      age: stryMutAct_9fa48("351") ? donnees.age && 25 : stryMutAct_9fa48("350") ? false : stryMutAct_9fa48("349") ? true : (stryCov_9fa48("349", "350", "351"), donnees.age || 25)
    });
    utilisateurs.push(nouvelUtilisateur);
    return stryMutAct_9fa48("352") ? {} : (stryCov_9fa48("352"), {
      ...nouvelUtilisateur
    });
  }
}
function mettreAJour(id, donnees) {
  if (stryMutAct_9fa48("353")) {
    {}
  } else {
    stryCov_9fa48("353");
    const index = utilisateurs.findIndex(stryMutAct_9fa48("354") ? () => undefined : (stryCov_9fa48("354"), u => stryMutAct_9fa48("357") ? u.id !== Number(id) : stryMutAct_9fa48("356") ? false : stryMutAct_9fa48("355") ? true : (stryCov_9fa48("355", "356", "357"), u.id === Number(id))));
    if (stryMutAct_9fa48("360") ? index !== -1 : stryMutAct_9fa48("359") ? false : stryMutAct_9fa48("358") ? true : (stryCov_9fa48("358", "359", "360"), index === (stryMutAct_9fa48("361") ? +1 : (stryCov_9fa48("361"), -1)))) return null;
    utilisateurs[index] = stryMutAct_9fa48("362") ? {} : (stryCov_9fa48("362"), {
      ...utilisateurs[index],
      ...donnees,
      id: Number(id)
    });
    return stryMutAct_9fa48("363") ? {} : (stryCov_9fa48("363"), {
      ...utilisateurs[index]
    });
  }
}
function supprimer(id) {
  if (stryMutAct_9fa48("364")) {
    {}
  } else {
    stryCov_9fa48("364");
    const index = utilisateurs.findIndex(stryMutAct_9fa48("365") ? () => undefined : (stryCov_9fa48("365"), u => stryMutAct_9fa48("368") ? u.id !== Number(id) : stryMutAct_9fa48("367") ? false : stryMutAct_9fa48("366") ? true : (stryCov_9fa48("366", "367", "368"), u.id === Number(id))));
    if (stryMutAct_9fa48("371") ? index !== -1 : stryMutAct_9fa48("370") ? false : stryMutAct_9fa48("369") ? true : (stryCov_9fa48("369", "370", "371"), index === (stryMutAct_9fa48("372") ? +1 : (stryCov_9fa48("372"), -1)))) return stryMutAct_9fa48("373") ? true : (stryCov_9fa48("373"), false);
    utilisateurs.splice(index, 1);
    return stryMutAct_9fa48("374") ? false : (stryCov_9fa48("374"), true);
  }
}
function reinitialiserEtat() {
  if (stryMutAct_9fa48("375")) {
    {}
  } else {
    stryCov_9fa48("375");
    utilisateurs = JSON.parse(JSON.stringify(donneesInitiales));
    dernierId = 3;
  }
}
module.exports = stryMutAct_9fa48("376") ? {} : (stryCov_9fa48("376"), {
  tous,
  trouverParId,
  trouverParEmail,
  ajouter,
  mettreAJour,
  supprimer,
  reinitialiserEtat
});