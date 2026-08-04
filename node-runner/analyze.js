const fs = require("fs");
const acorn = require("acorn");
const walk = require("acorn-walk");

const cheminFichier = process.argv[2];

if (!cheminFichier) {
  console.error("Erreur : aucun chemin de fichier fourni en argument");
  process.exit(1);
}

let codeSource;
try {
  codeSource = fs.readFileSync(cheminFichier, "utf-8");
} catch (err) {
  console.error(`Erreur : impossible de lire le fichier "${cheminFichier}" : ${err.message}`);
  process.exit(1);
}

let ast;
try {
  ast = acorn.parse(codeSource, {
    ecmaVersion: "latest",
    sourceType: "module",
    locations: true
  });
} catch (err) {
  console.error(`Erreur de parsing AST : ${err.message}`);
  process.exit(1);
}

// ---- Extraction ----

const fonctions = [];
const routes = [];
const branches = [];
const casErreur = [];

const METHODES_HTTP = ["get", "post", "put", "delete", "patch"];
const NOMS_ROUTEUR_ACCEPTES = ["app", "router"];

function extraireParametres(paramsNode) {
  return paramsNode.map(p => {
    if (p.type === "Identifier") return p.name;
    if (p.type === "AssignmentPattern" && p.left.type === "Identifier") return p.left.name;
    if (p.type === "RestElement" && p.argument.type === "Identifier") return "..." + p.argument.name;
    return "<parametre_complexe>";
  });
}

function detecterRouteExpress(node) {
  if (node.callee.type !== "MemberExpression") return null;

  const objet = node.callee.object;
  const propriete = node.callee.property;

  if (objet.type !== "Identifier" || !NOMS_ROUTEUR_ACCEPTES.includes(objet.name)) return null;
  if (propriete.type !== "Identifier" || !METHODES_HTTP.includes(propriete.name)) return null;

  const args = node.arguments;
  const premierArg = args[0];
  const chemin = (premierArg && premierArg.type === "Literal") ? premierArg.value : "<chemin_dynamique>";

  const dernierArg = args[args.length - 1];
  let parametresHandler = [];
  if (dernierArg && (dernierArg.type === "ArrowFunctionExpression" || dernierArg.type === "FunctionExpression")) {
    parametresHandler = extraireParametres(dernierArg.params);
  }

  return {
    methode: propriete.name.toUpperCase(),
    chemin,
    parametresHandler,
    nombreMiddlewares: args.length - 2 >= 0 ? args.length - 2 : 0,
    ligne: node.loc.start.line
  };
}

function detecterReponseErreurHttp(node) {
  if (node.callee.type !== "MemberExpression") return null;

  const propriete = node.callee.property;
  if (propriete.type !== "Identifier" || propriete.name !== "status") return null;

  const argument = node.arguments[0];
  if (!argument || argument.type !== "Literal" || typeof argument.value !== "number") return null;

  const code = argument.value;
  if (code < 400 || code >= 600) return null;

  return {
    type: "reponse-erreur-http",
    codeHttp: code,
    ligne: node.loc.start.line
  };
}

walk.simple(ast, {
  FunctionDeclaration(node) {
    fonctions.push({
      nom: node.id ? node.id.name : "<anonyme>",
      type: "FunctionDeclaration",
      parametres: extraireParametres(node.params),
      ligne: node.loc.start.line
    });
  },

  VariableDeclarator(node) {
    if (node.init && (node.init.type === "ArrowFunctionExpression" || node.init.type === "FunctionExpression")
        && node.id.type === "Identifier") {
      fonctions.push({
        nom: node.id.name,
        type: node.init.type,
        parametres: extraireParametres(node.init.params),
        ligne: node.loc.start.line
      });
    }
  },

  CallExpression(node) {
    const route = detecterRouteExpress(node);
    if (route) routes.push(route);

    const erreurHttp = detecterReponseErreurHttp(node);
    if (erreurHttp) casErreur.push(erreurHttp);
  },

  IfStatement(node) {
    branches.push({
      type: "if",
      aUnElse: node.alternate !== null,
      ligne: node.loc.start.line
    });
  },

  SwitchStatement(node) {
    branches.push({
      type: "switch",
      nombreCas: node.cases.length,
      ligne: node.loc.start.line
    });
  },

  TryStatement(node) {
    casErreur.push({
      type: "try-catch",
      aUnFinally: node.finalizer !== null,
      ligne: node.loc.start.line
    });
  },

  ThrowStatement(node) {
    casErreur.push({
      type: "throw",
      ligne: node.loc.start.line
    });
  }
});

const resume = {
  fichier: cheminFichier,
  fonctions,
  routes,
  branches,
  casErreur
};

console.log(JSON.stringify(resume));
