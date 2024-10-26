package dev.mbo.springkotlins3

import org.springframework.boot.fromApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.with

@EnableModuleS3
@SpringBootTest
class TestApplication

fun main(args: Array<String>) {
    fromApplication<TestApplication>().with(TestcontainersConfiguration::class).run(*args)
}
