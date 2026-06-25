$ErrorActionPreference = 'Stop'

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

$services = @(
    @{ Name = 'MySQL'; Port = 3306 },
    @{ Name = 'Redis'; Port = 6379 },
    @{ Name = 'Spring Boot'; Port = 8080 },
    @{ Name = 'Vue'; Port = 5173 }
)

$failed = $false
foreach ($service in $services) {
    $up = Test-TcpPort $service.Port
    $color = if ($up) { 'Green' } else { 'Red' }
    $status = if ($up) { 'UP' } else { 'DOWN' }
    Write-Host ("[{0}] {1} :{2}" -f $status, $service.Name, $service.Port) -ForegroundColor $color
    if (-not $up) { $failed = $true }
}

if ($failed) {
    throw 'One or more required services are not running.'
}

$health = (Invoke-RestMethod -Uri 'http://localhost:8080/api/health' -TimeoutSec 5).data
Write-Host ("[HEALTH] status={0}, database={1}, redis={2}, proxy={3}, demo={4}" -f $health.status, $health.database, $health.redis, $health.proxyMode, $health.demoDataEnabled)
if ($health.status -ne 'UP') { throw 'Backend health check failed.' }

$loginBody = @{ username = 'demo_user'; password = 'demo123456' } | ConvertTo-Json
$login = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -ContentType 'application/json' -Body $loginBody -TimeoutSec 8
if (-not $login.data.accessToken) { throw 'Demo user login failed.' }
Write-Host '[PASS] Demo user login succeeded.' -ForegroundColor Green

$frontend = Invoke-WebRequest -Uri 'http://localhost:5173/login' -UseBasicParsing -TimeoutSec 5
if ($frontend.StatusCode -ne 200) { throw 'Frontend page check failed.' }
Write-Host '[PASS] Frontend login page returned HTTP 200.' -ForegroundColor Green
Write-Host '[READY] The project is ready for demonstration.' -ForegroundColor Green
