package com.stella.game.player

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.amqp.core.*
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.web.client.RestTemplate
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
@EnableJpaRepositories(basePackages = arrayOf("com.stella.game.player"))
@EntityScan(basePackages = arrayOf("com.stella.game.player"))
class PlayerApplicationConfig {
    @Bean
    fun swaggerApi(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .paths(PathSelectors.any())
                .build()
    }

    private fun apiInfo(): ApiInfo {
        return ApiInfoBuilder()
                .title("API for player entities")
                .description("MicroService that Contains player entity repository")
                .version("1.0")
                .build()
    }

    @Bean(name = arrayOf("OBJECT_MAPPER_BEAN"))
    fun jsonObjectMapper(): ObjectMapper {
        return Jackson2ObjectMapperBuilder.json()
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) //ISODate
                .modules(JavaTimeModule())
                .build()
    }

    @LoadBalanced
    @Bean
    @Profile("docker")
    fun restTemplateBalancer(): RestTemplate {
        return RestTemplate()
    }

    @Bean
    @Profile("!docker")
    fun restTemplate() : RestTemplate {
        return RestTemplate()
    }

    @Bean
    fun fanout(): FanoutExchange {
        return FanoutExchange("player-created")
    }

    @Bean
    fun queue() : Queue {
        return AnonymousQueue()
    }

    @Bean
    fun binding(fanout: FanoutExchange, queue: Queue) : Binding {
        return BindingBuilder.bind(queue).to(fanout)
    }

}