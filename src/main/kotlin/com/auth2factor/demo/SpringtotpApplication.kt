package com.auth2factor.demo

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import dev.turingcomplete.kotlinonetimepassword.GoogleAuthenticator
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.http.converter.BufferedImageHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.awt.PageAttributes
import java.awt.image.BufferedImage
import java.util.*

@EnableScheduling
@SpringBootApplication
class SpringTotpApplication {
    companion object {
        private val log = LoggerFactory.getLogger(SpringTotpApplication::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(SpringTotpApplication::class.java, *args)
        }
    }

    private val secret = GoogleAuthenticator.createRandomSecret();

    init {
        log.info(secret);
    }

    @Scheduled(fixedRate = 1000L)
    fun ping() {
        val timestamp = Date(System.currentTimeMillis())
        val code = GoogleAuthenticator(secret).generate(timestamp)
        log.info(code)
    }

    @Bean
    fun QrCodeWriter() = QRCodeWriter();

    @Bean
    fun imageConverter(): HttpMessageConverter<BufferedImage> {
        return BufferedImageHttpMessageConverter()
    }


    class BarcodeController(private val generator: CodeGenerator) {
        @GetMapping("/barCode/{secret}", produces = [MediaType.IMAGE_PNG_VALUE])
        fun barCode(@PathVariable secret: String): BufferedImage {
            return generator.generate("Labeler", "abdelkader.cyclope@gmail.com", secret)
        }

    }

    @Component
    class CodeGenerator(private val writer: QRCodeWriter) {
        fun generate(issuer: String, email: String, secret: String): BufferedImage {
            val uri = "otpauth://totp/${issuer}:${email}?secret=${secret}&issuer=${issuer}"
            val matrix = writer.encode(uri, BarcodeFormat.QR_CODE, 200, 200)
            return MatrixToImageWriter.toBufferedImage(matrix)
        }
    }

}