param(
    [switch]$NoBrowser,
    [string]$DatabaseUsername = 'root',
    [string]$DatabasePassword = 'root'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$frontendRoot = Join-Path $projectRoot 'frontend'

function Test-TcpPort([int]$Port) {
    $client = [System.Net.Sockets.TcpClient]::new()
    try {
        $result = $client.BeginConnect('127.0.0.1', $Port, $null, $null)
        return $result.AsyncWaitHandle.WaitOne(700) -and $client.Connected
    } catch {
        return $false
    } finally {
        $client.Dispose()
    }
}

function Wait-TcpPort([int]$Port, [int]$Seconds = 90) {
    $deadline = (Get-Date).AddSeconds($Seconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-TcpPort $Port) { return }
        Start-Sleep -Seconds 1
    }
    throw "Port $Port did not become ready within $Seconds seconds. Check the new terminal window for errors."
}

function Resolve-Executable([string]$Name, [string[]]$Candidates) {
    $command = Get-Command $Name -ErrorAction SilentlyContinue
    if ($command) { return $command.Source }
    foreach ($candidate in $Candidates) {
        if (Test-Path -LiteralPath $candidate) { return $candidate }
    }
    throw "Required command '$Name' was not found."
}

if (-not (Test-TcpPort 3306)) {
    throw 'MySQL is not running on port 3306. Start MySQL first.'
}
if (-not (Test-TcpPort 6379)) {
    throw 'Redis is not running on port 6379. Start Redis first.'
}

$maven = Resolve-Executable 'mvn.cmd' @('C:\apache-maven\bin\mvn.cmd')
$npm = Resolve-Executable 'npm.cmd' @('A:\node.js\npm.cmd', 'C:\Program Files\nodejs\npm.cmd')
$escapedRoot = $projectRoot.Replace("'", "''")
$escapedFrontend = $frontendRoot.Replace("'", "''")
$escapedMaven = $maven.Replace("'", "''")
$escapedNpm = $npm.Replace("'", "''")
$escapedDatabaseUsername = $DatabaseUsername.Replace("'", "''")
$escapedDatabasePassword = $DatabasePassword.Replace("'", "''")

if (-not (Test-TcpPort 8080)) {
    $backendCommand = @"
Set-Location -LiteralPath '$escapedRoot'
`$env:DEMO_DATA_ENABLED = 'true'
`$env:PROXY_MODE = 'MOCK'
`$env:DB_USERNAME = '$escapedDatabaseUsername'
`$env:DB_PASSWORD = '$escapedDatabasePassword'
& '$escapedMaven' spring-boot:run
"@
    Start-Process powershell.exe -WindowStyle Normal -ArgumentList '-NoExit', '-ExecutionPolicy', 'Bypass', '-Command', $backendCommand
    Write-Host '[START] Spring Boot is starting...' -ForegroundColor Cyan
    Wait-TcpPort 8080
} else {
    Write-Host '[SKIP] Port 8080 is already in use.' -ForegroundColor Yellow
}

try {
    $healthResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/health' -TimeoutSec 5
    $health = $healthResponse.data
} catch {
    throw 'Port 8080 is open, but the current backend has no health endpoint. Stop the old backend and run this script again.'
}

if ($health.status -ne 'UP') {
    throw "Backend is degraded. MySQL=$($health.database), Redis=$($health.redis)."
}
if (-not $health.demoDataEnabled) {
    throw 'Backend is running without demo data. Stop it and restart with DEMO_DATA_ENABLED=true.'
}

if (-not (Test-TcpPort 5173)) {
    $frontendCommand = @"
Set-Location -LiteralPath '$escapedFrontend'
& '$escapedNpm' run dev
"@
    Start-Process powershell.exe -WindowStyle Normal -ArgumentList '-NoExit', '-ExecutionPolicy', 'Bypass', '-Command', $frontendCommand
    Write-Host '[START] Vue frontend is starting...' -ForegroundColor Cyan
    Wait-TcpPort 5173
} else {
    Write-Host '[SKIP] Port 5173 is already in use.' -ForegroundColor Yellow
}

$loginBody = @{ username = 'demo_user'; password = 'demo123456' } | ConvertTo-Json
$loginResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -ContentType 'application/json' -Body $loginBody -TimeoutSec 8
if (-not $loginResponse.data.accessToken) {
    throw 'Demo account login check failed.'
}

Write-Host ''
Write-Host 'KeyBridge AI demo is ready.' -ForegroundColor Green
Write-Host "  Database: $($health.database)"
Write-Host "  Redis:    $($health.redis)"
Write-Host "  Proxy:    $($health.proxyMode)"
Write-Host '  Frontend: http://localhost:5173/login'
Write-Host '  Swagger:  http://localhost:8080/swagger-ui/index.html'
Write-Host '  User:     demo_user / demo123456'
Write-Host '  Admin:    admin / admin123456'

if (-not $NoBrowser) {
    Start-Process 'http://localhost:5173/login'
}
