package com.ecommerce.users

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UsersServiceApplication

fun main(args: Array<String>) {
    runApplication<UsersServiceApplication>(*args)
}
