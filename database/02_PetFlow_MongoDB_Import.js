// Run with: mongosh database/02_PetFlow_MongoDB_Import.js
// Then run the mongoimport commands printed/documented in database/README.md.
// This script clears only these six application collections before a snapshot import.

use("petflow_nosql");

const collectionSchemas = {
  tutores_pets: {
    bsonType: "object",
    required: ["tutor_id", "nome", "email"],
    properties: {
      tutor_id: { bsonType: "number" },
      nome: { bsonType: "string" },
      email: { bsonType: "string" },
      telefone: { bsonType: ["string", "null"] },
      role: { bsonType: "string", enum: ["ADMIN", "TUTOR"] },
      criado_em: { bsonType: ["string", "null"] },
      pets: {
        bsonType: ["array", "null"],
        items: {
          bsonType: "object",
          required: ["pet_id", "nome", "especie"],
          properties: {
            pet_id: { bsonType: "number" },
            nome: { bsonType: "string" },
            raca: { bsonType: ["string", "null"] },
            especie: { bsonType: "string" },
            peso: { bsonType: ["number", "null"] },
            data_nasc: { bsonType: ["string", "null"] }
          }
        }
      }
    }
  },
  eventos_saude: {
    bsonType: "object",
    required: ["evento_id", "pet", "tipo_evento", "data_evento", "status"],
    properties: {
      evento_id: { bsonType: "number" },
      pet: {
        bsonType: "object",
        properties: {
          pet_id: { bsonType: "number" },
          nome: { bsonType: "string" },
          tutor_id: { bsonType: "number" },
          tutor: { bsonType: "string" }
        }
      },
      tipo_evento: {
        bsonType: "object",
        properties: {
          tipo_evento_id: { bsonType: "number" },
          nome: { bsonType: "string" },
          categoria: { bsonType: ["string", "null"] },
          pontos_reward: { bsonType: "number" }
        }
      },
      clinica: {
        bsonType: ["object", "null"],
        properties: { clinica_id: { bsonType: "number" }, nome: { bsonType: "string" } }
      },
      descricao: { bsonType: ["string", "null"] },
      data_evento: { bsonType: "string" },
      status: { bsonType: "string", enum: ["AGENDADO", "REALIZADO", "CANCELADO"] }
    }
  },
  carteira_recompensas: {
    bsonType: "object",
    required: ["tutor_id", "nome", "saldo_pontos"],
    properties: {
      tutor_id: { bsonType: "number" },
      nome: { bsonType: "string" },
      saldo_pontos: { bsonType: "number" },
      extrato_pontos: { bsonType: ["array", "null"] },
      cupons_resgatados: { bsonType: ["array", "null"] }
    }
  },
  assinaturas: {
    bsonType: "object",
    required: ["assinatura_id", "status", "pet", "plano"],
    properties: {
      assinatura_id: { bsonType: "number" },
      status: { bsonType: "string", enum: ["ATIVO", "ENCERRADO", "CANCELADO", "EXPIRADO"] },
      data_inicio: { bsonType: ["string", "null"] },
      data_fim: { bsonType: ["string", "null"] },
      pet: { bsonType: "object" },
      plano: { bsonType: "object" }
    }
  },
  catalogo_parceiros: {
    bsonType: "object",
    required: ["parceiro_id", "nome_parceiro", "clinica"],
    properties: {
      parceiro_id: { bsonType: "number" },
      nome_parceiro: { bsonType: "string" },
      categoria: { bsonType: ["string", "null"] },
      desconto_percentual: { bsonType: ["number", "null"] },
      clinica: { bsonType: "object" },
      templates: { bsonType: ["array", "null"] }
    }
  },
  niveis_risco: {
    bsonType: "object",
    required: ["nivel_id", "nome", "score_minimo", "score_maximo"],
    properties: {
      nivel_id: { bsonType: "number" },
      nome: { bsonType: "string" },
      descricao: { bsonType: ["string", "null"] },
      score_minimo: { bsonType: "number" },
      score_maximo: { bsonType: "number" }
    }
  }
};

for (const [name, validator] of Object.entries(collectionSchemas)) {
  if (!db.getCollectionNames().includes(name)) {
    db.createCollection(name, { validator: { $jsonSchema: validator }, validationLevel: "moderate" });
  } else {
    db.runCommand({
      collMod: name,
      validator: { $jsonSchema: validator },
      validationLevel: "moderate"
    });
    db.getCollection(name).deleteMany({});
  }
}

db.tutores_pets.createIndex({ tutor_id: 1 }, { unique: true });
db.tutores_pets.createIndex({ email: 1 }, { unique: true });
db.tutores_pets.createIndex({ "pets.nome": 1 });
db.eventos_saude.createIndex({ evento_id: 1 }, { unique: true });
db.eventos_saude.createIndex({ "pet.pet_id": 1, data_evento: -1 });
db.eventos_saude.createIndex({ status: 1, data_evento: -1 });
db.carteira_recompensas.createIndex({ tutor_id: 1 }, { unique: true });
db.assinaturas.createIndex({ assinatura_id: 1 }, { unique: true });
db.assinaturas.createIndex({ "pet.pet_id": 1, status: 1 });
db.catalogo_parceiros.createIndex({ parceiro_id: 1 }, { unique: true });
db.catalogo_parceiros.createIndex({ categoria: 1 });
db.niveis_risco.createIndex({ nivel_id: 1 }, { unique: true });

print("Coleções validadas e índices prontos. Importe os seis arquivos JSON documentados em database/README.md.");