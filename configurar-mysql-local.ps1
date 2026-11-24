param([string]$MySqlClient, [string]$AdminUser = 'root')

$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
if (-not $MySqlClient) {
    $command = Get-Command mysql.exe -ErrorAction SilentlyContinue
    if ($command) { $MySqlClient = $command.Source }
    else {
        $client = Get-ChildItem -Path "$env:ProgramFiles/MySQL/*/bin/mysql.exe" -File -ErrorAction SilentlyContinue |
            Select-Object -First 1
        if ($client) { $MySqlClient = $client.FullName }
    }
}
if (-not $MySqlClient -or -not (Test-Path -LiteralPath $MySqlClient)) {
    throw 'No se encontro mysql.exe. Indica su ruta con -MySqlClient.'
}
$services = 'identity', 'catalog', 'contact'
foreach ($service in $services) {
    if (Test-Path -LiteralPath ".$service-local.properties") {
        throw "Ya existe .$service-local.properties. Conserva la configuracion actual."
    }
}

$sql = [IO.File]::ReadAllText((Join-Path $PSScriptRoot 'mysql-setup.sql')).Replace('ORIGEN_CLIENTE','localhost')
Write-Host 'Ingresa la clave de administrador de MySQL en el aviso Enter password.'
$rows = & $MySqlClient --protocol=TCP --host=127.0.0.1 --port=3306 "--user=$AdminUser" --password --batch --raw --skip-column-names "--execute=$sql"
if ($LASTEXITCODE -ne 0) { throw 'MySQL no completo la configuracion.' }

$credentials = @{}
foreach ($row in $rows) {
    $parts = $row.Split([char]9)
    if ($parts.Length -ge 3 -and $parts[0] -match '^martini_(identity|catalog|contact)$') {
        $credentials[$Matches[1]] = $parts[2].Replace('\','\\')
    }
}

$adminEmail = 'admin.demo@example.test'
$adminPassword = [guid]::NewGuid().ToString('N')
$encoding = [Text.UTF8Encoding]::new($false)
foreach ($service in $services) {
    $password = $credentials[$service]
    if (-not $password) {
        $secure = Read-Host "Ingresa la clave EXISTENTE de martini_$service" -AsSecureString
        $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
        try { $password = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr).Replace('\','\\') }
        finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
    }
    if (-not $password) { throw "Falta la clave de martini_$service." }
    $properties = @("spring.datasource.password=$password")
    if ($service -eq 'identity') {
        $properties += "ADMIN_EMAIL=$adminEmail", "ADMIN_PASSWORD=$adminPassword"
    }
    [IO.File]::WriteAllLines((Join-Path $PSScriptRoot ".$service-local.properties"), $properties, $encoding)
}
Write-Host 'Configuracion guardada. En VS Code, ejecuta Backend completo (MySQL local).'
Write-Host "Administrador inicial en una base nueva: $adminEmail"
Write-Host 'La clave queda en .identity-local.properties, en la linea ADMIN_PASSWORD.'
