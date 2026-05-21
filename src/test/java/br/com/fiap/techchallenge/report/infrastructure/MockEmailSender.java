package br.com.fiap.techchallenge.report.infrastructure;

import br.com.fiap.techchallenge.report.application.dto.report.WeeklyFeedbackReport;
import br.com.fiap.techchallenge.report.application.ports.WeeklyFeedbackReportSerializerPort;
import br.com.fiap.techchallenge.report.infrastructure.sender.EmailReportSender;
import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mock
@ApplicationScoped
public class MockEmailSender extends EmailReportSender {

    private static final Logger LOG = LoggerFactory.getLogger(MockEmailSender.class);

    @Override
    public void send(WeeklyFeedbackReport weeklyFeedbackReport) {
        LOG.info("[MOCK] Simulação de envio de e-mail");
    }
}
