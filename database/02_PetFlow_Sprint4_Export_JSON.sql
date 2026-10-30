-- =============================================================
-- PetFlow - SPRINT 4 - EXPORTACAO DO DATASET RELACIONAL PARA JSON
-- Materia: Mastering Relational and Non-Relational Database
-- =============================================================
-- OBJETIVO (requisito oficial da Sprint 4):
--   "O dataset exportado em JSON deve: ser importado para o
--    MongoDB; criar estrutura de documentos compativel com banco
--    nao relacional; seguir principios de banco NoSQL; organizar
--    os dados de maneira eficiente e flexivel para consultas."
--
-- ESTRATEGIA:
--   Diferente da Funcao 1 da Sprint 3 (fn_pet_tutor_to_json, que
--   monta JSON manualmente via concatenacao de string por
--   exigencia didatica daquela entrega), este script usa os
--   recursos nativos de JSON do Oracle (JSON_OBJECT / JSON_ARRAYAGG)
--   para gerar 6 datasets DENORMALIZADOS, um por colecao MongoDB
--   de destino. Cada JSON agrega, dentro do proprio documento, as
--   entidades relacionais que fazem sentido consultar juntas no
--   MongoDB (principio "documentos que sao consultados juntos
--   devem ser armazenados juntos").
--
--   As 6 colecoes juntas cobrem TODAS as tabelas de negocio do
--   modelo relacional (a unica excecao proposital sao as tabelas
--   de dominio puro SPECIES, EVENT_TYPE e REWARD_ACTION, que sao
--   embutidas como atributos dentro dos documentos que as usam --
--   isso e uma pratica normal de modelagem NoSQL para tabelas de
--   dominio/lookup, e nao uma omissao de dados).
--
-- COLECOES GERADAS:
--   1) tutores_pets          -> tutor + enderecos + pets (+ especie)
--   2) eventos_saude          -> health_event + pet + tipo de evento + clinica
--   3) carteira_recompensas  -> tutor + reward_point + cupons resgatados
--   4) assinaturas            -> subscription + pet/tutor + plan + clinica
--   5) score_risco            -> pet + historico de risk_score (+ risk_level)
--   6) catalogo_parceiros    -> partner_discount + clinica + coupon_template
--                                 + cupons ainda DISPONIVEIS
--
-- COMO GERAR OS ARQUIVOS .json (SQL*Plus / SQLcl):
--   Rode este script via SQL*Plus ou SQLcl (o SPOOL grava o
--   resultado de cada SELECT em um arquivo .json no diretorio
--   corrente). Ajuste SET options conforme a versao do cliente.
--
--   sqlplus usuario/senha@conexao @02_PetFlow_Sprint4_Export_JSON.sql
--
-- Os 6 arquivos gerados sao o "dataset exportado em JSON" exigido
-- pelo requisito, prontos para importacao no MongoDB via
-- mongoimport (ver script 03_PetFlow_Sprint4_MongoDB_Import.js).
-- =============================================================

SET PAGESIZE 0
SET LINESIZE 32767
SET LONG 2000000000
SET LONGCHUNKSIZE 2000000000
SET TRIMSPOOL ON
SET FEEDBACK OFF
SET HEADING OFF
SET ECHO OFF
SET TERMOUT OFF
SET VERIFY OFF
SET WRAP OFF


-- =============================================================
-- COLECAO 1: tutores_pets
-- Um documento por tutor, com enderecos e pets (com especie)
-- aninhados como arrays dentro do proprio documento.
-- =============================================================

SPOOL tutores_pets.json

