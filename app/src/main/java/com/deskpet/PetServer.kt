package com.deskpet

import fi.iki.elonen.NanoHTTPD

class PetServer(private val engine: PetEngine, port: Int = 8765) : NanoHTTPD(port) {

    private val validStates = setOf(
        "idle", "thinking", "building", "working",
        "happy", "sleeping", "error", "done", "attention"
    )

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val method = session.method

        try {
            when {
                uri == "/api/state" && method == Method.GET -> {
                    val data = engine.getStateData()
                    val json = """{"state": "${data["state"]}", "since": ${data["since"]}}"""
                    return newFixedLengthResponse(json)
                        .apply { mimeType = "application/json; charset=utf-8" }
                }
                uri == "/api/state" && method == Method.POST -> {
                    val params = mutableMapOf<String, String>()
                    session.parseBody(params)
                    val state = params["postData"] ?: session.queryParameterString ?: ""
                    val cleanState = state
                        .replace("state=", "")
                        .replace("%22", "")
                        .replace("\"", "")
                        .trim()
                    if (cleanState in validStates) {
                        engine.transition(cleanState)
                        return newFixedLengthResponse("""{"ok": true}""")
                            .apply { mimeType = "application/json; charset=utf-8" }
                    } else {
                        return newFixedLengthResponse("""{"error": "invalid state"}""")
                            .apply {
                                mimeType = "application/json; charset=utf-8"
                                code = 400
                            }
                    }
                }
                uri == "/health" -> {
                    return newFixedLengthResponse("OK")
                }
                else -> {
                    return newFixedLengthResponse("Not Found")
                        .apply { code = 404 }
                }
            }
        } catch (e: Exception) {
            return newFixedLengthResponse("""{"error": "server error"}""")
                .apply {
                    mimeType = "application/json; charset=utf-8"
                    code = 500
                }
        }
    }
}