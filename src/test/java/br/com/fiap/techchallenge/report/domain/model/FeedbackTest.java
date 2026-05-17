package br.com.fiap.techchallenge.report.domain.model;

import br.com.fiap.techchallenge.report.domain.enums.Urgencia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Feedback")
@Tag("unit")
@Tag("domain")
class FeedbackTest {

    @Nested
    @DisplayName("Criação de feedback")
    class CriacaoDeFeedback {

        @Test
        @DisplayName("Deve criar feedback com dados válidos")
        void deveCriarNovoFeedbackComDadosValidos() {
            // Arrange
            UUID id = UUID.randomUUID();
            String descricao = "A aula foi muito boa";
            int nota = 9;
            Urgencia urgencia = Urgencia.BAIXA;
            OffsetDateTime dataCriacao = OffsetDateTime.now();

            // Act
            Feedback feedback = new Feedback(
                    id,
                    descricao,
                    nota,
                    urgencia,
                    dataCriacao
            );

            // Assert
            assertAll(
                    () -> assertEquals(id, feedback.id()),
                    () -> assertEquals(descricao, feedback.descricao()),
                    () -> assertEquals(nota, feedback.nota()),
                    () -> assertEquals(urgencia, feedback.urgencia()),
                    () -> assertEquals(dataCriacao, feedback.dataCriacao())
            );
        }
    }

    @Nested
    @DisplayName("Validação da descrição")
    class ValidacaoDaDescricao {

        @Test
        @DisplayName("Deve falhar quando descrição for nula")
        void deveFalharQuandoDescricaoForNula() {
            // Arrange
            UUID id = UUID.randomUUID();

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> new Feedback(
                    id,
                    null,
                    8,
                    Urgencia.BAIXA,
                    OffsetDateTime.now()
            ));
        }

        @Test
        @DisplayName("Deve falhar quando descrição for vazia")
        void deveFalharQuandoDescricaoForVazia() {
            // Arrange
            UUID id = UUID.randomUUID();

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> new Feedback(
                    id,
                    "   ",
                    8,
                    Urgencia.BAIXA,
                    OffsetDateTime.now()
            ));
        }
    }

    @Nested
    @DisplayName("Validação da nota")
    class ValidacaoDaNota {

        @Test
        @DisplayName("Deve falhar quando nota for menor que zero")
        void deveFalharQuandoNotaForMenorQueZero() {
            // Arrange
            UUID id = UUID.randomUUID();

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> new Feedback(
                    id,
                    "Comentário válido",
                    -1,
                    Urgencia.BAIXA,
                    OffsetDateTime.now()
            ));
        }

        @Test
        @DisplayName("Deve falhar quando nota for maior que dez")
        void deveFalharQuandoNotaForMaiorQueDez() {
            // Arrange
            UUID id = UUID.randomUUID();

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> new Feedback(
                    id,
                    "Comentário válido",
                    11,
                    Urgencia.BAIXA,
                    OffsetDateTime.now()
            ));
        }
    }
}
