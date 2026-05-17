package br.com.fiap.techchallenge.report.infrastructure.persistence.repository;

import br.com.fiap.techchallenge.report.domain.enums.Urgencia;
import br.com.fiap.techchallenge.report.domain.model.Feedback;
import br.com.fiap.techchallenge.report.infrastructure.persistence.entity.FeedbackEntity;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@DisplayName("JpaFeedbackRepositoryAdapter")
@Tag("integration")
@Tag("infrastructure")
class JpaFeedbackRepositoryAdapterTest {

    @Inject
    JpaFeedbackRepositoryAdapter repositoryAdapter;

    @Inject
    PanacheFeedbackRepository panacheRepository;

    @Test
    @TestTransaction
    @DisplayName("Deve buscar feedbacks dentro do período ordenados por data de criação")
    void deveBuscarFeedbacksDentroDoPeriodoOrdenadosPorDataCriacao() {
        // Arrange
        OffsetDateTime inicio = dataHoraUtc(2026, 5, 1, 0, 0);
        OffsetDateTime fim = dataHoraUtc(2026, 5, 8, 0, 0);

        panacheRepository.deleteAll();

        FeedbackEntity foraDoPeriodoAntes = feedbackEntity(
                "Feedback anterior",
                5,
                Urgencia.MEDIA,
                dataHoraUtc(2026, 4, 30, 23, 59)
        );

        FeedbackEntity noLimiteInicial = feedbackEntity(
                "Feedback no limite inicial",
                8,
                Urgencia.BAIXA,
                inicio
        );

        FeedbackEntity dentroDoPeriodoMaisRecente = feedbackEntity(
                "Segundo feedback dentro do período",
                9,
                Urgencia.BAIXA,
                dataHoraUtc(2026, 5, 2, 10, 0)
        );

        FeedbackEntity dentroDoPeriodoMaisAntigo = feedbackEntity(
                "Primeiro feedback dentro do período",
                2,
                Urgencia.ALTA,
                dataHoraUtc(2026, 5, 1, 9, 0)
        );

        FeedbackEntity foraDoPeriodoNoLimiteFinal = feedbackEntity(
                "Feedback no limite final",
                7,
                Urgencia.MEDIA,
                fim
        );

        panacheRepository.persist(foraDoPeriodoAntes);
        panacheRepository.persist(noLimiteInicial);
        panacheRepository.persist(dentroDoPeriodoMaisRecente);
        panacheRepository.persist(dentroDoPeriodoMaisAntigo);
        panacheRepository.persist(foraDoPeriodoNoLimiteFinal);
        panacheRepository.flush();

        // Act
        List<Feedback> feedbacks = repositoryAdapter.findByDataCriacaoBetween(inicio, fim);

        // Assert
        assertAll(
                () -> assertEquals(3, feedbacks.size()),

                () -> assertEquals("Feedback no limite inicial", feedbacks.get(0).descricao()),
                () -> assertEquals(8, feedbacks.get(0).nota()),
                () -> assertEquals(Urgencia.BAIXA, feedbacks.get(0).urgencia()),

                () -> assertEquals("Primeiro feedback dentro do período", feedbacks.get(1).descricao()),
                () -> assertEquals(2, feedbacks.get(1).nota()),
                () -> assertEquals(Urgencia.ALTA, feedbacks.get(1).urgencia()),

                () -> assertEquals("Segundo feedback dentro do período", feedbacks.get(2).descricao()),
                () -> assertEquals(9, feedbacks.get(2).nota()),
                () -> assertEquals(Urgencia.BAIXA, feedbacks.get(2).urgencia())
        );
    }

    private static OffsetDateTime dataHoraUtc(int ano, int mes, int dia, int hora, int minuto) {
        return OffsetDateTime.of(ano, mes, dia, hora, minuto, 0, 0, ZoneOffset.UTC);
    }

    private static FeedbackEntity feedbackEntity(
            String descricao,
            int nota,
            Urgencia urgencia,
            OffsetDateTime dataCriacao
    ) {
        FeedbackEntity entity = new FeedbackEntity();
        entity.setId(UUID.randomUUID());
        entity.setDescricao(descricao);
        entity.setNota(nota);
        entity.setUrgencia(urgencia);
        entity.setDataCriacao(dataCriacao);
        return entity;
    }
}
