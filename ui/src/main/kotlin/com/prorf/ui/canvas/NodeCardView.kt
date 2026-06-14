package com.prorf.ui.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
internal const val NODE_CARD_WIDTH_DP = 180f

/** Approx vertical center of card from its top edge (dp) — used for port/edge alignment. */
internal const val NODE_PORT_Y_DP = 30f

/**
 * Visual representation of a node card on the workflow canvas.
 *
 * Layout: 4dp left category stripe + content (name row + value/type row).
 * Output value gets bold labelLarge when computed; a 3-letter category abbr
 * is shown when idle. Port dots extend beyond left/right edges.
 */
@Composable
fun NodeCardView(card: UiNodeCard, modifier: Modifier = Modifier) {
    val catColor = categoryColor(card.typeId)
    val cardShape = RoundedCornerShape(8.dp)
    val borderColor = when {
        card.isSelected -> MaterialTheme.colorScheme.primary
        card.status == NodeStatus.ERROR -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val borderWidth = if (card.isSelected) 2.dp else 0.5.dp
    val elevation = if (card.isSelected) 6.dp else 2.dp
    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(modifier = modifier.width(NODE_CARD_WIDTH_DP.dp)) {
        // Main card: left stripe + content column
        Row(
            modifier = Modifier
                .shadow(elevation, cardShape)
                .clip(cardShape)
                .background(surfaceColor)
                .border(borderWidth, borderColor, cardShape),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Category color stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(catColor),
            )

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp, vertical = 9.dp),
            ) {
                // Header: display name + status dot
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = card.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(4.dp))
                    StatusDot(status = card.status)
                }
                Spacer(Modifier.height(5.dp))
                // Value row: split num (bold) + unit (small) OR category abbreviation
                if (card.outputSummary.isNotEmpty()) {
                    val isNegativeOutput = card.outputSummary.trimStart().startsWith("-")
                    val valueColor = if (isNegativeOutput)
                        MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                    else catColor
                    val spIdx = card.outputSummary.indexOf(' ')
                    val numPart = if (spIdx > 0) card.outputSummary.substring(0, spIdx)
                                  else card.outputSummary
                    val unitPart = if (spIdx > 0) card.outputSummary.substring(spIdx + 1) else ""
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = numPart,
                            style = MaterialTheme.typography.labelLarge,
                            color = valueColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (unitPart.isNotEmpty()) {
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = unitPart,
                                style = MaterialTheme.typography.labelSmall,
                                color = valueColor.copy(alpha = 0.65f),
                                fontSize = 8.sp,
                                maxLines = 1,
                            )
                        }
                    }
                } else {
                    Text(
                        text = card.typeId.substringAfterLast('.'),
                        style = MaterialTheme.typography.labelSmall,
                        color = catColor.copy(alpha = 0.65f),
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        // Input port dot — left edge, vertically centered
        PortDot(
            color = catColor,
            ringColor = surfaceColor,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-5).dp),
        )

        // Output port dot — right edge, vertically centered
        PortDot(
            color = catColor,
            ringColor = surfaceColor,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 5.dp),
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

@Composable
private fun StatusDot(status: NodeStatus) {
    val color = when (status) {
        NodeStatus.IDLE -> Color(0xFF9CA3AF)
        NodeStatus.RUNNING -> Color(0xFF3B82F6)
        NodeStatus.SUCCESS -> Color(0xFF22C55E)
        NodeStatus.ERROR -> Color(0xFFEF4444)
    }
    Box(modifier = Modifier.size(7.dp).background(color, CircleShape))
}

/**
 * Derives a category accent color from the node typeId string.
 * No RF domain knowledge — pure structural pattern matching.
 */
private fun categoryColor(typeId: String): Color {
    val name = typeId.substringAfterLast('.')
    return when {
        name.contains("Source") || name.contains("Signal") || name.contains("Noise") -> Color(0xFF2F80ED)
        name.contains("Amplifier") -> Color(0xFF27AE60)
        name.contains("Attenuator") || name.contains("Cable") || name.contains("Filter") -> Color(0xFFF2994A)
        name.contains("Loss") || name.contains("Channel") || name.contains("Path") -> Color(0xFF9B51E0)
        name.contains("Receiver") || name.contains("Sensitivity") -> Color(0xFFEB5757)
        else -> Color(0xFF64748B)
    }
}

/** Short 2–3 letter category badge shown when no execution output is available. */
private fun categoryAbbr(typeId: String): String {
    val name = typeId.substringAfterLast('.')
    return when {
        name.contains("Signal") -> "SRC"
        name.contains("Noise") -> "NSE"
        name.contains("Amplifier") -> "AMP"
        name.contains("Attenuator") -> "ATT"
        name.contains("Cable") -> "CBL"
        name.contains("Filter") -> "FLT"
        name.contains("Loss") || name.contains("Path") || name.contains("Channel") -> "CH"
        name.contains("Receiver") -> "RCV"
        else -> name.take(3).uppercase()
    }
}
