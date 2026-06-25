Add-Type -AssemblyName System.Drawing
$img = [System.Drawing.Image]::FromFile('app\src\main\res\drawable\logo_transparent.png')
$bmp = new-object System.Drawing.Bitmap($img)
$img.Dispose()
$bgColor = $bmp.GetPixel(0, 0)
$bmp.MakeTransparent($bgColor)
$bmp.Save('app\src\main\res\drawable\logo_fixed.png', [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Dispose()
