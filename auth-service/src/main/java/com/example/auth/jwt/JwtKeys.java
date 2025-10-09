package com.example.auth.jwt;

import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.util.Base64URL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import java.util.UUID;

@Component
public class JwtKeys {
    private final RSAPrivateKey privateKey;
    private final RSAPublicKey  publicKey;
    private final String kid;
    private final String issuer;

    public JwtKeys(
            @Value("${auth.privateKeyPemBase64:}") String privB64,
            @Value("${auth.publicKeyPemBase64:}")  String pubB64,
            @Value("${auth.issuer}") String issuer
    ) throws Exception {
        this.issuer = issuer;

        if (!privB64.isBlank() && !pubB64.isBlank()) {
            // load provided PEMs (base64-encoded PEM content)
            this.privateKey = PemSupport.readPrivateKeyFromBase64Pem(privB64);
            this.publicKey  = PemSupport.readPublicKeyFromBase64Pem(pubB64);
        } else {
            // dev fallback: generate ephemeral keypair
            var kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            this.privateKey = (RSAPrivateKey) kp.getPrivate();
            this.publicKey  = (RSAPublicKey) kp.getPublic();
        }
        this.kid = UUID.randomUUID().toString();
    }

    public RSAPrivateKey privateKey() { return privateKey; }
    public RSAPublicKey  publicKey()  { return publicKey;  }
    public String kid()              { return kid; }
    public String issuer()           { return issuer; }

    public JWKSet jwkSet() {
        var jwk = new RSAKey.Builder(publicKey)
                .privateKey(privateKey) // not returned in JWKS JSON
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(new Algorithm("RS256"))
                .keyID(kid)
                .build();
        return new JWKSet(jwk.toPublicJWK());
    }

    // Minimal PEM helper
    static final class PemSupport {
        static RSAPrivateKey readPrivateKeyFromBase64Pem(String base64Pem) throws Exception {
            String pem = new String(java.util.Base64.getDecoder().decode(base64Pem));
            return (RSAPrivateKey) java.security.KeyFactory.getInstance("RSA")
                    .generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(strip(pem)));
        }
        static RSAPublicKey readPublicKeyFromBase64Pem(String base64Pem) throws Exception {
            String pem = new String(java.util.Base64.getDecoder().decode(base64Pem));
            return (RSAPublicKey) java.security.KeyFactory.getInstance("RSA")
                    .generatePublic(new java.security.spec.X509EncodedKeySpec(strip(pem)));
        }
        private static byte[] strip(String pem) {
            String clean = pem.replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s", "");
            return java.util.Base64.getDecoder().decode(clean);
        }
    }
}
