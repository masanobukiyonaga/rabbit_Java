$mavenVersion = "3.9.6"
$mavenUrl = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
$installDir = "$PSScriptRoot\maven"
$zipFile = "$installDir\maven.zip"

# Create directory
if (-not (Test-Path -Path $installDir)) {
    New-Item -ItemType Directory -Path $installDir | Out-Null
}

# Download Maven
Write-Host "Downloading Maven $mavenVersion..."
Invoke-WebRequest -Uri $mavenUrl -OutFile $zipFile

# Extract
Write-Host "Extracting Maven..."
Expand-Archive -Path $zipFile -DestinationPath $installDir -Force

# Clean up zip
Remove-Item -Path $zipFile

# Get the extracted folder name
$extractedFolder = Get-ChildItem -Path $installDir | Where-Object { $_.PSIsContainer } | Select-Object -First 1
$mavenBin = "$($extractedFolder.FullName)\bin"

# Add to PATH for this session
$env:PATH = "$mavenBin;$env:PATH"

Write-Host "Maven installed successfully to $extractedFolder"
Write-Host "Verifying installation..."
mvn -version

Write-Host "`nIMPORTANT: To use 'mvn' in future sessions, you will need to add '$mavenBin' to your PATH environment variable, or run this setup script again."
