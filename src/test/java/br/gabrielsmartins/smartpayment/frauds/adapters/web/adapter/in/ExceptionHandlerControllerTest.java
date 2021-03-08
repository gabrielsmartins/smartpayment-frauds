package br.gabrielsmartins.smartpayment.frauds.adapters.web.adapter.in;

import br.gabrielsmartins.smartpayment.frauds.adapters.web.adapter.in.dto.FraudDTO;
import br.gabrielsmartins.smartpayment.frauds.adapters.web.adapter.in.mapper.FraudItemWebMapper;
import br.gabrielsmartins.smartpayment.frauds.adapters.web.adapter.in.mapper.FraudWebMapper;
import br.gabrielsmartins.smartpayment.frauds.application.domain.Fraud;
import br.gabrielsmartins.smartpayment.frauds.application.domain.FraudItem;
import br.gabrielsmartins.smartpayment.frauds.application.domain.enums.PaymentMethod;
import br.gabrielsmartins.smartpayment.frauds.application.ports.in.SaveFraudUseCase;
import br.gabrielsmartins.smartpayment.frauds.application.ports.in.SearchFraudUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

import static br.gabrielsmartins.smartpayment.frauds.adapters.web.support.FraudDTOSupport.defaultFraudDTO;
import static br.gabrielsmartins.smartpayment.frauds.adapters.web.support.FraudItemDTOSupport.defaultFraudItemDTO;
import static br.gabrielsmartins.smartpayment.frauds.adapters.web.support.PaymentMethodDTOSupport.defaultPaymentMethodDTO;
import static br.gabrielsmartins.smartpayment.frauds.application.support.FraudItemSupport.defaultFraudItem;
import static br.gabrielsmartins.smartpayment.frauds.application.support.FraudSupport.defaultFraud;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = SaveFraudController.class)
@Import({FraudWebMapper.class, FraudItemWebMapper.class})
public class ExceptionHandlerControllerTest {

    @MockBean
    private SaveFraudUseCase useCase;

    @MockBean
    private SearchFraudUseCase searchFraudUseCase;

    @Autowired
    private WebTestClient webClient;

    private ObjectMapper mapper;

    @BeforeEach
    public void setup(){
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Given Fraud When Is Invalid Then Return Bad Request")
    public void givenFraudWhenIsInvalidThenReturnBadRequest() throws JsonProcessingException {

        FraudDTO fraudDTO = new FraudDTO();

        String body = mapper.writeValueAsString(fraudDTO);

        Fraud fraudMock = defaultFraud().build();
        fraudMock.addItem(defaultFraudItem().build());
        fraudMock.addPaymentMethod(PaymentMethod.CASH, BigDecimal.valueOf(1500));

        when(useCase.save(any(Fraud.class))).thenReturn(Mono.just(fraudMock));

        webClient.post()
                .uri("/frauds-v1/frauds")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("fields").isNotEmpty();
    }

    @Test
    @DisplayName("Given Fraud When Not Exists Then Return Not Found")
    public void givenFraudWhenNotExistsThenReturnNotFound() throws JsonProcessingException {

        FraudDTO fraudDTO = defaultFraudDTO().build();
        fraudDTO.addItem(defaultFraudItemDTO().build());
        fraudDTO.addPaymentMethod(defaultPaymentMethodDTO().build());

        String body = mapper.writeValueAsString(fraudDTO);

        Fraud fraud = defaultFraud().build();
        FraudItem fraudItem = defaultFraudItem().build();
        fraud.addItem(fraudItem);

        when(searchFraudUseCase.findById(any(UUID.class))).thenReturn(Mono.empty());

        webClient.put()
                .uri("/frauds-v1/frauds/{id}", fraudDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("message").isNotEmpty();

        verify(this.searchFraudUseCase, times(1)).findById(any(UUID.class));
        verify(this.useCase, never()).save(any(Fraud.class));
    }

    @Test
    @DisplayName("Given Fraud When Error Occurs Then Return Internal Server Error")
    public void givenFraudWhenErrorOccursThenReturnInternalServerError() throws JsonProcessingException {

        FraudDTO fraudDTO = defaultFraudDTO().build();
        fraudDTO.addItem(defaultFraudItemDTO().build());
        fraudDTO.addPaymentMethod(defaultPaymentMethodDTO().build());

        String body = mapper.writeValueAsString(fraudDTO);

        Fraud fraud = defaultFraud().build();
        FraudItem fraudItem = defaultFraudItem().build();
        fraud.addItem(fraudItem);

        when(searchFraudUseCase.findById(any(UUID.class))).thenAnswer(invocation -> {
            throw new RuntimeException("Error");
        });

        webClient.put()
                .uri("/frauds-v1/frauds/{id}", fraudDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("message").isNotEmpty();
    }
}
