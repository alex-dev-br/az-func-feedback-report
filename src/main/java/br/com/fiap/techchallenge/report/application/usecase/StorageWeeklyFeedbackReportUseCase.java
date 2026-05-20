package br.com.fiap.techchallenge.report.application.usecase;

import br.com.fiap.techchallenge.report.application.dto.report.StoredReportResult;
import br.com.fiap.techchallenge.report.application.dto.report.WeeklyFeedbackReport;
import br.com.fiap.techchallenge.report.application.ports.ReportStoragePort;
import br.com.fiap.techchallenge.report.application.ports.WeeklyFeedbackReportSerializerPort;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class StorageWeeklyFeedbackReportUseCase {

    public static final String BASE_DIR_REPORTS_WEEKLY = "reports/weekly/";
    public static final DateTimeFormatter ANO_MES_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final WeeklyFeedbackReportSerializerPort serializer;
    private final ReportStoragePort storage;

    public StorageWeeklyFeedbackReportUseCase(WeeklyFeedbackReportSerializerPort serializer,ReportStoragePort storage) {
        this.serializer = Objects.requireNonNull(serializer, "serializer é obrigatório");
        this.storage = Objects.requireNonNull(storage, "storage é obrigatório");
    }

    public StoredReportResult execute(WeeklyFeedbackReport report) {
        Objects.requireNonNull(report, "report é obrigatório");
        OffsetDateTime now = report.inicio();
        var reportName = String.format("%s%s/%s-relatorio-semanal-feedbacks.json",
                BASE_DIR_REPORTS_WEEKLY, now.format(ANO_MES_FORMATTER), now.format(DATE_TIME_FORMATTER));
        String content = serializer.serialize(report);
        return storage.save(reportName, content, "application/json");
    }

}
