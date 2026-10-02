-- Exporta um snapshot do schema atual da API para colecoes MongoDB.
-- Executar com SQLcl/SQL*Plus conectado ao mesmo schema Oracle do backend:
--   sql -S /nolog
--   CONNECT usuario/senha@conexao
--   @database/01_PetFlow_Export_JSON.sql
--
-- Gera JSON arrays no diretorio de execucao. PASSWORD_HASH nunca e exportado.
-- Os lookups SPECIES, EVENT_TYPE e REWARD_ACTION sao embutidos nos documentos.
-- RISK_LEVEL tambem e exportado; RISK_SCORE e ADDRESS nao existem no schema V1
-- desta API e, portanto, nao fazem parte deste dataset.

SET PAGESIZE 0
SET LINESIZE 32767
SET LONG 2000000000
SET LONGCHUNKSIZE 2000000000
SET TRIMSPOOL ON
SET FEEDBACK OFF
SET HEADING OFF
SET ECHO OFF
SET VERIFY OFF
SET WRAP OFF

SPOOL tutores_pets.json
SELECT JSON_ARRAYAGG(
         JSON_OBJECT(
           'tutor_id' VALUE t.id,
           'nome' VALUE t.name,
           'email' VALUE t.email,
           'telefone' VALUE t.phone,
           'role' VALUE t.role,
           'criado_em' VALUE TO_CHAR(t.created_at, 'YYYY-MM-DD"T"HH24:MI:SS'),
           'pets' VALUE (
             SELECT JSON_ARRAYAGG(
                      JSON_OBJECT(
                        'pet_id' VALUE p.id,
                        'nome' VALUE p.name,
                        'raca' VALUE p.breed,
                        'especie' VALUE s.name,
                        'peso' VALUE p.weight,
                        'data_nasc' VALUE TO_CHAR(p.birth_date, 'YYYY-MM-DD')
                        RETURNING CLOB
                      )
                      RETURNING CLOB
                    )
             FROM pet p
             JOIN species s ON s.id = p.species_id
             WHERE p.tutor_id = t.id
           )
           RETURNING CLOB
         )
         RETURNING CLOB
       )
FROM tutor t;
SPOOL OFF

SPOOL eventos_saude.json
SELECT JSON_ARRAYAGG(
         JSON_OBJECT(
           'evento_id' VALUE he.id,
           'pet' VALUE JSON_OBJECT(
             'pet_id' VALUE p.id,
             'nome' VALUE p.name,
             'tutor_id' VALUE t.id,
             'tutor' VALUE t.name
             RETURNING CLOB
           ),
           'tipo_evento' VALUE JSON_OBJECT(
             'tipo_evento_id' VALUE et.id,
             'nome' VALUE et.name,
             'categoria' VALUE et.category,
             'pontos_reward' VALUE et.points_reward
             RETURNING CLOB
           ),
           'clinica' VALUE (
             SELECT JSON_OBJECT(
               'clinica_id' VALUE c.id,
               'nome' VALUE c.name
               RETURNING CLOB
             )
             FROM dual
             WHERE c.id IS NOT NULL
           ),
           'descricao' VALUE he.description,
           'data_evento' VALUE TO_CHAR(he.event_date, 'YYYY-MM-DD'),
           'status' VALUE he.status
           RETURNING CLOB
         )
         RETURNING CLOB
       )
FROM health_event he
JOIN pet p ON p.id = he.pet_id
JOIN tutor t ON t.id = p.tutor_id
JOIN event_type et ON et.id = he.event_type_id
LEFT JOIN clinic c ON c.id = he.clinic_id;
SPOOL OFF

SPOOL carteira_recompensas.json
SELECT JSON_ARRAYAGG(
         JSON_OBJECT(
           'tutor_id' VALUE t.id,
           'nome' VALUE t.name,
           'saldo_pontos' VALUE (
             SELECT NVL(SUM(rp.points), 0)
             FROM reward_point rp
             WHERE rp.tutor_id = t.id
           ),
           'extrato_pontos' VALUE (
             SELECT JSON_ARRAYAGG(
                      JSON_OBJECT(
                        'acao' VALUE ra.name,
                        'descricao' VALUE ra.description,
                        'pontos' VALUE rp.points,
                        'tipo_referencia' VALUE rp.reference_type,
                        'referencia_id' VALUE rp.reference_id,
                        'data' VALUE TO_CHAR(rp.created_at, 'YYYY-MM-DD"T"HH24:MI:SS')
                        RETURNING CLOB
                      )
                      RETURNING CLOB
                    )
             FROM reward_point rp
             JOIN reward_action ra ON ra.id = rp.reward_action_id
             WHERE rp.tutor_id = t.id
           ),
           'cupons_resgatados' VALUE (
             SELECT JSON_ARRAYAGG(
                      JSON_OBJECT(
                        'resgate_id' VALUE r.id,
                        'cupom_id' VALUE cp.id,
                        'codigo' VALUE cp.code,
                        'titulo' VALUE ct.title,
                        'pontos_usados' VALUE r.points_used,
                        'resgatado_em' VALUE TO_CHAR(r.created_at, 'YYYY-MM-DD"T"HH24:MI:SS')
                        RETURNING CLOB
                      )
                      RETURNING CLOB
                    )
             FROM redeem r
             JOIN coupon cp ON cp.id = r.coupon_id
             JOIN coupon_template ct ON ct.id = cp.template_id
             WHERE r.tutor_id = t.id
           )
           RETURNING CLOB
         )
         RETURNING CLOB
       )
