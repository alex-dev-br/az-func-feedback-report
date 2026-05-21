package br.com.fiap.techchallenge.report.application.ports;

import br.com.fiap.techchallenge.report.application.dto.report.WeeklyFeedbackReport;

public interface ReportSenderPort {
    void send(WeeklyFeedbackReport report);
}
