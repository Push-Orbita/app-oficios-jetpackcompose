package com.cocido.oficios.feature.home.common

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos

@OptIn(ExperimentalMaterial3Api::class)
class SettledPullRefresh(
    val state: PullToRefreshState,
    val isRefreshing: Boolean,
    val onRefresh: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberSettledPullRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
): SettledPullRefresh {
    val state = rememberPullToRefreshState()
    val currentOnRefresh by rememberUpdatedState(onRefresh)
    var refreshRequested by remember { mutableStateOf(false) }
    LaunchedEffect(isRefreshing, refreshRequested) {
        if (PullRefreshSettle.shouldSettle(refreshRequested, isRefreshing)) {
            withFrameNanos { }
            state.snapTo(0f)
            refreshRequested = false
        }
    }
    return SettledPullRefresh(
        state = state,
        isRefreshing = PullRefreshSettle.displayedRefreshing(refreshRequested, isRefreshing),
        onRefresh = {
            refreshRequested = true
            currentOnRefresh()
        },
    )
}
