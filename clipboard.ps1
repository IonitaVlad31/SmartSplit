Add-Type -AssemblyName System.Windows.Forms
$img = [System.Windows.Forms.Clipboard]::GetImage()
if ($img) {
    $img.Save("c:\Users\gheor\OneDrive\Desktop\Facultate\Android\SmartSplit\app\src\main\res\drawable\logo.png", [System.Drawing.Imaging.ImageFormat]::Png)
    Write-Output "Saved!"
} else {
    Write-Output "No image"
}
