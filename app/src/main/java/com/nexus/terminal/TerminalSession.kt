package com.nexus.terminal

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * Sessão shell real do processo do app.
 * Mantém o mesmo /system/bin/sh vivo para preservar cd, variáveis e histórico
 * durante a sessão. Isso ainda usa pipes, não PTY; programas de tela cheia
 * exigem a camada nativa de pseudo-terminal planejada para a próxima etapa.
 */
class TerminalSession(
    private val onOutput: (String) -> Unit,
    private val onCommandDone: () -> Unit,
    private val onExit: () -> Unit
) {
    private var process: Process? = null
    private var writer: OutputStreamWriter? = null
    private var readerThread: Thread? = null
    private val started = AtomicBoolean(false)
    private val marker = "__NEXUS_COMMAND_DONE_7F3A__"

    @Synchronized
    fun start() {
        if (started.get()) return
        try {
            val shell = ProcessBuilder("/system/bin/sh")
                .redirectErrorStream(true)
                .start()
            process = shell
            writer = OutputStreamWriter(shell.outputStream)
            started.set(true)
            readerThread = thread(name = "nexus-shell-reader", isDaemon = true) {
                try {
                    BufferedReader(InputStreamReader(shell.inputStream)).useLines { lines ->
                        lines.forEach { line ->
                            if (line == marker) onCommandDone() else onOutput("$line\n")
                        }
                    }
                } finally {
                    started.set(false)
                    onExit()
                }
            }
            sendRaw("printf 'NEXUS_SHELL_READY\\n'")
        } catch (error: Exception) {
            onOutput("Falha ao iniciar shell: ${error.message}\n")
            onExit()
        }
    }

    @Synchronized
    fun execute(command: String) {
        if (command.isBlank()) return
        if (!started.get()) start()
        sendRaw(command)
        sendRaw("printf '$marker\\n'")
    }

    @Synchronized
    private fun sendRaw(command: String) {
        try {
            writer?.apply { write(command); write("\n"); flush() }
        } catch (error: Exception) {
            onOutput("Erro ao enviar comando: ${error.message}\n")
        }
    }

    @Synchronized
    fun interrupt() {
        // Sem PTY, o sinal mais seguro é encerrar a sessão do processo atual.
        // A próxima execução cria automaticamente uma nova sessão limpa.
        process?.destroy()
        process = null
        writer = null
        started.set(false)
    }

    @Synchronized
    fun close() {
        process?.destroy()
        process = null
        writer = null
        started.set(false)
    }

    fun isRunning(): Boolean = started.get()
}
