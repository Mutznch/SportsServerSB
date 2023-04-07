package br.pucpr.sportsserver.lib.security;

import br.pucpr.sportsserver.rest.users.User;
import br.pucpr.sportsserver.rest.users.response.UserResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;

@Component
public class JWT {
    private SecuritySettings settings;
    private static String PREFIX = "Bearer";

    public JWT(SecuritySettings settings) {
        this.settings = settings;
    }

    public Authentication extract(HttpServletRequest req) {
        final var header = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(PREFIX)) return null;

        final var token = header.replace(PREFIX, "").trim();
        final var claims = Jwts.parserBuilder()
                .setSigningKey(settings.getSecret().getBytes())
                .deserializeJsonWith(new JacksonDeserializer<>(Map.of("user", User.class)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        if (!settings.getIssuer().equals(claims.getIssuer())) return null;

        final var user = claims.get("user", User.class);
        if (user == null) return null;

        final var authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .toList();
        return UsernamePasswordAuthenticationToken.authenticated(user, user.getId(), authorities);
    }

    public static Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneOffset.UTC).toInstant());
    }
    public String createToken(UserResponse user) {
        final var now = LocalDate.now();
        user.setSports(null);
        return Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(settings.getSecret().getBytes()))
                .serializeToJsonWith(new JacksonSerializer<>())
                .setIssuedAt(toDate(now))
                .setExpiration(toDate(now.plusDays(5)))
                .setIssuer(settings.getIssuer())
                .setSubject(user.getId().toString())
                .addClaims(Map.of("user", user))
                .compact();



    }
}
