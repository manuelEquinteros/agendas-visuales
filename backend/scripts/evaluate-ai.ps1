$endpoint = "http://localhost:8080/api/assistant/interpret"

$cases = @(
    @{
        id = "AI-01"
        name = "Agenda diaria completa"
        message = "Creá una agenda diaria para mañana con desayunar a las 8:00 y vestirse a las 8:30."
    },
    @{
        id = "AI-02"
        name = "Agenda sin fecha"
        message = "Creá una agenda diaria con desayunar y vestirse."
    },
    @{
        id = "AI-03"
        name = "Agenda diaria sin horarios"
        message = "Para mañana quiero una agenda con desayunar, vestirse e ir a la escuela."
    },
    @{
        id = "AI-04"
        name = "Agenda semanal"
        message = "Creá una agenda semanal desde el próximo lunes. El lunes tiene escuela y natación. El martes tiene terapia. El miércoles tiene escuela."
    },
    @{
        id = "AI-05"
        name = "Fecha explícita"
        message = "Creá una agenda diaria para el 15/10/2026 con almorzar y hacer la tarea."
    },
    @{
        id = "AI-06"
        name = "Fuera de alcance"
        message = "¿Cuál es la capital de Francia?"
    },
    @{
        id = "AI-07"
        name = "Solicitud incompleta"
        message = "Quiero hacer una agenda."
    },
    @{
        id = "AI-08"
        name = "Hora sin hora final"
        message = "Creá una agenda para mañana con natación a las 17:00."
    },
    @{
        id = "AI-09"
        name = "Acompañante explícito"
        message = "Mañana a las 10:00 tiene fonoaudiología con mamá."
    }
)

$outputDirectory =
    Join-Path $PSScriptRoot "..\target\ai-evaluation"

New-Item `
    -ItemType Directory `
    -Force `
    -Path $outputDirectory |
    Out-Null


foreach ($case in $cases) {

    Write-Host ""
    Write-Host "========================================"
    Write-Host "$($case.id) - $($case.name)"
    Write-Host "========================================"
    Write-Host $case.message
    Write-Host ""

    $body = @{
        message = $case.message
        context = $null
    } | ConvertTo-Json -Depth 20

    $bodyUtf8 =
        [System.Text.Encoding]::UTF8.GetBytes(
            $body
        )

    try {

        $response =
            Invoke-RestMethod `
                -Uri $endpoint `
                -Method Post `
                -ContentType "application/json; charset=utf-8" `
                -Body $bodyUtf8

        $json =
            $response |
            ConvertTo-Json -Depth 20

        Write-Host $json

        $outputFile =
            Join-Path `
                $outputDirectory `
                "$($case.id).json"

        $json |
            Out-File `
                -FilePath $outputFile `
                -Encoding utf8

    }
    catch {

        Write-Host "ERROR"
        Write-Host "Status:" $_.Exception.Response.StatusCode
        Write-Host $_.ErrorDetails.Message
    }

    # Evitamos disparar todas las consultas
    # juntas contra el Free Tier.

    Start-Sleep -Seconds 5
}