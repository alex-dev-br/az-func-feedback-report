package br.com.fiap.techchallenge.report.infrastructure.config;

import br.com.fiap.techchallenge.report.application.controller.GenerateReportController;
import br.com.fiap.techchallenge.report.application.ports.FeedbackRepositoryPort;
import br.com.fiap.techchallenge.report.application.ports.ReportSenderPort;
import br.com.fiap.techchallenge.report.application.ports.ReportStoragePort;
import br.com.fiap.techchallenge.report.application.ports.WeeklyFeedbackReportSerializerPort;
import br.com.fiap.techchallenge.report.application.usecase.GenerateWeeklyFeedbackReportUseCase;
import br.com.fiap.techchallenge.report.application.usecase.StorageWeeklyFeedbackReportUseCase;
import br.com.fiap.techchallenge.report.infrastructure.sender.EmailReportSender;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class Producers {

    @Produces
    @ApplicationScoped
    public GenerateWeeklyFeedbackReportUseCase generateWeeklyFeedbackReportUseCaseProducer(
            FeedbackRepositoryPort feedbackRepository) {
        return new GenerateWeeklyFeedbackReportUseCase(feedbackRepository);
    }

    @Produces
    @ApplicationScoped
    public StorageWeeklyFeedbackReportUseCase storageWeeklyFeedbackReportUseCaseProducer(
            WeeklyFeedbackReportSerializerPort serializer, ReportStoragePort storage) {
        return new StorageWeeklyFeedbackReportUseCase(serializer, storage);
    }

    @Produces
    @ApplicationScoped
    public ReportSenderPort reportSenderPortProducer() {
        return new EmailReportSender();
    }

    @Produces
    @ApplicationScoped
    public GenerateReportController generateReportControllerProducer(
            GenerateWeeklyFeedbackReportUseCase generateWeeklyFeedbackReportUseCase,
            StorageWeeklyFeedbackReportUseCase storageWeeklyFeedbackReportUseCase,
            ReportSenderPort reportSenderPort) {
        return new GenerateReportController(generateWeeklyFeedbackReportUseCase, storageWeeklyFeedbackReportUseCase, reportSenderPort);
    }
}
