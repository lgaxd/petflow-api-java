// =============================================================
// PetFlow - SPRINT 4 - CARGA (IMPORTACAO) DOS JSON NO MONGODB
// Materia: Mastering Relational and Non-Relational Database
// =============================================================
// Importa os 6 arquivos .json exportados do Oracle (script 02)
// para as colecoes criadas pelo script 03 (validadores e indices).
// Substitui o uso manual do mongoimport: roda inteiramente no
// mongosh e e idempotente (esvazia a colecao antes de inserir).
//
// ORDEM:
//   1) mongosh --file 03_PetFlow_Sprint4_MongoDB_Import.js   (estrutura)
//   2) mongosh --file 05_PetFlow_Sprint4_MongoDB_Load.js     (carga)
//
// Execute a partir da pasta que contem os .json, ou informe a pasta:
//   DATA_DIR=C:/caminho/dos/json mongosh "<connection string>" --file 05_PetFlow_Sprint4_MongoDB_Load.js
// (a connection string e opcional; sem ela usa mongodb://localhost:27017)
// =============================================================

const fs = require("fs");
const dataDir = process.env.DATA_DIR || ".";

use("petflow_nosql");

const colecoes = [
  "tutores_pets",
  "eventos_saude",
  "carteira_recompensas",
  "assinaturas",
  "score_risco",
  "catalogo_parceiros",
];

let falhas = 0;

for (const nome of colecoes) {
  const arquivo = dataDir + "/" + nome + ".json";
  try {
    const docs = JSON.parse(fs.readFileSync(arquivo, "utf8"));
    db.getCollection(nome).deleteMany({});
    const resultado = db.getCollection(nome).insertMany(docs);
    print("[OK]   " + nome + ": " + Object.keys(resultado.insertedIds).length + " documentos importados de " + arquivo);
  } catch (erro) {
    falhas++;
    print("[ERRO] " + nome + ": " + String(erro.message).slice(0, 300));
  }
}

print("");
print(falhas === 0 ? "Carga concluida com sucesso." : "Carga concluida com " + falhas + " erro(s).");
for (const nome of colecoes) {
  print(nome + ": " + db.getCollection(nome).countDocuments() + " documentos");
}
