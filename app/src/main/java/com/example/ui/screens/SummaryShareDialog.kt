package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.i18n.AppStrings
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcStatusPill
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassCard
import com.example.ui.theme.MoltenAmber
import com.example.ui.theme.PassEmerald
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.WeldUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SummaryShareDialog(
    uiState: WeldUiState,
    onShowMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lang = uiState.preferences.language
    val prefs = uiState.preferences
    val metrics = uiState.metrics
    val positionsStr = metrics.uniquePositions.ifEmpty { listOf("None yet") }.joinToString(", ")

    val plainReport = buildString {
        appendLine("====================================")
        appendLine("   JRS WeldMentor - Practice Report")
        appendLine("   Developed by Md Jakariya Hasan (Jakariya41jr.xyz)")
        appendLine("====================================")
        appendLine("Trainee: ${prefs.traineeName}")
        appendLine("Target Trade: ${prefs.targetTrade}")
        appendLine("Total Logged Jobs: ${metrics.totalJobs} (Pass: ${metrics.passCount}, Rate: ${metrics.passRatePct}%)")
        appendLine("Positions Practiced: $positionsStr")
        appendLine("Viva Quiz Score: ${metrics.quizAccuracyPct}% (${metrics.totalQuizAnswered} answered)")
        appendLine("Daily Streak: ${prefs.streakDays} days")
        if (uiState.jobs.isNotEmpty()) {
            appendLine("\nRecent Jobs:")
            uiState.jobs.take(5).forEach { j ->
                appendLine("- ${j.date} | ${j.process} | ${j.position} | ${j.baseMaterial} | ${j.rodFillerAndSize} | ${j.status}")
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = SlateSurface,
            border = BorderStroke(1.dp, ArcCyan.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(AppStrings.shareSummaryTitle.get(lang), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(AppStrings.builtBy.get(lang), style = MaterialTheme.typography.labelSmall, color = ArcCyan)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_summary_dialog_btn")) {
                        Icon(Icons.Filled.Close, contentDescription = AppStrings.cancel.get(lang), tint = TextPrimary)
                    }
                }

                GlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = ArcCyan, contentPadding = 14.dp) {
                    Text(prefs.traineeName, style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                    Text(prefs.targetTrade, style = MaterialTheme.typography.bodySmall, color = ArcCyan)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = GlassBorder)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SummaryStat("Jobs", "${metrics.totalJobs}")
                        SummaryStat("Pass Rate", "${metrics.passRatePct}%")
                        SummaryStat("Viva", "${metrics.quizAccuracyPct}%")
                        SummaryStat("Streak", "${prefs.streakDays}d")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(AppStrings.positionsMastered.get(lang), style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (metrics.uniquePositions.isEmpty()) {
                            ArcStatusPill(text = "1G–6G+ Ready", color = MoltenAmber)
                        } else {
                            metrics.uniquePositions.forEach { pos ->
                                ArcStatusPill(text = pos, color = PassEmerald)
                            }
                        }
                    }
                }

                if (uiState.jobs.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Recent Welding Jobs", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                        uiState.jobs.take(5).forEach { j ->
                            Text(
                                text = "• ${j.date} | ${j.process} ${j.position} (${j.rodFillerAndSize}) — ${j.status}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("JRS WeldMentor Summary", plainReport))
                            onShowMessage(AppStrings.copiedFeedback.get(lang))
                        },
                        modifier = Modifier.weight(1f).testTag("summary_copy_btn"),
                        border = BorderStroke(1.dp, ArcCyan.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(AppStrings.copyReport.get(lang), color = ArcCyan, style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "JRS WeldMentor Summary - ${prefs.traineeName}")
                                putExtra(Intent.EXTRA_TEXT, plainReport)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, AppStrings.shareReport.get(lang)))
                        },
                        modifier = Modifier.weight(1f).testTag("summary_share_btn"),
                        border = BorderStroke(1.dp, PassEmerald.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, tint = PassEmerald, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(AppStrings.shareReport.get(lang), color = PassEmerald, style = MaterialTheme.typography.labelMedium)
                    }
                }

                Button(
                    onClick = { printHtmlSummary(context, prefs.traineeName, prefs.targetTrade, plainReport) },
                    modifier = Modifier.fillMaxWidth().testTag("summary_print_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan, contentColor = Color(0xFF04131E))
                ) {
                    Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppStrings.printReport.get(lang))
                }
            }
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = ArcCyan)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

private fun printHtmlSummary(
    context: Context,
    traineeName: String,
    targetTrade: String,
    plainReport: String
) {
    val webView = WebView(context)
    val escaped = plainReport
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\n", "<br/>")
    val html = """
        <html>
        <body style="font-family: sans-serif; padding: 24px; color: #0f172a;">
          <h2 style="margin-bottom:4px;">JRS WeldMentor — Practice Summary</h2>
          <p style="color:#0284c7; margin-top:0;">Developed by Md Jakariya Hasan (Jakariya41jr.xyz)</p>
          <p><strong>Trainee:</strong> $traineeName &nbsp;|&nbsp; <strong>Trade:</strong> $targetTrade</p>
          <hr/>
          <pre style="font-family: monospace; white-space: pre-wrap; line-height:1.5;">$escaped</pre>
        </body>
        </html>
    """.trimIndent()

    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val adapter = webView.createPrintDocumentAdapter("JRS_WeldMentor_Summary")
            printManager.print("JRS WeldMentor Practice Summary", adapter, PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
}
