$projectRoot = "c:\Users\Ewan\Documents\NetBeansProjects\DigitalLibrary"
$javafxLib = "$projectRoot\lib\javafx-sdk-23.0.1\lib"
$libDir = "$projectRoot\lib"
$outDir = "$projectRoot\build\classes"
$srcDir = "$projectRoot\src"

# Create output directory
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

# Clean output directory (optional, safer for manual run)
# Remove-Item "$outDir\*" -Recurse -Force -ErrorAction SilentlyContinue

$libs = Get-ChildItem -Path $libDir -Filter *.jar | Select-Object -ExpandProperty FullName
$classpath = $libs -join ";"

# Compile
Write-Host "Compiling sources..."
$sources = Get-ChildItem -Path $srcDir -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
if ($sources) {
    javac -d $outDir -cp $classpath --module-path $javafxLib --add-modules javafx.controls,javafx.fxml $sources
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Compilation failed."
        exit $LASTEXITCODE
    }
} else {
    Write-Warning "No sources found."
}

# Copy Resources (fxml, images, etc.)
Write-Host "Copying resources..."
Copy-Item "$srcDir\*" -Destination $outDir -Recurse -Force -Exclude *.java

# Run
Write-Host "Running MainApp..."
java -cp "$outDir;$classpath" --module-path $javafxLib --add-modules javafx.controls,javafx.fxml controllers.MainApp
