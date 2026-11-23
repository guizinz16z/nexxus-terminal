package com.nexus.terminal

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Navy = Color(0xFF080B16)
private val Panel = Color(0xFF11182A)
private val PanelAlt = Color(0xFF17213A)
private val Violet = Color(0xFF8B5CF6)
private val Cyan = Color(0xFF38BDF8)
private val Muted = Color(0xFF93A1C2)

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
            primary = Violet, secondary = Cyan, background = Navy, surface = Panel,
            onBackground = Color(0xFFE9EDFF), onSurface = Color(0xFFE9EDFF)
        ), content = content
    )
}

private enum class Tab(val label: String) {
    HOME("Início"), TERMINAL("Terminal"), FILES("Arquivos"), EDITOR("Editor"), SETTINGS("Ajustes")
}

@Composable
private fun NexusApp(context: Context) {
    var selected by rememberSaveable { mutableStateOf(Tab.HOME.name) }
    val tab = Tab.valueOf(selected)
    Scaffold(
        containerColor = Navy,
        bottomBar = {
            NavigationBar(containerColor = Panel, modifier = Modifier.navigationBarsPadding()) {
                listOf(Tab.HOME, Tab.TERMINAL, Tab.FILES, Tab.EDITOR, Tab.SETTINGS).forEach { item ->
                    NavigationBarItem(
                        selected = tab == item, onClick = { selected = item.name },
                        icon = { Icon(tabIcon(item), contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 9.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            AnimatedContent(targetState = tab, label = "navigation", transitionSpec = {
                (fadeIn(tween(180)) + slideInHorizontally(tween(220)) { it / 8 }) togetherWith
                    (fadeOut(tween(120)) + slideOutHorizontally(tween(160)) { -it / 8 })
            }) { destination ->
                when (destination) {
                    Tab.HOME -> Dashboard(onTerminal = { selected = Tab.TERMINAL.name }, onFiles = { selected = Tab.FILES.name }, onEditor = { selected = Tab.EDITOR.name })
                    Tab.TERMINAL -> TerminalScreen()
                    Tab.FILES -> FilesScreen(context)
                    Tab.EDITOR -> EditorScreen(context)
                    Tab.SETTINGS -> SettingsScreen()
                }
            }
        }
    }
}

private fun tabIcon(tab: Tab) = when (tab) {
    Tab.HOME -> Icons.Default.Home
    Tab.TERMINAL -> Icons.Default.Terminal
    Tab.FILES -> Icons.Default.Folder
    Tab.EDITOR -> Icons.Default.Code
    Tab.SETTINGS -> Icons.Default.Settings
}

@Composable
private fun Dashboard(onTerminal: () -> Unit, onFiles: () -> Unit, onEditor: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Spacer(Modifier.height(18.dp))
            Text("NEXUS", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Text("TERMINAL", color = Violet, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 4.sp)
            Spacer(Modifier.height(18.dp))
            Text("Boa noite, desenvolvedor", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text("Seu ambiente está pronto para criar.", color = Muted, fontSize = 14.sp)
        }
        item { StatusCard() }
        item {
            Text("Acesso rápido", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                QuickAction("Terminal", Icons.Default.Terminal, onTerminal, Modifier.weight(1f))
                QuickAction("Arquivos", Icons.Default.Folder, onFiles, Modifier.weight(1f))
                QuickAction("Editor", Icons.Default.Code, onEditor, Modifier.weight(1f))
            }
        }
        item { SectionTitle("Projetos recentes") }
        item { ProjectRow("nexus-scripts", "Pronto para editar", Cyan) }
        item { ProjectRow("automacao-api", "Importe uma pasta para começar", Violet) }
        item { SectionTitle("Atividade") }
        item { Text("As execuções e alterações aparecerão aqui.", color = Muted, fontSize = 13.sp) }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun StatusCard() {
    Card(colors = CardDefaults.cardColors(containerColor = PanelAlt), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(Color(0xFF4ADE80), RoundedCornerShape(5.dp)))
                Spacer(Modifier.width(8.dp)); Text("AMBIENTE ONLINE", color = Color(0xFF86EFAC), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Metric("Shell", "Disponível"); Metric("Python", "Em integração"); Metric("Projetos", "SAF pronto")
            }
        }
    }
}

@Composable private fun Metric(label: String, value: String) { Column { Text(label, color = Muted, fontSize = 11.sp); Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium) } }

@Composable
private fun QuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Card(modifier.clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(vertical = 15.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Violet, modifier = Modifier.size(24.dp)); Spacer(Modifier.height(7.dp)); Text(label, color = Color.White, fontSize = 12.sp)
        }
    }
}

@Composable private fun SectionTitle(text: String) { Text(text, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, modifier = Modifier.padding(top = 4.dp)) }

@Composable
private fun ProjectRow(name: String, time: String, accent: Color) {
    Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Code, null, tint = accent, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { Text(name, color = Color.White, fontWeight = FontWeight.Medium); Text(time, color = Muted, fontSize = 12.sp) }
        Icon(Icons.Default.MoreVert, null, tint = Muted, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun TerminalScreen() {
    var command by rememberSaveable { mutableStateOf("echo NEXUS TERMINAL") }
    var output by rememberSaveable { mutableStateOf("NEXUS shell\nDigite um comando para executar no shell disponível do Android.\n") }
    val history = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()
    var running by remember { mutableStateOf(false) }
    var process by remember { mutableStateOf<Process?>(null) }
    var job by remember { mutableStateOf<Job?>(null) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) { Text("Terminal", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("Shell Android • saída real", color = Muted, fontSize = 12.sp) }
            AssistChip(onClick = { output = "NEXUS shell\n" }, label = { Text("Limpar") }, leadingIcon = { Icon(Icons.Default.Refresh, null) })
        }
        Spacer(Modifier.height(12.dp))
        Surface(Modifier.weight(1f).fillMaxWidth(), color = Color(0xFF05070D), shape = RoundedCornerShape(16.dp)) {
            LazyColumn(Modifier.padding(14.dp)) { item { Text(output, color = Color(0xFFB7F7D0), fontFamily = FontFamily.Monospace, fontSize = 13.sp) } }
        }
        Spacer(Modifier.height(10.dp))
        if (history.isNotEmpty()) {
            Text("Histórico: ${history.takeLast(3).joinToString("  •  ")}", color = Muted, fontSize = 11.sp, maxLines = 1)
            Spacer(Modifier.height(5.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = command, onValueChange = { command = it }, modifier = Modifier.weight(1f), singleLine = true, label = { Text("Comando") })
            Spacer(Modifier.width(6.dp))
            IconButton(onClick = {
                if (running) { process?.destroy(); job?.cancel(); running = false }
                else {
                    val requested = command.trim(); if (requested.isEmpty()) return@IconButton
                    history.add(requested); running = true
                    job = scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            try {
                                val p = ProcessBuilder("/system/bin/sh", "-c", requested).redirectErrorStream(true).start(); process = p
                                val text = p.inputStream.bufferedReader().readText(); p.waitFor(); text.ifBlank { "(sem saída)" }
                            } catch (e: Exception) { "Erro: ${e.message}" }
                        }
                        output += "\n$ $requested\n$result\n"; process = null; running = false
                    }
                }
            }) { Icon(if (running) Icons.Default.Stop else Icons.Default.PlayArrow, if (running) "Parar" else "Executar", tint = if (running) Color(0xFFF87171) else Violet) }
        }
    }
}

@Composable
private fun FilesScreen(context: Context) {
    val preferences = remember { context.getSharedPreferences("nexus", Context.MODE_PRIVATE) }
    var rootUri by rememberSaveable { mutableStateOf(preferences.getString("root_uri", null)) }
    var files by remember { mutableStateOf<List<DocumentFile>>(emptyList()) }
    var message by remember { mutableStateOf("Escolha uma pasta para começar.") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            preferences.edit().putString("root_uri", uri.toString()).apply(); rootUri = uri.toString(); message = "Pasta vinculada com sucesso."
        }
    }
    fun refresh() { files = rootUri?.let { DocumentFile.fromTreeUri(context, Uri.parse(it))?.listFiles()?.sortedBy { file -> file.name?.lowercase() } ?: emptyList() } ?: emptyList() }
    LaunchedEffect(rootUri) { refresh() }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) { Text("Arquivos", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("Armazenamento seguro do Android", color = Muted, fontSize = 13.sp) }
            IconButton(onClick = { launcher.launch(null) }) { Icon(Icons.Default.FolderOpen, "Escolher pasta", tint = Violet) }
        }
        Spacer(Modifier.height(12.dp))
        if (rootUri == null) {
            EmptyFilesCard(onChoose = { launcher.launch(null) })
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { refresh() }, label = { Text("Atualizar") }, leadingIcon = { Icon(Icons.Default.Refresh, null) })
                AssistChip(onClick = { DocumentFile.fromTreeUri(context, Uri.parse(rootUri))?.createDirectory("novo-projeto"); refresh() }, label = { Text("Nova pasta") }, leadingIcon = { Icon(Icons.Default.Add, null) })
                AssistChip(onClick = { DocumentFile.fromTreeUri(context, Uri.parse(rootUri))?.createFile("text/plain", "novo_arquivo.txt"); refresh() }, label = { Text("Novo arquivo") }, leadingIcon = { Icon(Icons.Default.Description, null) })
            }
            Spacer(Modifier.height(10.dp)); Text(message, color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp)); Divider(color = Color(0xFF26304A)); Spacer(Modifier.height(4.dp))
            if (files.isEmpty()) Text("Pasta vazia. Crie um projeto ou arquivo acima.", color = Muted, modifier = Modifier.padding(top = 18.dp))
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) { items(files, key = { it.uri.toString() }) { file -> FileRow(file) } }
        }
    }
}

