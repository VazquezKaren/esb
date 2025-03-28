package com.utd.ti.soa.esb_service.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;


import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.utd.ti.soa.esb_service.model.Client;
import com.utd.ti.soa.esb_service.utils.Auth;


@RestController
@RequestMapping("/api/v2/esb")
public class ESBControllerCliente {

    private final WebClient webClient = WebClient.create();
    private final Auth auth = new Auth();

    @PostMapping("/client")
    public ResponseEntity<String> createClient(@RequestBody Client client,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {

        System.out.println("Request Body: " + client);
        System.out.println("Token recibido: " + token);

        // Validar el token
        if (!auth.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body("Token inválido o expirado");
        }

        // Enviar la solicitud al servicio externo con el token
        String response = webClient.post()
                .uri("http://localhost:6002/api/v2/client/create")
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(client)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/client")
    public ResponseEntity<String> getClients(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String token
    ) {
        System.out.println("Token recibido: " + token);

    // Validar el token
    if (!auth.validateToken(token)) {
        return ResponseEntity.status(401)
                .body("Token inválido o expirado");
    }
        try {
            String response = webClient.get()
                .uri("http://localhost:6002/api/v2/client/all") 
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> System.out.println("Error: " + error.getMessage()))
                .block();

            return ResponseEntity.ok(response);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Error en la solicitud: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }

    @PatchMapping("/client/{id}")
    public ResponseEntity<String> updateClient(@PathVariable String id, @RequestBody Client client,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
    if (!auth.validateToken(token)) {
        return ResponseEntity.status(401).body("Token inválido o expirado");
    }
    try {
        String response = webClient.patch()
                .uri("http://localhost:6002/api/v2/client/" + id)  // Aquí se corrige la URI
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(client)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return ResponseEntity.ok(response);
    } catch (WebClientResponseException e) {
        return ResponseEntity.status(e.getStatusCode()).body("Error en la solicitud: " + e.getMessage());
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
    }
}


    @PatchMapping("/client/{id}/status")
    public ResponseEntity<String> deleteStatus(@PathVariable String id, @RequestBody Client Client,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        if (!auth.validateToken(token)) {
            return ResponseEntity.status(401).body("Token inválido o expirado");
        }
        try {
            String response = webClient.patch()
                    .uri("http://localhost:6002/api/v2/client/" + id + "/status")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .bodyValue(Client)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Error en la solicitud: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }
}




