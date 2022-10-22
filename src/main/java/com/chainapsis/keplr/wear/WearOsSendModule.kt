package com.chainapsis.keplr.wear

import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class WearOsSendModule(
    private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

    private val messageClient by lazy { Wearable.getMessageClient(reactContext) }
    private val nodeClient by lazy { Wearable.getNodeClient(reactContext) }

    private val collectScope = CoroutineScope(Dispatchers.Default)

    @ReactMethod
    fun send(pubKey: String) {
        collectScope.launch {
            try {
                val nodes = nodeClient.connectedNodes.await()

                nodes.map { node ->
                    async {
                        messageClient.sendMessage(node.id, PUB_KEY_PATH, pubKey.toByteArray())
                            .await()
                    }
                }.awaitAll()

                Log.d("module", "Starting activity requests sent successfully")
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d("module", "Starting activity failed: $exception")
            }
        }
    }

    override fun getName(): String {
        return MODULE_NAME
    }

    companion object {
        private const val MODULE_NAME = "WearOsSendModule"

        private const val PUB_KEY = "pub-key"
        private const val PUB_KEY_PATH = "/pub-key"
    }
}