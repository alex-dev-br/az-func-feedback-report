package br.com.fiap.techchallenge.report.infrastructure.cron;

import br.com.fiap.techchallenge.report.application.dto.report.StoredWeeklyFeedbackReportResult;
import br.com.fiap.techchallenge.report.application.usecase.GenerateAndStoreWeeklyFeedbackReportUseCase;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalTime;

public class GenerateReportFunction {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateReportFunction.class);

    private static final ZoneId ZONE_ID_SP = ZoneId.of("America/Sao_Paulo");

    /*
     * Azure Functions interpreta expressões NCRONTAB em UTC por padrão.
     * 03:00 UTC corresponde a 00:00 em America/Sao_Paulo.
     */
    private static final String WEEKLY_REPORT_SCHEDULE_UTC = "0 0 3 * * 6";

    private final GenerateAndStoreWeeklyFeedbackReportUseCase generateAndStoreWeeklyFeedbackReportUseCase;

    @Inject
    public GenerateReportFunction(
            GenerateAndStoreWeeklyFeedbackReportUseCase generateAndStoreWeeklyFeedbackReportUseCase) {
        this.generateAndStoreWeeklyFeedbackReportUseCase = generateAndStoreWeeklyFeedbackReportUseCase;
    }

    @FunctionName("func-feedback-report")
    public void report(
            @TimerTrigger(name = "gerarRelatorio", schedule = WEEKLY_REPORT_SCHEDULE_UTC) String timerInfo,
            ExecutionContext context) {

        OffsetDateTime fim = inicioDoDiaAtualEmSaoPaulo().with(LocalTime.MAX);
        OffsetDateTime inicio = fim.minusWeeks(1).with(LocalTime.MIN);

        LOG.info(
                "Iniciando geração do relatório semanal. inicio={}, fim={}, timerInfo={}",
                inicio,
                fim,
                timerInfo
        );

        StoredWeeklyFeedbackReportResult result = generateAndStoreWeeklyFeedbackReportUseCase.execute(inicio, fim);

        LOG.info(
                "Relatório semanal gerado e armazenado com sucesso. inicio={}, fim={}, totalAvaliacoes={}, blobName={}, blobUrl={}",
                result.report().inicio(),
                result.report().fim(),
                result.report().totalAvaliacoes(),
                result.storage().blobName(),
                result.storage().blobUrl()
        );
    }

    private static OffsetDateTime inicioDoDiaAtualEmSaoPaulo() {
        return ZonedDateTime
                .now(ZONE_ID_SP)
                .toLocalDate()
                .atStartOfDay(ZONE_ID_SP)
                .toOffsetDateTime();
    }
}
