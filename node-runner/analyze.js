const fs = require("fs");
const acorn = require("acorn");


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

try {
  const ast = acorn.parse(codeSource, {
    ecmaVersion: "latest",   
    sourceType: "module",    
    locations: true          
  });

  // IMPORTANT : uniquement le JSON sur stdout, rien d'autre.
  // C'est ce que Java va lire et parser.
  console.log(JSON.stringify(ast));

} catch (err) {
  // Erreur de syntaxe dans le fichier analyse (ex: JS invalide)
  console.error(`Erreur de parsing AST : ${err.message}`);
  process.exit(1);
}
