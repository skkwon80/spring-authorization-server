package com.example.authres.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.jackson2.SecurityJackson2Modules
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import java.time.Duration
import java.util.*

@Configuration
class AuthorizationServerConfig {
    @Bean
    fun registeredClientRepository(jdbcTemplate: JdbcTemplate): RegisteredClientRepository {
        val registeredClient = RegisteredClient
            .withId(UUID.randomUUID().toString())
            .clientId("client")
            .clientSecret(passwordEncoder().encode("secret"))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri("http://localhost:8080/code")
            .scope(OidcScopes.OPENID)
            .scope("articles.read")
            .tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofHours(2)).build())
            .build()

        val registeredClientRepository = JdbcRegisteredClientRepository(jdbcTemplate)
        registeredClientRepository.save(registeredClient)

        return registeredClientRepository
    }

    @Bean
    fun authorizationService(
        jdbcTemplate: JdbcTemplate,
        registeredClientRepository: RegisteredClientRepository
    ): OAuth2AuthorizationService {
        val service = JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository)
        val mapper = JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper(registeredClientRepository)

        mapper.setObjectMapper(objectMapper())
        service.setAuthorizationRowMapper(mapper)

        return service
    }

    @Bean
    fun authorizationConsentService(
        jdbcTemplate: JdbcTemplate,
        registeredClientRepository: RegisteredClientRepository
    ): OAuth2AuthorizationConsentService {
        return JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository)
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        val classLoader = JdbcOAuth2AuthorizationService::class.java.classLoader
        val securityModules = SecurityJackson2Modules.getModules(classLoader)
        mapper.registerModules(securityModules)
        mapper.registerModule(OAuth2AuthorizationServerJackson2Module())

        // Authorization code를 발급받는 과정에서 사용자 인증 정보를 역직렬화하는 도중 발생하는 오류 및 예외 처리
        // Security의 기본 User는 역직렬화에 허용되어 있어 오류가 발생하지 않지만,
        // 사용자가 직접 생성한 CustomUserPrincipal 객체는 역직렬화를 가능하게 하려면 addMixIn을 통해 추가 설정이 필요
        mapper.registerModules(kotlinModule())
        mapper.addMixIn(CustomUserPrincipal::class.java, CustomUserPrincipalMixin::class.java)

        return mapper
    }
}
