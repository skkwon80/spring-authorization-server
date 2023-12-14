package com.example.authres.config

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.io.Serializable

class CustomUserPrincipal(
    private val username: String,
    private val password: String?
) : UserDetails, Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }

    override fun getUsername() = username

    override fun getPassword() = password

    override fun getAuthorities(): Collection<GrantedAuthority> = hashSetOf()

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = true
}
