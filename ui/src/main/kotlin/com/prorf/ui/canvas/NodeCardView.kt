package com.prorf.ui.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prorf.ui.model.NodeStatus
import com.prorf.ui.model.UiNodeCard

/**
 * Visual representation of a single node on the workflow canvas.
 * Spec: must show title, parameter summary, status indicator, output summary.
 */
@Composable
fun NodeCardView(card: UiNodeCard, modifier: Modifier = Modifier) {
    val borderColor = when {
        card.isSelected -> MaterialTheme.colorScheme.primary
        card.status == NodeStatus.ERROR -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    Column(
        modifier = modifier
            .width(160.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            )
            .border(
                width = if (card.isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = card.displayName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        if (card.outputSummary.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = card.outputSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 2,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        StatusDot(status = card.status)
    }
}

@Composable
private fun StatusDot(status: NodeStatus) {
    val color = when (status) {
        NodeStatus.IDLE -> Color.Gray
        NodeStatus.RUNNING -> Color(0xFF1976D2)
        NodeStatus.SUCCESS -> Color(0xFF388E3C)
        NodeStatus.ERROR -> Color(0xFFD32F2F)
    }
    androidx.compose.foundation.Canvas(modifier = Modifier) {
        drawCircle(color = color, radius = 4f)
    }
}
