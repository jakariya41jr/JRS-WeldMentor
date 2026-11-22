package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.content.CurrencyCatalog
import com.example.data.content.CurrencyInfo
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.theme.ArcStatusPill
import com.example.ui.theme.GlassCard
import com.example.ui.theme.WeldTheme
import com.example.ui.viewmodel.WeldUiState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CurrencyConverterScreen(
    uiState: WeldUiState,
    onRefreshLiveRates: () -> Unit,
    onSetCustomRate: (String, String, Double?) -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs = uiState.preferences
    val lang = prefs.language
    val colors = WeldTheme.colors

    var subTab by remember { mutableIntStateOf(0) } // 0 = Converter, 1 = Salary & OT
    var fromCode by remember { mutableStateOf("SAR") }
    var toCode by remember { mutableStateOf("BDT") }
    var amountInput by remember { mutableStateOf("1500") }

    // Salary & OT state
    var salaryCurrencyCode by remember { mutableStateOf("SAR") }
    var basicSalaryInput by remember { mutableStateOf("1800") }
    var otHoursInput by remember { mutableStateOf("60") }
    var otUseMultiplier by remember { mutableStateOf(true) }
    var otFixedRateInput by remember { mutableStateOf("12") }
    var otMultiplierInput by remember { mutableStateOf("1.5") }
    var stdHoursInput by remember { mutableStateOf("208") }
    var allowanceInput by remember { mutableStateOf("300") }

    // Picker & Custom Rate dialogs
    var pickerTarget by remember { mutableStateOf<String?>(null) } // "FROM", "TO", "SALARY"
    var showCustomRateDialog by remember { mutableStateOf(false) }

    val mergedRates = remember(prefs.cachedRatesPerUsd) {
        CurrencyCatalog.mergeRates(prefs.cachedRatesPerUsd)
    }
    val allCurrencies = remember(mergedRates) {
        CurrencyCatalog.getAllSupportedCurrencies(mergedRates)
    }

    val activeFrom = if (subTab == 0) fromCode else salaryCurrencyCode
    val activeTo = if (subTab == 0) toCode else "BDT"
    val pairKey = "${activeFrom}_${activeTo}"
    val customPairRate = prefs.customPairRates[pairKey]

    val autoRate = remember(activeFrom, activeTo, mergedRates) {
        val fromUsd = mergedRates[activeFrom] ?: 1.0
        val toUsd = mergedRates[activeTo] ?: 1.0
        toUsd / fromUsd
    }
    val effectiveRate = customPairRate ?: autoRate
    val hasLiveCache = prefs.ratesLastUpdatedMillis > 0L

    val lastUpdatedLabel = remember(prefs.ratesLastUpdatedMillis, lang) {
        if (prefs.ratesLastUpdatedMillis <= 0L) {
            AppStrings.currencyDefaultOffline.get(lang)
        } else {
            val fmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.US)
            "${AppStrings.currencyLastUpdated.get(lang)}: ${fmt.format(Date(prefs.ratesLastUpdatedMillis))}"
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mode Switcher: Converter vs Salary & OT Estimator
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = subTab == 0,
                    onClick = { subTab = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("currency_tab_converter"),
                    icon = { Icon(Icons.Filled.CurrencyExchange, contentDescription = null, modifier = Modifier.size(16.dp)) }
                ) {
                    Text(AppStrings.currencyTabConverter.get(lang))
                }
                SegmentedButton(
                    selected = subTab == 1,
                    onClick = { subTab = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("currency_tab_salary"),
                    icon = { Icon(Icons.Filled.Calculate, contentDescription = null, modifier = Modifier.size(16.dp)) }
                ) {
                    Text(AppStrings.currencyTabSalary.get(lang))
                }
            }
        }

        // Rate Status & Refresh / Custom Rate Controls
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentGlow = if (customPairRate != null) colors.moltenAmber else colors.arcCyan,
                contentPadding = 14.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusText = when {
                        customPairRate != null -> AppStrings.currencyCustomActive.get(lang)
                        hasLiveCache -> AppStrings.currencyLiveCached.get(lang)
                        else -> AppStrings.currencyDefaultOffline.get(lang)
                    }
                    val statusColor = when {
                        customPairRate != null -> colors.moltenAmber
                        hasLiveCache -> colors.passEmerald
                        else -> colors.arcCyan
                    }
                    ArcStatusPill(
                        text = statusText,
                        color = statusColor,
                        icon = if (uiState.isOnline) Icons.Filled.CloudDone else Icons.Filled.CloudOff
                    )
                    Text(
                        text = "1 $activeFrom = ${formatRate(effectiveRate)} $activeTo",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = lastUpdatedLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRefreshLiveRates,
                        enabled = !uiState.isRefreshingRates,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("refresh_live_rates_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.arcCyan,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        if (uiState.isRefreshingRates) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.isRefreshingRates) AppStrings.currencyRefreshing.get(lang)
                            else AppStrings.currencyRefreshLive.get(lang),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = { showCustomRateDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_rate_btn"),
                        border = BorderStroke(1.dp, colors.moltenAmber.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, tint = colors.moltenAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = AppStrings.currencyCustomBtn.get(lang),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.moltenAmber,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (customPairRate != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(
                        onClick = { onSetCustomRate(activeFrom, activeTo, null) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(AppStrings.currencyResetRate.get(lang), color = colors.arcCyan)
                    }
                }
            }
        }

        // Pinned Quick Chips for Popular Welding Destinations
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = AppStrings.currencyPopularLabel.get(lang),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textSecondary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CurrencyCatalog.pinnedWeldingCodes) { code ->
                        val info = CurrencyCatalog.findByCode(code)
                        val selected = if (subTab == 0) fromCode == code else salaryCurrencyCode == code
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (subTab == 0) {
                                    fromCode = code
                                    if (toCode == code) toCode = "BDT"
                                } else {
                                    salaryCurrencyCode = code
                                }
                            },
                            label = { Text("${info.flag} $code") },
                            modifier = Modifier.testTag("quick_currency_$code"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.arcCyan.copy(alpha = 0.2f),
                                selectedLabelColor = colors.arcCyan
                            )
                        )
                    }
                }
            }
        }

        if (subTab == 0) {
            // Standard Currency Converter
            val fromInfo = CurrencyCatalog.findByCode(fromCode)
            val toInfo = CurrencyCatalog.findByCode(toCode)
            val amountVal = amountInput.toDoubleOrNull() ?: 0.0
            val convertedVal = amountVal * effectiveRate

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = colors.arcCyan.copy(alpha = 0.35f),
                    contentPadding = 16.dp
                ) {
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { newVal ->
                            if (newVal.isEmpty() || newVal.matches(Regex("^\\d*\\.?\\d*$"))) {
                                amountInput = newVal
                            }
                        },
                        label = { Text(AppStrings.currencyAmountLabel.get(lang)) },
                        prefix = { Text("${fromInfo.symbol} ", color = colors.arcCyan) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("currency_amount_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("100", "500", "1000", "1500", "2500", "5000").forEach { preset ->
                            FilterChip(
                                selected = amountInput == preset,
                                onClick = { amountInput = preset },
                                label = { Text(preset, style = MaterialTheme.typography.labelMedium) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // From Currency Selector
                    CurrencySelectorRow(
                        label = AppStrings.currencyFromLabel.get(lang),
                        currency = fromInfo,
                        lang = lang,
                        tag = "currency_from_selector",
                        onClick = { pickerTarget = "FROM" }
                    )

                    // Swap Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            onClick = {
                                val temp = fromCode
                                fromCode = toCode
                                toCode = temp
                            },
                            modifier = Modifier.testTag("currency_swap_btn"),
                            shape = RoundedCornerShape(50),
                            color = colors.arcCyan.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, colors.arcCyan.copy(alpha = 0.45f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SwapVert,
                                contentDescription = "Swap currencies",
                                tint = colors.arcCyan,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // To Currency Selector
                    CurrencySelectorRow(
                        label = AppStrings.currencyToLabel.get(lang),
                        currency = toInfo,
                        lang = lang,
                        tag = "currency_to_selector",
                        onClick = { pickerTarget = "TO" }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Converted Output Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.passEmerald.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, colors.passEmerald.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${formatMoney(amountVal)} ${fromInfo.code} (${fromInfo.name.get(lang)}) =",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${toInfo.symbol} ${formatMoney(convertedVal)} ${toInfo.code}",
                                style = MaterialTheme.typography.displaySmall,
                                color = colors.passEmerald,
                                modifier = Modifier.testTag("currency_converted_output")
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${toInfo.flag} ${toInfo.name.get(lang)} • ${toInfo.country.get(lang)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }
        } else {
            // Overseas Salary & Overtime Quick Estimator
            val salaryCurrency = CurrencyCatalog.findByCode(salaryCurrencyCode)
            val bdtInfo = CurrencyCatalog.findByCode("BDT")
            val basic = basicSalaryInput.toDoubleOrNull() ?: 0.0
            val otHours = otHoursInput.toDoubleOrNull() ?: 0.0
            val stdHours = (stdHoursInput.toDoubleOrNull() ?: 208.0).coerceAtLeast(1.0)
            val otHourlyRate = if (otUseMultiplier) {
                (basic / stdHours) * (otMultiplierInput.toDoubleOrNull() ?: 1.5)
            } else {
                otFixedRateInput.toDoubleOrNull() ?: 0.0
            }
            val otTotal = otHours * otHourlyRate
            val allowance = allowanceInput.toDoubleOrNull() ?: 0.0
            val totalForeign = basic + otTotal + allowance
            val totalBdt = totalForeign * effectiveRate

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = colors.violetSpark,
                    contentPadding = 16.dp
                ) {
                    CurrencySelectorRow(
                        label = AppStrings.currencyFromLabel.get(lang),
                        currency = salaryCurrency,
                        lang = lang,
                        tag = "salary_currency_selector",
                        onClick = { pickerTarget = "SALARY" }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = basicSalaryInput,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) basicSalaryInput = it },
                        label = { Text("${AppStrings.salaryBasicLabel.get(lang)} (${salaryCurrency.code})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("salary_basic_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = otHoursInput,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) otHoursInput = it },
                        label = { Text(AppStrings.salaryOtHoursLabel.get(lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("salary_ot_hours_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = otUseMultiplier,
                            onClick = { otUseMultiplier = true },
                            label = { Text(AppStrings.salaryOtModeMultiplier.get(lang)) }
                        )
                        FilterChip(
                            selected = !otUseMultiplier,
                            onClick = { otUseMultiplier = false },
                            label = { Text(AppStrings.salaryOtModeFixed.get(lang)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (otUseMultiplier) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = otMultiplierInput,
                                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) otMultiplierInput = it },
                                label = { Text(AppStrings.salaryOtMultiplierLabel.get(lang)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = stdHoursInput,
                                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) stdHoursInput = it },
                                label = { Text(AppStrings.salaryStdHoursLabel.get(lang)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = otFixedRateInput,
                            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) otFixedRateInput = it },
                            label = { Text("${AppStrings.salaryOtRateLabel.get(lang)} (${salaryCurrency.code}/hr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = allowanceInput,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) allowanceInput = it },
                        label = { Text("${AppStrings.salaryAllowanceLabel.get(lang)} (${salaryCurrency.code})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Monthly Breakdown Result Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.passEmerald.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, colors.passEmerald.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = AppStrings.salaryBreakdownTitle.get(lang),
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.arcCyan
                            )
                            SalaryLine(AppStrings.salaryBasicPay.get(lang), "${formatMoney(basic)} ${salaryCurrency.code}")
                            SalaryLine(
                                "${AppStrings.salaryOtPay.get(lang)} (${formatRate(otHourlyRate)}/hr)",
                                "${formatMoney(otTotal)} ${salaryCurrency.code}"
                            )
                            SalaryLine(AppStrings.salaryAllowancePay.get(lang), "${formatMoney(allowance)} ${salaryCurrency.code}")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = colors.glassBorder)
                            SalaryLine(
                                AppStrings.salaryTotalForeign.get(lang),
                                "${salaryCurrency.symbol} ${formatMoney(totalForeign)} ${salaryCurrency.code}"
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = AppStrings.salaryTotalBdt.get(lang),
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "${bdtInfo.symbol} ${formatMoney(totalBdt)} BDT",
                                style = MaterialTheme.typography.displaySmall,
                                color = colors.passEmerald,
                                modifier = Modifier.testTag("salary_total_bdt_output")
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    pickerTarget?.let { target ->
        CurrencyPickerDialog(
            currencies = allCurrencies,
            lang = lang,
            onSelect = { chosen ->
                when (target) {
                    "FROM" -> fromCode = chosen.code
                    "TO" -> toCode = chosen.code
                    "SALARY" -> salaryCurrencyCode = chosen.code
                }
                pickerTarget = null
            },
            onDismiss = { pickerTarget = null }
        )
    }

    if (showCustomRateDialog) {
        CustomRateDialog(
            fromCode = activeFrom,
            toCode = activeTo,
            currentRate = customPairRate ?: autoRate,
            lang = lang,
            onSave = { custom ->
                onSetCustomRate(activeFrom, activeTo, custom)
                showCustomRateDialog = false
            },
            onReset = {
                onSetCustomRate(activeFrom, activeTo, null)
                showCustomRateDialog = false
            },
            onDismiss = { showCustomRateDialog = false }
        )
    }
}

@Composable
private fun CurrencySelectorRow(
    label: String,
    currency: CurrencyInfo,
    lang: AppLanguage,
    tag: String,
    onClick: () -> Unit
) {
    val colors = WeldTheme.colors
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceElevated.copy(alpha = if (colors.isLight) 1f else 0.78f),
        border = BorderStroke(1.dp, colors.glassBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(currency.flag, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$label • ${currency.code} (${currency.symbol})",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.arcCyan
                )
                Text(
                    text = "${currency.name.get(lang)} — ${currency.country.get(lang)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = colors.textSecondary)
        }
    }
}

@Composable
private fun SalaryLine(label: String, value: String) {
    val colors = WeldTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        Text(value, style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
    }
}

@Composable
private fun CurrencyPickerDialog(
    currencies: List<CurrencyInfo>,
    lang: AppLanguage,
    onSelect: (CurrencyInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = WeldTheme.colors
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, currencies) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) currencies
        else currencies.filter { c ->
            c.code.lowercase().contains(q) ||
                c.name.en.lowercase().contains(q) ||
                c.name.bn.lowercase().contains(q) ||
                c.country.en.lowercase().contains(q) ||
                c.country.bn.lowercase().contains(q)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.arcCyan.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 540.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(AppStrings.currencyPickTitle.get(lang), style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(AppStrings.currencySearchHint.get(lang), color = colors.textMuted) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = colors.arcCyan) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("currency_search_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered, key = { it.code }) { item ->
                        Surface(
                            onClick = { onSelect(item) },
                            shape = RoundedCornerShape(12.dp),
                            color = colors.surfaceElevated.copy(alpha = if (colors.isLight) 1f else 0.6f),
                            border = BorderStroke(1.dp, colors.glassBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("currency_pick_${item.code}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.flag, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${item.code} (${item.symbol}) — ${item.name.get(lang)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.country.get(lang),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(AppStrings.cancel.get(lang), color = colors.textSecondary)
                }
            }
        }
    }
}

@Composable
private fun CustomRateDialog(
    fromCode: String,
    toCode: String,
    currentRate: Double,
    lang: AppLanguage,
    onSave: (Double) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = WeldTheme.colors
    var rateText by remember { mutableStateOf(formatRate(currentRate)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Text("${AppStrings.currencyCustomTitle.get(lang)} (1 $fromCode → $toCode)", color = colors.textPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(AppStrings.currencyCustomHint.get(lang), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) rateText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_rate_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = rateText.toDoubleOrNull()
                    if (parsed != null && parsed > 0.0) onSave(parsed)
                },
                modifier = Modifier.testTag("save_custom_rate_btn")
            ) {
                Text(AppStrings.save.get(lang))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onReset) {
                    Text(AppStrings.currencyResetRate.get(lang), color = colors.moltenAmber)
                }
                TextButton(onClick = onDismiss) {
                    Text(AppStrings.cancel.get(lang), color = colors.textSecondary)
                }
            }
        }
    )
}

private fun formatMoney(value: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return nf.format(value)
}

private fun formatRate(rate: Double): String {
    return if (rate >= 10.0) String.format(Locale.US, "%.2f", rate)
    else if (rate >= 1.0) String.format(Locale.US, "%.4f", rate)
    else String.format(Locale.US, "%.5f", rate)
}
