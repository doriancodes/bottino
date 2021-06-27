package dorian.codes.bottino

import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class BottinoApplication

fun main(args: Array<String>) {
    val token = System.getenv().get("TOKEN")!!
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

    client.eventDispatcher.on(MessageCreateEvent::class.java)
        .map(MessageCreateEvent::getMessage)
        .filter { message -> message.author.map { user -> !user.isBot }.orElse(false) }
        .filter { message -> message.content.toLowerCase() == "!ping" }
        .flatMap(Message::getChannel)
        .flatMap { channel -> channel.createMessage("Pong!") }
        .subscribe()

    client?.onDisconnect()?.block()
    runApplication<BottinoApplication>(*args)
}
