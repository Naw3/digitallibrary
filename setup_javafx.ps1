$javafxUrl = "https://download2.gluonhq.com/openjfx/23.0.1/openjfx-23.0.1_windows-x64_bin-sdk.zip"
$zipPath = "lib\javafx-sdk.zip"
$destPath = "lib"

Write-Host "Downloading JavaFX SDK..."
Invoke-WebRequest -Uri $javafxUrl -OutFile $zipPath

Write-Host "Extracting JavaFX SDK..."
Expand-Archive -Path $zipPath -DestinationPath $destPath -Force

Write-Host "JavaFX SDK setup complete."
Remove-Item $zipPath
