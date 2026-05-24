package com.quickalert.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quickalert.app.data.AlertRule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditScreen(
    ruleId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: RuleViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var senderPattern by remember { mutableStateOf("") }
    var keywordPattern by remember { mutableStateOf("") }
    var senderMatchType by remember { mutableIntStateOf(AlertRule.MATCH_CONTAINS) }
    var keywordMatchType by remember { mutableIntStateOf(AlertRule.MATCH_CONTAINS) }
    var enabled by remember { mutableStateOf(true) }
    var vibrate by remember { mutableStateOf(true) }
    var isLoaded by remember { mutableStateOf(ruleId == null) }

    LaunchedEffect(ruleId) {
        if (ruleId != null) {
            viewModel.getRuleById(ruleId) { rule ->
                rule?.let {
                    name = it.name
                    senderPattern = it.senderPattern
                    keywordPattern = it.keywordPattern
                    senderMatchType = it.senderMatchType
                    keywordMatchType = it.keywordMatchType
                    enabled = it.enabled
                    vibrate = it.vibrate
                }
                isLoaded = true
            }
        }
    }

    if (!isLoaded) return

    val isEdit = ruleId != null
    val matchTypes = listOf("包含", "精确", "开头", "正则")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "编辑规则" else "新建规则", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 规则名称
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("规则名称") },
                placeholder = { Text("例如: 验证码提醒") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 发送方匹配
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "发送方匹配",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = senderPattern,
                        onValueChange = { senderPattern = it },
                        label = { Text("发送方号码/名称") },
                        placeholder = { Text("留空则匹配所有发送方") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("匹配方式:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        matchTypes.forEachIndexed { index, label ->
                            FilterChip(
                                selected = senderMatchType == index,
                                onClick = { senderMatchType = index },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // 关键词匹配
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "关键词匹配",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = keywordPattern,
                        onValueChange = { keywordPattern = it },
                        label = { Text("关键词") },
                        placeholder = { Text("留空则匹配所有内容") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("匹配方式:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        matchTypes.forEachIndexed { index, label ->
                            FilterChip(
                                selected = keywordMatchType == index,
                                onClick = { keywordMatchType = index },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // 选项
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("启用规则")
                        Switch(
                            checked = enabled,
                            onCheckedChange = { enabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("振动提醒")
                        Switch(
                            checked = vibrate,
                            onCheckedChange = { vibrate = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // 保存按钮
            Button(
                onClick = {
                    val rule = AlertRule(
                        id = ruleId ?: 0,
                        name = name.ifBlank { "未命名规则" },
                        senderPattern = senderPattern,
                        keywordPattern = keywordPattern,
                        senderMatchType = senderMatchType,
                        keywordMatchType = keywordMatchType,
                        enabled = enabled,
                        vibrate = vibrate,
                        updatedAt = System.currentTimeMillis()
                    )
                    viewModel.saveRule(rule) { onNavigateBack() }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() || senderPattern.isNotBlank() || keywordPattern.isNotBlank()
            ) {
                Text("保存规则", modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