SELECT JSON_ARRAYAGG(
         JSON_OBJECT(
           'tutor_id'    VALUE t.id,
           'nome'        VALUE t.name,
           'email'       VALUE t.email,
           'telefone'    VALUE t.phone,
           'role'        VALUE t.role,
           'criado_em'   VALUE TO_CHAR(t.created_at, 'YYYY-MM-DD"T"HH24:MI:SS'),
           'enderecos'   VALUE (
             SELECT JSON_ARRAYAGG(
                      JSON_OBJECT(
                        'endereco_id' VALUE a.id,
                        'logradouro'  VALUE a.street,
                        'cidade'      VALUE a.city,
                        'estado'      VALUE a.state,
                        'cep'         VALUE a.zip_code
                      )
                    )
             FROM address a
             WHERE a.tutor_id = t.id
           ),
           'pets'        VALUE (
             SELECT JSON_ARRAYAGG(
                      JSON_OBJECT(
                        'pet_id'       VALUE p.id,
                        'nome'         VALUE p.name,
                        'raca'         VALUE p.breed,
                        'especie'      VALUE s.name,
                        'peso'         VALUE p.weight,
                        'data_nasc'    VALUE TO_CHAR(p.birth_date, 'YYYY-MM-DD')
                      )
                    )
             FROM pet p
             JOIN species s ON s.id = p.species_id
             WHERE p.tutor_id = t.id
           )
           RETURNING CLOB
         )
         RETURNING CLOB
       ) AS doc
FROM tutor t;

SPOOL OFF


-- =============================================================
-- COLECAO 2: eventos_saude
-- Um documento por health_event, com dados do pet, do tipo de
-- evento e da clinica ja resolvidos (sem necessidade de novo
-- JOIN no MongoDB para exibir o historico de saude do pet).
-- =============================================================

-- NOTA: o campo 'clinica' usa uma subquery escalar (SELECT ... FROM DUAL
-- WHERE c.id IS NOT NULL) em vez de "CASE WHEN c.id IS NOT NULL THEN
-- JSON_OBJECT(...) END". O Oracle detecta automaticamente o formato JSON
-- quando um JSON_OBJECT/JSON_ARRAYAGG e usado diretamente como VALUE ou
-- dentro de uma subquery, mas perde essa deteccao quando o JSON_OBJECT
-- fica dentro de um CASE -- o que gera ORA-40590 (invalid or unsupported
-- FORMAT clause). Mantenha o padrao "subquery escalar" para qualquer novo
-- campo opcional/condicional que voce adicionar a este script.

SPOOL eventos_saude.json

SELECT JSON_ARRAYAGG(
         JSON_OBJECT(
           'evento_id'    VALUE he.id,
           'pet'          VALUE JSON_OBJECT(
                                   'pet_id' VALUE p.id,
                                   'nome'   VALUE p.name,
                                   'tutor'  VALUE t.name
                                 ),
           'tipo_evento'  VALUE JSON_OBJECT(
                                   'nome'           VALUE et.name,
                                   'categoria'      VALUE et.category,
                                   'pontos_reward'  VALUE et.points_reward
                                 ),
           'clinica'      VALUE (
             SELECT JSON_OBJECT('clinica_id' VALUE c.id, 'nome' VALUE c.name)
             FROM DUAL
             WHERE c.id IS NOT NULL
           ),
           'descricao'    VALUE he.description,
           'data_evento'  VALUE TO_CHAR(he.event_date, 'YYYY-MM-DD'),
           'status'       VALUE he.status
           RETURNING CLOB
         )
         RETURNING CLOB
       ) AS doc
FROM health_event he
JOIN pet p           ON p.id = he.pet_id
JOIN tutor t          ON t.id = p.tutor_id
JOIN event_type et    ON et.id = he.event_type_id
LEFT JOIN clinic c    ON c.id = he.clinic_id;

SPOOL OFF


-- =============================================================
-- COLECAO 3: carteira_recompensas
-- Um documento por tutor, agregando o extrato de pontos
-- (reward_point) e os cupons ja resgatados (redeem + coupon +
-- coupon_template), representando a "carteira de recompensas"
-- do tutor como um unico documento consultavel no MongoDB.
-- =============================================================

