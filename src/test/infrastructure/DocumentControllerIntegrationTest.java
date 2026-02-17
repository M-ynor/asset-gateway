package com.bhd.documentgateway.infrastructure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Base64;
import java.util.Map;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@SpringBootTest
class DocumentControllerIntegrationTest {

    @Autowired
    ApplicationContext context;

    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(context)
                .apply(springSecurity())
                .configureClient()
                .build();
    }

    @Test
    void upload_returns202WithId() {
        String body = """
                {"filename":"doc.pdf","encodedFile":"%s","contentType":"application/pdf","documentType":"CONTRACT","channel":"DIGITAL"}
                """
                .formatted(Base64.getEncoder().encodeToString("content".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        webTestClient.mutateWith(mockUser("bhd").roles("USER"))
                .post()
                .uri("/api/bhd/mgmt/1/documents/actions/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .jsonPath("$.id").exists();
    }

    @Test
    void upload_withoutAuth_returns401() {
        webTestClient.post()
                .uri("/api/bhd/mgmt/1/documents/actions/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void search_withSortBy_returns200() {
        webTestClient.mutateWith(mockUser("bhd").roles("USER"))
                .get()
                .uri(uri -> uri.path("/api/bhd/mgmt/1/documents").queryParam("sortBy", "uploadDate").build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Map.class);
    }

    @Test
    void search_withoutSortBy_returns200() {
        webTestClient.mutateWith(mockUser("bhd").roles("USER"))
                .get()
                .uri("/api/bhd/mgmt/1/documents")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Map.class);
    }

    @Test
    void search_byId_returns200() {
        webTestClient.mutateWith(mockUser("bhd").roles("USER"))
                .get()
                .uri(uri -> uri.path("/api/bhd/mgmt/1/documents").queryParam("id", "some-id").build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Map.class);
    }
}
