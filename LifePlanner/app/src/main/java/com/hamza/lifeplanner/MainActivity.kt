package com.hamza.lifeplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.hamza.lifeplanner.data.AuthRepository
import com.hamza.lifeplanner.navigation.NavGraph
import com.hamza.lifeplanner.ui.theme.LifePlannerTheme

// La clase MainActivity es el punto de entrada principal de la aplicación.
// Desde aquí se inicia la interfaz Compose y la navegación de la app.
class MainActivity : ComponentActivity() {

    // Repositorio de autenticación que se usará en toda la navegación
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permite que la app aproveche toda la pantalla, incluyendo bordes
        enableEdgeToEdge()

        // setContent define toda la interfaz usando Jetpack Compose
        setContent {

            // Aplicamos el tema visual global de la aplicación
            LifePlannerTheme {

                // Controlador de navegación para movernos entre pantallas
                val navController = rememberNavController()

                // Cargamos el grafo de navegación principal
                NavGraph(
                    navController = navController,
                    authRepository = authRepository
                )
            }
        }
    }
}