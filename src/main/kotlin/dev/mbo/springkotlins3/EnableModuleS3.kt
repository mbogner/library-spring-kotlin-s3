package dev.mbo.springkotlins3

import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Import(ModuleS3Config::class)
annotation class EnableModuleS3
