package com.example.ui.screens.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LightAccentGold
import com.example.ui.theme.LightPrimary
import com.example.ui.theme.LightPrimaryDark
import kotlinx.coroutines.delay

/**
 * Animated Splash Screen checking for active match immediately!
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0.5f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
        delay(1200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(LightPrimary, LightPrimaryDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale.value)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(LightAccentGold),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SportsCricket,
                    contentDescription = null,
                    tint = Color(0xFF2B1D00),
                    modifier = Modifier.size(54.dp)
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Mahi Cric Pro",
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                color = Color.White
            )
            Text(
                text = "Offline Pro Cricket Scorer",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = LightAccentGold
            )
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(
                color = LightAccentGold,
                strokeWidth = 3.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 3-slide Welcome Onboarding for first launch
 */
@Composable
fun WelcomeSlidesScreen(
    onFinishWelcome: () -> Unit
) {
    var currentSlide by remember { mutableStateOf(0) }

    val slides = listOf(
        SlideData(
            title = "Pro Cricket Scoring",
            subtitle = "Zero-scroll fixed scoring pad copied from CricHeroes with 3D buttons, overthrows, and instant strike rotation.",
            icon = Icons.Default.SportsCricket
        ),
        SlideData(
            title = "100% Offline & Auto-Resume",
            subtitle = "Power outage? App killed? Phone restart? Room WAL database commits every ball before display so nothing is ever lost.",
            icon = Icons.Default.Bolt
        ),
        SlideData(
            title = "DLS, NRR & Full Stats",
            subtitle = "Standard DLS calculator, tournament points table with accurate NRR, and player MVP leaderboards.",
            icon = Icons.Default.SportsBaseball
        )
    )

    val current = slides[currentSlide]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Skip Button
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onFinishWelcome) {
                Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Slide Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(LightPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = current.icon,
                    contentDescription = null,
                    tint = LightPrimary,
                    modifier = Modifier.size(64.dp)
                )
            }
            Spacer(Modifier.height(28.dp))
            Text(
                text = current.title,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = current.subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        // Indicators & Next Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                slides.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentSlide) 20.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(if (index == currentSlide) LightPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )
                }
            }

            Button(
                onClick = {
                    if (currentSlide < slides.size - 1) currentSlide++ else onFinishWelcome()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightPrimary)
            ) {
                Text(
                    text = if (currentSlide == slides.size - 1) "GET STARTED" else "NEXT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

data class SlideData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)