FROM tutor t;
SPOOL OFF

SPOOL assinaturas.json
SELECT JSON_ARRAYAGG(
         JSON_OBJECT(
           'assinatura_id' VALUE sub.id,
           'status' VALUE sub.status,
           'data_inicio' VALUE TO_CHAR(sub.start_date, 'YYYY-MM-DD'),
           'data_fim' VALUE TO_CHAR(sub.end_date, 'YYYY-MM-DD'),
           'pet' VALUE JSON_OBJECT(
             'pet_id' VALUE p.id,
             'nome' VALUE p.name,
             'tutor_id' VALUE t.id,
             'tutor' VALUE t.name
             RETURNING CLOB
           ),
           'plano' VALUE JSON_OBJECT(
             'plano_id' VALUE pl.id,
             'nome' VALUE pl.name,
             'descricao' VALUE pl.description,
             'preco' VALUE pl.price,
             'duracao_dias' VALUE pl.duration_days,
             'pontos_por_evento' VALUE pl.points_per_event,
             'clinica' VALUE JSON_OBJECT(
               'clinica_id' VALUE cl.id,
               'nome' VALUE cl.name
               RETURNING CLOB
             )
             RETURNING CLOB
           )
           RETURNING CLOB
         )
         RETURNING CLOB
       )
FROM subscription sub
JOIN pet p ON p.id = sub.pet_id
JOIN tutor t ON t.id = p.tutor_id
JOIN plan pl ON pl.id = sub.plan_id
JOIN clinic cl ON cl.id = pl.clinic_id;
SPOOL OFF

SPOOL catalogo_parceiros.json
SELECT JSON_ARRAYAGG(
         JSON_OBJECT(
           'parceiro_id' VALUE pd.id,
           'nome_parceiro' VALUE pd.partner_name,
           'categoria' VALUE pd.category,
           'desconto_percentual' VALUE pd.discount_percent,
           'clinica' VALUE JSON_OBJECT(
             'clinica_id' VALUE cl.id,
             'nome' VALUE cl.name
             RETURNING CLOB
           ),
           'templates' VALUE (
             SELECT JSON_ARRAYAGG(
                      JSON_OBJECT(
                        'template_id' VALUE ct.id,
                        'titulo' VALUE ct.title,
                        'valor_desconto' VALUE ct.discount_value,
                        'tipo_desconto' VALUE ct.discount_type,
                        'pontos_necessarios' VALUE ct.points_required,
                        'cupons' VALUE (
                          SELECT JSON_ARRAYAGG(
                                   JSON_OBJECT(
                                     'cupom_id' VALUE cp.id,
                                     'codigo' VALUE cp.code,
                                     'status' VALUE cp.status,
                                     'validade' VALUE TO_CHAR(cp.expiration_date, 'YYYY-MM-DD')
                                     RETURNING CLOB
                                   )
                                   RETURNING CLOB
                                 )
                          FROM coupon cp
                          WHERE cp.template_id = ct.id
                        )
                        RETURNING CLOB
                      )
                      RETURNING CLOB
                    )
             FROM coupon_template ct
             WHERE ct.partner_discount_id = pd.id
           )
           RETURNING CLOB
         )
         RETURNING CLOB
       )
FROM partner_discount pd
JOIN clinic cl ON cl.id = pd.clinic_id;
SPOOL OFF

SPOOL niveis_risco.json
SELECT JSON_ARRAYAGG(
         JSON_OBJECT(
           'nivel_id' VALUE id,
           'nome' VALUE name,
           'descricao' VALUE description,
           'score_minimo' VALUE min_score,
           'score_maximo' VALUE max_score
           RETURNING CLOB
         )
         RETURNING CLOB
       )
FROM risk_level;
SPOOL OFF

SET HEADING ON
SET FEEDBACK ON
SET TERMOUT ON
PROMPT Exportados tutores_pets.json, eventos_saude.json, carteira_recompensas.json, assinaturas.json, catalogo_parceiros.json e niveis_risco.json