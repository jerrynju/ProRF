package com.prorf.ui.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prorf.ui.model.NodeStatus
import com.prorf.ui.model.UiNodeCard

/** Card width in dp — shared with WorkflowCanvas for edge endpoint math. */
internal const val NODE_CARD_WIDTH_DP = 200f

/** Colored header band height in dp. */
internal const val NODE_HEADER_HEIGHT_DP = 26f

/**
 * Vertical center of the full card (dp) — used for port/edge alignment.
 * Header(26) + body padding+text(52) ≈ 78dp total → center ≈ 39dp.
 */
internal const val NODE_PORT_Y_DP = 39f

/**
 * Node card for the workflow canvas.
 *
 * Visual: full-width colored header (type abbr + status dot) + white body
 * (display name + output value or idle hint). Port dots protrude at the
 * vertical center of the card for edge attachment.
 */
@Composable
fun NodeCardView(card: UiNodeCard, modifier: Modifier = Modifier) {
    val catColor = categoryColor(card.typeId)
    val abbr = categoryAbbr(card.typeId)
    val cardShape = RoundedCornerShape(8.dp)

    val borderColor = when {
        card.isSelected -> MaterialTheme.colorScheme.primary
        card.status == NodeStatus.ERROR -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    }
    val borderWidth = if (card.isSelected) 2.dp else 0.5.dp
    val elevation = if (card.isSelected) 6.dp else 2.dp
    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(modifier = modifier.width(NODE_CARD_WIDTH_DP.dp)) {
        Column(
            modifier = Modifier
                .shadow(elevation, cardShape)
                .clip(cardShape)
                .background(surfaceColor)
                .border(borderWidth, borderColor, cardShape),
        ) {
            // ── Colored header band ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NODE_HEADER_HEIGHT_DP.dp)
                    .background(catColor),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = abbr,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                    )
                    Spacer(Modifier.weight(1f))
                    val statusColor = when (card.status) {
                        NodeStatus.IDLE -> Color.White.copy(alpha = 0.30f)
                        NodeStatus.RUNNING -> Color.White
                        NodeStatus.SUCCESS -> Color(0xFF4ADE80)
                        NodeStatus.ERROR -> Color(0xFFFCA5A5)
                    }
                    Box(Modifier.size(6.dp).background(statusColor, CircleShape))
                }
            }

            // ── Body ─────────────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text = card.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(5.dp))

                if (card.outputSummary.isNotEmpty()) {
                    val isNeg = card.outputSummary.trimStart().startsWith("-")
                    val valueColor = if (isNeg) MaterialTheme.colorScheme.error.copy(alpha = 0.9f) else catColor
                    val spIdx = card.outputSummary.indexOf(' ')
                    val numPart = if (spIdx > 0) card.outputSummary.substring(0, spIdx) else card.outputSummary
                    val unitPart = if (spIdx > 0) card.outputSummary.substring(spIdx + 1) else ""
                    Row(verticalAlignment = Alignment.Baseline) {
                        Text(
                            text = numPart,
                            style = MaterialTheme.typography.labelLarge,
                            color = valueColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                        if (unitPart.isNotEmpty()) {
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = unitPart,
                                style = MaterialTheme.typography.labelSmall,
                                color = valueColor.copy(alpha = 0.6f),
                                fontSize = 8.sp,
                            )
                        }
                    }
                } else {
                    // Idle state: show port dots + type hint
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(5.dp).background(catColor.copy(alpha = 0.35f), CircleShape))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = card.typeId.substringAfterLast('.'),
                            style = MaterialTheme.typography.labelSmall,
                            color = catColor.copy(alpha = 0.5f),
                            fontSize = 8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(4.dp))
                        Box(Modifier.size(5.dp).background(catColor.copy(alpha = 0.35f), CircleShape))
                    }
                }
            }
        }

        // Input port dot — protrudes from left edge at vertical center
        PortDot(
            color = catColor,
            ringColor = surfaceColor,
            modifier = Modifier.align(Alignment.CenterStart).offset(x = (-5).dp),
        )

        // Output port dot — protrudes from right edge at vertical center
        PortDot(
            color = catColor,
            ringColor = surfaceColor,
            modifier = Modifier.align(Alignment.CenterEnd).offset(x = 5.dp),
        )
    }
}

@Composable
private fun PortDot(color: Color, ringColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(10.dp)
            .background(ringColor, CircleShape)
            .border(2.dp, color, CircleShape),
    )
}

/** Category accent color derived from typeId. */
internal fun categoryColor(typeId: String): Color {
    val name = typeId.substringAfterLast('.')
    return when {
        name.contains("Source") || name.contains("Signal") || name.contains("Noise") -> Color(0xFF2F80ED)
        name.contains("Amplifier") -> Color(0xFFE05A00)
        name.contains("Attenuator") || name.contains("Cable") || name.contains("Filter") -> Color(0xFFF2994A)
        name.contains("Loss") || name.contains("Channel") || name.contains("Path") -> Color(0xFF27AE60)
        name.contains("Receiver") || name.contains("Sensitivity") -> Color(0xFF7B2FBE)
        else -> Color(0xFF64748B)
    }
}

/** Short 2–4 letter type abbreviation shown in the header band. */
internal fun categoryAbbr(typeId: String): String {
    val name = typeId.substringAfterLast('.')
    return when {
        name.contains("Signal") -> "TX"
        name.contains("Noise") -> "NSE"
        name.contains("Amplifier") -> "AMP"
        name.contains("Attenuator") -> "ATT"
        name.contains("Cable") -> "CBL"
        name.contains("Filter") -> "FLT"
        name.contains("Loss") || name.contains("Path") || name.contains("Channel") -> "PATH"
        name.contains("Receiver") -> "RX"
        name.contains("Sensitivity") -> "SNS"
        else -> name.take(4).uppercase()
    }
}
