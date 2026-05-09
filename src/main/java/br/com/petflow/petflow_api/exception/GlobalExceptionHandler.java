package br.com.petflow.petflow_api.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final HttpServletRequest request;

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Entidade não encontrada")
                .message(ex.getMessage())
                .path(getPath())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Recurso duplicado")
                .message(ex.getMessage())
                .path(getPath())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_CONTENT.value())
                .error("Regra de negócio violada")
                .message(ex.getMessage())
                .code(ex.getCode())
                .path(getPath())
                .build();

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(error);
    }

    @ExceptionHandler(InsufficientPointsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPoints(InsufficientPointsException ex) {

        Map<String, Object> details = new HashMap<>();
        details.put("availablePoints", ex.getAvailablePoints());
        details.put("requiredPoints", ex.getRequiredPoints());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_CONTENT.value())
                .error("Pontos insuficientes")
                .message(ex.getMessage())
                .code(ex.getCode())
                .details(details)
                .path(getPath())
                .build();

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(error);
    }

    @ExceptionHandler(ExpiredCouponException.class)
    public ResponseEntity<ErrorResponse> handleExpiredCoupon(ExpiredCouponException ex) {

        Map<String, Object> details = new HashMap<>();
        details.put("couponCode", ex.getCouponCode());
        details.put("expirationDate", ex.getExpirationDate());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_CONTENT.value())
                .error("Cupom expirado")
                .message(ex.getMessage())
                .code(ex.getCode())
                .details(details)
                .path(getPath())
                .build();

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(error);
    }

    @ExceptionHandler(CouponAlreadyRedeemedException.class)
    public ResponseEntity<ErrorResponse> handleCouponAlreadyRedeemed(CouponAlreadyRedeemedException ex) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_CONTENT.value())
                .error("Cupom já resgatado")
                .message(ex.getMessage())
                .code(ex.getCode())
                .path(getPath())
                .build();

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(error);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatusTransition(
            InvalidStatusTransitionException ex) {

        Map<String, Object> details = new HashMap<>();
        details.put("entityType", ex.getEntityType());
        details.put("currentStatus", ex.getCurrentStatus());
        details.put("targetStatus", ex.getTargetStatus());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_CONTENT.value())
                .error("Transição de status inválida")
                .message(ex.getMessage())
                .code(ex.getCode())
                .details(details)
                .path(getPath())
                .build();

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {

                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();

                    errors.put(fieldName, errorMessage);
                });

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Erro de validação")
                .message("Campos inválidos")
                .validationErrors(errors)
                .path(getPath())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex) {

        Map<String, String> errors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (error1, error2) -> error1
                ));

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Erro de validação")
                .message("Violação de restrições")
                .validationErrors(errors)
                .path(getPath())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {

        String message = "Violação de integridade de dados";

        if (ex.getMostSpecificCause() != null &&
                ex.getMostSpecificCause().getMessage() != null) {

            String detailedMessage =
                    ex.getMostSpecificCause().getMessage().toLowerCase();

            if (detailedMessage.contains("unique") ||
                    detailedMessage.contains("duplicate")) {

                message = "Registro duplicado. Verifique os campos únicos.";
            }

            if (detailedMessage.contains("foreign key")) {

                message = "Registro relacionado não encontrado.";
            }

            if (detailedMessage.contains("cannot be null")) {

                message = "Existem campos obrigatórios não preenchidos.";
            }
        }

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Erro de integridade")
                .message(message)
                .path(getPath())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Parâmetro inválido")
                .message(String.format(
                        "O parâmetro '%s' recebeu um valor inválido: '%s'",
                        ex.getName(),
                        ex.getValue()
                ))
                .path(getPath())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("JSON inválido")
                .message("O corpo da requisição está mal formatado.")
                .path(getPath())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(
            MissingServletRequestParameterException ex) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Parâmetro obrigatório ausente")
                .message("O parâmetro '" + ex.getParameterName() + "' é obrigatório.")
                .path(getPath())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error("Método HTTP não suportado")
                .message("O método HTTP informado não é suportado para esta rota.")
                .path(getPath())
                .build();

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(error);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Endpoint não encontrado")
                .message("A rota solicitada não existe.")
                .path(getPath())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        ex.printStackTrace();

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Erro interno do servidor")
                .message("Ocorreu um erro inesperado.")
                .path(getPath())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }

    private String getPath() {
        return request.getRequestURI();
    }
}