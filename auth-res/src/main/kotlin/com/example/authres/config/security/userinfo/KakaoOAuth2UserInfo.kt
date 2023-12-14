package com.example.authres.config.security.userinfo

class KakaoOAuth2UserInfo(
    attributes: Map<String, Any>
) : OAuth2UserInfo {
    override val id = (attributes["id"] as? Long).toString()
    override val email = (attributes["kakao_account"] as HashMap<*, *>)["email"] as String
    override val name = (attributes["kakao_account"] as HashMap<*, *>)["profile_nickname"] as? String
    override val imageUrl = (attributes["kakao_account"] as HashMap<*, *>)["profile_image"] as? String
}
