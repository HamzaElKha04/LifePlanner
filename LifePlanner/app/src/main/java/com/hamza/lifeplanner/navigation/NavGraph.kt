package com.hamza.lifeplanner.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hamza.lifeplanner.data.AuthRepository
import com.hamza.lifeplanner.ui.screens.LoginScreen
import com.hamza.lifeplanner.ui.screens.MainTabsScreen
import com.hamza.lifeplanner.ui.screens.RegisterScreen

/*
--------------------------------------------------
Navegación principal de LifePlanner
--------------------------------------------------

Este archivo define las rutas principales de la app.

La navegación se divide en:
- Login
- Registro
- Pantallas principales de la aplicación

Si el usuario ya tiene una sesión iniciada, entra directamente
a la aplicación. Si no, se muestra primero la pantalla de login.
*/

/* Rutas principales de navegación */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN_TABS = "main_tabs"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    authRepository: AuthRepository
) {
    /*
    Pantalla inicial:
    - Si el usuario ya está autenticado, entra directamente a la app.
    - Si no hay sesión activa, empieza en login.
    */
    val startDestination = if (authRepository.isUserLoggedIn()) {
        Routes.MAIN_TABS
    } else {
        Routes.LOGIN
    }

    /*
    NavHost contiene las pantallas principales y controla
    la navegación entre ellas.
    */
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        /* Pantalla de login */
        composable(Routes.LOGIN) {
            LoginScreen(
                authRepository = authRepository,

                /* Si el login es correcto, se entra a la pantalla principal */
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN_TABS) {
                        /*
                        Se elimina login del historial para que el usuario
                        no pueda volver atrás después de iniciar sesión.
                        */
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                    }
                },

                /* Navegación hacia la pantalla de registro */
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        /* Pantalla de registro */
        composable(Routes.REGISTER) {
            RegisterScreen(
                authRepository = authRepository,

                /* Si el registro es correcto, se entra directamente a la app */
                onRegisterSuccess = {
                    navController.navigate(Routes.MAIN_TABS) {
                        /*
                        Se elimina login del historial para evitar volver
                        a las pantallas de acceso después del registro.
                        */
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                    }
                },

                /* Vuelve a la pantalla de login */
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        /* Pantalla principal de la aplicación */
        composable(Routes.MAIN_TABS) {
            MainTabsScreen(
                authRepository = authRepository,

                /* Al cerrar sesión, se vuelve a login */
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        /*
                        Se elimina la pantalla principal del historial
                        para evitar volver atrás tras cerrar sesión.
                        */
                        popUpTo(Routes.MAIN_TABS) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}