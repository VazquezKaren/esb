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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.utd.ti.soa.esb_service.model.User;
import com.utd.ti.soa.esb_service.utils.Auth;

import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/v1/esb")
public class ESBController {

    private final WebClient webClient = WebClient.create();
    private final Auth auth = new Auth();

    @PostMapping("/user")
    public ResponseEntity<String> createUser(@RequestBody User user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {

        System.out.println("Request Body: " + user);
        System.out.println("Token recibido: " + token);

        // Validar el token
        if (!auth.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body("Token inválido o expirado");
        }

        // Enviar la solicitud al servicio externo con el token
        String response = webClient.post()
                .uri("http://users.railway.internal:3003/api/v1/users/create")
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(user)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/user")
    public ResponseEntity<String> getUsers(
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
                .uri("http://users.railway.internal:3003/api/v1/users/all") 
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

    @PatchMapping("/user/{id}")
    public ResponseEntity<String> updateUser(@PathVariable String id, @RequestBody User user,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
    if (!auth.validateToken(token)) {
        return ResponseEntity.status(401).body("Token inválido o expirado");
    }
    try {
        String response = webClient.patch()
                .uri("http://users.railway.internal:3003/api/v1/users/" + id)  // Aquí se corrige la URI
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(user)
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
    
    @PatchMapping("/user/{id}/status")
    public ResponseEntity<String> deleteStatus(@PathVariable String id, @RequestBody User user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        if (!auth.validateToken(token)) {
            return ResponseEntity.status(401).body("Token inválido o expirado");
        }
        try {
            String response = webClient.patch()
                    .uri("http://users.railway.internal:3003/api/v1/users/" + id + "/status")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .bodyValue(user)
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
    @PostMapping("/login")
    public Mono<ResponseEntity<String>> loginUser(@RequestBody User user) {
    return webClient.post()
            .uri("http://users.railway.internal:3003/api/v1/users/login")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(user)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, response -> 
                Mono.error(new RuntimeException("Credenciales incorrectas")))
            .onStatus(HttpStatus::is5xxServerError, response -> 
                Mono.error(new RuntimeException("Error en el servidor")))
            .bodyToMono(String.class)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body("Error: " + e.getMessage())));

    }
}




