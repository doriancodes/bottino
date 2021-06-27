package dorian.codes.bottino

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BottinoApplication

fun main(args: Array<String>) {
	println("HELLO")
	runApplication<BottinoApplication>(*args)
}
