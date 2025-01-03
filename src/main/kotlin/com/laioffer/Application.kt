package com.laioffer

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class Playlist (
    val id: Long,
    val songs: List<Song>
)

@Serializable
data class Song(
    val name: String,
    val lyric: String,
    val src: String,
    val length: String
)

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

// extension function
fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
        })
    }
    // TODO: adding the routing configuration here
    routing {
        // restful server: GET, POST, PUT, DELETE
        get("/") { // 后面加上的部分
            call.respondText("Hello World!")
        }

        get("/feed") { // 后面加上的部分
            val jsonString: String? = this::class.java.classLoader.getResource("feed.json")?.readText()
            // 在可能是null的结果后加一个问号，如果返回的是null就会保留null信息，不会crash
            call.respondText(jsonString ?: "null", ContentType.Application.Json)
        }

        get("/playlists") { // 后面加上的部分
            val jsonString: String? = this::class.java.classLoader.getResource("playlists.json")?.readText()
            // 在可能是null的结果后加一个问号，如果返回的是null就会保留null信息，不会crash
            call.respondText(jsonString ?: "null", ContentType.Application.Json)
        }

        get("/playlist/{id}") { // 返回对应的id
//            val jsonString: String? = this::class.java.classLoader.getResource("playlists.json")?.readText()
            // 由于string不好提取，所以 jsonString ->(Deserialize) List<PlaylistItem> -> loop
            // 等效于if (jsonString != null)
            this::class.java.classLoader.getResource("playlists.json")?.readText()?.let{ jsonString ->
                val playlists: List<Playlist> = Json.decodeFromString(ListSerializer(Playlist.serializer()), jsonString)
                val id = call.parameters["id"]
                // operator
                val item = playlists.firstOrNull { it.id.toString() == id }
                call.respondNullable(item)
            } ?: call.respond("null")
        }

        static("/") {
            staticBasePackage = "static"
            static("songs") {
                resources("songs")
            }
        }
    }
}