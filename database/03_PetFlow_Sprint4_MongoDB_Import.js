// =============================================================
// PetFlow - SPRINT 4 - ESTRUTURA E IMPORTACAO NO MONGODB
// Materia: Mastering Relational and Non-Relational Database
// =============================================================
// OBJETIVO (requisito oficial da Sprint 4):
//   "O dataset exportado em JSON deve: ser importado para o
//    MongoDB; criar estrutura de documentos compativel com banco
//    nao relacional; seguir principios de banco NoSQL; organizar
//    os dados de maneira eficiente e flexivel para consultas."
//
// PRE-REQUISITO: rodar antes o script
//   02_PetFlow_Sprint4_Export_JSON.sql (Oracle) para gerar:
//     tutores_pets.json
//     eventos_saude.json
//     carteira_recompensas.json
//     assinaturas.json
//     score_risco.json
//     catalogo_parceiros.json
//
// As 6 colecoes juntas cobrem todas as tabelas de negocio do
// modelo relacional (SPECIES, EVENT_TYPE e REWARD_ACTION, por
// serem tabelas de dominio/lookup, ficam embutidas como atributos
// dentro dos documentos que as usam, o que e a pratica normal de
// modelagem NoSQL para esse tipo de tabela).
//
// FORMA DE USO:
//   Este arquivo documenta a estrutura das colecoes e serve como
//   script mongosh (mongosh < 03_PetFlow_Sprint4_MongoDB_Import.js)
//   PARA A PARTE DE CRIACAO DE BANCO/VALIDACAO/INDICES.
//   A CARGA dos .json pode ser feita sem mongoimport com o script
//   05_PetFlow_Sprint4_MongoDB_Load.js (mongosh + insertMany, idempotente).
//   A IMPORTACAO em si dos arquivos .json e feita via mongoimport
//   (linha de comando, fora do mongosh), pois mongoimport nao roda
//   dentro de scripts .js do mongosh. Os comandos exatos estao
//   comentados logo abaixo de cada colecao.
// =============================================================

// -------------------------------------------------------------
// 1) Selecionar/criar o banco NoSQL do projeto
// -------------------------------------------------------------
use("petflow_nosql");

// Recria o banco do zero: assim o script pode ser executado mais de uma vez
// (createCollection falharia se a colecao ja existisse com outro validador).
db.dropDatabase();


// -------------------------------------------------------------
// 2) Colecao "tutores_pets"
//    Documento = 1 tutor, com enderecos[] e pets[] aninhados.
//    Import (rodar no terminal, fora do mongosh):
//
//    mongoimport --db petflow_nosql --collection tutores_pets \
//      --file tutores_pets.json --jsonArray
// -------------------------------------------------------------

db.createCollection("tutores_pets", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["tutor_id", "nome", "email"],
      properties: {
        tutor_id: { bsonType: "number", description: "ID do tutor na base relacional (Oracle)" },
        nome: { bsonType: "string" },
        email: { bsonType: "string" },
        telefone: { bsonType: ["string", "null"] },
        role: { bsonType: "string", enum: ["ADMIN", "TUTOR"] },
        criado_em: { bsonType: "string" },
        enderecos: {
          bsonType: ["array", "null"],
          items: {
            bsonType: "object",
            properties: {
              endereco_id: { bsonType: "number" },
              logradouro: { bsonType: "string" },
              cidade: { bsonType: "string" },
              estado: { bsonType: "string" },
              cep: { bsonType: "string" }
            }
          }
        },
        pets: {
          bsonType: ["array", "null"],
          items: {
            bsonType: "object",
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
    }
  },
  validationLevel: "moderate"
});

db.tutores_pets.createIndex({ tutor_id: 1 }, { unique: true });
db.tutores_pets.createIndex({ email: 1 }, { unique: true });
db.tutores_pets.createIndex({ "pets.nome": 1 });


// -------------------------------------------------------------
// 3) Colecao "eventos_saude"
//    Documento = 1 evento de saude, com pet/tutor, tipo de
//    evento e clinica ja resolvidos (dados desnormalizados para
//    leitura eficiente do historico de saude do pet).
//    Import (rodar no terminal, fora do mongosh):
//
//    mongoimport --db petflow_nosql --collection eventos_saude \
//      --file eventos_saude.json --jsonArray
// -------------------------------------------------------------

