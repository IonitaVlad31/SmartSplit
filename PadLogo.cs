using System;
using System.Drawing;
using System.Drawing.Imaging;

class Program {
    static void Main() {
        string inPath = @"app\src\main\res\drawable\logo_black.png";
        string outPath = @"app\src\main\res\drawable\logo_padded.png";
        
        using (Bitmap original = new Bitmap(inPath)) {
            using (Bitmap padded = new Bitmap(1024, 1024)) {
                using (Graphics g = Graphics.FromImage(padded)) {
                    // Fill background
                    g.Clear(Color.FromArgb(255, 18, 18, 18)); // #121212
                    
                    // Calculate scaling to fit within 600x600 (safe zone)
                    float scale = Math.Min(600f / original.Width, 600f / original.Height);
                    int newW = (int)(original.Width * scale);
                    int newH = (int)(original.Height * scale);
                    
                    int destX = (1024 - newW) / 2;
                    int destY = (1024 - newH) / 2;
                    
                    g.DrawImage(original, destX, destY, newW, newH);
                }
                padded.Save(outPath, ImageFormat.Png);
            }
        }
    }
}