SPOOL carteira_recompensas.json

SELECT JSON_ARRAYAGG(
         JSON_OBJECT(
           'tutor_id'        VALUE t.id,
           'nome'            VALUE t.name,
           'saldo_pontos'    VALUE (
             SELECT NVL(SUM(rp.points), 0)
             FROM reward_point rp
             WHERE rp.tutor_id = t.id
           ),
           'extrato_pontos'  VALUE (
             SELECT JSON_ARRAYAGG(
                      JSON_OBJECT(
                        'acao'            VALUE ra.name,
                        'pontos'          VALUE rp.points,
                        'tipo_referencia' VALUE rp.reference_type,
                        'data'            VALUE TO_CHAR(rp.created_at, 'YYYY-MM-DD"T"HH24:MI:SS')
                      )
                    )
             FROM reward_point rp
             JOIN reward_action ra ON ra.id = rp.reward_action_id
             WHERE rp.tutor_id = t.id
           ),
           'cupons_resgatados' VALUE (
             SELECT JSON_ARRAYAGG(
                      JSON_OBJECT(
                        'codigo'        VALUE cp.code,
                        'titulo'        VALUE ct.title,
                        'pontos_usados' VALUE r.points_used,
                        'resgatado_em'  VALUE TO_CHAR(r.created_at, 'YYYY-MM-DD"T"HH24:MI:SS')
                      )
                    )
             FROM redeem r
             JOIN coupon cp          ON cp.id = r.coupon_id
             JOIN coupon_template ct ON ct.id = cp.template_id
             WHERE r.tutor_id = t.id
           )
           RETURNING CLOB
         )
         RETURNING CLOB
       ) AS doc
FROM tutor t
WHERE EXISTS (SELECT 1 FROM reward_point rp WHERE rp.tutor_id = t.id);

SPOOL OFF


-- =============================================================
-- COLECAO 4: assinaturas
-- Um documento por subscription, com o pet/tutor e o plano (com a
-- clinica do plano) ja resolvidos. Cobre SUBSCRIPTION e PLAN, que
-- nao apareciam em nenhuma das 3 primeiras colecoes.
-- =============================================================

SPOOL assinaturas.json

SELECT JSON_ARRAYAGG(
         JSON_OBJECT(
           'assinatura_id' VALUE sub.id,
           'status'        VALUE sub.status,
           'data_inicio'   VALUE TO_CHAR(sub.start_date, 'YYYY-MM-DD'),
           'data_fim'      VALUE TO_CHAR(sub.end_date, 'YYYY-MM-DD'),
           'pet'           VALUE JSON_OBJECT(
                                   'pet_id' VALUE p.id,
                                   'nome'   VALUE p.name,
                                   'tutor'  VALUE t.name
                                 ),
           'plano'         VALUE JSON_OBJECT(
                                   'plano_id'          VALUE pl.id,
                                   'nome'              VALUE pl.name,
                                   'descricao'         VALUE pl.description,
                                   'preco'             VALUE pl.price,
                                   'duracao_dias'      VALUE pl.duration_days,
                                   'pontos_por_evento' VALUE pl.points_per_event,
                                   'clinica'           VALUE JSON_OBJECT(
                                                                'clinica_id' VALUE cl.id,
                                                                'nome'       VALUE cl.name
                                                              )
                                 )
           RETURNING CLOB
         )
         RETURNING CLOB
       ) AS doc
FROM subscription sub
JOIN pet p     ON p.id  = sub.pet_id
JOIN tutor t   ON t.id  = p.tutor_id
JOIN plan pl   ON pl.id = sub.plan_id
JOIN clinic cl ON cl.id = pl.clinic_id;

SPOOL OFF


-- =============================================================
-- COLECAO 5: score_risco
-- Um documento por pet que possui pelo menos um calculo de score
-- de risco, com o HISTORICO completo de risk_score (+ risk_level
-- resolvido) aninhado como array, ordenado cronologicamente. Cobre
-- RISK_SCORE e RISK_LEVEL, que nao apareciam nas colecoes
-- anteriores.
-- =============================================================

