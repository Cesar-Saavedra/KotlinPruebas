package com.example.appnombre_grupo1.model

import java.security.MessageDigest

// Las contraseñas se guardan como hash SHA-256, nunca en texto plano
fun hashPassword(password: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(password.toByteArray())
        .joinToString("") { "%02x".format(it) }
