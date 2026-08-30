package com.sameer.job.config;

import com.sameer.job.jwt.JwtConstant;
import com.sameer.job.jwt.JwtUtil;
import org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class RouteConfig {

    private final JwtUtil jwtUtil;

    public RouteConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public RouterFunction<ServerResponse> authRoutes() {
        return GatewayRouterFunctions.route("auth-routes")
                                     .route(RequestPredicates.path("/auth/**"), HandlerFunctions.http())
                                     .filter(LoadBalancerFilterFunctions.lb("job-portal-user-service"))
                                     .build();

    }

    @Bean
    @Order(-1)
    public RouterFunction<ServerResponse> userAdminRoutes() {
        return GatewayRouterFunctions.route("user-admin-routes")
                                     .route(RequestPredicates.GET("/api/users"), HandlerFunctions.http())
                                     .route(RequestPredicates.PATCH("/api/users/*/suspend"), HandlerFunctions.http())
                                     .route(RequestPredicates.PATCH("/api/users/*/activate"), HandlerFunctions.http())
                                     .route(RequestPredicates.DELETE("/api/users/*/delete"), HandlerFunctions.http())
                                     .filter(LoadBalancerFilterFunctions.lb("job-portal-user-service"))
                                     .before(this::jwtAuthFilter)
                                     .before(request -> requireRole(request, "ROLE_ADMIN"))
                                     .build();
    }

    @Bean
    @Order(-1)
    public RouterFunction<ServerResponse> companyAdminRoutes() {
        return GatewayRouterFunctions.route("company-admin-routes")
                                     .route(RequestPredicates.PATCH("/api/companies/*/verify"), HandlerFunctions.http())
                                     .route(RequestPredicates.PATCH("/api/companies/*/deactivate"), HandlerFunctions.http())
                                     .filter(LoadBalancerFilterFunctions.lb("job-portal-company-service"))
                                     .before(this::jwtAuthFilter)
                                     .before(request -> requireRole(request, "ROLE_ADMIN"))
                                     .build();
    }

    @Bean
    @Order(-1)
    public RouterFunction<ServerResponse> jobAdminRoutes() {
        return GatewayRouterFunctions.route("job-admin-routes")
                                     .route(RequestPredicates.GET("/api/jobs/admin"), HandlerFunctions.http())
                                     .filter(LoadBalancerFilterFunctions.lb("job-portal-job-service"))
                                     .before(this::jwtAuthFilter)
                                     .before(request -> requireRole(request, "ROLE_ADMIN"))
                                     .build();
    }

    private ServerRequest requireRole(ServerRequest request, String requiredRole) {
        String roles = request.headers().firstHeader("X-User-Role");

        if (roles == null || !roles.contains(requiredRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for role " + requiredRole);
        }
        return request;
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceRoutes() {
        return GatewayRouterFunctions.route("user-service-routes")
                                     .route(RequestPredicates.path("/api/users/**"), HandlerFunctions.http())
                                     .filter(LoadBalancerFilterFunctions.lb("job-portal-user-service"))
                                     .before(this::jwtAuthFilter)
                                     .build();
    }

    @Bean
    public RouterFunction<ServerResponse> companyServiceRoutes() {
        return GatewayRouterFunctions.route("company-service-routes")
                                     .route(RequestPredicates.path("/api/companies/**"), HandlerFunctions.http())
                                     .filter(LoadBalancerFilterFunctions.lb("job-portal-company-service"))
                                     .before(this::jwtAuthFilter)
                                     .build();
    }

    @Bean
    public RouterFunction<ServerResponse> jobServiceRoutes() {
        return GatewayRouterFunctions.route("job-service-routes")
                                     .route(RequestPredicates.path("/api/jobs/**")
                                                             .or(RequestPredicates.path("/api/job-categories/**"))
                                                             .or(RequestPredicates.path("/api/job-skills/**"))
                                                             .or(RequestPredicates.path("/api/job-tags/**"))
                                             , HandlerFunctions.http())
                                     .filter(LoadBalancerFilterFunctions.lb("job-portal-job-service"))
                                     .before(this::jwtAuthFilter)
                                     .build();
    }

    @Bean
    public RouterFunction<ServerResponse> applicationServiceRoutes() {
        return GatewayRouterFunctions.route("application-service-routes")
                                     .route(
                                             RequestPredicates.path("/api/applications/**"),
                                             HandlerFunctions.http()
                                     )
                                     .filter(LoadBalancerFilterFunctions.lb("job-portal-application-service"))
                                     .before(this::jwtAuthFilter)
                                     .build();
    }


    @Bean
    public RouterFunction<ServerResponse> resumeServiceRoutes() {
        return GatewayRouterFunctions.route("resume-service-routes")
                                     .route(RequestPredicates.path("/api/resumes/**"), HandlerFunctions.http())
                                     .filter(LoadBalancerFilterFunctions.lb("job-portal-resume-service"))
                                     .before(this::jwtAuthFilter)
                                     .build();
    }

    @Bean
    public RouterFunction<ServerResponse> preferenceServiceRoutes() {
        return GatewayRouterFunctions.route("preference-service-routes")
                                     .route(RequestPredicates.path("/api/preferences/**"), HandlerFunctions.http())
                                     .filter(LoadBalancerFilterFunctions.lb("job-portal-preferences"))
                                     .before(this::jwtAuthFilter)
                                     .build();
    }
    @Bean
    public RouterFunction<ServerResponse> aiServiceRoutes() {
        return GatewayRouterFunctions.route("ai-service-routes")
                                     .route(RequestPredicates.path("/api/ai/**"), HandlerFunctions.http())
                                     .filter(LoadBalancerFilterFunctions.lb("job-portal-ai-service"))
                                     .before(this::jwtAuthFilter)
                                     .build();
    }

//     Jwt Filter

    private ServerRequest jwtAuthFilter(ServerRequest request) {
        String authHeader = request.headers().firstHeader(JwtConstant.JWT_HEADER);

        if (authHeader == null || !authHeader.startsWith(JwtConstant.TOKEN_PREFIX)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Missing or invalid authorization header"
            );
        }

        String token = authHeader.substring(JwtConstant.TOKEN_PREFIX.length());
        if (!jwtUtil.isTokenValid(token)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired JWT Token"
            );
        }

        String email = jwtUtil.extractEmail(token);
        String authorities = jwtUtil.extractAuthorities(token);
        Long userId = jwtUtil.extractUserId(token);

        return ServerRequest.from(request)
                            .headers(headers -> {
                                headers.remove("X-User-Id");
                                headers.remove("X-User-Email");
                                headers.remove("X-User-Role");
                                if (userId != null) {
                                    headers.set("X-User-Id", String.valueOf(userId));
                                }
                                if (email != null) {
                                    headers.set("X-User-Email", email);
                                }
                                if (authorities != null) {
                                    headers.set("X-User-Role", authorities);
                                }
                            })
                            .build();
    }


}
