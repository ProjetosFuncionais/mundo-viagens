package br.com.mundoviagens;

import br.com.mundoviagens.domain.*;
import br.com.mundoviagens.repository.*;
import br.com.mundoviagens.service.ReservaService;
import br.com.mundoviagens.web.Dtos.*;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired HotelRepository hotels;
    @Autowired ReservaRepository reservas;
    @Autowired CancelamentoRepository cancelamentos;
    @Autowired ReservaService reservaService;
    @Autowired JwtEncoder encoder;
    @Autowired PlatformTransactionManager txManager;
    @MockitoBean Clock clock;
    Instant now;
    String flightId;

    @BeforeEach void setup() {
        cancelamentos.deleteAll(); reservas.deleteAll(); hotels.deleteAll(); users.deleteAll();
        now=Instant.now().truncatedTo(ChronoUnit.SECONDS);
        when(clock.instant()).thenReturn(now);
        when(clock.withZone(ZoneId.of("America/Sao_Paulo"))).thenReturn(Clock.fixed(now, ZoneId.of("America/Sao_Paulo")));
        flightId="FL-001_"+now.atZone(ZoneId.of("America/Sao_Paulo")).toLocalDate().plusDays(7);
    }
    record Account(UUID id, String token) {}
    Account register(int number) throws Exception {
        var response=read(send(post("/api/auth/register"), null, Map.of("nome", "Cliente " + number,
            "cpf", String.format("%011d", number), "email", "cliente"+number+"@example.com", "password", "Senha123!"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.user.password").doesNotExist()));
        return new Account(UUID.fromString(response.at("/user/id").asText()), response.get("token").asText());
    }
    Account admin() throws Exception {
        Account account=register(999);
        User user=users.findById(account.id()).orElseThrow(); user.setRole(Role.DONA); users.save(user);
        return account;
    }
    ResultActions send(MockHttpServletRequestBuilder request, Account account, Object body) throws Exception {
        if (account != null) request.header("Authorization", "Bearer "+account.token());
        if (body != null) request.contentType("application/json").content(json.writeValueAsBytes(body));
        return mvc.perform(request);
    }
    JsonNode read(ResultActions result) throws Exception { return json.readTree(result.andReturn().getResponse().getContentAsString()); }
    JsonNode book(Account account, String method, UUID hotelId) throws Exception {
        return read(send(post("/api/reservas"), account, new ReservaRequest(flightId, hotelId, PaymentMethod.valueOf(method)))
            .andExpect(status().isCreated()));
    }
    UUID id(JsonNode node) { return UUID.fromString(node.get("id").asText()); }
    void departure(UUID id, Instant departure) {
        new TransactionTemplate(txManager).executeWithoutResult(s -> reservas.findById(id).orElseThrow().getFlightDetails().setDataPartida(departure));
    }
    Map<String, Object> hotelInput(String name, String city, double price) {
        return Map.of("nome", name, "cidade", city, "precoDiaria", price, "quartosTotais", 10);
    }

    @Test void registrationLoginAndJwtValidation() throws Exception {
        Account account=register(1);
        assertThat(users.findById(account.id()).orElseThrow().getPassword()).startsWith("$2").isNotEqualTo("Senha123!");
        send(get("/api/auth/me"), account, null).andExpect(status().isOk()).andExpect(jsonPath("$.role").value("CLIENTE"));
        send(post("/api/auth/login"), null, Map.of("email", "CLIENTE1@example.com", "password", "Senha123!"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.token").isString());
        send(post("/api/auth/login"), null, Map.of("email", "cliente1@example.com", "password", "incorreta"))
            .andExpect(status().isUnauthorized());
        send(get("/api/auth/me"), null, null).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer "+account.token()+"invalid")).andExpect(status().isUnauthorized());
        var claims=JwtClaimsSet.builder().issuer("mundo-viagens").subject(account.id().toString())
            .issuedAt(now.minusSeconds(7200)).expiresAt(now.minusSeconds(3600)).build();
        String expired=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer "+expired)).andExpect(status().isUnauthorized());
    }
    @Test void duplicateAndInvalidRegistrationCannotEscalatePrivileges() throws Exception {
        register(1);
        send(post("/api/auth/register"), null, Map.of("nome", "Outro", "cpf", "00000000002", "email", "CLIENTE1@example.com", "password", "Senha123!"))
            .andExpect(status().isConflict());
        send(post("/api/auth/register"), null, Map.of("nome", "Outro", "cpf", "00000000001", "email", "outro@example.com", "password", "Senha123!"))
            .andExpect(status().isConflict());
        send(post("/api/auth/register"), null, Map.of("nome", "Outro", "cpf", "00000000002", "email", "outro@example.com", "password", "Senha123!", "role", "DONA"))
            .andExpect(status().isBadRequest());
        send(post("/api/auth/register"), null, Map.of("nome", "", "cpf", "12", "email", "invalid", "password", "123"))
            .andExpect(status().isBadRequest());
    }
    @Test void flightsFilterByOriginDestinationAndDate() throws Exception {
        String date=flightId.substring(7);
        mvc.perform(get("/api/voos").param("origem", "sao paulo").param("destino", "rio").param("data", date))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(flightId));
        mvc.perform(get("/api/voos").param("data", "2000-01-01")).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/voos").param("data", "invalid")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/voos/invalid")).andExpect(status().isNotFound());
        mvc.perform(get("/api/voos")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4));
    }
    @Test void hotelCrudAndPermissions() throws Exception {
        Account client=register(1), admin=admin();
        var data=hotelInput("Hotel Rio", "Rio de Janeiro (GIG)", 100);
        send(post("/api/hoteis"), null, data).andExpect(status().isUnauthorized());
        send(post("/api/hoteis"), client, data).andExpect(status().isForbidden());
        JsonNode hotel=read(send(post("/api/hoteis"), admin, data).andExpect(status().isCreated()));
        String path="/api/hoteis/"+id(hotel);
        send(get(path), null, null).andExpect(status().isOk());
        send(put(path), client, data).andExpect(status().isForbidden());
        send(delete(path), client, null).andExpect(status().isForbidden());
        send(put(path), admin, hotelInput("Novo nome", "Rio de Janeiro (GIG)", 120)).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Novo nome"));
        send(post("/api/hoteis"), admin, hotelInput("Hotel", "Rio", -1)).andExpect(status().isBadRequest());
        send(delete(path), admin, null).andExpect(status().isNoContent());
        send(get(path), null, null).andExpect(status().isNotFound());
    }
    @ParameterizedTest @EnumSource(value=Role.class, names={"ATENDENTE", "GERENTE", "DONA"})
    void staffRolesCanManageAndRoleChangesAreImmediate(Role role) throws Exception {
        Account staff=register(1);
        User user=users.findById(staff.id()).orElseThrow(); user.setRole(role); users.save(user);
        send(get("/api/reservas"), staff, null).andExpect(status().isOk());
        send(get("/api/clientes"), staff, null).andExpect(status().isOk()).andExpect(jsonPath("$[0].password").doesNotExist());
        send(post("/api/hoteis"), staff, hotelInput("Hotel", "Rio", 100)).andExpect(status().isCreated());
        user.setRole(Role.CLIENTE); users.save(user);
        send(get("/api/reservas"), staff, null).andExpect(status().isForbidden());
        send(get("/api/clientes"), staff, null).andExpect(status().isForbidden());
    }
    @Test void bookingUsesServerPricesAndPreservesHotelSnapshot() throws Exception {
        Account client=register(1), admin=admin();
        JsonNode hotel=read(send(post("/api/hoteis"), admin, hotelInput("Hotel Rio", "Rio de Janeiro (GIG)", 100)).andExpect(status().isCreated()));
        JsonNode booking=book(client, "cartao", id(hotel));
        assertThat(booking.get("total").decimalValue()).isEqualByComparingTo("650.00");
        assertThat(booking.get("status").asText()).isEqualTo("confirmada");
        assertThat(booking.get("ticketId").asText()).startsWith("TKT-");
        send(post("/api/reservas"), client, Map.of("flightId", flightId, "paymentMethod", "cartao", "total", 1)).andExpect(status().isBadRequest());
        send(put("/api/hoteis/"+id(hotel)), admin, hotelInput("Renomeado", "Rio de Janeiro (GIG)", 999)).andExpect(status().isOk());
        send(get("/api/reservas/minhas"), client, null).andExpect(jsonPath("$[0].hotelDetails.nome").value("Hotel Rio"))
            .andExpect(jsonPath("$[0].total").value(650));
        send(delete("/api/hoteis/"+id(hotel)), admin, null).andExpect(status().isConflict());
    }
    @Test void paymentRequiresAdminAndCancellationCannotBeReconfirmed() throws Exception {
        Account client=register(1), admin=admin();
        JsonNode booking=book(client, "boleto", null); String path="/api/reservas/"+id(booking);
        assertThat(booking.get("status").asText()).isEqualTo("pendente_pagamento");
        send(post(path+"/confirmar-pagamento"), client, null).andExpect(status().isForbidden());
        JsonNode confirmed=read(send(post(path+"/confirmar-pagamento"), admin, null).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("confirmada")));
        send(post(path+"/confirmar-pagamento"), admin, null).andExpect(jsonPath("$.ticketId").value(confirmed.get("ticketId").asText()));
        send(post(path+"/cancelar"), client, null).andExpect(status().isOk()).andExpect(jsonPath("$.ticketId").isEmpty());
        send(post(path+"/confirmar-pagamento"), admin, null).andExpect(status().isConflict());
    }
    @Test void usersOnlySeeAndCancelTheirOwnBookings() throws Exception {
        Account first=register(1), second=register(2);
        JsonNode booking=book(first, "cartao", null);
        send(get("/api/reservas/minhas"), second, null).andExpect(jsonPath("$.length()").value(0));
        send(get("/api/reservas"), first, null).andExpect(status().isForbidden());
        send(post("/api/reservas/"+id(booking)+"/cancelar"), second, null).andExpect(status().isForbidden());
        assertThat(cancelamentos.count()).isZero();
        send(post("/api/reservas"), second, Map.of("flightId", flightId, "paymentMethod", "cartao", "userId", first.id())).andExpect(status().isBadRequest());
    }
    @Test void cancellationChargesTwentyPercentStrictlyUnder48Hours() throws Exception {
        Account client=register(1);
        UUID atBoundary=id(book(client, "cartao", null));
        UUID belowBoundary=id(book(client, "cartao", null));
        departure(atBoundary, now.plus(Duration.ofHours(48)));
        departure(belowBoundary, now.plus(Duration.ofHours(48)).minusSeconds(1));
        send(post("/api/reservas/"+atBoundary+"/cancelar"), client, null).andExpect(status().isOk()).andExpect(jsonPath("$.multa").value(0));
        send(post("/api/reservas/"+belowBoundary+"/cancelar"), client, null).andExpect(status().isOk()).andExpect(jsonPath("$.multa").value(70));
        send(post("/api/reservas/"+belowBoundary+"/cancelar"), client, null).andExpect(status().isOk()).andExpect(jsonPath("$.multa").value(70));
        assertThat(cancelamentos.count()).isEqualTo(2);
    }
    @Test void thirdRecentCancellationBlocksNewBookingsButKeepsHistoryAndCanBeUnblocked() throws Exception {
        Account client=register(1), admin=admin();
        for (int i=0; i<3; i++) {
            UUID booking=id(book(client, "boleto", null));
            send(post("/api/reservas/"+booking+"/cancelar"), client, null).andExpect(status().isOk());
        }
        send(get("/api/auth/me"), client, null).andExpect(status().isOk()).andExpect(jsonPath("$.bloqueado").value(true));
        send(post("/api/reservas"), client, new ReservaRequest(flightId, null, PaymentMethod.boleto)).andExpect(status().isForbidden());
        send(get("/api/reservas/minhas"), client, null).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
        send(patch("/api/clientes/"+client.id()+"/bloqueio"), client, Map.of("bloqueado", false)).andExpect(status().isForbidden());
        send(patch("/api/clientes/"+client.id()+"/bloqueio"), admin, Map.of("bloqueado", false)).andExpect(status().isOk());
        book(client, "boleto", null);
    }
    @Test void cancellationExactly30DaysAgoIsExcluded() throws Exception {
        Account client=register(1);
        UUID old=id(book(client, "cartao", null));
        send(post("/api/reservas/"+old+"/cancelar"), client, null).andExpect(status().isOk());
        Cancelamento log=cancelamentos.findAll().getFirst(); log.setDataCancelamento(now.minus(Duration.ofDays(30))); cancelamentos.save(log);
        for (int i=0; i<2; i++) {
            UUID booking=id(book(client, "cartao", null));
            send(post("/api/reservas/"+booking+"/cancelar"), client, null).andExpect(status().isOk());
        }
        assertThat(users.findById(client.id()).orElseThrow().getBloqueado()).isFalse();
        UUID third=id(book(client, "cartao", null));
        send(post("/api/reservas/"+third+"/cancelar"), client, null).andExpect(status().isOk());
        assertThat(users.findById(client.id()).orElseThrow().getBloqueado()).isTrue();
    }
    @Test void concurrentCancellationsCountOnceAndStillBlockAtThree() throws Exception {
        Account client=register(1);
        List<UUID> ids=new ArrayList<>();
        for (int i=0; i<3; i++) ids.add(id(book(client, "cartao", null)));
        CountDownLatch start=new CountDownLatch(1);
        try (ExecutorService executor=Executors.newFixedThreadPool(4)) {
            List<Future<ReservaResponse>> futures=new ArrayList<>();
            for (UUID id : List.of(ids.get(0), ids.get(0), ids.get(1), ids.get(2))) {
                futures.add(executor.submit(() -> { start.await(); return reservaService.cancel(id, client.id(), false); }));
            }
            start.countDown();
            for (Future<ReservaResponse> future : futures) assertThat(future.get(10, TimeUnit.SECONDS).status()).isEqualTo(ReservaStatus.cancelada);
        }
        assertThat(cancelamentos.count()).isEqualTo(3);
        assertThat(users.findById(client.id()).orElseThrow().getBloqueado()).isTrue();
    }
    @Test void pastDeparturesAndIncompatibleHotelsAreRejected() throws Exception {
        Account client=register(1), admin=admin();
        JsonNode hotel=read(send(post("/api/hoteis"), admin, hotelInput("Hotel Miami", "Miami (MIA)", 100)).andExpect(status().isCreated()));
        send(post("/api/reservas"), client, new ReservaRequest(flightId, id(hotel), PaymentMethod.cartao)).andExpect(status().isBadRequest());
        send(post("/api/reservas"), client, new ReservaRequest("FL-001_2000-01-01", null, PaymentMethod.cartao)).andExpect(status().isConflict());
        UUID booking=id(book(client, "boleto", null)); departure(booking, now);
        send(post("/api/reservas/"+booking+"/cancelar"), client, null).andExpect(status().isConflict());
        send(post("/api/reservas/"+booking+"/confirmar-pagamento"), admin, null).andExpect(status().isConflict());
        assertThat(cancelamentos.count()).isZero();
    }
    @Test void corsAllowsConfiguredFrontendOnly() throws Exception {
        mvc.perform(options("/api/reservas").header("Origin", "http://localhost:3000")
            .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "Authorization,Content-Type"))
            .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
        mvc.perform(options("/api/reservas").header("Origin", "https://untrusted.example")
            .header("Access-Control-Request-Method", "POST")).andExpect(status().isForbidden());
    }
}
