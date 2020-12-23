import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.net.http.HttpHeaders;
import java.util.*;

public class Auth {
    private final String schemeId;
    private final String secret;

    public Auth(String schemeId, String secret) {
        this.schemeId = schemeId;
        this.secret = secret;
    }

    private String generateJWT() {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withAudience(schemeId)
                    .withIssuedAt(new Date(System.currentTimeMillis()))
                    .withJWTId(UUID.randomUUID().toString())
                    .sign(algorithm);
        } catch (JWTCreationException jwtE) {
            throw new JWTCreationException("Error creating JWT token", new Throwable(jwtE));
        }
    }

    private String generateBearerJWT() {
        return "Bearer : " + generateJWT();
    }

    private void verifyJWT(String token) {
        String existingToken = generateJWT();
        DecodedJWT decodedJWT = JWT.decode(token);

        if(!existingToken.equals(decodedJWT.toString())){
            throw new JWTVerificationException("JWT token could not be verified");
        }
        System.out.println("Token is verified");
    }

    private HttpHeaders generateSetuHeaders(String setuProductInstanceID){
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Authorization", List.of(generateBearerJWT()));
        headers.put("X-Setu-Product-Instance-ID", List.of(setuProductInstanceID));
        return new HttpHeaders(headers);
    }
}