db.createCollection("eventos_saude", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["evento_id", "pet", "tipo_evento", "data_evento", "status"],
      properties: {
        evento_id: { bsonType: "number" },
        pet: {
          bsonType: "object",
          properties: {
            pet_id: { bsonType: "number" },
            nome: { bsonType: "string" },
            tutor: { bsonType: "string" }
          }
        },
        tipo_evento: {
          bsonType: "object",
          properties: {
            nome: { bsonType: "string" },
            categoria: {
              bsonType: "string",
              enum: ["PREVENTIVO", "TERAPEUTICO", "BEM_ESTAR", "EMERGENCIA"]
            },
            pontos_reward: { bsonType: "number" }
          }
        },
        clinica: {
          bsonType: ["object", "null"],
          properties: {
            clinica_id: { bsonType: "number" },
            nome: { bsonType: "string" }
          }
        },
        descricao: { bsonType: ["string", "null"] },
        data_evento: { bsonType: "string" },
        status: {
          bsonType: "string",
          enum: ["REALIZADO", "PENDENTE", "CANCELADO"]
        }
      }
    }
  },
  validationLevel: "moderate"
});

db.eventos_saude.createIndex({ evento_id: 1 }, { unique: true });
db.eventos_saude.createIndex({ "pet.pet_id": 1, data_evento: -1 });
db.eventos_saude.createIndex({ status: 1 });
db.eventos_saude.createIndex({ "tipo_evento.categoria": 1 });


// -------------------------------------------------------------
// 4) Colecao "carteira_recompensas"
//    Documento = 1 tutor, com saldo de pontos ja calculado,
//    extrato de pontos[] e cupons_resgatados[] aninhados.
//    Import (rodar no terminal, fora do mongosh):
//
//    mongoimport --db petflow_nosql --collection carteira_recompensas \
//      --file carteira_recompensas.json --jsonArray
// -------------------------------------------------------------

db.createCollection("carteira_recompensas", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["tutor_id", "nome", "saldo_pontos"],
      properties: {
        tutor_id: { bsonType: "number" },
        nome: { bsonType: "string" },
        saldo_pontos: { bsonType: "number" },
        extrato_pontos: {
          bsonType: ["array", "null"],
          items: {
            bsonType: "object",
            properties: {
              acao: { bsonType: "string" },
              pontos: { bsonType: "number" },
              tipo_referencia: { bsonType: ["string", "null"] },
              data: { bsonType: "string" }
            }
          }
        },
        cupons_resgatados: {
          bsonType: ["array", "null"],
          items: {
            bsonType: "object",
            properties: {
              codigo: { bsonType: "string" },
              titulo: { bsonType: "string" },
              pontos_usados: { bsonType: "number" },
              resgatado_em: { bsonType: "string" }
            }
          }
        }
      }
    }
  },
  validationLevel: "moderate"
});

db.carteira_recompensas.createIndex({ tutor_id: 1 }, { unique: true });
db.carteira_recompensas.createIndex({ saldo_pontos: -1 });


// -------------------------------------------------------------
// 5) Colecao "assinaturas"
//    Documento = 1 subscription, com pet/tutor e plano (+ clinica
//    do plano) resolvidos. Cobre SUBSCRIPTION e PLAN.
//    Import (rodar no terminal, fora do mongosh):
//
//    mongoimport --db petflow_nosql --collection assinaturas \
//      --file assinaturas.json --jsonArray
// -------------------------------------------------------------

db.createCollection("assinaturas", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["assinatura_id", "status", "pet", "plano"],
      properties: {
        assinatura_id: { bsonType: "number" },
        status: { bsonType: "string", enum: ["ATIVO", "ENCERRADO", "CANCELADO", "EXPIRADO"] },
        data_inicio: { bsonType: ["string", "null"] },
        data_fim: { bsonType: ["string", "null"] },
        pet: {
          bsonType: "object",
          properties: {
            pet_id: { bsonType: "number" },
            nome: { bsonType: "string" },
            tutor: { bsonType: "string" }
          }
        },
        plano: {
          bsonType: "object",
          properties: {
            plano_id: { bsonType: "number" },
            nome: { bsonType: "string" },
            descricao: { bsonType: ["string", "null"] },
            preco: { bsonType: "number" },
            duracao_dias: { bsonType: "number" },
            pontos_por_evento: { bsonType: "number" },
            clinica: {
              bsonType: "object",
              properties: {
                clinica_id: { bsonType: "number" },
                nome: { bsonType: "string" }
              }
            }
          }
        }
      }
    }
  },
  validationLevel: "moderate"
});

db.assinaturas.createIndex({ assinatura_id: 1 }, { unique: true });
db.assinaturas.createIndex({ "pet.pet_id": 1 });
db.assinaturas.createIndex({ status: 1 });


