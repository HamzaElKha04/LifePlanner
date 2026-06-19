package com.hamza.lifeplanner.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
--------------------------------------------------
Tipografía principal de LifePlanner
--------------------------------------------------

Este archivo define la tipografía base utilizada por
el tema general de la aplicación.

Se configura el estilo bodyLarge para mantener un
texto claro y legible en las pantallas principales.
*/

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)