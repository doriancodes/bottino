package dorian.codes.bottino

import com.fasterxml.jackson.databind.ObjectMapper
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient
import javax.security.auth.login.LoginException


@SpringBootApplication
class BottinoApplication: ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val message: Message = event.message //The message that was received.

        val channel: MessageChannel = event.channel //This is the MessageChannel that the message was sent to.

        val msg: String = message.contentDisplay //This returns a human readable version of the Message. Similar to
        if (msg == "!ping") {
            //This will send a message, "pong!", by constructing a RestAction and "queueing" the action with the Requester.
            // By calling queue(), we send the Request to the Requester which will send it to discord. Using queue() or any
            // of its different forms will handle ratelimiting for you automatically!
            channel.sendMessage("pong!").queue()
        } else if(msg == "!quote") {
            channel.sendMessage(getQuote()!!).queue()
        }

    }

    companion object {
        /**
         * This is the method where the program starts.
         */

        fun startBot(token: String) {
            try {
                val jda: JDA =
                    JDABuilder.createDefault(token) // The token of the account that is logging in.
                        .addEventListeners(BottinoApplication()) // An instance of a class that will handle events.
                        .build()
                jda.awaitReady() // Blocking guarantees that JDA will be completely loaded.
                println("Finished Building JDA!")
            } catch (e: LoginException) {
                //If anything goes wrong in terms of authentication, this is the exception that will represent it
                e.printStackTrace()
            } catch (e: InterruptedException) {
                //Due to the fact that awaitReady is a blocking method, one which waits until JDA is fully loaded,
                // the waiting can be interrupted. This is the exception that would fire in that situation.
                //As a note: in this extremely simplified example this will never occur. In fact, this will never occur unless
                // you use awaitReady in a thread that has the possibility of being interrupted (async thread usage and interrupts)
                e.printStackTrace()
            }
        }

    }

}


fun main(args: Array<String>) {
    val token = System.getenv()["TOKEN"]!!

    BottinoApplication.startBot(token)
    runApplication<BottinoApplication>(*args)
}



@Bean
fun getQuote(): String? {


    val mapper = ObjectMapper()
    val webClient = WebClient.create()


    val response = webClient.get()
        .uri("https://zenquotes.io/api/random")
        .retrieve()
        .bodyToMono(String::class.java)
        .block()!!.replace("[", "").replace("]", "")

    return mapper.findAndRegisterModules().readValue<Quote>(response, Quote::class.java).formatQuote()


}

private fun Quote.formatQuote(): String = "$q - $a"


data class Quote(
    val q: String,
    val a: String,
    val h: String
)