// -------------------------------------------------------------
// 6) Colecao "score_risco"
//    Documento = 1 pet, com o historico completo de risk_score
//    (+ risk_level resolvido) aninhado como array cronologico.
//    Cobre RISK_SCORE e RISK_LEVEL.
//    Import (rodar no terminal, fora do mongosh):
//
//    mongoimport --db petflow_nosql --collection score_risco \
//      --file score_risco.json --jsonArray
// -------------------------------------------------------------

db.createCollection("score_risco", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["pet_id", "nome", "historico_score"],
      properties: {
        pet_id: { bsonType: "number" },
        nome: { bsonType: "string" },
        tutor: { bsonType: "string" },
        historico_score: {
          bsonType: ["array", "null"],
          items: {
            bsonType: "object",
            properties: {
              score: { bsonType: "number" },
              nivel: {
                bsonType: "object",
                properties: {
                  nome: { bsonType: "string" },
                  min_score: { bsonType: "number" },
                  max_score: { bsonType: "number" }
                }
              },
              calculado_em: { bsonType: "string" }
            }
          }
        }
      }
    }
  },
  validationLevel: "moderate"
});

db.score_risco.createIndex({ pet_id: 1 }, { unique: true });
db.score_risco.createIndex({ "historico_score.nivel.nome": 1 });


// -------------------------------------------------------------
// 7) Colecao "catalogo_parceiros"
//    Documento = 1 partner_discount, com a clinica resolvida e os
//    coupon_template aninhados; cada template traz apenas os
//    cupons com status DISPONIVEL. Cobre PARTNER_DISCOUNT,
//    COUPON_TEMPLATE e COUPON (disponiveis).
//    Import (rodar no terminal, fora do mongosh):
//
//    mongoimport --db petflow_nosql --collection catalogo_parceiros \
//      --file catalogo_parceiros.json --jsonArray
// -------------------------------------------------------------

db.createCollection("catalogo_parceiros", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["parceiro_id", "nome_parceiro", "clinica"],
      properties: {
        parceiro_id: { bsonType: "number" },
        nome_parceiro: { bsonType: "string" },
        categoria: { bsonType: "string" },
        desconto_percentual: { bsonType: "number" },
        clinica: {
          bsonType: "object",
          properties: {
            clinica_id: { bsonType: "number" },
            nome: { bsonType: "string" }
          }
        },
        cupons_templates: {
          bsonType: ["array", "null"],
          items: {
            bsonType: "object",
            properties: {
              template_id: { bsonType: "number" },
              titulo: { bsonType: "string" },
              valor_desconto: { bsonType: "number" },
              tipo_desconto: { bsonType: "string", enum: ["PERCENTUAL", "VALOR_FIXO"] },
              pontos_necessarios: { bsonType: "number" },
              cupons_disponiveis: {
                bsonType: ["array", "null"],
                items: {
                  bsonType: "object",
                  properties: {
                    codigo: { bsonType: "string" },
                    validade: { bsonType: ["string", "null"] }
                  }
                }
              }
            }
          }
        }
      }
    }
  },
  validationLevel: "moderate"
});

db.catalogo_parceiros.createIndex({ parceiro_id: 1 }, { unique: true });
db.catalogo_parceiros.createIndex({ categoria: 1 });


// -------------------------------------------------------------
// 8) Consultas de verificacao pos-importacao
//    (evidencias sugeridas para o video: rodar apos o
//    mongoimport das 6 colecoes)
// -------------------------------------------------------------

print("Documentos em tutores_pets: " + db.tutores_pets.countDocuments());
print("Documentos em eventos_saude: " + db.eventos_saude.countDocuments());
print("Documentos em carteira_recompensas: " + db.carteira_recompensas.countDocuments());
print("Documentos em assinaturas: " + db.assinaturas.countDocuments());
print("Documentos em score_risco: " + db.score_risco.countDocuments());
print("Documentos em catalogo_parceiros: " + db.catalogo_parceiros.countDocuments());

// Exemplo de consulta flexivel que so faz sentido em NoSQL
// (buscar tutores que tenham algum pet da especie "Cachorro",
// sem precisar de JOIN):
printjson(
  db.tutores_pets.find(
    { "pets.especie": "Cachorro" },
    { nome: 1, "pets.nome": 1, _id: 0 }
  ).limit(5).toArray()
);

// Exemplo: historico de saude de um pet especifico, ja com o
// tipo de evento e a clinica resolvidos no mesmo documento:
printjson(
  db.eventos_saude.find(
    { "pet.nome": "Rex" }
  ).sort({ data_evento: -1 }).toArray()
);
