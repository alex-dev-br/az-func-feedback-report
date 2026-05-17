package br.com.fiap.techchallenge.report.infrastructure.report;

import br.com.fiap.techchallenge.report.application.dto.report.FeedbackReportItem;
import br.com.fiap.techchallenge.report.application.dto.report.WeeklyFeedbackReport;
import br.com.fiap.techchallenge.report.domain.enums.Urgencia;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@QuarkusTest
@DisplayName("JacksonWeeklyFeedbackReportSerializerAdapter")
@Tag("integration")
@Tag("infrastructure")
class JacksonWeeklyFeedbackReportSerializerAdapterTest {

    @Inject
    JacksonWeeklyFeedbackReportSerializerAdapter serializer;

    @Inject
    ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve serializar relatório semanal em JSON")
    void deveSerializarRelatorioSemanalEmJson() throws Exception {
        // Arrange
        UUID feedbackId = UUID.randomUUID();

        OffsetDateTime inicio = OffsetDateTime.of(2026, 5, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime fim = OffsetDateTime.of(2026, 5, 8, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime dataEnvio = OffsetDateTime.of(2026, 5, 2, 10, 0, 0, 0, ZoneOffset.UTC);

        Map<LocalDate, Long> quantidadePorDia = Map.of(
                LocalDate.of(2026, 5, 2), 1L
        );

        Map<Urgencia, Long> quantidadePorUrgencia = new EnumMap<>(Urgencia.class);
        quantidadePorUrgencia.put(Urgencia.BAIXA, 1L);
        quantidadePorUrgencia.put(Urgencia.MEDIA, 0L);
        quantidadePorUrgencia.put(Urgencia.ALTA, 0L);

        WeeklyFeedbackReport report = new WeeklyFeedbackReport(
                inicio,
                fim,
                1,
                10.0,
                quantidadePorDia,
                quantidadePorUrgencia,
                List.of(new FeedbackReportItem(
                        feedbackId,
                        "Aula excelente",
                        10,
                        Urgencia.BAIXA,
                        dataEnvio
                ))
        );

        // Act
        String json = serializer.serialize(report);
        JsonNode root = objectMapper.readTree(json);

        // Assert
        assertAll(
                () -> assertFalse(json.isBlank()),
                () -> assertEquals(1, root.get("totalAvaliacoes").asLong()),
                () -> assertEquals(10.0, root.get("mediaAvaliacoes").asDouble()),
                () -> assertEquals(1, root.get("quantidadePorDia").get("2026-05-02").asLong()),
                () -> assertEquals(1, root.get("quantidadePorUrgencia").get("BAIXA").asLong()),
                () -> assertEquals(0, root.get("quantidadePorUrgencia").get("MEDIA").asLong()),
                () -> assertEquals(0, root.get("quantidadePorUrgencia").get("ALTA").asLong()),
                () -> assertEquals(1, root.get("feedbacks").size()),
                () -> assertEquals(feedbackId.toString(), root.get("feedbacks").get(0).get("id").asText()),
                () -> assertEquals("Aula excelente", root.get("feedbacks").get(0).get("descricao").asText()),
                () -> assertEquals(10, root.get("feedbacks").get(0).get("nota").asInt()),
                () -> assertEquals("BAIXA", root.get("feedbacks").get(0).get("urgencia").asText())
        );
    }
}
