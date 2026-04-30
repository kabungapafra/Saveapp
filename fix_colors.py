import os
import glob

layout_dir = '/home/darlin-pafra/AndroidStudioProjects/Save/Front-end/app/src/main/res/layout'

files = glob.glob(os.path.join(layout_dir, '*.xml'))

for f in files:
    with open(f, 'r') as file:
        content = file.read()
    
    new_content = content.replace('@android:color/white', '@color/white')
    new_content = new_content.replace('app:cardBackgroundColor="#FFFFFF"', 'app:cardBackgroundColor="@color/white"')
    new_content = new_content.replace('android:background="#FFFFFF"', 'android:background="@color/white"')
    new_content = new_content.replace('android:background="#fff"', 'android:background="@color/white"')
    new_content = new_content.replace('android:background="#FFF"', 'android:background="@color/white"')
    
    if new_content != content:
        with open(f, 'w') as file:
            file.write(new_content)
        print(f'Updated {os.path.basename(f)}')
