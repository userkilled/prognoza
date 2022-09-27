package hr.dtakac.prognoza.ui.forecast

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import hr.dtakac.prognoza.entities.forecast.ForecastDescription
import hr.dtakac.prognoza.presentation.forecast.ForecastViewModel
import hr.dtakac.prognoza.ui.theme.PrognozaTheme
import hr.dtakac.prognoza.ui.places.PlacesScreen
import hr.dtakac.prognoza.ui.theme.applyOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    viewModel: ForecastViewModel = hiltViewModel()
) {
    // Refresh state every time screen is re-entered
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.getState()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val state by remember { viewModel.state }
    val forecast = state.forecast
    val error = state.error

    PrognozaTheme(forecast?.today?.shortDescription ?: ForecastDescription.Short.UNKNOWN) {
        val colorAnimationSpec = remember {
            tween<Color>(durationMillis = 1000)
        }
        val surface by animateColorAsState(
            targetValue = PrognozaTheme.colors.surface.applyOverlay(
                overlayColor = PrognozaTheme.colors.moodOverlay
            ),
            animationSpec = colorAnimationSpec
        )
        val onSurface by animateColorAsState(
            targetValue = PrognozaTheme.colors.onSurface.copy(alpha = PrognozaTheme.alpha.high),
            animationSpec = colorAnimationSpec
        )
        val barSurface by animateColorAsState(
            targetValue = PrognozaTheme.colors.surface.applyOverlay(
                overlayColor = PrognozaTheme.colors.moodOverlay,
                overlayAlpha = 0.24f
            ),
            animationSpec = colorAnimationSpec
        )
        val onBarSurface by animateColorAsState(
            targetValue = PrognozaTheme.colors.onSurface.copy(alpha = PrognozaTheme.alpha.high),
            animationSpec = colorAnimationSpec
        )

        val systemUiController = rememberSystemUiController()
        systemUiController.setSystemBarsColor(barSurface)
        systemUiController.setNavigationBarColor(barSurface)

        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContentColor = onBarSurface,
                    drawerShape = RectangleShape,
                    drawerContainerColor = barSurface
                ) {
                    PlacesScreen(
                        onPlaceSelected = {
                            scope.launch { drawerState.close() }
                            viewModel.getState()
                        }
                    )
                }
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(surface)
                ) {
                    var toolbarPlaceVisible by remember { mutableStateOf(false) }
                    var toolbarDateVisible by remember { mutableStateOf(false) }
                    var toolbarTemperatureVisible by remember { mutableStateOf(false) }

                    Box(contentAlignment = Alignment.BottomCenter) {
                        ForecastToolbar(
                            place = forecast?.place?.asString() ?: "",
                            placeVisible = toolbarPlaceVisible,
                            date = forecast?.today?.date?.asString() ?: "",
                            dateVisible = toolbarDateVisible,
                            temperature = forecast?.today?.temperature?.asString() ?: "",
                            temperatureVisible = toolbarTemperatureVisible,
                            backgroundColor = barSurface,
                            contentColor = onBarSurface,
                            onMenuClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        )

                        androidx.compose.animation.AnimatedVisibility(
                            visible = state.isLoading,
                            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                        ) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp),
                                color = onBarSurface,
                                trackColor = barSurface
                            )
                        }
                    }

                    if (forecast == null) {
                        if (error != null) {
                            ForecastError(
                                text = error.asString(),
                                surfaceColor = surface,
                                contentColor = onSurface
                            )
                        }
                    } else {
                        Box {
                            ForecastContent(
                                forecast = forecast,
                                surfaceColor = surface,
                                contentColor = onSurface,
                                isPlaceVisible = { toolbarPlaceVisible = !it },
                                isDateVisible = { toolbarDateVisible = !it },
                                isTemperatureVisible = { toolbarTemperatureVisible = !it }
                            )

                            if (error != null) {
                                var showSnackBar by remember { mutableStateOf(false) }
                                LaunchedEffect(forecast, error) {
                                    scope.launch {
                                        showSnackBar = true
                                        delay(5000L)
                                        showSnackBar = false
                                    }
                                }

                                // SnackBar needs to be prominent and stand out
                                PrognozaTheme(
                                    description = forecast.today.shortDescription,
                                    useDarkTheme = !isSystemInDarkTheme()
                                ) {
                                    val snackBarSurface by animateColorAsState(
                                        // todo: extract overlay alpha to theme
                                        targetValue = PrognozaTheme.colors.surface.applyOverlay(
                                            overlayColor = PrognozaTheme.colors.moodOverlay,
                                            overlayAlpha = 0.24f
                                        ),
                                        animationSpec = colorAnimationSpec
                                    )
                                    val onSnackBarSurface by animateColorAsState(
                                        targetValue = PrognozaTheme.colors.onSurface
                                    )
                                    ForecastSnackBar(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(16.dp),
                                        text = error.asString(),
                                        visible = showSnackBar,
                                        surfaceColor = snackBarSurface,
                                        onSurfaceColor = onSnackBarSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}