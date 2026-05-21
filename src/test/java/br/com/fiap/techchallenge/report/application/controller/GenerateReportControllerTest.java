package br.com.fiap.techchallenge.report.application.controller;

import br.com.fiap.techchallenge.report.application.dto.report.StoredReportResult;
import br.com.fiap.techchallenge.report.application.dto.report.StoredWeeklyFeedbackReportResult;
import br.com.fiap.techchallenge.report.application.dto.report.WeeklyFeedbackReport;
import br.com.fiap.techchallenge.report.application.ports.ReportSenderPort;
import br.com.fiap.techchallenge.report.application.usecase.GenerateWeeklyFeedbackReportUseCase;
import br.com.fiap.techchallenge.report.application.usecase.StorageWeeklyFeedbackReportUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateReportController")
@Tag("unit")
@Tag("application")
class GenerateReportControllerTest {

    @Mock
    private GenerateWeeklyFeedbackReportUseCase generateWeeklyFeedbackReportUseCase;

    @Mock
    private StorageWeeklyFeedbackReportUseCase storageWeeklyFeedbackReportUseCase;

    @Mock
    private ReportSenderPort reportSenderPort;

    @InjectMocks
    private GenerateReportController controller;

    @Nested
    @DisplayName("Validações de entrada e construtor")
    class ValidacoesIniciais {

        @Test
        @DisplayName("Deve lançar NullPointerException quando generateWeeklyFeedbackReportUseCase for nulo")
        void deveLancarExcecaoQuandoGenerateWeeklyFeedbackReportUseCaseForNulo() {
            // When & Then
            assertThatThrownBy(() -> new GenerateReportController(null, storageWeeklyFeedbackReportUseCase, reportSenderPort))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("generateWeeklyFeedbackReportUseCase é obrigatório");
        }

        @Test
        @DisplayName("Deve lançar NullPointerException quando storageWeeklyFeedbackReportUseCase for nulo")
        void deveLancarExcecaoQuandoStorageWeeklyFeedbackReportUseCaseForNulo() {
            // When & Then
            assertThatThrownBy(() -> new GenerateReportController(generateWeeklyFeedbackReportUseCase, null, reportSenderPort))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("storageWeeklyFeedbackReportUseCase é obrigatório");
        }

        @Test
        @DisplayName("Deve lançar NullPointerException quando reportSenderPort for nulo")
        void deveLancarExcecaoQuandoReportSenderPortForNulo() {
            // When & Then
            assertThatThrownBy(() -> new GenerateReportController(generateWeeklyFeedbackReportUseCase, storageWeeklyFeedbackReportUseCase, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("reportSender é obrigatório");
        }
    }

    @Nested
    @DisplayName("Cenários de execução")
    class CenariosDeExecucao {

        @Test
        @DisplayName("Deve gerar e armazenar o relatório de feedbacks de forma orquestrada com sucesso")
        void deveGerarEArmazenarRelatorioComSucesso() throws Exception {
            // Given
            WeeklyFeedbackReport mockReport = mock(WeeklyFeedbackReport.class);
            StoredReportResult mockStoredResult = mock(StoredReportResult.class);

            given(generateWeeklyFeedbackReportUseCase.execute(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                    .willReturn(mockReport);
            given(storageWeeklyFeedbackReportUseCase.execute(mockReport))
                    .willReturn(mockStoredResult);

            ArgumentCaptor<OffsetDateTime> beginCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
            ArgumentCaptor<OffsetDateTime> endCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

            // Act
            StoredWeeklyFeedbackReportResult result = controller.execute();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.report()).isSameAs(mockReport);
            assertThat(result.storage()).isSameAs(mockStoredResult);

            then(generateWeeklyFeedbackReportUseCase).should().execute(beginCaptor.capture(), endCaptor.capture());
            then(storageWeeklyFeedbackReportUseCase).should().execute(mockReport);
            then(reportSenderPort).should().send(mockReport);

            OffsetDateTime capturedBegin = beginCaptor.getValue();
            OffsetDateTime capturedEnd = endCaptor.getValue();

            // Validações do período calculado dinamicamente
            ZonedDateTime nowSp = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
            
            assertThat(capturedEnd.toLocalDate()).isEqualTo(nowSp.toLocalDate());
            assertThat(capturedEnd.toLocalTime()).isEqualTo(LocalTime.MAX);
            
            assertThat(capturedBegin.toLocalDate()).isEqualTo(nowSp.toLocalDate().minusWeeks(1));
            assertThat(capturedBegin.toLocalTime()).isEqualTo(LocalTime.MIN);

            // A diferença entre início e fim deve ser de aproximadamente 1 semana
            Duration duration = Duration.between(capturedBegin, capturedEnd);
            assertThat(duration.toDays()).isEqualTo(7);
        }
    }
}
