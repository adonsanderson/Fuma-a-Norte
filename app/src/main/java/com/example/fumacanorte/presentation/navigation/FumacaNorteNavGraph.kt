package com.example.fumacanorte.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.fumacanorte.presentation.detail.HourlyDetailRoute
import com.example.fumacanorte.presentation.weather.WeatherDashboardRoute

@Composable
fun FumacaNorteNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = FumacaNorteRoute.WeatherDashboard,
        modifier = modifier
    ) {
        composable<FumacaNorteRoute.WeatherDashboard> {
            WeatherDashboardRoute(
                onNavigateToHourlyDetail = { timeIso ->
                    navController.navigate(FumacaNorteRoute.HourlyDetail(timeIso = timeIso))
                }
            )
        }

        composable<FumacaNorteRoute.HourlyDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<FumacaNorteRoute.HourlyDetail>()
            HourlyDetailRoute(
                timeIso = route.timeIso,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
