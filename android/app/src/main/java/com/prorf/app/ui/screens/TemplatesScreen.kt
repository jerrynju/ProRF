package com.prorf.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prorf.app.R
import com.prorf.app.ui.components.templateName
import com.prorf.app.ui.components.templateDesc
import com.prorf.app.data.Catalog
import com.prorf.app.ui.theme.Prf

/** Free template library: tapping a template creates a real workflow and opens the editor. */
@Composable
fun TemplatesScreen(onCreate: (String) -> Unit, twoColumn: Boolean = false) {
    val p = Prf.colors
    val zh = Catalog.isZh()
    val samples = Catalog.sampleWorkflows(zh)

    Column(Modifier.fillMaxSize().background(p.bg)) {
        Column(
            Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(p.sec, p.prim)))
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 18.dp),
        ) {
            Text(stringResource(R.string.templates_title), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.templates_subtitle), fontSize = 12.sp, color = Color.White.copy(alpha = .8f))
        }

        val nodeCountById = remember(samples) {
            samples.associate { wf -> wf.id.removePrefix("wf-") to wf.nodes.size }
        }
        val items = Catalog.templates
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            val chunks = if (twoColumn) items.chunked(2) else items.chunked(1)
            items(chunks.size) { row ->
                Row(Modifier.padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    chunks[row].forEach { (tplId, emoji) ->
                        val nodeCount = nodeCountById[tplId] ?: 5
                        Box(Modifier.weight(1f)) {
                            TemplateCard(tplId, emoji, nodeCount) { onCreate(tplId) }
                        }
                    }
                    if (chunks[row].size == 1 && twoColumn) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(tplId: String, emoji: String, nodeCount: Int, onUse: () -> Unit) {
    val p = Prf.colors
    Column(
        Modifier.fillMaxWidth()
            .background(p.surf, RoundedCornerShape(16.dp))
            .border(1.dp, p.line2, RoundedCornerShape(16.dp))
            .clickable(onClick = onUse)
            .padding(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).background(p.primTint, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) { Text(emoji, fontSize = 22.sp) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(templateName(tplId), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = p.txt1)
                Text(stringResource(R.string.template_modules, nodeCount), fontSize = 11.sp, color = p.txt3)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(templateDesc(tplId), fontSize = 12.sp, color = p.txt2)
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = onUse,
            colors = ButtonDefaults.buttonColors(containerColor = p.prim, contentColor = p.onPrim),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.align(Alignment.End),
        ) { Text(stringResource(R.string.use_template), fontSize = 11.sp, fontWeight = FontWeight.Bold) }
    }
}
