package dorian.codes.bottino

import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono


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


    client.eventDispatcher.on(MessageCreateEvent::class.java)
        .map(MessageCreateEvent::getMessage)
        .filter { message -> message.author.map { user -> !user.isBot }.orElse(false) }
        .filter { message -> message.content.toLowerCase() == "!ping" }
        .flatMap(Message::getChannel)
        .flatMap { channel -> channel.createMessage("Pong!") }
        .subscribe()

    //client?.onDisconnect()?.block()
    runApplication<BottinoApplication>(*args)
}

@Bean
fun getQuote(): String? {

    val webClient = WebClient.create()
    return webClient.get()
        .uri("https://zenquotes.io/api/random")
        .retrieve()
        .bodyToMono(String::class.java)
        .block()

}

internal interface Command {
    // Since we are expecting to do reactive things in this method, like
    // send a message, then this method will also return a reactive type.
    fun execute(event: MessageCreateEvent?): Mono<Void?>?
}

