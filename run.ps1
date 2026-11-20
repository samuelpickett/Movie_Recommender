# This file contains AI-generated code.
param([switch]$Gui)

$ErrorActionPreference = 'Stop'
$projectDirectory = $PSScriptRoot
$utf8 = [System.Text.UTF8Encoding]::new($false)
[Console]::OutputEncoding = $utf8
$OutputEncoding = $utf8
$compiler = Get-Command javac -ErrorAction SilentlyContinue

if ($compiler) {
    $compilerPath = $compiler.Source
} elseif ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
    $compilerPath = Join-Path $env:JAVA_HOME 'bin\javac.exe'
} else {
    $compilerPath = Get-ChildItem 'C:\Program Files\JetBrains' -Recurse -Filter javac.exe -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty FullName
}

if (-not $compilerPath) {
    throw 'Java 17 or newer is required. Install a JDK or set JAVA_HOME.'
}

$javaPath = Join-Path (Split-Path -Parent $compilerPath) 'java.exe'
$outputDirectory = Join-Path $projectDirectory 'out'
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null
$sourceFiles = @(Get-ChildItem -LiteralPath (Join-Path $projectDirectory 'src\movies') -Filter '*.java' |
    Select-Object -ExpandProperty FullName)
& $compilerPath --release 17 -d $outputDirectory $sourceFiles
if ($LASTEXITCODE -ne 0) {
    throw 'Java compilation failed.'
}

Push-Location $projectDirectory
try {
    $entryPoint = if ($Gui) { 'movies.GuiMain' } else { 'movies.Main' }
    & $javaPath '-Dstdout.encoding=UTF-8' '-Dstderr.encoding=UTF-8' -cp $outputDirectory $entryPoint @args
} finally {
    Pop-Location
}
