package com.sistemasdistribuidos.Projeto_Sistemas_Distribuidos;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;

public class JwtService {

    // IMPORTANTE: Mude esta chave para uma string longa e secreta!
    // Esta é a chave que assina seus tokens.
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
        "SuaChaveSecretaSuperLongaEDificilDeAdivinharParaOProjeto".getBytes()
    );

    // Tempo de expiração do token (ex: 1 hora)
    private static final long EXPIRATION_TIME = 3600_000; // 1 hora em milissegundos

    /**
     * Gera um novo token JWT para um usuário.
     */
    public String generateToken(String id, String usuario, String funcao) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(id) // Define o 'sub' (subject) como o ID do usuário
                .claim("usuario", usuario) // Adiciona o nome de usuário
                .claim("funcao", funcao)   // Adiciona a função (role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida um token e retorna as "claims" (dados) contidas nele.
     * Retorna null se o token for inválido (expirado, assinatura errada, etc.)
     */
    public Claims validateAndGetClaims(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims;
        } catch (Exception e) {
            // Token inválido (expirado, assinatura incorreta, mal formatado)
            return null;
        }
    }
}