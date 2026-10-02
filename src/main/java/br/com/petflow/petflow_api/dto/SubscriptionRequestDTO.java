package br.com.petflow.petflow_api.dto;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.Builder;

import java.io.IOException;
import java.time.LocalDate;

@Builder
@JsonDeserialize(using = SubscriptionRequestDTO.Deserializer.class)
public record SubscriptionRequestDTO(
    @NotNull(message = "A data de início é obrigatória")
    LocalDate startDate,

    LocalDate endDate,

    @NotBlank(message = "O status é obrigatório")
    @Size(max = 20, message = "O status deve ter no máximo 20 caracteres")
    String status,

    @NotNull(message = "O ID do pet é obrigatório")
    Long petId,

    @NotNull(message = "O ID do plano é obrigatório")
    Long planId
) {

    public static class Deserializer extends StdDeserializer<SubscriptionRequestDTO> {

        public Deserializer() {
            super(SubscriptionRequestDTO.class);
        }

        @Override
        public SubscriptionRequestDTO deserialize(JsonParser parser, DeserializationContext context)
                throws IOException {
            JsonNode json = parser.getCodec().readTree(parser);
            String status = json.has("status")
                    ? json.get("status").isNull() ? null : json.get("status").asText()
                    : "ATIVO";
            return new SubscriptionRequestDTO(
                    value(parser, json, "startDate", LocalDate.class),
                    value(parser, json, "endDate", LocalDate.class),
                    status,
                    value(parser, json, "petId", Long.class),
                    value(parser, json, "planId", Long.class));
        }

        private <T> T value(JsonParser parser, JsonNode json, String name, Class<T> type) throws IOException {
            JsonNode node = json.get(name);
            return node == null || node.isNull() ? null : parser.getCodec().treeToValue(node, type);
        }
    }

    public static SubscriptionRequestDTOBuilder builder() {
        return new SubscriptionRequestDTOBuilder().status("ATIVO");
    }
}