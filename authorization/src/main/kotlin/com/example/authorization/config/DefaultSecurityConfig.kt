package com.example.authorization.config

import org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter
import org.springframework.security.web.session.HttpSessionEventPublisher
import org.springframework.security.web.util.matcher.AntPathRequestMatcher


@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
class DefaultSecurityConfig(
    val passwordEncoder: BCryptPasswordEncoder
) {
    @Bean
    @Throws(Exception::class)
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests { authorize ->
            authorize
                .requestMatchers(
                    AntPathRequestMatcher("/assets/**"),
                    AntPathRequestMatcher("/webjars/**"),
                    AntPathRequestMatcher("/login"),
                    toH2Console()
                )
                .permitAll()
                .anyRequest().authenticated()
        }.csrf {
            // h2-console 접근 권한
            it.ignoringRequestMatchers(toH2Console())
        }.headers {
            // h2-console 접근 권한
            it.addHeaderWriter(XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))
        }.formLogin(withDefaults())

        return http.build()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        val userDetails: UserDetails = User.builder()
            .username("user")
            .password(passwordEncoder.encode("password"))
            .roles("USER")
            .build()
        return InMemoryUserDetailsManager(userDetails)
    }

    /**
     * 이 빈을 등록하면 스프링 컨텍스트에서 SessionRegistry를 주입받을 수 있게 됩니다.
     * 주로 다른 구성 요소나 서비스에서 이 SessionRegistry를 활용하여
     * 현재 로그인된 사용자들의 세션 정보를 조회하거나 조작할 수 있습니다.
     */
    @Bean
    fun sessionRegistry(): SessionRegistry {
        return SessionRegistryImpl()
    }

    /**
     * 이 코드는 스프링에서 HttpSession 이벤트를 처리하기 위한 빈을 등록하는 부분입니다.
     * HttpSessionEventPublisher는 HttpSessionListener를 구현한 리스너를 등록하여
     * HttpSession 이벤트를 감지하고 처리할 수 있도록 도와줍니다.
     * HttpSessionEventPublisher는 주로 스프링 시큐리티와 함께 사용되며,
     * 세션 생성, 소멸 등과 같은 세션 관련 이벤트를 감지하여 처리하는 데 활용됩니다.
     * 이를 통해 보안 관련 이벤트를 처리하거나 특정 세션의 타임아웃 등을 관리할 수 있습니다.
     * 이 빈을 등록하면 스프링 컨텍스트에서 HttpSessionEventPublisher를 주입받을 수 있게 됩니다.
     * 이렇게 등록된 빈은 스프링이 서블릿 컨테이너에서 HttpSession 이벤트를 받아들이고
     * 이를 스프링 이벤트로 변환하는 역할을 합니다.
     */
    @Bean
    fun httpSessionEventPublisher(): HttpSessionEventPublisher {
        return HttpSessionEventPublisher()
    }
}