package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.exception.BusinessRuleException;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.sql.Types;

@Service
public class GamificationProcedureService {

    private static final Logger log = LoggerFactory.getLogger(GamificationProcedureService.class);
    private final JdbcTemplate jdbcTemplate;

    public GamificationProcedureService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Integer getTutorPoints(Long tutorId) {
        log.info("Chamando PKG_PETFLOW_GAMIFICATION.GET_TUTOR_POINTS");
        try {
            return jdbcTemplate.execute(
                    "{ call PKG_PETFLOW_GAMIFICATION.GET_TUTOR_POINTS(?, ?) }",
                    (CallableStatementCallback<Integer>) statement -> {
                        statement.setLong(1, tutorId);
                        statement.registerOutParameter(2, Types.NUMERIC);
                        statement.execute();
                        int points = statement.getInt(2);
                        return statement.wasNull() ? 0 : points;
                    });
        } catch (DataAccessException exception) {
            throw procedureException(exception);
        }
    }

    private RuntimeException mapGrantException(
            ProcedureException exception,
            String referenceType,
            Long referenceId) {
        return switch (exception.oracleCode()) {
            case 20101 -> new EntityNotFoundException(switch (referenceType) {
                case "PET" -> "Pet";
                case "HEALTH_EVENT" -> "Evento de Saúde";
                case "SUBSCRIPTION" -> "Assinatura";
                default -> "Referência de recompensa";
            }, referenceId);
            case 20102 -> new BusinessRuleException(
                    "O evento de saúde precisa estar REALIZADO para conceder pontos.", "INVALID_EVENT_STATUS");
            case 20103 -> new BusinessRuleException(
                    "Referência de recompensa inválida.", "INVALID_REWARD_REFERENCE");
            case 20104 -> new AccessDeniedException("A referência de recompensa pertence a outro tutor.");
            default -> exception;
        };
    }

    public void grantPoints(Long tutorId, String actionName, String referenceType, Long referenceId) {
        log.info("Chamando PKG_PETFLOW_GAMIFICATION.GRANT_POINTS para ação {}", actionName);
        try {
            jdbcTemplate.execute(
                    "{ call PKG_PETFLOW_GAMIFICATION.GRANT_POINTS(?, ?, ?, ?) }",
                    (CallableStatementCallback<Void>) statement -> {
                        statement.setLong(1, tutorId);
                        statement.setString(2, actionName);
                        statement.setString(3, referenceType);
                        statement.setLong(4, referenceId);
                        statement.execute();
                        return null;
                    });
        } catch (DataAccessException exception) {
            throw mapGrantException(procedureException(exception), referenceType, referenceId);
        }
    }

    public RedeemResult redeemCoupon(Long tutorId, Long couponId) {
        log.info("Chamando PKG_PETFLOW_GAMIFICATION.REDEEM_COUPON");
        try {
            return jdbcTemplate.execute(
                    "{ call PKG_PETFLOW_GAMIFICATION.REDEEM_COUPON(?, ?, ?, ?) }",
                    (CallableStatementCallback<RedeemResult>) statement -> {
                        statement.setLong(1, tutorId);
                        statement.setLong(2, couponId);
                        statement.registerOutParameter(3, Types.NUMERIC);
                        statement.registerOutParameter(4, Types.NUMERIC);
                        statement.execute();
                        return new RedeemResult(statement.getLong(3), statement.getInt(4));
                    });
        } catch (DataAccessException exception) {
            throw procedureException(exception);
        }
    }

    private ProcedureException procedureException(DataAccessException exception) {
        SQLException sqlException = findSqlException(exception);
        if (sqlException == null) {
            return new ProcedureException(0, exception.getMessage(), exception);
        }
        return new ProcedureException(Math.abs(sqlException.getErrorCode()), sqlException.getMessage(), exception);
    }

    private SQLException findSqlException(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof SQLException sqlException) {
                return sqlException;
            }
            current = current.getCause();
        }
        return null;
    }

    public record RedeemResult(Long redeemId, Integer pointsUsed) {}

    public static class ProcedureException extends RuntimeException {
        private final int oracleCode;

        public ProcedureException(int oracleCode, String message, Throwable cause) {
            super(message, cause);
            this.oracleCode = oracleCode;
        }

        public int oracleCode() {
            return oracleCode;
        }
    }
}