SPOOL score_risco.json

SELECT JSON_ARRAYAGG(
         JSON_OBJECT(
           'pet_id'           VALUE p.id,
           'nome'             VALUE p.name,
           'tutor'            VALUE t.name,
           'historico_score'  VALUE (
             SELECT JSON_ARRAYAGG(
                      JSON_OBJECT(
                        'score'        VALUE rs.score,
                        'nivel'        VALUE JSON_OBJECT(
                                                'nome'      VALUE rl.name,
                                                'min_score' VALUE rl.min_score,
                                                'max_score' VALUE rl.max_score
                                              ),
                        'calculado_em' VALUE TO_CHAR(rs.calculated_at, 'YYYY-MM-DD"T"HH24:MI:SS')
                      )
                      ORDER BY rs.calculated_at
                    )
             FROM risk_score rs
             JOIN risk_level rl ON rl.id = rs.risk_level_id
             WHERE rs.pet_id = p.id
           )
           RETURNING CLOB
         )
         RETURNING CLOB
       ) AS doc
FROM pet p
JOIN tutor t ON t.id = p.tutor_id
WHERE EXISTS (SELECT 1 FROM risk_score rs WHERE rs.pet_id = p.id);

SPOOL OFF


-- =============================================================
-- COLECAO 6: catalogo_parceiros
-- Um documento por partner_discount (parceiro/clinica), com a
-- clinica resolvida e os coupon_template aninhados; cada template
-- traz a lista de cupons ainda com status DISPONIVEL (cupons ja
-- RESGATADO/EXPIRADO ficam no historico do tutor, em
-- carteira_recompensas). Cobre PARTNER_DISCOUNT, COUPON_TEMPLATE
-- e COUPON (status DISPONIVEL), que nao apareciam nas colecoes
-- anteriores.
-- =============================================================

SPOOL catalogo_parceiros.json

SELECT JSON_ARRAYAGG(
         JSON_OBJECT(
           'parceiro_id'          VALUE pd.id,
           'nome_parceiro'        VALUE pd.partner_name,
           'categoria'            VALUE pd.category,
           'desconto_percentual'  VALUE pd.discount_percent,
           'clinica'              VALUE JSON_OBJECT(
                                          'clinica_id' VALUE cl.id,
                                          'nome'       VALUE cl.name
                                        ),
           'cupons_templates'     VALUE (
             SELECT JSON_ARRAYAGG(
                      JSON_OBJECT(
                        'template_id'         VALUE ct.id,
                        'titulo'              VALUE ct.title,
                        'valor_desconto'      VALUE ct.discount_value,
                        'tipo_desconto'       VALUE ct.discount_type,
                        'pontos_necessarios'  VALUE ct.points_required,
                        'cupons_disponiveis'  VALUE (
                          SELECT JSON_ARRAYAGG(
                                   JSON_OBJECT(
                                     'codigo'   VALUE cp.code,
                                     'validade' VALUE TO_CHAR(cp.expiration_date, 'YYYY-MM-DD')
                                   )
                                 )
                          FROM coupon cp
                          WHERE cp.template_id = ct.id
                            AND cp.status = 'DISPONIVEL'
                        )
                      )
                    )
             FROM coupon_template ct
             WHERE ct.partner_discount_id = pd.id
           )
           RETURNING CLOB
         )
         RETURNING CLOB
       ) AS doc
FROM partner_discount pd
JOIN clinic cl ON cl.id = pd.clinic_id;

SPOOL OFF


SET HEADING ON
SET FEEDBACK ON
SET TERMOUT ON
PROMPT Exportacao concluida: tutores_pets.json, eventos_saude.json, carteira_recompensas.json, assinaturas.json, score_risco.json, catalogo_parceiros.json
