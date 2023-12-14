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
    fun me(principal: Principal): String {
//        OAuth2AuthenticationToken [Principal=Name: [2931304987], Granted Authorities: [[]], User Attributes: [{id=2931304987, connected_at=2023-07-25T00:49:51Z, kakao_account={profile_nickname_needs_agreement=false, has_email=true, email_needs_agreement=false, is_email_valid=true, is_email_verified=true, email=skkwon80@gmail.com}}], Credentials=[PROTECTED], Authenticated=true, Details=WebAuthenticationDetails [RemoteIpAddress=0:0:0:0:0:0:0:1, SessionId=4C3E3758F7D47E423A65E9F14B2F2A41], Granted Authorities=[]]
        println(principal)
        return "Hi ${principal.name}!"
    }

    @GetMapping("/me/oidc-principal")
    fun getOidcUserPrincipal(
        @AuthenticationPrincipal principal: OidcUser
    ): OidcUser {
        return principal
    }
}
