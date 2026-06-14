# Structurizr Local Viewing Guide

This project includes helper scripts for two local viewing workflows:

- interactive web preview with Structurizr local viewer
- IDE-friendly export to Mermaid or PlantUML

## Files

- DSL source: `docs/design/structurizr-backend-android.dsl`
- synchronized Lite workspace: `docs/design/workspace.dsl`
- local viewer startup script: `scripts/run-structurizr-lite.ps1`
- export script: `scripts/export-structurizr-diagrams.ps1`

## 1. Interactive Web Preview

Use this when you want the closest experience to the native Structurizr viewer.

```powershell
.\scripts\run-structurizr-lite.ps1
```

If PowerShell execution policy blocks `.ps1`, use either of these instead:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-structurizr-lite.ps1
```

```cmd
.\scripts\run-structurizr-lite.cmd
```

Then open:

```text
http://localhost:8081
```

Notes:

- The script first copies `structurizr-backend-android.dsl` to `docs/design/workspace.dsl`
- The Docker container stays in the foreground; keep that terminal open while you browse
- Port `8081` is used by default to avoid colliding with the backend on `8080`
- You can change the port if needed:

```powershell
.\scripts\run-structurizr-lite.ps1 -Port 8090
```

### Common failure on Windows

If you see errors like `open //./pipe/docker_engine: Access is denied` or Docker config access errors:

- make sure Docker Desktop is running
- restart the terminal after Docker Desktop starts
- make sure your Windows account has permission to access Docker Desktop and the Docker daemon
- if needed, start Docker Desktop once with elevated privileges and then retry from a fresh terminal

The helper scripts now fail fast when Docker is installed but not actually usable from the current shell.

## 2. Export for VS Code or IntelliJ IDEA

Use this when you prefer to stay inside the editor.

### Mermaid export

```powershell
.\scripts\export-structurizr-diagrams.ps1 -Format mermaid
```

If PowerShell execution policy blocks `.ps1`, use:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\export-structurizr-diagrams.ps1 -Format mermaid
```

or:

```cmd
.\scripts\export-structurizr-diagrams.cmd -Format mermaid
```

Generated files will be placed under:

```text
docs/design/generated/structurizr/mermaid
```

Recommended usage:

- VS Code: open the generated Mermaid file with any Mermaid preview extension
- IntelliJ IDEA: open the generated Mermaid file with Mermaid support enabled
- if export fails immediately, check Docker availability first with `docker info`

### PlantUML export

```powershell
.\scripts\export-structurizr-diagrams.ps1 -Format plantuml
```

If PowerShell execution policy blocks `.ps1`, use:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\export-structurizr-diagrams.ps1 -Format plantuml
```

or:

```cmd
.\scripts\export-structurizr-diagrams.cmd -Format plantuml
```

Generated files will be placed under:

```text
docs/design/generated/structurizr/plantuml
```

Recommended usage:

- IntelliJ IDEA: open the generated `.puml` files with PlantUML support
- VS Code: open the generated `.puml` files with any PlantUML preview extension
- if export fails immediately, check Docker availability first with `docker info`

## 3. When to Use Which

- Use `run-structurizr-lite.ps1` when you want the best interactive Structurizr experience
- Use Mermaid export when your team mainly reads architecture in VS Code
- Use PlantUML export when your team mainly reads diagrams in IntelliJ IDEA

## 4. Current Recommendation

This repository treats the Structurizr local web viewer as the primary viewer. Mermaid and PlantUML export are secondary convenience paths for editor-local preview.
