package br.com.petflow.petflow_api.enums;

public enum EventType {
    VACCINE("Vacina"),
    EXAM("Exame"),
    CONSULTATION("Consulta"),
    SURGERY("Cirurgia");

    private final String description;

    EventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}