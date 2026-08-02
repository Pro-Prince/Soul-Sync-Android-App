import os

directory = r"c:\Users\HP\Desktop\Soul Sync Android App\app\src\main\java\com\example\ui"

for root, _, files in os.walk(directory):
    for file in files:
        if file.endswith(".kt"):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            new_content = content.replace("twotone", "rounded").replace("TwoTone", "Rounded")
            
            if content != new_content:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f"Updated {filepath}")
