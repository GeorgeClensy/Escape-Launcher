package com.geecee.escapelauncher.core.data.network

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Minimal client for the Todoist Sync API (https://developer.todoist.com/api/v1/#tag/Sync).
 *
 * One endpoint does everything: it takes a batch of commands to apply and a sync token, and
 * returns the changes since that token plus the outcome of each command.
 */
@Singleton
class TodoistSyncApi @Inject constructor() {

    class AuthException : IOException("Todoist rejected the API token")
    class HttpException(val code: Int, body: String) : IOException("Todoist returned HTTP $code: ${body.take(200)}")

    data class Command(
        val type: String,
        val uuid: String,
        val tempId: String?,
        val args: Map<String, String?>
    )

    data class Item(
        val id: String,
        val content: String,
        val projectId: String,
        val parentId: String?,
        val childOrder: Int,
        val checked: Boolean,
        val isDeleted: Boolean
    )

    data class Project(
        val id: String,
        val name: String,
        val isInbox: Boolean,
        val isDeleted: Boolean,
        val isArchived: Boolean
    )

    sealed class CommandResult {
        data object Ok : CommandResult()
        data class Error(val message: String) : CommandResult()
    }

    data class Response(
        val syncToken: String,
        val fullSync: Boolean,
        val items: List<Item>,
        val projects: List<Project>?,
        val tempIdMapping: Map<String, String>,
        val syncStatus: Map<String, CommandResult>
    )

    suspend fun sync(
        apiToken: String,
        syncToken: String,
        resourceTypes: List<String>,
        commands: List<Command>
    ): Response = withContext(Dispatchers.IO) {
        val form = buildString {
            append("sync_token=").append(URLEncoder.encode(syncToken, "UTF-8"))
            append("&resource_types=").append(URLEncoder.encode(JsonArray(resourceTypes.map { JsonPrimitive(it) }).toString(), "UTF-8"))
            if (commands.isNotEmpty()) {
                append("&commands=").append(URLEncoder.encode(encodeCommands(commands), "UTF-8"))
            }
        }

        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            doOutput = true
            setRequestProperty("Authorization", "Bearer $apiToken")
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
            setRequestProperty("Accept", "application/json")
        }

        try {
            connection.outputStream.use { it.write(form.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            if (code == 401 || code == 403) throw AuthException()
            val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw HttpException(code, body)
            parseResponse(json.parseToJsonElement(body).jsonObject)
        } finally {
            connection.disconnect()
        }
    }

    internal fun encodeCommands(commands: List<Command>): String {
        val array = JsonArray(commands.map { command ->
            val fields = mutableMapOf<String, JsonElement>(
                "type" to JsonPrimitive(command.type),
                "uuid" to JsonPrimitive(command.uuid),
                "args" to JsonObject(command.args.mapValues { (_, v) -> if (v == null) JsonNull else JsonPrimitive(v) })
            )
            command.tempId?.let { fields["temp_id"] = JsonPrimitive(it) }
            JsonObject(fields)
        })
        return array.toString()
    }

    internal fun parseResponse(root: JsonObject): Response {
        val items: List<Item> = (root["items"] as? JsonArray)?.map(::parseItem) ?: emptyList()
        val projects: List<Project>? = (root["projects"] as? JsonArray)?.map(::parseProject)
        val mapping = root["temp_id_mapping"]?.let { el ->
            if (el is JsonObject) el.mapValues { (_, v) -> v.jsonPrimitive.content } else null
        } ?: emptyMap()
        val status = root["sync_status"]?.let { el ->
            if (el is JsonObject) el.mapValues { (_, v) ->
                if (v is JsonPrimitive && v.contentOrNull == "ok") CommandResult.Ok
                else CommandResult.Error((v as? JsonObject)?.get("error")?.jsonPrimitive?.contentOrNull ?: v.toString())
            } else null
        } ?: emptyMap()
        return Response(
            syncToken = root["sync_token"]?.jsonPrimitive?.content ?: "",
            fullSync = root["full_sync"].asBoolean(),
            items = items,
            projects = projects,
            tempIdMapping = mapping,
            syncStatus = status
        )
    }

    private fun parseItem(el: JsonElement): Item {
        val o = el.jsonObject
        return Item(
            id = o["id"].asString(),
            content = o["content"].asString(),
            projectId = o["project_id"].asString(),
            parentId = o["parent_id"]?.takeUnless { it is JsonNull }?.asString(),
            childOrder = o["child_order"]?.jsonPrimitive?.intOrNull ?: 0,
            checked = o["checked"].asBoolean(),
            isDeleted = o["is_deleted"].asBoolean()
        )
    }

    private fun parseProject(el: JsonElement): Project {
        val o = el.jsonObject
        return Project(
            id = o["id"].asString(),
            name = o["name"].asString(),
            isInbox = o["inbox_project"].asBoolean(),
            isDeleted = o["is_deleted"].asBoolean(),
            isArchived = o["is_archived"].asBoolean()
        )
    }

    private fun JsonElement?.asString(): String = (this as? JsonPrimitive)?.contentOrNull ?: ""

    /** Todoist has used both `true` and `1` for flags over the years; accept either. */
    private fun JsonElement?.asBoolean(): Boolean {
        val p = this as? JsonPrimitive ?: return false
        return p.booleanOrNull ?: (p.intOrNull?.let { it != 0 } ?: false)
    }

    companion object {
        private const val ENDPOINT = "https://api.todoist.com/api/v1/sync"
        private const val TIMEOUT_MS = 15_000
        internal val json = Json { ignoreUnknownKeys = true; isLenient = true }
    }
}
