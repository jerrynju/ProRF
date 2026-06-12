package com.prorf.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prorf.app.R
import com.prorf.app.ui.theme.Prf

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    val p = Prf.colors
    Box(
        Modifier.fillMaxSize().background(Brush.linearGradient(listOf(p.prim, p.sec))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(vertical = 32.dp),
        ) {
            // App icon
            Box(
                Modifier.size(88.dp).background(Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Text("\uD83D\uDCE1", fontSize = 40.sp) }

            // App name
            Text(
                stringResource(R.string.app_name),
                fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White,
            )

            // Description
            Text(
                stringResource(R.string.onboarding_desc),
                fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 320.dp).padding(horizontal = 24.dp),
            )

            Spacer(Modifier.height(12.dp))

            // Feature bullets
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.widthIn(max = 280.dp),
            ) {
                OnboardingBullet(stringResource(R.string.onboarding_offline), Color.White)
                OnboardingBullet(stringResource(R.string.onboarding_free), Color.White)
                OnboardingBullet(stringResource(R.string.onboarding_private), Color.White)
                OnboardingBullet(stringResource(R.string.onboarding_noperms), Color.White)
            }

            Spacer(Modifier.height(28.dp))

            // Get Started button
            Button(
                onClick = onGetStarted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = p.prim,
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 36.dp, vertical = 14.dp),
            ) { Text(stringResource(R.string.onboarding_action), fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun OnboardingBullet(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier.size(6.dp).background(color.copy(alpha = 0.8f), CircleShape),
        )
        Text(text, fontSize = 13.sp, color = color.copy(alpha = 0.9f))
    }
}
