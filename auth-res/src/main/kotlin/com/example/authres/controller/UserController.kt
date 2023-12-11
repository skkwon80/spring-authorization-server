package com.example.authres.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal


@RestController
@RequestMapping("/users")
class UserController {
    @GetMapping("/me")
    fun me(principal: Principal) = "Hi ${principal.name}!"

    @GetMapping("/me/oidc-principal")
    fun getOidcUserPrincipal(
        @AuthenticationPrincipal principal: OidcUser
    ): OidcUser {
        return principal
    }
}
