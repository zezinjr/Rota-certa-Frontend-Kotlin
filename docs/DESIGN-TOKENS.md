# 🎨 Design tokens — painel web ↔ app Android

O app do colaborador usa **as mesmas configurações visuais** do painel Angular.
Todos os valores estão centralizados em
[`ui/theme/Theme.kt`](../app/src/main/java/br/com/rotacerta/colaborador/ui/theme/Theme.kt) (`RcColors`),
e cada um aponta para o arquivo SCSS de origem. **Ao mudar uma cor no web, mude aqui também.**

![Comparativo](screenshots/00-comparativo-web-app.png)

## Cores

| Token Kotlin | Valor | Origem no Angular | Uso |
|---|---|---|---|
| `Topbar` | `#1e40af` | `app-menu.component.scss` `.topbar`, `.menu-item.active` | Barra superior, item ativo da navegação |
| `TopbarText` | `#dbeafe` | `.topbar-menu button` | Texto claro sobre a topbar |
| `TopbarSubtle` | `#bfdbfe` | `.user-role` | Papel do usuário ("Colaborador") |
| `Accent` | `#3b82f6` | `.logo-dot`, `.avatar`, `.route-item .line`, `.map-point.blue` | Logo, avatar, linhas/pontos da rota, progresso |
| `Background` | `#0f172a` | `.layout-container`, `.content`, `.vehicle-card`, `.fake-map` | Fundo do app, cartões internos, mapa |
| `Panel` | `#1e2937` | `.panel`, `.stat-card`, grade do `.fake-map` | Painéis e cards de estatística |
| `Divider` | `#334155` | `.alert-item` border | Divisórias |
| `TextStrong` | `#f1f5f9` | `.page-header h1`, `.stat-card h2` | Títulos e números |
| `Text` | `#e2e8f0` | `.content`, `.panel` | Texto padrão |
| `TextMenu` | `#cbd5e1` | `.menu-item` | Itens de navegação inativos |
| `TextLabel` | `#94a3b8` | `.stat-card span`, `.progress-label` | Rótulos |
| `TextMuted` | `#64748b` | `.menu-title`, `.date`, `small` | Legendas, datas |
| `SuccessBg` / `Success` | `#166534` / `#4ade80` | `.badge.success`, `.green` | "em rota", "confirmado" |
| `WarningBg` / `Warning` | `#854d0e` / `#facc15` | `.badge.warning`, `.map-point.yellow` | "atrasado", "pendente", seu ponto no mapa |
| `DangerBg` / `Danger` | `#7f1d1d` / `#f87171` | *(sem equivalente; escala Tailwind)* | "ausente" |
| `MatPrimary` | `#3f51b5` | tema `indigo-pink` (`mat-raised-button color="primary"`) | Botões primários, foco dos campos |
| `MatWarn` | `#f44336` | tema `indigo-pink` (`color="warn"`) | Botões de sair/excluir, erros |
| `CardWhite` / `CardTitle` / `CardText` | `#fff` / `#111827` / `#374151` | `mat-card` dos formulários (`employee-detail`, `list-card`) | Formulários (login, senha) |

## Tipografia, ícones e formas

- **Fonte:** Roboto (o `index.html` carrega Roboto 300/400/500; no Android é a fonte padrão).
- **Ícones:** Material Icons **preenchidos** com os mesmos nomes do menu (`directions_bus`, `route`, `schedule`, `business`, `settings`...).
- **Tamanhos:** título de página 28sp/600 (`h1 1.75rem`), título de painel 17sp/600 (`h3 1.05rem`), número de estatística 26sp/700, badge 12sp/500, título de seção 11sp caixa alta (`.menu-title`).
- **Raios:** botões e `mat-card` 4dp · `.menu-item` 8dp · `.vehicle-card`/mapa 10dp · `.panel`/`.stat-card` 12dp · `.badge` 20dp (pílula).
- **Topbar:** 64dp, sombra, ponto azul de 10dp + "Rotacerta" 20sp/600, nome + papel + avatar de 36dp.
- **Tema único escuro**, igual ao painel (o app não alterna para claro).

## Componentes equivalentes

| Painel web | App (Compose) |
|---|---|
| `.topbar` | `RcTopBar` |
| `.menu-item` (sidebar) | `MenuItem` (navegação inferior) |
| `.page-header` | `PageHeader` |
| `.panel` | `Panel` |
| `.stat-card` | `StatCard` |
| `.vehicle-card` + `mat-progress-bar` | `InnerCard` + `ProgressRow` |
| `.badge.success/.warning` | `Badge(kind = SUCCESS/WARNING/…)` |
| `.alert-item` | `AlertItem` |
| `.fake-map` | `RouteMap` (com as coordenadas reais) |
| `mat-raised-button` / `mat-stroked-button` | `RcButton` / `RcStrokedButton` |
| `mat-card` + `mat-form-field` | `FormCard` + `FormField` |
| `DialogComponent` (confirmação) | `ConfirmDialog` |
| `MatSnackBar` | `SnackbarHost` (fundo `#323232`) |
