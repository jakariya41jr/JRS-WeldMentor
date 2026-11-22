package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.audio.rememberVoiceAssistantController
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.i18n.BiText
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcStatusPill
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassCard
import com.example.ui.theme.MoltenAmber
import com.example.ui.theme.PassEmerald
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarnRose
import com.example.ui.viewmodel.WeldUiState

private val STARTER_PROMPTS = listOf(
    BiText("How to pass 6G TIG root without suck-back?", "সাক-ব্যাক ছাড়া 6G TIG রুট পাস করার উপায় কী?"),
    BiText("LB52 vs E7018: amps & key differences", "LB52 এবং E7018-এর মধ্যে পার্থক্য ও অ্যাম্পিয়ার"),
    BiText("How do I fix Undercut in 3G Vertical Up?", "3G ভার্টিক্যাল আপে আন্ডারকাট কীভাবে বন্ধ করবো?"),
    BiText("Explain TIG Cup, Gas Lens & Short Back Cap", "TIG সিরামিক কাপ, গ্যাস লেন্স ও শর্ট ব্যাক ক্যাপ বুঝিয়ে দিন")
)

@Composable
fun AiMentorScreen(
    uiState: WeldUiState,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = uiState.preferences.language
    val hasKey = uiState.preferences.geminiApiKey.isNotBlank()
    val isOnline = uiState.isOnline
    val chatState = uiState.aiChat
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val voice = rememberVoiceAssistantController(lang)
    var autoSpeakAi by remember { mutableStateOf(true) }
    var lastSpokenMessageId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(chatState.messages.size) {
        if (chatState.messages.isNotEmpty()) {
            listState.animateScrollToItem(chatState.messages.size - 1)
            val latest = chatState.messages.last()
            if (autoSpeakAi && !latest.isUser && latest.id != lastSpokenMessageId) {
                lastSpokenMessageId = latest.id
                voice.speak(latest.text)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Header Status Strip
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            cornerRadius = 16.dp,
            contentPadding = 12.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.aiMentorTitle.get(lang), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ArcStatusPill(text = AppStrings.aiStrictScopeBadge.get(lang), color = ArcCyan)
                        ArcStatusPill(
                            text = if (hasKey) "BYOK Ready" else "No Key",
                            color = if (hasKey) PassEmerald else MoltenAmber
                        )
                    }
                }
                IconButton(
                    onClick = {
                        autoSpeakAi = !autoSpeakAi
                        if (!autoSpeakAi) voice.stopSpeaking()
                    },
                    modifier = Modifier.testTag("ai_auto_speak_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (autoSpeakAi) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                        contentDescription = AppStrings.voiceAutoSpeak.get(lang),
                        tint = if (autoSpeakAi) PassEmerald else TextSecondary
                    )
                }
                if (chatState.messages.isNotEmpty()) {
                    IconButton(onClick = {
                        voice.stopSpeaking()
                        onClearChat()
                    }, modifier = Modifier.testTag("clear_ai_chat_btn")) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = AppStrings.clearChat.get(lang), tint = TextSecondary)
                    }
                }
                IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("ai_open_settings_btn")) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = ArcCyan)
                }
            }
        }

        if (!hasKey) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = MoltenAmber,
                    contentPadding = 22.dp
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Key, contentDescription = null, tint = MoltenAmber, modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = AppStrings.aiNoKeyTitle.get(lang),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = AppStrings.aiNoKeyDesc.get(lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onOpenSettings,
                            modifier = Modifier.testTag("configure_api_key_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = ArcCyan, contentColor = Color(0xFF04131E))
                        ) {
                            Text(AppStrings.openSettingsBtn.get(lang))
                        }
                    }
                }
            }
        } else {
            if (!isOnline) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = WarnRose.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, WarnRose.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CloudOff, contentDescription = null, tint = WarnRose, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(AppStrings.aiOfflineBanner.get(lang), style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (chatState.messages.isEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14.dp) {
                            Text(
                                text = if (lang == AppLanguage.BN) {
                                    "ওয়েল্ডিং প্রসেস, 1G–6G+, রড কোড, TIG টর্চ বা ভাইভা নিয়ে প্রশ্ন করুন:"
                                } else {
                                    "Ask anything about welding processes, 1G–6G+, rods, TIG setup, defects, or viva prep:"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                items(chatState.messages, key = { it.id }) { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (msg.isUser) 16.dp else 4.dp,
                                bottomEnd = if (msg.isUser) 4.dp else 16.dp
                            ),
                            color = if (msg.isUser) ArcCyan.copy(alpha = 0.22f) else SlateSurfaceElevated,
                            border = BorderStroke(
                                1.dp,
                                if (msg.isUser) ArcCyan.copy(alpha = 0.45f) else GlassBorder
                            ),
                            modifier = Modifier.fillMaxWidth(0.88f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                if (!msg.isUser) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Surface(
                                            onClick = {
                                                if (voice.isSpeaking) voice.stopSpeaking() else voice.speak(msg.text)
                                            },
                                            shape = RoundedCornerShape(50),
                                            color = ArcCyan.copy(alpha = 0.14f),
                                            modifier = Modifier.testTag("ai_speak_msg_${msg.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (voice.isSpeaking) Icons.Filled.Stop else Icons.Filled.VolumeUp,
                                                    contentDescription = AppStrings.voiceTapListen.get(lang),
                                                    tint = ArcCyan,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (voice.isSpeaking) AppStrings.voiceStopSpeaking.get(lang) else AppStrings.voiceTapListen.get(lang),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = ArcCyan
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (chatState.isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = ArcCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == AppLanguage.BN) "উত্তর তৈরি হচ্ছে..." else "Thinking...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                chatState.errorMessage?.let { err ->
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = WarnRose.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, WarnRose.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(err, style = MaterialTheme.typography.bodySmall, color = WarnRose, modifier = Modifier.padding(10.dp))
                        }
                    }
                }
            }

            // Quick Starter Prompts
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(STARTER_PROMPTS) { prompt ->
                    SuggestionChip(
                        onClick = { if (isOnline && !chatState.isLoading) onSendMessage(prompt.get(lang)) },
                        label = { Text(prompt.get(lang), style = MaterialTheme.typography.bodySmall, color = TextPrimary) }
                    )
                }
            }

            voice.voiceError?.let { vErr ->
                Text(
                    text = vErr,
                    style = MaterialTheme.typography.bodySmall,
                    color = WarnRose,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Input Row with Voice STT Mic Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = {
                        if (voice.isListening) {
                            voice.stopListening()
                        } else {
                            voice.startListening { spoken ->
                                input = if (input.isBlank()) spoken else "$input $spoken"
                            }
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .testTag("ai_voice_mic_btn"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (voice.isListening) WarnRose else SlateSurfaceElevated,
                    border = BorderStroke(1.dp, if (voice.isListening) WarnRose else ArcCyan.copy(alpha = 0.45f))
                ) {
                    Icon(
                        imageVector = if (voice.isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = if (voice.isListening) AppStrings.voiceListening.get(lang) else AppStrings.voiceSpeakBtn.get(lang),
                        tint = if (voice.isListening) Color.White else ArcCyan,
                        modifier = Modifier.padding(13.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = {
                        Text(
                            text = if (voice.isListening) AppStrings.voiceListening.get(lang) else AppStrings.aiInputHint.get(lang),
                            color = if (voice.isListening) ArcCyan else TextMuted
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_chat_input"),
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    onClick = {
                        if (input.isNotBlank() && isOnline && !chatState.isLoading) {
                            onSendMessage(input)
                            input = ""
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .testTag("ai_chat_send_btn"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (input.isNotBlank() && isOnline) ArcCyan else SlateSurfaceElevated
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (input.isNotBlank() && isOnline) Color(0xFF04131E) else TextMuted,
                        modifier = Modifier.padding(13.dp)
                    )
                }
            }
        }
    }
}
