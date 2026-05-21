package br.com.fiap.techchallenge.report.infrastructure.cron;

import br.com.fiap.techchallenge.report.application.controller.GenerateReportController;
import br.com.fiap.techchallenge.report.application.dto.report.StoredWeeklyFeedbackReportResult;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateReportFunction {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateReportFunction.class);

    /*
     * Azure Functions interpreta expressões NCRONTAB em UTC por padrão.
     * 03:00 UTC corresponde a 00:00 em America/Sao_Paulo.
     */
    private static final String WEEKLY_REPORT_SCHEDULE_UTC = "0 0 3 * * 6";

    private final GenerateReportController generateReportController;

    @Inject
    public GenerateReportFunction(GenerateReportController generateReportController) {
        this.generateReportController = generateReportController;
    }

    @FunctionName("func-feedback-report")
    public void report(
            @TimerTrigger(name = "gerarRelatorio", schedule = WEEKLY_REPORT_SCHEDULE_UTC)
            String timerInfo, ExecutionContext context) {
        try {
            StoredWeeklyFeedbackReportResult result = generateReportController.execute();
            LOG.info(
                    "Relatório semanal gerado e armazenado com sucesso. inicio={}, fim={}, totalAvaliacoes={}, blobName={}, blobUrl={}",
                    result.report().inicio(),
                    result.report().fim(),
                    result.report().totalAvaliacoes(),
                    result.storage().blobName(),
                    result.storage().blobUrl()
            );
        } catch (Exception e) {
            LOG.error("Erro ao gerar ou armazenar relatório semanal de feedbacks", e);
            throw new RuntimeException("Falha na execução do relatório semanal", e);
        }
    }
}
