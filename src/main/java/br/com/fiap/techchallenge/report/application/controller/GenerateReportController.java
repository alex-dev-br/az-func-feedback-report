package br.com.fiap.techchallenge.report.application.controller;

import br.com.fiap.techchallenge.report.application.dto.report.StoredWeeklyFeedbackReportResult;
import br.com.fiap.techchallenge.report.application.usecase.GenerateWeeklyFeedbackReportUseCase;
import br.com.fiap.techchallenge.report.application.usecase.StorageWeeklyFeedbackReportUseCase;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

public class GenerateReportController {

    private static final ZoneId ZONE_ID_SP = ZoneId.of("America/Sao_Paulo");

    private final GenerateWeeklyFeedbackReportUseCase generateWeeklyFeedbackReportUseCase;
    private final StorageWeeklyFeedbackReportUseCase storageWeeklyFeedbackReportUseCase;

    public GenerateReportController(
            GenerateWeeklyFeedbackReportUseCase generateWeeklyFeedbackReportUseCase,
            StorageWeeklyFeedbackReportUseCase storageWeeklyFeedbackReportUseCase) {
        this.generateWeeklyFeedbackReportUseCase = Objects.requireNonNull(generateWeeklyFeedbackReportUseCase, "generateWeeklyFeedbackReportUseCase é obrigatório");
        this.storageWeeklyFeedbackReportUseCase = Objects.requireNonNull(storageWeeklyFeedbackReportUseCase, "storageWeeklyFeedbackReportUseCase é obrigatório");
    }

    public StoredWeeklyFeedbackReportResult execute() {
        var now = ZonedDateTime.now(ZONE_ID_SP).toOffsetDateTime();

        var end = now.with(LocalTime.MAX);
        var begin = end.minusWeeks(1).with(LocalTime.MIN);

        var report = generateWeeklyFeedbackReportUseCase.execute(begin, end);
        var storedReport = storageWeeklyFeedbackReportUseCase.execute(report);
        return new StoredWeeklyFeedbackReportResult(report, storedReport);
    }

}
