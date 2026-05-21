package br.com.fiap.techchallenge.report.infrastructure.sender;

import br.com.fiap.techchallenge.report.application.dto.report.WeeklyFeedbackReport;
import br.com.fiap.techchallenge.report.application.ports.ReportSenderPort;
import br.com.fiap.techchallenge.report.application.ports.WeeklyFeedbackReportSerializerPort;
import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailAddress;
import com.azure.communication.email.models.EmailAttachment;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import io.micrometer.core.annotation.Timed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@ApplicationScoped
public class EmailReportSender implements ReportSenderPort {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Logger LOG = LoggerFactory.getLogger(EmailReportSender.class);

    @Inject
    @ConfigProperty(name = "app.email.connection-string")
    String connectionString;

    @Inject
    @ConfigProperty(name = "app.admin-emails")
    String adminEmails;

    @Inject
    @ConfigProperty(name = "app.email.sender-address")
    String senderAddress;

    @Inject
    @ConfigProperty(name = "app.email.subject")
    String subject;

    @Inject
    WeeklyFeedbackReportSerializerPort weeklyFeedbackReportSerializer;

    @Timed(value = "report.sendEmail.duration", description = "Tempo de execução do envio do email com relatório semanal de feedbacks")
    @Override
    public void send(WeeklyFeedbackReport weeklyFeedbackReport) {
        EmailClient emailClient = new EmailClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        List<EmailAddress> addresses = extrairEmailsAdministradores(adminEmails)
                .stream()
                .map(EmailAddress::new)
                .toList();

        if (addresses.isEmpty()) {
            throw new IllegalStateException("Nenhum e-mail de administrador configurado em app.admin-emails.");
        }

        OffsetDateTime now = weeklyFeedbackReport.inicio();
        var reportJson = weeklyFeedbackReportSerializer.serialize(weeklyFeedbackReport);

        var reportName = String.format("%s-relatorio-semanal-feedbacks.json", now.format(DATE_TIME_FORMATTER));
        EmailAttachment reportAttachment = new EmailAttachment(reportName,"application/json",
                BinaryData.fromString(reportJson));

        EmailMessage emailMessage = new EmailMessage()
                .setSenderAddress(senderAddress)
                .setToRecipients(addresses)
                .setSubject(subject)
                .setAttachments(reportAttachment)
                .setBodyHtml(HTML_BODY);

        SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(emailMessage, null);
        PollResponse<EmailSendResult> result = poller.waitForCompletion(java.time.Duration.ofSeconds(10));

        LOG.info("Email sent with message Id: {}", result.getValue().getId());
    }

    private List<String> extrairEmailsAdministradores(String emailsConfigurados) {
        if (emailsConfigurados == null || emailsConfigurados.isBlank()) {
            return List.of();
        }

        return Stream.of(emailsConfigurados.split("[;,]"))
                .map(String::trim)
                .filter(email -> !email.isBlank())
                .distinct()
                .toList();
    }

    private static final String HTML_BODY = """
    <!DOCTYPE html>
    <html>
    <head>
        <style>
            body {
                font-family: Arial, sans-serif;
                background-color: #f4f4f4;
                margin: 0;
                padding: 0;
            }
            .container {
                background-color: #ffffff;
                width: 100%;
                max-width: 600px;
                margin: 20px auto;
                padding: 30px;
                border-radius: 8px;
                box-shadow: 0 4px 8px rgba(0,0,0,0.1);
            }
            .header {
                color: #333333;
                border-bottom: 2px solid #0078D4; /* Cor azul estilo Azure/Corporativo */
                padding-bottom: 10px;
                margin-bottom: 20px;
            }
            .content {
                color: #555555;
                line-height: 1.6;
            }
            .footer {
                margin-top: 30px;
                font-size: 12px;
                color: #999999;
                text-align: center;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="header">
                <h2>Relatório Semanal de Feedbacks</h2>
            </div>
            <div class="content">
                <p>Olá,</p>
                <p>O relatório semanal consolidado de feedbacks já foi gerado e está disponível em <strong>anexo</strong>.</p> 
                <p>O arquivo foi disponibilizado no formato JSON contendo todos os detalhes e métricas referentes ao período.</p>
            </div>
            <div class="footer">
                <p>Esta é uma mensagem automática, por favor não responda a este e-mail.</p>
            </div>
        </div>
    </body>
    </html>
    """;

}
