package com.ai.assistant.jarvis.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun JarvisAppUI(onActivateClicked: () -> Unit) {
    var isListening by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)), // Deep futuristic blue/black
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "JARVIS",
            color = Color.Cyan,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 8.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "System Neural Network Active",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(60.dp))

        // Futuristic Orb Animation
        JarvisOrb(isListening = isListening)

        Spacer(modifier = Modifier.height(60.dp))

        Button(
            onClick = {
                isListening = !isListening
                onActivateClicked()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isListening) Color.Red else Color.Cyan,
                contentColor = if (isListening) Color.White else Color.Black
            ),
            modifier = Modifier.padding(16.dp).height(56.dp).fillMaxWidth(0.7f)
        ) {
            Text(
                text = if (isListening) "DEACTIVATE MODULE" else "ACTIVATE AI CORE",
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun JarvisOrb(isListening: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.5f else 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening) 500 else 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening) 500 else 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
    ) {
        // Outer glowing ring
        Box(
            modifier = Modifier
                .size(160.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha * 0.5f
                }
                .clip(CircleShape)
                .background(Color.Cyan.copy(alpha = 0.3f))
        )
        // Core
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Cyan)
        )
    }
}
