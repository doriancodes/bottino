package dorian.codes.bottino

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PingController {

    @GetMapping("/keep-alive")
    fun keepAlive(): ResponseEntity<String> {
        return ResponseEntity("ALIVE AND WELL!", HttpStatus.OK)
    }

    @GetMapping("/")
    fun home(): ResponseEntity<String> {
        return ResponseEntity("Homepage", HttpStatus.OK)
    }
}