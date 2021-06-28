package dorian.codes.bottino

import com.fasterxml.jackson.databind.ObjectMapper
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import org.reactivestreams.Publisher
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*
import java.util.function.Function
import java.util.function.Predicate


@SpringBootApplication
class BottinoApplication

fun main(args: Array<String>) {
    val token = System.getenv()["TOKEN"]!!

    /*
    val commands: Map<String, Command> = mapOf("ping" to { event: MessageCreateEvent ->
        event.message.channel.block()?.createMessage("Pong!")?.block()
    })*/

    val client: GatewayDiscordClient? = DiscordClientBuilder.create(token)
        .build()
        .login()
        .block()

    client!!.eventDispatcher.on(ReadyEvent::class.java)
        .subscribe { event: ReadyEvent ->
            val self: User = event.self
            println(
                java.lang.String.format(
                    "Logged in as %s#%s", self.username, self.discriminator
                )
            )
        }

    /*

    client.eventDispatcher.on(MessageCreateEvent::class.java)
        .flatMap { event: MessageCreateEvent ->
            Mono.justOrEmpty(event.message.content)
                .flatMap { content: String ->
                    Flux.fromIterable<Map.Entry<String, Command>>(commands.entries)// We will be using ! as our "prefix" to any command in the system.
                        .filter { entry: Map.Entry<String, Command> -> content.startsWith('!' + entry.key) }
                        .flatMap{ entry: Map.Entry<String, Command>  -> entry.value.execute(event) }
                        .next()
                }
        }
        .subscribe()*/

    val actions = mapOf(
        "!ping" to "Pong!",
        "!quote" to getQuote()
    )


    client.eventDispatcher.on(MessageCreateEvent::class.java)
        .map(MessageCreateEvent::getMessage)
        .filter { message -> message.author.map { user -> !user.isBot }.orElse(false) }
        .filter { message -> actions.keys.contains(message.content.lowercase()) }
        .flatMap(Message::getChannel)
        .flatMap { channel -> channel.createMessage(actions.get(channel.lastMessage.block()!!.content))
           // channel.createMessage(getQuote())
        }
        .subscribe()

    /*
    client.eventDispatcher.on(MessageCreateEvent::class.java) // 3.1 Message.getContent() is a String
        .flatMap { event: MessageCreateEvent ->
            Mono.just(event.message.content)
                .flatMap{ content: String ->
                    Flux.fromIterable(actions.entries) // We will be using ! as our "prefix" to any command in the system.
                        .filter(Predicate<Map.Entry<String, String?>> { entry -> content.startsWith('!' + entry.key) })
                        .flatMap(Function<Map.Entry<String, String?>, Publisher<*>> { entry ->
                            event.message.channel.block()!!.createMessage(entry.value)
                        })
                        .next()
                }
        }
        .subscribe()*/

    //client?.onDisconnect()?.block()
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

internal interface Command {
    // Since we are expecting to do reactive things in this method, like
    // send a message, then this method will also return a reactive type.
    fun execute(event: MessageCreateEvent?): Mono<Void?>?
}

