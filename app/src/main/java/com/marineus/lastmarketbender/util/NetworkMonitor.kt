package com.marineus.lastmarketbender.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}

@Singleton
class ConnectivityManagerNetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkMonitor {
    override val isOnline: Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                updateOnlineStatus(network)
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                updateOnlineStatus(network)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onUnavailable() {
                trySend(false)
            }

            private fun updateOnlineStatus(network: Network) {
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                val online = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                trySend(online)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Initial check
        val activeNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
        val isInitialOnline = caps != null && 
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        trySend(isInitialOnline)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
    .distinctUntilChanged()
}
