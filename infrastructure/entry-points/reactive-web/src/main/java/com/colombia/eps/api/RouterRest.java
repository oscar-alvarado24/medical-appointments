package com.colombia.eps.api;

import com.colombia.eps.api.helper.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(GET(Constants.ENDPOINT_GET_BY_EMAIL), handler::getAttention)
                .andRoute(POST(Constants.ENDPOINT_SAVE_ATTENTION), handler::saveAttention)
                .andRoute(GET(Constants.ENDPOINT_GET_BY_ID), handler::getAttentionById);

    }
}
