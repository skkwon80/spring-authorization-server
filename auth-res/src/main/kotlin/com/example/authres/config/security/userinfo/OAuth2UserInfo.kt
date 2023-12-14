package com.example.authres.config.security.userinfo

interface OAuth2UserInfo {
    val id: String
    val email: String
    val name: String?
    val imageUrl: String?
}
