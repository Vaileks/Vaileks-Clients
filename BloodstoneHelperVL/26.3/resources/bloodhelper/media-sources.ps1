# Shared GSMTC application selection. This library has no side effects or player controls.
function Select-MediaSession($Manager, [string]$PreferredSource) {
    $current = $Manager.GetCurrentSession()
    if ([string]::IsNullOrEmpty($PreferredSource)) { return $current }
    if ($null -ne $current -and [string]$current.SourceAppUserModelId -ceq $PreferredSource) { return $current }
    foreach ($candidate in $Manager.GetSessions()) {
        if ([string]$candidate.SourceAppUserModelId -ceq $PreferredSource) { return $candidate }
    }
    # A pinned application being closed must not redirect controls to a different player.
    return $null
}

function Get-MediaSourcesPacket($Manager, $Selected) {
    $apps = @(); $seen = New-Object 'System.Collections.Generic.HashSet[string]' ([StringComparer]::Ordinal)
    foreach ($candidate in $Manager.GetSessions()) {
        if ($apps.Count -ge 64) { break }
        try {
            $id = [string]$candidate.SourceAppUserModelId
            if ($id.Length -eq 0 -or $id.Length -gt 512 -or $id -match '[\x00-\x1f\x7f]' -or -not $seen.Add($id)) { continue }
            $playing = $false
            try { $playing = [string]$candidate.GetPlaybackInfo().PlaybackStatus -ceq 'Playing' } catch {}
            $apps += @{ id = $id; playing = $playing }
        } catch {}
    }
    $active = ''
    if ($null -ne $Selected) { $active = [string]$Selected.SourceAppUserModelId }
    return @{ kind = 'sources'; apps = @($apps); activeId = $active }
}
