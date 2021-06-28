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


@SpringBootApplication
class BottinoApplication

fun main(args: Array<String>) {
    val token = System.getenv()["TOKEN"]!!

    val commands: Map<String, (MessageCreateEvent) -> Message?> = mapOf("ping" to { event: MessageCreateEvent ->
        event.message.channel.block()?.createMessage("Pong!")?.block()
    })

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

    client.eventDispatcher.on(MessageCreateEvent::class.java) // subscribe is like block, in that it will *request* for action
        // to be done, but instead of blocking the thread, waiting for it
        // to finish, it will just execute the results asynchronously.
        .subscribe { event: MessageCreateEvent ->
            // 3.1 Message.getContent() is a String
            val content = event.message.content
            for ((key, value) in commands) {
                // We will be using ! as our "prefix" to any command in the system.
                if (content.startsWith("!$key")) {
                    value
                    break
                }
            }
        }


    /*
    client.eventDispatcher.on(MessageCreateEvent::class.java)
        .map(MessageCreateEvent::getMessage)
        .filter { message -> message.author.map { user -> !user.isBot }.orElse(false) }
        .filter { message -> message.content.toLowerCase() == "!ping" }
        .flatMap(Message::getChannel)
        .flatMap { channel -> channel.createMessage("Pong!") }
        .subscribe()*/

    //client?.onDisconnect()?.block()
    runApplication<BottinoApplication>(*args)
}

@Bean
fun getQuote(): WebClient? {
    return WebClient.create("https://zenquotes.io/api/random")
}

internal interface Command {
    fun execute(event: MessageCreateEvent?)
}

