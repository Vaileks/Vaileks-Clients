param([Parameter(Mandatory=$true)][string]$ImagePath)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing
$source = [System.Drawing.Image]::FromFile($ImagePath)
try {
    $bitmap = New-Object System.Drawing.Bitmap($source)
    try {
        $copied = $false
        for ($attempt = 0; $attempt -lt 5; $attempt++) {
            try { [System.Windows.Forms.Clipboard]::SetImage($bitmap); $copied = $true; break }
            catch { Start-Sleep -Milliseconds 120 }
        }
        if (-not $copied -or -not [System.Windows.Forms.Clipboard]::ContainsImage()) { throw 'Image clipboard unavailable' }
    } finally { $bitmap.Dispose() }
} finally { $source.Dispose() }
