package com.example.authres.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

@Table(
    name = "users",
    indexes = [
        Index(name = "uk_uuid", columnList = "uuid", unique = true),
        Index(name = "uk_username", columnList = "username", unique = true)
    ]
)
@Entity
class User : BaseEntity() {
    @Column(name = "uuid", nullable = false)
    lateinit var uuid: String

    @Column(name = "username", nullable = false, length = 32)
    lateinit var username: String

    @Column(name = "password", length = 60)
    var password: String? = null
}
