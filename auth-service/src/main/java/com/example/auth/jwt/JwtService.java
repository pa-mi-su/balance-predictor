package com.example.auth.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final JwtKeys keys;

    public JwtService(JwtKeys keys) {
        this.keys = keys;
    }

    public String issueAccessToken(long userId, String email) throws Exception {
        var signer = new RSASSASigner(keys.privateKey());
        var now = Instant.now();

        var claims = new JWTClaimsSet.Builder()
                .issuer(keys.issuer())
                .subject(String.valueOf(userId)) // we use sub as userId
                .claim("email", email)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(60 * 60))) // 1h
                .build();

        var header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(keys.kid())
                .type(JOSEObjectType.JWT)
                .build();

        var jwt = new SignedJWT(header, claims);
        jwt.sign(signer);
        return jwt.serialize();
    }

    public Map<String, Object> jwks() {
        return keys.jwkSet().toJSONObject();
    }
}
