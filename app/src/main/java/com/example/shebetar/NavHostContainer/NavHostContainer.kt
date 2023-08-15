package com.example.shebetar.NavHostContainer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.shebetar.HomeScreen.HomeScreen
import com.example.shebetar.NotificationsScreen.NotificationsScreen
import com.example.shebetar.PostCreationScreen.PostCreationScreen
import com.example.shebetar.ProfileScreen.RegisterComponent
import com.example.shebetar.SearchScreen.SearchScreen
import kotlinx.coroutines.CoroutineScope

@Composable
fun NavHostContainer(
    navController: NavHostController,
    padding: PaddingValues,
    scope: CoroutineScope,
    scaffoldState: ScaffoldState
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(paddingValues = padding),
        builder = {
            composable("home") {
                HomeScreen(navController, scope, scaffoldState)
            }
            composable("search") {
                SearchScreen()
            }
            composable("notifications") {
                NotificationsScreen()
            }
            composable("postCreation"){
                PostCreationScreen(navController)
            }
            composable("register"){
                RegisterComponent()
            }
        })

}