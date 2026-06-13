param(
    [ValidateSet("mermaid", "plantuml")]
    [string]$Format = "mermaid",
    [string]$DslPath = "",
    [string]$OutputDir = ""
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$designDir = Join-Path $repoRoot "docs\design"
$syncScript = Join-Path $PSScriptRoot "sync-structurizr-workspace.ps1"

if ([string]::IsNullOrWhiteSpace($DslPath)) {
    $DslPath = Join-Path $designDir "structurizr-backend-android.dsl"
}

if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = Join-Path $designDir "generated\structurizr\$Format"
}

$workspaceDsl = Join-Path $designDir "workspace.dsl"
& $syncScript -SourceDsl $DslPath -WorkspaceDsl $workspaceDsl

if (-not (Test-Path -LiteralPath $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

$resolvedWorkspaceDsl = [System.IO.Path]::GetFullPath($workspaceDsl)
$resolvedOutputDir = [System.IO.Path]::GetFullPath($OutputDir)
$resolvedRepoRoot = [System.IO.Path]::GetFullPath($repoRoot)

$structurizr = Get-Command structurizr -ErrorAction SilentlyContinue
if ($null -ne $structurizr) {
    Write-Host "Exporting with local Structurizr CLI..."
    & structurizr export -workspace $resolvedWorkspaceDsl -format $Format -output $resolvedOutputDir
    if ($LASTEXITCODE -ne 0) {
        throw "Structurizr CLI export failed."
    }
} else {
    $docker = Get-Command docker -ErrorAction SilentlyContinue
    if ($null -eq $docker) {
        throw "Neither 'structurizr' nor 'docker' was found in PATH. Install one of them to export diagrams."
    }

    $null = & docker info 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "Docker is installed but not usable from the current shell. Make sure Docker Desktop is running and that your account can access the Docker daemon."
    }

    Write-Host "Exporting with Structurizr CLI in Docker..."
    & docker run --rm -v "${resolvedRepoRoot}:/workspace" -w /workspace structurizr/cli export -workspace "docs/design/workspace.dsl" -format $Format -output ("docs/design/generated/structurizr/" + $Format)
    if ($LASTEXITCODE -ne 0) {
        throw "Structurizr CLI export in Docker failed."
    }
}

Write-Host "Export complete:"
Write-Host "  format: $Format"
Write-Host "  output: $resolvedOutputDir"
