package com.t2m.g2nee.gateway.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JWTUtil {
    private SecretKey secretKey;


    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {

        this.secretKey =
                new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());

        //키를 객체타입으로 만들어저장

    }

    /**
     * Jwt 의 Payload 값을 확인하기위해서 쓰이는 메서드입니다.
     *
     * @param token 엑세스 토큰 기입.
     * @return Claims 클레임 들이 반환.
     */
    public boolean isValidateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (SignatureException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public static String getUsernameFromAccessToken(String accessToken) {
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String[] access_chunks = accessToken.split("\\.");
        String access_payload = new String(decoder.decode(access_chunks[1]));
        JSONObject aObject = new JSONObject(access_payload);
        String username = aObject.getString("username");
        return username;
    }
}
