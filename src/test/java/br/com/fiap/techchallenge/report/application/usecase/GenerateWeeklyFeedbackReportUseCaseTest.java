package br.com.fiap.techchallenge.report.application.usecase;

import br.com.fiap.techchallenge.report.application.dto.report.WeeklyFeedbackReport;
import br.com.fiap.techchallenge.report.application.ports.FeedbackRepositoryPort;
import br.com.fiap.techchallenge.report.domain.enums.Urgencia;
import br.com.fiap.techchallenge.report.domain.model.Feedback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GenerateWeeklyFeedbackReportUseCase")
@Tag("unit")
@Tag("application")
class GenerateWeeklyFeedbackReportUseCaseTest {

    @Nested
    @DisplayName("Geração do relatório")
    class GeracaoDoRelatorio {

        @Test
        @DisplayName("Deve gerar relatório semanal com resumo dos feedbacks")
        void deveGerarRelatorioSemanalComResumoDosFeedbacks() {
            // Arrange
            OffsetDateTime inicio = dataHoraUtc(2026, 5, 1, 0, 0);
            OffsetDateTime fim = dataHoraUtc(2026, 5, 8, 0, 0);

            FakeFeedbackRepository repository = new FakeFeedbackRepository(List.of(
                    feedback(
                            "Aula excelente",
                            10,
                            Urgencia.BAIXA,
                            dataHoraUtc(2026, 5, 1, 10, 0)
                    ),
                    feedback(
                            "Aula razoável",
                            6,
                            Urgencia.MEDIA,
                            dataHoraUtc(2026, 5, 1, 14, 0)
                    ),
                    feedback(
                            "Sistema travando",
                            2,
                            Urgencia.ALTA,
                            dataHoraUtc(2026, 5, 2, 9, 0)
                    )
            ));

            GenerateWeeklyFeedbackReportUseCase useCase = new GenerateWeeklyFeedbackReportUseCase(repository);

            // Act
            WeeklyFeedbackReport report = useCase.execute(inicio, fim);

            // Assert
            assertAll(
                    () -> assertEquals(inicio, report.inicio()),
                    () -> assertEquals(fim, report.fim()),
                    () -> assertEquals(3, report.totalAvaliacoes()),
                    () -> assertEquals(6.0, report.mediaAvaliacoes()),
                    () -> assertEquals(2L, report.quantidadePorDia().get(LocalDate.of(2026, 5, 1))),
                    () -> assertEquals(1L, report.quantidadePorDia().get(LocalDate.of(2026, 5, 2))),
                    () -> assertEquals(1L, report.quantidadePorUrgencia().get(Urgencia.BAIXA)),
                    () -> assertEquals(1L, report.quantidadePorUrgencia().get(Urgencia.MEDIA)),
                    () -> assertEquals(1L, report.quantidadePorUrgencia().get(Urgencia.ALTA)),
                    () -> assertEquals(3, report.feedbacks().size()),
                    () -> assertEquals("Aula excelente", report.feedbacks().get(0).descricao()),
                    () -> assertEquals("Sistema travando", report.feedbacks().get(2).descricao())
            );
        }

        @Test
        @DisplayName("Deve gerar relatório vazio quando não houver feedbacks no período")
        void deveGerarRelatorioVazioQuandoNaoHouverFeedbacksNoPeriodo() {
            // Arrange
            OffsetDateTime inicio = dataHoraUtc(2026, 5, 1, 0, 0);
            OffsetDateTime fim = dataHoraUtc(2026, 5, 8, 0, 0);

            FakeFeedbackRepository repository = new FakeFeedbackRepository(List.of());
            GenerateWeeklyFeedbackReportUseCase useCase = new GenerateWeeklyFeedbackReportUseCase(repository);

            // Act
            WeeklyFeedbackReport report = useCase.execute(inicio, fim);

            // Assert
            assertAll(
                    () -> assertEquals(0, report.totalAvaliacoes()),
                    () -> assertEquals(0.0, report.mediaAvaliacoes()),
                    () -> assertTrue(report.quantidadePorDia().isEmpty()),
                    () -> assertEquals(0L, report.quantidadePorUrgencia().get(Urgencia.BAIXA)),
                    () -> assertEquals(0L, report.quantidadePorUrgencia().get(Urgencia.MEDIA)),
                    () -> assertEquals(0L, report.quantidadePorUrgencia().get(Urgencia.ALTA)),
                    () -> assertTrue(report.feedbacks().isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Validação do período")
    class ValidacaoDoPeriodo {

        @Test
        @DisplayName("Deve falhar quando início for nulo")
        void deveFalharQuandoInicioForNulo() {
            // Arrange
            GenerateWeeklyFeedbackReportUseCase useCase = new GenerateWeeklyFeedbackReportUseCase(
                    new FakeFeedbackRepository(List.of())
            );

            OffsetDateTime fim = OffsetDateTime.now();

            // Act & Assert
            assertThrows(NullPointerException.class, () -> useCase.execute(null, fim));
        }

        @Test
        @DisplayName("Deve falhar quando fim for nulo")
        void deveFalharQuandoFimForNulo() {
            // Arrange
            GenerateWeeklyFeedbackReportUseCase useCase = new GenerateWeeklyFeedbackReportUseCase(
                    new FakeFeedbackRepository(List.of())
            );

            OffsetDateTime inicio = OffsetDateTime.now();

            // Act & Assert
            assertThrows(NullPointerException.class, () -> useCase.execute(inicio, null));
        }

        @Test
        @DisplayName("Deve falhar quando início for igual ao fim")
        void deveFalharQuandoInicioForIgualAoFim() {
            // Arrange
            OffsetDateTime data = OffsetDateTime.now();

            GenerateWeeklyFeedbackReportUseCase useCase = new GenerateWeeklyFeedbackReportUseCase(
                    new FakeFeedbackRepository(List.of())
            );

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> useCase.execute(data, data));
        }

        @Test
        @DisplayName("Deve falhar quando início for depois do fim")
        void deveFalharQuandoInicioForDepoisDoFim() {
            // Arrange
            OffsetDateTime inicio = dataHoraUtc(2026, 5, 8, 0, 0);
            OffsetDateTime fim = dataHoraUtc(2026, 5, 1, 0, 0);

            GenerateWeeklyFeedbackReportUseCase useCase = new GenerateWeeklyFeedbackReportUseCase(
                    new FakeFeedbackRepository(List.of())
            );

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> useCase.execute(inicio, fim));
        }
    }

    private static OffsetDateTime dataHoraUtc(int ano, int mes, int dia, int hora, int minuto) {
        return OffsetDateTime.of(ano, mes, dia, hora, minuto, 0, 0, ZoneOffset.UTC);
    }

    private static Feedback feedback(String descricao, int nota, Urgencia urgencia, OffsetDateTime dataCriacao) {
        return new Feedback(
                UUID.randomUUID(),
                descricao,
                nota,
                urgencia,
                dataCriacao
        );
    }

    private static class FakeFeedbackRepository implements FeedbackRepositoryPort {

        private final List<Feedback> feedbacks = new ArrayList<>();

        private FakeFeedbackRepository(List<Feedback> feedbacks) {
            this.feedbacks.addAll(feedbacks);
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
