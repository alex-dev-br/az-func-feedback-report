package br.com.fiap.techchallenge.report.application.usecase;

import br.com.fiap.techchallenge.report.application.dto.report.StoredReportResult;
import br.com.fiap.techchallenge.report.application.dto.report.StoredWeeklyFeedbackReportResult;
import br.com.fiap.techchallenge.report.application.dto.report.WeeklyFeedbackReport;
import br.com.fiap.techchallenge.report.application.ports.FeedbackRepositoryPort;
import br.com.fiap.techchallenge.report.application.ports.ReportStoragePort;
import br.com.fiap.techchallenge.report.application.ports.WeeklyFeedbackReportSerializerPort;
import br.com.fiap.techchallenge.report.domain.enums.Urgencia;
import br.com.fiap.techchallenge.report.domain.model.Feedback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("GenerateAndStoreWeeklyFeedbackReportUseCase")
@Tag("unit")
@Tag("application")
class GenerateAndStoreWeeklyFeedbackReportUseCaseTest {

    private static final ZoneId ZONE_ID_SP = ZoneId.of("America/Sao_Paulo");

    @Nested
    @DisplayName("Geração e armazenamento do relatório")
    class GeracaoEArmazenamentoDoRelatorio {

        @Test
        @DisplayName("Deve gerar, serializar e armazenar relatório semanal")
        void deveGerarSerializarEArmazenarRelatorioSemanal() {
            // Arrange
            OffsetDateTime inicio = inicioDoDiaEmSaoPaulo(2026, 5, 1);
            OffsetDateTime fim = inicioDoDiaEmSaoPaulo(2026, 5, 8);

            FakeFeedbackRepository repository = new FakeFeedbackRepository(List.of(
                    new Feedback(
                            UUID.randomUUID(),
                            "Aula excelente",
                            10,
                            Urgencia.BAIXA,
                            dataHoraEmSaoPaulo(2026, 5, 2, 10, 0)
                    )
            ));

            GenerateWeeklyFeedbackReportUseCase generateUseCase = new GenerateWeeklyFeedbackReportUseCase(repository);
            FakeSerializer serializer = new FakeSerializer();
            FakeStorage storage = new FakeStorage();

            GenerateAndStoreWeeklyFeedbackReportUseCase useCase = new GenerateAndStoreWeeklyFeedbackReportUseCase(
                    generateUseCase,
                    serializer,
                    storage
            );

            // Act
            StoredWeeklyFeedbackReportResult result = useCase.execute(inicio, fim);

            // Assert
            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(1, result.report().totalAvaliacoes()),
                    () -> assertEquals(
                            "reports/weekly/relatorio-semanal-feedbacks-2026-05-01_2026-05-08.json",
                            result.storage().blobName()
                    ),
                    () -> assertEquals("application/json", storage.contentTypeSalvo),
                    () -> assertEquals("{\"mock\":true}", storage.conteudoSalvo),
                    () -> assertEquals(result.storage().blobName(), storage.blobNameSalvo)
            );
        }
    }

    private static OffsetDateTime inicioDoDiaEmSaoPaulo(int ano, int mes, int dia) {
        return LocalDate
                .of(ano, mes, dia)
                .atStartOfDay(ZONE_ID_SP)
                .toOffsetDateTime();
    }

    private static OffsetDateTime dataHoraEmSaoPaulo(int ano, int mes, int dia, int hora, int minuto) {
        return LocalDate
                .of(ano, mes, dia)
                .atTime(hora, minuto)
                .atZone(ZONE_ID_SP)
                .toOffsetDateTime();
    }

    private static class FakeSerializer implements WeeklyFeedbackReportSerializerPort {

        @Override
        public String serialize(WeeklyFeedbackReport report) {
            return "{\"mock\":true}";
        }
    }

    private static class FakeStorage implements ReportStoragePort {

        private String blobNameSalvo;
        private String conteudoSalvo;
        private String contentTypeSalvo;

        @Override
        public StoredReportResult save(String blobName, String content, String contentType) {
            this.blobNameSalvo = blobName;
            this.conteudoSalvo = content;
            this.contentTypeSalvo = contentType;

            return new StoredReportResult(
                    blobName,
                    "http://localhost:10000/devstoreaccount1/feedback-reports/" + blobName,
                    OffsetDateTime.now(ZONE_ID_SP)
            );
        }
    }

    private static class FakeFeedbackRepository implements FeedbackRepositoryPort {

        private final List<Feedback> feedbacks;

        private FakeFeedbackRepository(List<Feedback> feedbacks) {
            this.feedbacks = feedbacks;
        }

        @Override
        public List<Feedback> findByDataCriacaoBetween(OffsetDateTime inicio, OffsetDateTime fim) {
            return feedbacks.stream()
                    .filter(feedback -> !feedback.dataCriacao().isBefore(inicio))
                    .filter(feedback -> feedback.dataCriacao().isBefore(fim))
                    .toList();
        }
    }
}
