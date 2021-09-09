package com.auth2factor.demo

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import dev.turingcomplete.kotlinonetimepassword.GoogleAuthenticator
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.http.converter.BufferedImageHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.awt.image.BufferedImage
import java.util.*
import java.util.stream.Collectors

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

    // var secret = GoogleAuthenticator.createRandomSecret();
    // secret key to be saved in BDD, the user have the save secret key in his mobile within Google Authenticator application
     var secret = "MLTTZFRF3VIAPI4I";
    lateinit var code: String

    init {
        log.info(secret);
    }

    @Scheduled(fixedRate = 1000L)
    fun ping() {
        val timestamp = Date(System.currentTimeMillis())
        code = GoogleAuthenticator(secret).generate(timestamp)
        log.info(code)
    }

    @Bean
    fun QrCodeWriter() = QRCodeWriter();

    @Bean
    fun imageConverter(): HttpMessageConverter<BufferedImage> {
        return BufferedImageHttpMessageConverter()
    }


    /*   fun isValid(code: String, timestamp: Date = Date(System.currentTimeMillis())): Boolean {
           return code == this.code;
       }
   */

    @RestController
    class BarcodeController(private val generator: CodeGenerator) {
        @GetMapping("/barCode/{secret}", produces = [MediaType.IMAGE_PNG_VALUE])
        fun barCode(@PathVariable secret: String): BufferedImage {
            return generator.generate("Labeler", "abdelkader.cyclope@gmail.com", secret)
        }
        @GetMapping("/isvalid/{secret}/{code}")
        fun isValid(@PathVariable code: String, @PathVariable secret: String): Boolean {
            val time: Long=  System.currentTimeMillis();
            val codes =   listOf(Date(time-60000), Date(time-30000), Date(time),Date(time+30000), Date(time+60000) ).stream().map { time-> GoogleAuthenticator(secret).generate(time) }.collect(Collectors.toList())
            codes.forEach { println(it ) }
            log.info("code :$code codesNow: $codes")
            return codes.contains(code);
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