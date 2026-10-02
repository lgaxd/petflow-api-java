CREATE OR REPLACE PACKAGE PKG_PETFLOW_GAMIFICATION AS
    PROCEDURE GET_TUTOR_POINTS(
        p_tutor_id IN NUMBER,
        p_total_points OUT NUMBER
    );

    PROCEDURE GRANT_POINTS(
        p_tutor_id IN NUMBER,
        p_action_name IN VARCHAR2,
        p_reference_type IN VARCHAR2,
        p_reference_id IN NUMBER
    );

    PROCEDURE REDEEM_COUPON(
        p_tutor_id IN NUMBER,
        p_coupon_id IN NUMBER,
        p_redeem_id OUT NUMBER,
        p_points_used OUT NUMBER
    );
END PKG_PETFLOW_GAMIFICATION;
/

CREATE OR REPLACE PACKAGE BODY PKG_PETFLOW_GAMIFICATION AS
    PROCEDURE GET_TUTOR_POINTS(
        p_tutor_id IN NUMBER,
        p_total_points OUT NUMBER
    ) AS
    BEGIN
        SELECT NVL(SUM(points), 0)
        INTO p_total_points
        FROM REWARD_POINT
        WHERE TUTOR_ID = p_tutor_id;
    END GET_TUTOR_POINTS;

    PROCEDURE GRANT_POINTS(
        p_tutor_id IN NUMBER,
        p_action_name IN VARCHAR2,
        p_reference_type IN VARCHAR2,
        p_reference_id IN NUMBER
    ) AS
        v_action_id REWARD_ACTION.ID%TYPE;
        v_points REWARD_ACTION.POINTS_VALUE%TYPE;
        v_owner_tutor_id TUTOR.ID%TYPE;
        v_pet_id PET.ID%TYPE;
        v_event_type_id EVENT_TYPE.ID%TYPE;
        v_status HEALTH_EVENT.STATUS%TYPE;
        v_existing_count NUMBER;
    BEGIN
        SELECT ID, POINTS_VALUE
        INTO v_action_id, v_points
        FROM REWARD_ACTION
        WHERE NAME = p_action_name;

        IF p_action_name = 'CADASTRO_PET' AND p_reference_type = 'PET' THEN
            SELECT TUTOR_ID
            INTO v_owner_tutor_id
            FROM PET
            WHERE ID = p_reference_id
            FOR UPDATE;
        ELSIF p_action_name = 'EVENTO_SAUDE_REALIZADO' AND p_reference_type = 'HEALTH_EVENT' THEN
            SELECT PET_ID, EVENT_TYPE_ID, STATUS
            INTO v_pet_id, v_event_type_id, v_status
            FROM HEALTH_EVENT
            WHERE ID = p_reference_id
            FOR UPDATE;

            IF v_status <> 'REALIZADO' THEN
                RAISE_APPLICATION_ERROR(-20102, 'HEALTH_EVENT_NOT_REALIZED');
            END IF;

            SELECT TUTOR_ID
            INTO v_owner_tutor_id
            FROM PET
            WHERE ID = v_pet_id;

            SELECT POINTS_REWARD
            INTO v_points
            FROM EVENT_TYPE
            WHERE ID = v_event_type_id;
        ELSIF p_action_name = 'ASSINATURA_ATIVA' AND p_reference_type = 'SUBSCRIPTION' THEN
            SELECT PET_ID
            INTO v_pet_id
            FROM SUBSCRIPTION
            WHERE ID = p_reference_id
              AND STATUS = 'ATIVO'
            FOR UPDATE;

            SELECT TUTOR_ID
            INTO v_owner_tutor_id
            FROM PET
            WHERE ID = v_pet_id;
        ELSE
            RAISE_APPLICATION_ERROR(-20103, 'INVALID_REWARD_REFERENCE');
        END IF;

        IF v_owner_tutor_id <> p_tutor_id THEN
            RAISE_APPLICATION_ERROR(-20104, 'REWARD_OWNER_MISMATCH');
        END IF;

        SELECT COUNT(*)
        INTO v_existing_count
        FROM REWARD_POINT
        WHERE REWARD_ACTION_ID = v_action_id
          AND REFERENCE_TYPE = p_reference_type
          AND REFERENCE_ID = p_reference_id;

        IF v_existing_count > 0 THEN
            RETURN;
        END IF;

        INSERT INTO REWARD_POINT (
            TUTOR_ID, REWARD_ACTION_ID, POINTS, REFERENCE_TYPE, REFERENCE_ID
        ) VALUES (
            p_tutor_id, v_action_id, v_points, p_reference_type, p_reference_id
        );
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20101, 'REWARD_REFERENCE_NOT_FOUND');
    END GRANT_POINTS;

    PROCEDURE REDEEM_COUPON(
        p_tutor_id IN NUMBER,
        p_coupon_id IN NUMBER,
        p_redeem_id OUT NUMBER,
        p_points_used OUT NUMBER
    ) AS
        v_coupon_status COUPON.STATUS%TYPE;
        v_coupon_code COUPON.CODE%TYPE;
        v_expiration_date COUPON.EXPIRATION_DATE%TYPE;
        v_template_id COUPON.TEMPLATE_ID%TYPE;
        v_points_required COUPON_TEMPLATE.POINTS_REQUIRED%TYPE;
        v_available_points NUMBER;
        v_action_id REWARD_ACTION.ID%TYPE;
        v_locked_tutor_id TUTOR.ID%TYPE;
    BEGIN
        SELECT ID
        INTO v_locked_tutor_id
        FROM TUTOR
        WHERE ID = p_tutor_id
        FOR UPDATE;

        SELECT STATUS, CODE, EXPIRATION_DATE, TEMPLATE_ID
        INTO v_coupon_status, v_coupon_code, v_expiration_date, v_template_id
        FROM COUPON
        WHERE ID = p_coupon_id
        FOR UPDATE;

        IF v_coupon_status <> 'DISPONIVEL' THEN
            RAISE_APPLICATION_ERROR(-20083, 'COUPON_UNAVAILABLE');
        END IF;

        SELECT POINTS_REQUIRED
        INTO v_points_required
        FROM COUPON_TEMPLATE
        WHERE ID = v_template_id;

        SELECT NVL(SUM(POINTS), 0)
        INTO v_available_points
        FROM REWARD_POINT
        WHERE TUTOR_ID = p_tutor_id;

        IF v_available_points < v_points_required THEN
            RAISE_APPLICATION_ERROR(-20081,
                'INSUFFICIENT_POINTS|' || v_available_points || '|' || v_points_required);
        END IF;

        IF v_expiration_date IS NOT NULL AND TRUNC(v_expiration_date) < TRUNC(SYSDATE) THEN
            RAISE_APPLICATION_ERROR(-20082,
                'EXPIRED_COUPON|' || v_coupon_code || '|' || TO_CHAR(v_expiration_date, 'YYYY-MM-DD'));
        END IF;

        SELECT ID
        INTO v_action_id
        FROM REWARD_ACTION
        WHERE NAME = 'RESGATE_CUPOM';

        INSERT INTO REDEEM (TUTOR_ID, COUPON_ID, POINTS_USED)
        VALUES (p_tutor_id, p_coupon_id, v_points_required)
        RETURNING ID INTO p_redeem_id;

        UPDATE COUPON
        SET STATUS = 'RESGATADO'
        WHERE ID = p_coupon_id;

        INSERT INTO REWARD_POINT (
            TUTOR_ID, REWARD_ACTION_ID, POINTS, REFERENCE_TYPE, REFERENCE_ID
        ) VALUES (
            p_tutor_id, v_action_id, -v_points_required, 'REDEEM', p_redeem_id
        );

        p_points_used := v_points_required;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20084, 'COUPON_OR_TUTOR_NOT_FOUND');
    END REDEEM_COUPON;
END PKG_PETFLOW_GAMIFICATION;
/