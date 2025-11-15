package com.ecommerce.gateway.config

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayConfig {

    @Bean
    fun customRouteLocator(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            // Products Service Routes
            .route("products-service") { r ->
                r.path("/api/products/**")
                    .filters { f ->
                        f.circuitBreaker { config ->
                            config.setName("products-service")
                                .setFallbackUri("forward:/fallback/products")
                        }
                    }
                    .uri("http://products-service:8081")
            }
            // Users Service Routes
            .route("users-service") { r ->
                r.path("/api/users/**")
                    .filters { f ->
                        f.circuitBreaker { config ->
                            config.setName("users-service")
                                .setFallbackUri("forward:/fallback/users")
                        }
                    }
                    .uri("http://users-service:8082")
            }
            // Orders Service Routes
            .route("orders-service") { r ->
                r.path("/api/orders/**")
                    .filters { f ->
                        f.circuitBreaker { config ->
                            config.setName("orders-service")
                                .setFallbackUri("forward:/fallback/orders")
                        }
                    }
                    .uri("http://orders-service:8083")
            }
            // Analytics Service Routes
            .route("analytics-service") { r ->
                r.path("/api/analytics/**")
                    .filters { f ->
                        f.circuitBreaker { config ->
                            config.setName("analytics-service")
                                .setFallbackUri("forward:/fallback/analytics")
                        }
                    }
                    .uri("http://analytics-service:8085")
            }
            .build()
    }
}
