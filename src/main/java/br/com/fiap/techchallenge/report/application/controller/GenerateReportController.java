package br.com.fiap.techchallenge.report.application.controller;

import br.com.fiap.techchallenge.report.application.dto.report.StoredReportResult;
import br.com.fiap.techchallenge.report.application.dto.report.StoredWeeklyFeedbackReportResult;
import br.com.fiap.techchallenge.report.application.ports.ReportSenderPort;
import br.com.fiap.techchallenge.report.application.usecase.GenerateWeeklyFeedbackReportUseCase;
import br.com.fiap.techchallenge.report.application.usecase.StorageWeeklyFeedbackReportUseCase;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.*;

public class GenerateReportController {


    private static final ZoneId ZONE_ID_SP = ZoneId.of("America/Sao_Paulo");
    public static final int TIMEOUT_EM_SEGUNDOS = 30;

    private final GenerateWeeklyFeedbackReportUseCase generateWeeklyFeedbackReportUseCase;
    private final StorageWeeklyFeedbackReportUseCase storageWeeklyFeedbackReportUseCase;
    private final ReportSenderPort reportSenderPort;
    private final ExecutorService executorService;

    public GenerateReportController(
            GenerateWeeklyFeedbackReportUseCase generateWeeklyFeedbackReportUseCase,
            StorageWeeklyFeedbackReportUseCase storageWeeklyFeedbackReportUseCase,
            ReportSenderPort reportSenderPort) {
        this.generateWeeklyFeedbackReportUseCase = Objects.requireNonNull(generateWeeklyFeedbackReportUseCase, "generateWeeklyFeedbackReportUseCase é obrigatório");
        this.storageWeeklyFeedbackReportUseCase = Objects.requireNonNull(storageWeeklyFeedbackReportUseCase, "storageWeeklyFeedbackReportUseCase é obrigatório");
        this.reportSenderPort = Objects.requireNonNull(reportSenderPort, "reportSender é obrigatório");
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public StoredWeeklyFeedbackReportResult execute() throws InterruptedException, ExecutionException, TimeoutException {
        var now = ZonedDateTime.now(ZONE_ID_SP).toOffsetDateTime();

        var end = now.with(LocalTime.MAX);
        var begin = end.minusWeeks(1).with(LocalTime.MIN);
        var report = generateWeeklyFeedbackReportUseCase.execute(begin, end);
        var storedReportFuture = executorService.submit(() -> storageWeeklyFeedbackReportUseCase.execute(report));
        var reportSenderFuture = executorService.submit(() -> reportSenderPort.send(report));

        StoredReportResult storedReport = storedReportFuture.get(TIMEOUT_EM_SEGUNDOS, TimeUnit.SECONDS);
        reportSenderFuture.get(TIMEOUT_EM_SEGUNDOS, TimeUnit.SECONDS);
        return new StoredWeeklyFeedbackReportResult(report, storedReport);
    }

}
