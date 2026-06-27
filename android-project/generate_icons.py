import os
from PIL import Image, ImageDraw, ImageFont

def create_icon(size, output_path):
    # Create black background
    img = Image.new('RGB', (size, size), color='black')
    draw = ImageDraw.Draw(img)
    
    # Text to draw
    text = "Emeth"
    
    # Try to load a font, fallback to default
    try:
        # Use a sans-serif font available on Windows
        font = ImageFont.truetype("arialbd.ttf", int(size * 0.4))
    except:
        font = ImageFont.load_default()
        
    # Get bounding box of the text
    bbox = draw.textbbox((0, 0), text, font=font)
    text_w = bbox[2] - bbox[0]
    text_h = bbox[3] - bbox[1]
    
    # Center text
    x = (size - text_w) / 2
    y = (size - text_h) / 2
    
    # Draw text in white
    draw.text((x, y), text, font=font, fill='white')
    
    img.save(output_path)

# Android icon sizes
sizes = {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192,
    'playstore': 512
}

base_dir = r"C:\Users\apple\OneDrive\Documents\Air OS\emeth\android-project\app\src\main\res"

for density, size in sizes.items():
    if density == 'playstore':
        path = r"C:\Users\apple\OneDrive\Documents\Air OS\emeth\android-project\app\src\main\ic_launcher-web.png"
    else:
        folder = os.path.join(base_dir, f"mipmap-{density}")
        os.makedirs(folder, exist_ok=True)
        path = os.path.join(folder, "ic_launcher.png")
        
    create_icon(size, path)
    
    # Create round icon too
    if density != 'playstore':
        round_path = os.path.join(folder, "ic_launcher_round.png")
        create_icon(size, round_path)

print("Icons generated.")
