package com.geecee.escapelauncher.core.data.network

import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TodoistSyncApiTest {
    private val api = TodoistSyncApi()

    @Test
    fun `parses a full sync response`() {
        val body = """
            {
              "sync_token": "tok-1",
              "full_sync": true,
              "temp_id_mapping": {"tmp-a": "6X6HrmjgW88crvMC"},
              "sync_status": {
                "cmd-1": "ok",
                "cmd-2": {"error_code": 22, "error": "Item not found", "http_code": 400}
              },
              "items": [
                {"id": "6X6HrmjgW88crvMC", "content": "Buy milk", "project_id": "p1", "parent_id": null,
                 "child_order": 2, "checked": false, "is_deleted": false, "priority": 1, "labels": []},
                {"id": "child1", "content": "Check fridge", "project_id": "p1", "parent_id": "6X6HrmjgW88crvMC",
                 "child_order": 1, "checked": 1, "is_deleted": 0}
              ],
              "projects": [
                {"id": "p1", "name": "Inbox", "inbox_project": true, "is_deleted": false, "is_archived": false},
                {"id": "p2", "name": "Old", "inbox_project": false, "is_deleted": false, "is_archived": true}
              ]
            }
        """.trimIndent()

        val response = api.parseResponse(TodoistSyncApi.json.parseToJsonElement(body).jsonObject)

        assertEquals("tok-1", response.syncToken)
        assertTrue(response.fullSync)
        assertEquals(mapOf("tmp-a" to "6X6HrmjgW88crvMC"), response.tempIdMapping)
        assertEquals(TodoistSyncApi.CommandResult.Ok, response.syncStatus["cmd-1"])
        assertEquals(TodoistSyncApi.CommandResult.Error("Item not found"), response.syncStatus["cmd-2"])

        assertEquals(2, response.items.size)
        val parent = response.items[0]
        assertEquals("Buy milk", parent.content)
        assertNull(parent.parentId)
        assertEquals(2, parent.childOrder)
        assertFalse(parent.checked)
        val child = response.items[1]
        assertEquals("6X6HrmjgW88crvMC", child.parentId)
        assertTrue("integer flags are accepted", child.checked)
        assertFalse(child.isDeleted)

        val projects = response.projects!!
        assertTrue(projects[0].isInbox)
        assertTrue(projects[1].isArchived)
    }

    @Test
    fun `incremental response without projects leaves them null`() {
        val body = """{"sync_token": "tok-2", "full_sync": false, "items": []}"""
        val response = api.parseResponse(TodoistSyncApi.json.parseToJsonElement(body).jsonObject)
        assertFalse(response.fullSync)
        assertNull(response.projects)
        assertTrue(response.items.isEmpty())
    }

    @Test
    fun `encodes commands with temp ids and drops null args`() {
        val encoded = api.encodeCommands(
            listOf(
                TodoistSyncApi.Command(
                    type = "item_add",
                    uuid = "u1",
                    tempId = "t1",
                    args = mapOf("content" to "Milk", "project_id" to "p1", "parent_id" to null)
                ),
                TodoistSyncApi.Command(type = "item_close", uuid = "u2", tempId = null, args = mapOf("id" to "x"))
            )
        )
        val array = TodoistSyncApi.json.parseToJsonElement(encoded).jsonArray
        assertEquals(2, array.size)
        val add = array[0].jsonObject
        assertEquals("item_add", add["type"]!!.jsonPrimitive.content)
        assertEquals("t1", add["temp_id"]!!.jsonPrimitive.content)
        val args = add["args"]!!.jsonObject
        assertEquals("Milk", args["content"]!!.jsonPrimitive.content)
        assertTrue(args["parent_id"]!!.jsonPrimitive.contentOrNullSafe() == null)
        assertNull(array[1].jsonObject["temp_id"])
    }

    private fun kotlinx.serialization.json.JsonPrimitive.contentOrNullSafe(): String? =
        if (this is kotlinx.serialization.json.JsonNull) null else content
}
