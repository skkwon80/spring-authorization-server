package com.example.authres.config.security.userinfo

class GoogleOAuth2UserInfo(
    attributes: Map<String, Any>
) : OAuth2UserInfo {
    override val id = attributes["sub"] as String
    override val email = attributes["email"] as String
    override val name = attributes["name"] as String
    override val imageUrl = attributes["picture"] as String
}
