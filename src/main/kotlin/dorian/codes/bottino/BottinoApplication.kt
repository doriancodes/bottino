package dorian.codes.bottino

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class BottinoApplication {
	companion object {
		@JvmStatic fun main(args: Array<String>) {
			SpringApplication.run(BottinoApplication::class.java, *args)
		}
	}
}
