using System;
using System.Drawing;
using System.Drawing.Imaging;

class Program {
    static void Main() {
        string path = @"app\src\main\res\drawable\logo_cropped.png";
        string outPath = @"app\src\main\res\drawable\logo_black.png";
        using (Bitmap bmp = new Bitmap(path)) {
            Color target = bmp.GetPixel(0, 0);
            for(int y=0; y<bmp.Height; y++) {
                for(int x=0; x<bmp.Width; x++) {
                    Color c = bmp.GetPixel(x, y);
                    // If color is close to top-left background
                    if (Math.Abs(c.R - target.R) < 30 && Math.Abs(c.G - target.G) < 30 && Math.Abs(c.B - target.B) < 30) {
                        bmp.SetPixel(x, y, Color.FromArgb(255, 18, 18, 18)); // #121212
                    }
                }
            }
            bmp.Save(outPath, ImageFormat.Png);
        }
    }
}
