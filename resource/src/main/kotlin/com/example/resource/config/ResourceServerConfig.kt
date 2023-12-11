package com.example.resource.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain


@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
class ResourceServerConfig(
    @Value("\${spring.profiles.active}") val activeProfile: String
) {
    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        httpSecurity
            .authorizeHttpRequests { request ->
                request.requestMatchers("/users/**").authenticated()
            }
            .oauth2ResourceServer { resource ->
                when (activeProfile) {
                    "jwt" -> resource.jwt(withDefaults())
                    "opaquetoken" -> resource.opaqueToken(withDefaults())
                }

            }
        return httpSecurity.build()
    }

//    @Bean
//    fun accessTokenConverter(): JwtAccessTokenConverter = JwtAccessTokenConverter().apply {
//        this.setSigningKey(signingKey)
//    }
}
