param(
    [string]$SourceDsl = "",
    [string]$WorkspaceDsl = ""
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$designDir = Join-Path $repoRoot "docs\design"

if ([string]::IsNullOrWhiteSpace($SourceDsl)) {
    $SourceDsl = Join-Path $designDir "structurizr-backend-android.dsl"
}

if ([string]::IsNullOrWhiteSpace($WorkspaceDsl)) {
    $WorkspaceDsl = Join-Path $designDir "workspace.dsl"
}

$resolvedSource = [System.IO.Path]::GetFullPath($SourceDsl)
$resolvedWorkspace = [System.IO.Path]::GetFullPath($WorkspaceDsl)

if (-not (Test-Path -LiteralPath $resolvedSource)) {
    throw "Structurizr source DSL not found: $resolvedSource"
}

$workspaceParent = Split-Path -Parent $resolvedWorkspace
if (-not (Test-Path -LiteralPath $workspaceParent)) {
    New-Item -ItemType Directory -Path $workspaceParent | Out-Null
}

Copy-Item -LiteralPath $resolvedSource -Destination $resolvedWorkspace -Force

Write-Host "Synced Structurizr workspace:"
Write-Host "  source:    $resolvedSource"
Write-Host "  workspace: $resolvedWorkspace"
