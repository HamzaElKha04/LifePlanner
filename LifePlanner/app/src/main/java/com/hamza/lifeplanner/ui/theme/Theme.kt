package com.hamza.lifeplanner.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/*
--------------------------------------------------
Tema visual principal de LifePlanner
--------------------------------------------------

Este archivo define el tema general de la aplicación.

Aquí se configuran:
- colores principales
- modo claro y oscuro
- colores dinámicos si el dispositivo los soporta
- tipografía general de la app

El tema se aplica desde MainActivity para que todas las
pantallas mantengan una apariencia coherente.
*/

/* Colores usados cuando el sistema está en modo oscuro */
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

/* Colores usados cuando el sistema está en modo claro */
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun LifePlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    /*
    Si el dispositivo soporta colores dinámicos, Android puede adaptar
    algunos colores al tema del sistema. Si no, se usan los colores
    definidos en LightColorScheme o DarkColorScheme.
    */
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    /*
    Aplica el tema general a todo el contenido de la aplicación.
    */
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}