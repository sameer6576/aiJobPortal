package com.sameer.job.config;

import com.sameer.job.jwt.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.function.ServerRequest;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RouteConfigAuthTest {

    private static final String SECRET = "test-jwt-secret-for-local-tests-only-0123456789";

    private RouteConfig routeConfig;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        routeConfig = new RouteConfig(new JwtUtil(SECRET));
        secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void optionalJwtWithoutHeaderIsAnonymousAndStripsForgedIdentity() {
        MockHttpServletRequest mock = new MockHttpServletRequest("GET", "/api/jobs/12");
        mock.addHeader("X-User-Id", "99");
        mock.addHeader("X-User-Role", "ROLE_ADMIN");
        ServerRequest request = ServerRequest.create(mock, List.of());

        ServerRequest forwarded = routeConfig.applyJwt(request, false);

        assertThat(forwarded.headers().firstHeader("X-User-Id")).isNull();
        assertThat(forwarded.headers().firstHeader("X-User-Role")).isNull();
    }

    @Test
    void optionalJwtWithValidBearerInjectsIdentity() {
        String token = signedToken(7L, "erin@example.com", "ROLE_EMPLOYER");
        MockHttpServletRequest mock = new MockHttpServletRequest("GET", "/api/jobs/12");
        mock.addHeader("Authorization", "Bearer " + token);
        mock.addHeader("X-User-Id", "99");
        ServerRequest request = ServerRequest.create(mock, List.of());

        ServerRequest forwarded = routeConfig.applyJwt(request, false);

        assertThat(forwarded.headers().firstHeader("X-User-Id")).isEqualTo("7");
        assertThat(forwarded.headers().firstHeader("X-User-Email")).isEqualTo("erin@example.com");
        assertThat(forwarded.headers().firstHeader("X-User-Role")).isEqualTo("ROLE_EMPLOYER");
    }

    @Test
    void optionalJwtRejectsMalformedAndInvalidTokens() {
        assertUnauthorized(false, "Token not-bearer");
        assertUnauthorized(false, "Bearer ");
        assertUnauthorized(false, "Bearer not-a-jwt");
    }

    @Test
    void requiredJwtRejectsMissingHeader() {
        ServerRequest request = ServerRequest.create(new MockHttpServletRequest("GET", "/api/jobs/my"), List.of());

        assertThatThrownBy(() -> routeConfig.applyJwt(request, true))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void publicGetPredicatesDoNotMatchProtectedJobPaths() {
        assertThat(RouteConfig.publicJobList().test(http("GET", "/api/jobs"))).isTrue();
        assertThat(RouteConfig.publicJobByNumericId().test(http("GET", "/api/jobs/42"))).isTrue();
        assertThat(RouteConfig.publicJobByNumericId().test(http("GET", "/api/jobs/my"))).isFalse();
        assertThat(RouteConfig.publicJobByNumericId().test(http("GET", "/api/jobs/admin"))).isFalse();
        assertThat(RouteConfig.publicJobByNumericId().test(http("GET", "/api/jobs/company/1"))).isFalse();
        assertThat(RouteConfig.publicJobList().test(http("POST", "/api/jobs/search/natural"))).isFalse();
        assertThat(RouteConfig.publicJobByNumericId().test(http("POST", "/api/jobs/9"))).isFalse();
        assertThat(RouteConfig.publicJobByNumericId().test(http("PUT", "/api/jobs/9"))).isFalse();
        assertThat(RouteConfig.publicJobByNumericId().test(http("PATCH", "/api/jobs/9/publish"))).isFalse();
        assertThat(RouteConfig.publicJobByNumericId().test(http("DELETE", "/api/jobs/9"))).isFalse();
        assertThat(RouteConfig.publicTaxonomyList("/api/job-categories").test(http("GET", "/api/job-categories"))).isTrue();
        assertThat(RouteConfig.publicTaxonomyList("/api/job-categories").test(http("POST", "/api/job-categories"))).isFalse();
    }

    private void assertUnauthorized(boolean required, String authorization) {
        MockHttpServletRequest mock = new MockHttpServletRequest("GET", "/api/jobs");
        mock.addHeader("Authorization", authorization);
        ServerRequest request = ServerRequest.create(mock, List.of());

        assertThatThrownBy(() -> routeConfig.applyJwt(request, required))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private static ServerRequest http(String method, String path) {
        return ServerRequest.create(new MockHttpServletRequest(method, path), List.of());
    }

    private String signedToken(Long userId, String email, String authorities) {
        return Jwts.builder()
                   .issuedAt(new Date())
                   .expiration(new Date(System.currentTimeMillis() + 60_000))
                   .claim("email", email)
                   .claim("authorities", authorities)
                   .claim("userId", userId)
                   .signWith(secretKey)
                   .compact();
    }
}
