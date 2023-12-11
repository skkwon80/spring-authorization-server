package com.example.authorization.config

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer
import org.springframework.security.oauth2.server.authorization.token.*
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*


@Profile("jwt")
@Configuration(proxyBeanMethods = false) // proxyBeanMethods 설정 필요한 지 검토
class AuthorizationServerConfigJWT {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Throws(java.lang.Exception::class)
    fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain? {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)

        http.getConfigurer(OAuth2AuthorizationServerConfigurer::class.java)
            .oidc(withDefaults()) // Enable OpenID Connect 1.0

        http.exceptionHandling { exceptions: ExceptionHandlingConfigurer<HttpSecurity> ->
            exceptions.authenticationEntryPoint(
                LoginUrlAuthenticationEntryPoint("/login")
            )
        }

        return http.build()
    }

    /**
     * 서버 재실행할 경우 이전에 발급했던 Jwt 토큰을 계속 사용할 수 있도록 JWK 고정
     */
    @Bean
    fun jwkSource(): JWKSource<SecurityContext> {
        val keyPair = keyPair()
        val publicKey = keyPair.public as RSAPublicKey
        val privateKey = keyPair.private as RSAPrivateKey
        val rsaKey = RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .build()

        val jwkSet = JWKSet(rsaKey)
        return ImmutableJWKSet(jwkSet)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun keyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        val secureRandom: SecureRandom = SecureRandom.getInstance("SHA1PRNG")
        // 서버 재실행할 경우 이전에 발급했던 Jwt 토큰을 계속 사용할 수 있도록 고정된 시드값 설정.
        secureRandom.setSeed("secure-seed".toByteArray(StandardCharsets.UTF_8))
        keyPairGenerator.initialize(2048, secureRandom)
        return keyPairGenerator.generateKeyPair()
    }
}
