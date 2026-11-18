# AI-generated code
param(
    [Parameter(Mandatory = $true)]
    [string]$SourceCsv
)

$ErrorActionPreference = 'Stop'
$destination = Join-Path $PSScriptRoot 'data\movies.csv'
$destinationDirectory = Split-Path -Parent $destination
New-Item -ItemType Directory -Force -Path $destinationDirectory | Out-Null

$movies = [System.Collections.Generic.List[object]]::new()
$ids = [System.Collections.Generic.HashSet[string]]::new()
$sha256 = [System.Security.Cryptography.SHA256]::Create()
$skipped = 0

try {
    foreach ($row in (Import-Csv -LiteralPath $SourceCsv -Encoding utf8)) {
        $runtime = 0
        $publicRating = 0.0
        $votes = [long]0
        $voteText = $row.Votes -replace ',', ''
        $genres = @($row.Genre -split ',' | ForEach-Object { $_.Trim() } | Where-Object { $_ })

        if ([string]::IsNullOrWhiteSpace($row.Title) -or $genres.Count -eq 0 -or
            -not [int]::TryParse($row.'Duration (min)', [ref]$runtime) -or $runtime -le 0 -or
            -not [double]::TryParse($row.Rating, [System.Globalization.NumberStyles]::Float,
                [System.Globalization.CultureInfo]::InvariantCulture, [ref]$publicRating) -or
            -not [long]::TryParse($voteText, [ref]$votes)) {
            $skipped++
            continue
        }

        # Title, year, and director are unique in this source and remain stable when its rows move.
        $key = ($row.Title.Trim() + '|' + $row.Year.Trim() + '|' + $row.Director.Trim()).ToLowerInvariant()
        $hashBytes = $sha256.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($key))
        $id = [Convert]::ToHexString($hashBytes).Substring(0, 16).ToLowerInvariant()
        if (-not $ids.Add($id)) {
            throw "Duplicate movie identity for '$($row.Title)' ($($row.Year))."
        }

        $movies.Add([pscustomobject][ordered]@{
            Id = $id
            Title = $row.Title.Trim()
            Year = $row.Year.Trim()
            Director = $row.Director.Trim()
            Certificate = $row.Certificate.Trim()
            RuntimeMinutes = $runtime
            Genres = $genres -join '|'
            PublicRating = $publicRating.ToString('0.0', [System.Globalization.CultureInfo]::InvariantCulture)
            Votes = $votes
        })
    }
} finally {
    $sha256.Dispose()
}

$movies | Export-Csv -LiteralPath $destination -Encoding utf8 -NoTypeInformation
Write-Host "Prepared $($movies.Count) movies; skipped $skipped incomplete rows."
Write-Host "Catalog: $destination"
