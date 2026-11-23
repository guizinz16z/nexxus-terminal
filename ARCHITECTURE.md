# NEXUS TERMINAL — Arquitetura inicial

## Resultado da auditoria

Este projeto está sendo iniciado em um sandbox Linux, não dentro do AndCode do celular. O ambiente auditado possui OpenJDK 21, Python 3, Node.js e Git, mas não possui Android SDK, Gradle, adb ou Kotlin instalados. Portanto, a compilação local não pode ser declarada neste momento. O projeto foi estruturado para compilação reproduzível no GitHub Actions e para abertura posterior no AndCode.

## Decisões

- **UI:** Kotlin, Jetpack Compose e Material 3, com tema escuro próprio.
- **Arquitetura:** módulos lógicos por domínio dentro do app: dashboard, terminal, arquivos, projetos, Python e configurações. O primeiro corte usa uma Activity única e ViewModels simples; a separação de interfaces permite trocar implementações sem reescrever a UI.
- **Persistência:** DataStore para preferências e Room como próxima etapa para histórico, projetos recentes e sessões.
- **Arquivos:** Storage Access Framework (SAF), usando `ACTION_OPEN_DOCUMENT_TREE`, `ACTION_OPEN_DOCUMENT` e `ACTION_CREATE_DOCUMENT`. O app não pede acesso irrestrito ao armazenamento sem necessidade.
- **Terminal:** executor de processos isolado. A primeira implementação executa o shell que existir no sistema Android e captura stdout/stderr. Isso não equivale a um rootfs Linux ou ao Termux. PTY, sinais e teclas especiais serão adicionados por uma implementação nativa compatível, com fallback explícito quando indisponível.
- **Python:** interface `PythonRuntime` separada da UI. Python no Android precisa ser embarcado; não existe um `python` de sistema que qualquer app possa chamar. A integração Chaquopy ou um runtime CPython empacotado será validada em etapa própria, especialmente para wheels com extensões nativas.
- **Git:** inicialmente via processo externo somente quando o binário estiver disponível; credenciais deverão usar Android Keystore e nunca entrar no código-fonte. Integração completa será adicionada após validar a estratégia de executáveis no Android.
- **Backup:** exportação/importação ZIP com confirmação e SAF, sem sobrescrever silenciosamente.

## Fases entregáveis

1. Base Gradle, identidade visual, dashboard e navegação.
2. Arquivos e projetos via SAF, com criação e abertura controladas.
3. Terminal funcional com shell disponível, histórico e cancelamento; depois PTY nativo.
4. Editor de texto com abas, busca e persistência.
5. Runtime Python embarcado e gerenciamento de pacotes compatíveis.
6. Git, ferramentas e backup.
7. Testes em dispositivo, hardening de permissões e workflow de release.

## Limitações declaradas

O NEXUS TERMINAL não poderá prometer compatibilidade completa com Linux, Bash ou pip. A disponibilidade depende da versão do Android, ABI do dispositivo, permissões, binários empacotados e das bibliotecas Python que tenham wheels Android compatíveis. O app deve exibir o estado real de cada capacidade.

## Fontes técnicas

- [1] https://docs.python.org/3/using/android.html — Python no Android usa modo embarcado e tem restrições de biblioteca.
- [2] https://developer.android.com/guide/topics/providers/document-provider — Storage Access Framework e permissões persistentes por documento.
- [3] https://chaquo.com/chaquopy/ — integração de Python com Gradle e Kotlin/Java.
- [4] https://github.com/termux/termux-app — referência de terminal Android; o app não incorpora automaticamente o ambiente do Termux.
- [5] https://github.com/android-actions/setup-android — configuração de SDK Android no GitHub Actions.
