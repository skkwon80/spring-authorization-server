package com.example.authres.config.security.userinfo

object OAuth2UserInfoFactory {
    fun getOAuth2UserInfo(registrationId: String, attributes: Map<String, Any>): OAuth2UserInfo {
        return when (registrationId) {
            "google" -> GoogleOAuth2UserInfo(attributes)
            "kakao" -> KakaoOAuth2UserInfo(attributes)
            else -> throw Exception("server.error.not-found")
        }
    }
}
