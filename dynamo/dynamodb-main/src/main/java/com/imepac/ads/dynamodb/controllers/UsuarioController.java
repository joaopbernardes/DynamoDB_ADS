package com.imepac.ads.dynamodb.controllers;

import com.imepac.ads.dynamodb.entities.Usuario;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final DynamoDbTemplate dynamoDbTemplate;

    public UsuarioController(DynamoDbTemplate dynamoDbTemplate) {
        this.dynamoDbTemplate = dynamoDbTemplate;
    }

    @PostMapping
    public ResponseEntity<String> createUsuario(@RequestBody Usuario usuario) {
        usuario.setSenha(UUID.randomUUID().toString());
        dynamoDbTemplate.save(usuario);
        return ResponseEntity.status(201).body("Usuario criado com ID: " + usuario.getSenha());
    }

    @GetMapping("/{email}/{senha}")
    public ResponseEntity<List<Usuario>> getUsuario(@PathVariable String email, @PathVariable String senha) {

        var key = Key.builder()
                .partitionValue(email)
                .sortValue(senha)
                .build();

        var condition = QueryConditional.sortBeginsWith(key);

        var query = QueryEnhancedRequest.builder()
                .queryConditional(condition)
                .build();

        try {
            var usuario = dynamoDbTemplate.query(query, Usuario.class);

            List<Usuario> usarios = usuario.items().stream().toList();
            return ResponseEntity.ok(usarios);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }
}
