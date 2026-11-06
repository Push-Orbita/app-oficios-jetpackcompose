package com.cocido.oficios.core.ui.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.cocido.oficios.core.common.sync.DataChangeSignals
import com.cocido.oficios.core.common.sync.DataTopic
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter

/**
 * Refresca la pantalla abierta cuando llega un push que afecta a [topics] (ver
 * [DataChangeSignals]). Solo escucha mientras la pantalla está RESUMED; en segundo plano
 * el refresh de ON_RESUME cubre el caso. Ráfagas de pushes se agrupan en un solo refresh.
 */
@OptIn(FlowPreview::class)
@Composable
fun RefreshOnDataChange(
    vararg topics: DataTopic,
    onChange: () -> Unit,
) {
    val latestOnChange = rememberUpdatedState(onChange)
    val lifecycleOwner = LocalLifecycleOwner.current
    val topicSet = topics.toSet()
    LaunchedEffect(lifecycleOwner, topicSet) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            DataChangeSignals.events
                .filter { it in topicSet }
                .debounce(COALESCE_MILLIS)
                .collect { latestOnChange.value() }
        }
    }
}

private const val COALESCE_MILLIS = 400L
