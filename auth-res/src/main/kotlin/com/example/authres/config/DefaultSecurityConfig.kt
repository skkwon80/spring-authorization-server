package com.example.authres.config

import com.example.authres.config.security.userinfo.OAuth2UserInfoFactory.getOAuth2UserInfo
import com.example.authres.entity.User
import com.example.authres.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter
import org.springframework.security.web.session.HttpSessionEventPublisher
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import java.util.*


@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
class DefaultSecurityConfig(
    @Value("\${spring.profiles.active}") val activeProfile: String,
    val userRepository: UserRepository,
    val passwordEncoder: BCryptPasswordEncoder,
    val clientRegistrationRepository: ClientRegistrationRepository,
    val usrDetailService: CustomUserDetailService
) {
    @Bean
    @Throws(Exception::class)
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        userRepository.save(User().apply {
            this.uuid = UUID.randomUUID().toString()
            this.username = "user"
            this.password = passwordEncoder.encode("password")
        })

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
        }.oauth2ResourceServer { resource ->
            when (activeProfile) {
                "jwt" -> resource.jwt(withDefaults())
                "opaquetoken" -> resource.opaqueToken(withDefaults())
            }
        }.oauth2Login { oauth2Login ->
            oauth2Login.permitAll()
//            oauth2Login.authorizationEndpoint { authorizationEndpoint ->
//                authorizationEndpoint.authorizationRequestResolver(object : OAuth2AuthorizationRequestResolver {
//                    override fun resolve(request: HttpServletRequest): OAuth2AuthorizationRequest? {
//                        println("authorizationRequestResolver1")
//                        val authorizationRequest = DefaultOAuth2AuthorizationRequestResolver(
//                            clientRegistrationRepository,
//                            OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
//                        ).resolve(request) ?: return null
//
//                        return OAuth2AuthorizationRequest.from(authorizationRequest).build()
//                    }
//
//                    override fun resolve(
//                        request: HttpServletRequest,
//                        clientRegistrationId: String
//                    ): OAuth2AuthorizationRequest? {
//                        println("authorizationRequestResolver2")
//                        return null
//                    }
//                })
//                authorizationEndpoint.authorizationRequestRepository(object :
//                    AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
//                    override fun loadAuthorizationRequest(request: HttpServletRequest?): OAuth2AuthorizationRequest? {
//                        println("loadAuthorizationRequest")
//                        return null
//                    }
//
//                    override fun removeAuthorizationRequest(
//                        request: HttpServletRequest?,
//                        response: HttpServletResponse?
//                    ): OAuth2AuthorizationRequest? {
//                        println("removeAuthorizationRequest")
//                        return null
//                    }
//
//                    override fun saveAuthorizationRequest(
//                        authorizationRequest: OAuth2AuthorizationRequest?,
//                        request: HttpServletRequest?,
//                        response: HttpServletResponse?
//                    ) {
//                        println("saveAuthorizationRequest")
//                    }
//                })
//            }
            oauth2Login.userInfoEndpoint { userInfoEndpoint ->
                userInfoEndpoint.userService(object : DefaultOAuth2UserService() {
                    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
                        val delegate = DefaultOAuth2UserService()
                        val oAuth2User = delegate.loadUser(userRequest)
                        val registrationId = userRequest.clientRegistration.registrationId
                        val oAuth2UserInfo =
                            getOAuth2UserInfo(registrationId, super.loadUser(userRequest).attributes)

                        println(userRequest.accessToken.tokenValue)
                        println(registrationId)
                        println(oAuth2UserInfo)
                        println("save or update user")

                        return DefaultOAuth2User(
                            setOf(),
                            oAuth2User.attributes,
                            userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName
                        )
                    }
                })
            }
            oauth2Login.successHandler { request, response, authentication ->
                println("successHandler")
            }
            oauth2Login.failureHandler { request, response, exception ->
                println("failureHandler")
            }
        }.csrf {
            // h2-console access authority
            it.ignoringRequestMatchers(toH2Console())
        }.headers {
            // h2-console access authority
            it.addHeaderWriter(XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))
        }.formLogin(withDefaults())

        return http.build()
    }

    @Bean
    fun sessionRegistry(): SessionRegistry {
        return SessionRegistryImpl()
    }

    @Bean
    fun httpSessionEventPublisher(): HttpSessionEventPublisher {
        return HttpSessionEventPublisher()
    }
}
