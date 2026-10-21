package com.cocido.oficios.core.network.test

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy

fun MockWebServer.enqueueJson(code: Int, body: String) {
    enqueue(
        MockResponse()
            .setResponseCode(code)
            .setBody(body)
            .addHeader("Content-Type", "application/json"),
    )
}

fun MockWebServer.enqueueFixture(code: Int, path: String) {
    enqueueJson(code, NetworkTestFactory.loadFixture(path))
}

fun MockWebServer.enqueueNoResponse() {
    enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
}

fun MockWebServer.enqueueDisconnect() {
    enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
}
