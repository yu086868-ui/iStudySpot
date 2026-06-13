param(
    [int]$Port = 8081,
    [string]$DslPath = ""
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$designDir = Join-Path $repoRoot "docs\design"
$syncScript = Join-Path $PSScriptRoot "sync-structurizr-workspace.ps1"

if ([string]::IsNullOrWhiteSpace($DslPath)) {
    $DslPath = Join-Path $designDir "structurizr-backend-android.dsl"
}

& $syncScript -SourceDsl $DslPath -WorkspaceDsl (Join-Path $designDir "workspace.dsl")

$docker = Get-Command docker -ErrorAction SilentlyContinue
if ($null -eq $docker) {
    throw "Docker was not found in PATH. Install Docker Desktop or run Structurizr Lite manually against $designDir."
}

$null = & docker info 2>$null
if ($LASTEXITCODE -ne 0) {
    throw "Docker is installed but not usable from the current shell. Make sure Docker Desktop is running and that your account can access the Docker daemon."
}

$resolvedDesignDir = [System.IO.Path]::GetFullPath($designDir)

Write-Host "Starting Structurizr local viewer..."
Write-Host "  workspace: $resolvedDesignDir"
Write-Host "  url:       http://localhost:$Port"
Write-Host ""
Write-Host "Leave this terminal open while viewing."
Write-Host "Stop the server with Ctrl+C."

& docker run --rm -p "${Port}:8080" -v "${resolvedDesignDir}:/usr/local/structurizr" structurizr/structurizr local
if ($LASTEXITCODE -ne 0) {
    throw "Structurizr local viewer failed to start. See the Docker output above for details."
}
