package com.nexus.terminal

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Navy = Color(0xFF080B16)
private val Panel = Color(0xFF11182A)
private val PanelAlt = Color(0xFF17213A)
private val Violet = Color(0xFF8B5CF6)
private val Cyan = Color(0xFF38BDF8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NexusTheme { NexusApp(this) } }
    }
}

@Composable
private fun NexusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme(
            primary = Violet,
            secondary = Cyan,
            background = Navy,
            surface = Panel,
            onBackground = Color(0xFFE9EDFF),
            onSurface = Color(0xFFE9EDFF)
        ),
        content = content
    )
}

private enum class Tab(val label: String) { HOME("Início"), TERMINAL("Terminal"), FILES("Arquivos"), SETTINGS("Configurações") }

@Composable
private fun NexusApp(context: Context) {
    var selected by remember { mutableStateOf(Tab.HOME) }
    Scaffold(
        containerColor = Navy,
        bottomBar = {
            NavigationBar(containerColor = Panel, modifier = Modifier.navigationBarsPadding()) {
                Tab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selected == tab,
                        onClick = { selected = tab },
                        icon = { Icon(tabIcon(tab), contentDescription = tab.label) },
                        label = { Text(tab.label, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (selected) {
                Tab.HOME -> Dashboard(onTerminal = { selected = Tab.TERMINAL }, onFiles = { selected = Tab.FILES })
                Tab.TERMINAL -> TerminalScreen(context)
                Tab.FILES -> FilesScreen()
                Tab.SETTINGS -> SettingsScreen()
            }
        }
    }
}

private fun tabIcon(tab: Tab) = when (tab) {
    Tab.HOME -> Icons.Default.Home
    Tab.TERMINAL -> Icons.Default.Terminal
    Tab.FILES -> Icons.Default.Folder
    Tab.SETTINGS -> Icons.Default.Settings
}

@Composable
private fun Dashboard(onTerminal: () -> Unit, onFiles: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Spacer(Modifier.height(20.dp))
            Text("NEXUS", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Text("TERMINAL", color = Violet, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 4.sp)
            Spacer(Modifier.height(20.dp))
            Text("Boa noite, desenvolvedor", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text("Seu ambiente está pronto para criar.", color = Color(0xFFAAB4D0), fontSize = 14.sp)
        }
        item { StatusCard() }
        item {
            Text("Acesso rápido", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                QuickAction("Terminal", Icons.Default.Terminal, onTerminal, Modifier.weight(1f))
                QuickAction("Arquivos", Icons.Default.Folder, onFiles, Modifier.weight(1f))
                QuickAction("Editor", Icons.Default.Code, {}, Modifier.weight(1f))
            }
        }
        item { SectionTitle("Projetos recentes") }
        items(listOf("nexus-scripts" to "Atualizado agora", "automacao-api" to "Há 2 horas")) { (name, time) -> ProjectRow(name, time) }
        item { SectionTitle("Atividade") }
        item { Text("Nenhuma execução registrada nesta sessão.", color = Color(0xFF8F9BB8), fontSize = 13.sp) }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun StatusCard() {
    Card(colors = CardDefaults.cardColors(containerColor = PanelAlt), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(Color(0xFF4ADE80), RoundedCornerShape(5.dp)))
                Spacer(Modifier.width(8.dp))
                Text("AMBIENTE ONLINE", color = Color(0xFF86EFAC), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Metric("Runtime", "Android shell")
                Metric("Python", "A validar")
                Metric("Projetos", "0 locais")
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column { Text(label, color = Color(0xFF93A1C2), fontSize = 11.sp); Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
}

@Composable
private fun QuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Card(modifier.clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(vertical = 15.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Violet, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(7.dp)); Text(label, color = Color.White, fontSize = 12.sp)
        }
    }
}

@Composable private fun SectionTitle(text: String) { Text(text, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, modifier = Modifier.padding(top = 4.dp)) }

@Composable
private fun ProjectRow(name: String, time: String) {
    Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Code, null, tint = Cyan, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { Text(name, color = Color.White, fontWeight = FontWeight.Medium); Text(time, color = Color(0xFF8F9BB8), fontSize = 12.sp) }
        Icon(Icons.Default.PlayArrow, null, tint = Violet, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun TerminalScreen(context: Context) {
    var command by remember { mutableStateOf("echo NEXUS TERMINAL") }
    var output by remember { mutableStateOf("NEXUS shell\nDigite um comando para executar no shell disponível do Android.\n") }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Terminal", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Shell do sistema • PTY avançado será integrado na próxima etapa", color = Color(0xFF93A1C2), fontSize = 12.sp)
        Spacer(Modifier.height(14.dp))
        Surface(Modifier.weight(1f).fillMaxWidth(), color = Color(0xFF05070D), shape = RoundedCornerShape(16.dp)) {
            Text(output, color = Color(0xFFB7F7D0), fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 13.sp, modifier = Modifier.padding(14.dp))
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.OutlinedTextField(value = command, onValueChange = { command = it }, modifier = Modifier.weight(1f), singleLine = true, label = { Text("Comando") })
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = {
                val requested = command
                scope.launch {
                    val result = withContext(Dispatchers.IO) { runShell(requested) }
                    output += "\n$ $requested\n$result\n"
                }
            }) { Text("Executar") }
        }
    }
}

private fun runShell(command: String): String = try {
    val process = ProcessBuilder("/system/bin/sh", "-c", command).redirectErrorStream(true).start()
    val text = process.inputStream.bufferedReader().readText()
    process.waitFor()
    text.ifBlank { "(sem saída)" }
} catch (error: Exception) { "Erro: ${error.message}" }

@Composable
private fun FilesScreen() {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Arquivos", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Acesso controlado pelo seletor seguro do Android (SAF).", color = Color(0xFF93A1C2), fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(18.dp)) { Icon(Icons.Default.Folder, null, tint = Violet); Spacer(Modifier.height(10.dp)); Text("Nenhuma pasta de projeto vinculada", color = Color.White, fontWeight = FontWeight.SemiBold); Text("Escolha uma pasta para começar a gerenciar seus projetos.", color = Color(0xFF9CA9C4), fontSize = 13.sp) }
        }
    }
}

@Composable
private fun SettingsScreen() {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Configurações", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        AssistChip(onClick = {}, label = { Text("Tema escuro ativo") }, leadingIcon = { Icon(Icons.Default.Memory, null) })
        Spacer(Modifier.height(10.dp)); Text("Versão 0.1.0 • base inicial", color = Color(0xFF93A1C2), fontSize = 13.sp)
    }
}
