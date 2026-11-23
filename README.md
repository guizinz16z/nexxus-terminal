# NEXUS TERMINAL

Ambiente Android de desenvolvimento com dashboard, terminal, arquivos, projetos e preparação para runtime Python.

## Estado atual

A primeira etapa contém a base nativa Android em Kotlin/Compose, tema visual, navegação inferior, dashboard com status real, terminal shell básico e tela de arquivos via SAF. Recursos que dependem de PTY nativo, Python embarcado, Git executável ou dispositivo físico permanecem sinalizados como etapas posteriores.

## Compilar no GitHub Actions

1. Crie um repositório no GitHub e envie este diretório.
2. O workflow em `.github/workflows/android.yml` instala JDK, Android SDK e executa `./gradlew assembleDebug`.
3. Baixe o artefato `nexus-terminal-debug-apk` na execução concluída.

O APK só deve ser considerado pronto quando o workflow terminar com sucesso. Este sandbox não tem Android SDK instalado para afirmar uma compilação local.

## Compilação local

Requer JDK compatível, Android SDK com plataforma 35 e build-tools, além do wrapper Gradle incluído. No AndCode, abra a pasta como projeto Gradle e sincronize as dependências.
