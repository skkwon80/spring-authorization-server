package com.example.authres.config

import com.example.authres.entity.User
import com.example.authres.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailService(
    val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(usrename: String): UserDetails {
        return userRepository.findByUsername(usrename)?.let { createUser(it) }
            ?: throw UsernameNotFoundException("user is not exists")
    }

    private fun createUser(user: User) = org.springframework.security.core.userdetails.User.builder()
        .username(user.username)
        .password(user.password)
        .roles()
        .build()
}
