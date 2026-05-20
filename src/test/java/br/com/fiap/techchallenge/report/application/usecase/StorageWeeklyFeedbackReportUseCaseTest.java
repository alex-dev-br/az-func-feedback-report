package br.com.fiap.techchallenge.report.application.usecase;

import br.com.fiap.techchallenge.report.application.dto.report.StoredReportResult;
import br.com.fiap.techchallenge.report.application.dto.report.WeeklyFeedbackReport;
import br.com.fiap.techchallenge.report.application.ports.ReportStoragePort;
import br.com.fiap.techchallenge.report.application.ports.WeeklyFeedbackReportSerializerPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("StorageWeeklyFeedbackReportUseCase")
@Tag("unit")
@Tag("application")
class StorageWeeklyFeedbackReportUseCaseTest {

    @Mock
    private WeeklyFeedbackReportSerializerPort serializer;

    @Mock
    private ReportStoragePort storage;

    @InjectMocks
    private StorageWeeklyFeedbackReportUseCase useCase;

    @Nested
    @DisplayName("Validações de entrada e construtor")
    class ValidacoesIniciais {

        @Test
        @DisplayName("Deve lançar NullPointerException quando serializer for nulo")
        void deveLancarExcecaoQuandoSerializerForNulo() {
            // When & Then
            assertThatThrownBy(() -> new StorageWeeklyFeedbackReportUseCase(null, storage))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("serializer é obrigatório");
        }

        @Test
        @DisplayName("Deve lançar NullPointerException quando storage for nulo")
        void deveLancarExcecaoQuandoStorageForNulo() {
            // When & Then
            assertThatThrownBy(() -> new StorageWeeklyFeedbackReportUseCase(serializer, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("storage é obrigatório");
        }

        @Test
        @DisplayName("Deve lançar NullPointerException quando relatório for nulo")
        void deveLancarExcecaoQuandoRelatorioForNulo() {
            // When & Then
            assertThatThrownBy(() -> useCase.execute(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("report é obrigatório");

            then(serializer).shouldHaveNoInteractions();
            then(storage).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("Cenários de execução")
    class CenariosDeExecucao {

        @Test
        @DisplayName("Deve serializar e armazenar o relatório semanal no caminho e formato corretos")
        void deveSerializarEArmazenarRelatorioComSucesso() {
            // Given
            OffsetDateTime inicio = OffsetDateTime.of(2026, 5, 20, 10, 30, 0, 0, ZoneOffset.UTC);
            OffsetDateTime fim = inicio.plusWeeks(1);
            WeeklyFeedbackReport report = new WeeklyFeedbackReport(
                    inicio,
                    fim,
                    10L,
                    8.5,
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyList()
            );

            String serializedJson = "{\"dummy\":\"json\"}";
            StoredReportResult mockResult = mock(StoredReportResult.class);
            String expectedReportName = "reports/weekly/2026/05/202605201030-relatorio-semanal-feedbacks.json";

            given(serializer.serialize(report)).willReturn(serializedJson);
            given(storage.save(expectedReportName, serializedJson, "application/json")).willReturn(mockResult);

            // When
            StoredReportResult result = useCase.execute(report);

            // Then
            assertThat(result).isSameAs(mockResult);
            then(serializer).should().serialize(report);
            then(storage).should().save(expectedReportName, serializedJson, "application/json");
        }
    }
}