@Composable private fun EmptyFilesCard(onChoose: () -> Unit) { Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(20.dp)) { Icon(Icons.Default.Folder, null, tint = Violet, modifier = Modifier.size(32.dp)); Spacer(Modifier.height(10.dp)); Text("Nenhuma pasta vinculada", color = Color.White, fontWeight = FontWeight.SemiBold); Text("O Android pedirá acesso somente à pasta que você escolher.", color = Muted, fontSize = 13.sp); Spacer(Modifier.height(14.dp)); Button(onClick = onChoose) { Text("Escolher pasta") } } } }

@Composable private fun FileRow(file: DocumentFile) { Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(13.dp)).padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description, null, tint = if (file.isDirectory) Cyan else Violet, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(file.name ?: "sem nome", color = Color.White, fontWeight = FontWeight.Medium); Text(if (file.isDirectory) "Pasta" else "Arquivo", color = Muted, fontSize = 11.sp) }; Icon(Icons.Default.MoreVert, null, tint = Muted) } }

@Composable
private fun EditorScreen(context: Context) {
    val preferences = remember { context.getSharedPreferences("nexus", Context.MODE_PRIVATE) }
    val rootUri = preferences.getString("root_uri", null)
    var fileName by rememberSaveable { mutableStateOf("main.py") }
    var code by rememberSaveable { mutableStateOf("# NEXUS TERMINAL\n\nprint(\"Olá, mundo!\")\n") }
    var saved by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) { Text("Editor", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("Edição rápida otimizada para celular", color = Muted, fontSize = 12.sp) }
            IconButton(onClick = {
                val root = rootUri?.let { DocumentFile.fromTreeUri(context, Uri.parse(it)) }
                if (root != null) {
                    root.findFile(fileName)?.delete()
                    val target = root.createFile("text/x-python", fileName)
                    if (target != null) context.contentResolver.openOutputStream(target.uri)?.use { it.write(code.toByteArray()) }
                    saved = true
                }
            }) { Icon(Icons.Default.Save, "Salvar", tint = if (saved) Color(0xFF4ADE80) else Violet) }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(value = fileName, onValueChange = { fileName = it; saved = false }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Nome do arquivo") })
        Spacer(Modifier.height(10.dp))
        Surface(Modifier.weight(1f).fillMaxWidth(), color = Color(0xFF05070D), shape = RoundedCornerShape(16.dp)) {
            BasicTextField(value = code, onValueChange = { code = it; saved = false }, textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFFE5E7EB), fontFamily = FontFamily.Monospace, fontSize = 14.sp, lineHeight = 21.sp), modifier = Modifier.padding(14.dp).fillMaxSize())
        }
        Spacer(Modifier.height(8.dp)); Text(if (rootUri == null) "Vincule uma pasta na tela Arquivos para salvar." else if (saved) "Arquivo salvo com sucesso." else "Alterações locais não salvas", color = if (saved) Color(0xFF86EFAC) else Muted, fontSize = 12.sp)
    }
}

@Composable
private fun SettingsScreen() {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Configurações", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Personalize o ambiente sem perder simplicidade.", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(18.dp))
        SettingsRow("Tema visual", "Escuro Nexus ativo", Icons.Default.Tune)
        SettingsRow("Execução", "Confirmação antes de ações destrutivas", Icons.Default.PlayArrow)
        SettingsRow("Armazenamento", "Acesso controlado por pasta", Icons.Default.Folder)
        Spacer(Modifier.height(18.dp)); AssistChip(onClick = {}, label = { Text("NEXUS TERMINAL 0.2.0") }, leadingIcon = { Icon(Icons.Default.Code, null) })
    }
}

@Composable private fun SettingsRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) { Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(15.dp)).padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Violet, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(12.dp)); Column { Text(title, color = Color.White, fontWeight = FontWeight.Medium); Text(subtitle, color = Muted, fontSize = 12.sp) } } }
