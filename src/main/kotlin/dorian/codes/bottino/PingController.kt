package dorian.codes.bottino

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Controller
class PingController {

    @RequestMapping("/keep-alive")
    fun keepAlive(): ResponseEntity<String> {
        return ResponseEntity("ALIVE AND WELL!", HttpStatus.OK)
    }

    @RequestMapping("/")
    fun home(): ResponseEntity<String> {
        return ResponseEntity(getQuote().toString(), HttpStatus.OK)
    }
}