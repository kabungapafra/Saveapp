import os

directory = 'app/src/main/res/layout'
files = [
    'fragment_analytics.xml',
    'fragment_settings.xml',
    'fragment_polls.xml',
    'fragment_notifications.xml',
    'fragment_members.xml',
    'fragment_support.xml',
    'fragment_stash.xml'
]

for filename in files:
    path = os.path.join(directory, filename)
    if not os.path.exists(path):
        continue
    with open(path, 'r') as f:
        lines = f.readlines()
        
    out_lines = []
    in_block = False
    
    # We will buffer lines until we see the end of FrameLayout or figure out if it's the theme toggle
    # Actually, simpler: read the whole file, find index of 'android:id="@+id/btnThemeToggle"'
    with open(path, 'r') as f:
        content = f.read()
        
    while 'android:id="@+id/btnThemeToggle"' in content:
        idx = content.find('android:id="@+id/btnThemeToggle"')
        
        # find the preceding <FrameLayout
        start_idx = content.rfind('<FrameLayout', 0, idx)
        
        # find the succeeding </FrameLayout>
        end_idx = content.find('</FrameLayout>', idx) + len('</FrameLayout>')
        
        # we might have leading spaces before <FrameLayout
        space_start = content.rfind('\n', 0, start_idx)
        if space_start != -1:
            start_idx = space_start + 1
            
        content = content[:start_idx] + content[end_idx:]
        
    content = content.replace('app:layout_constraintEnd_toStartOf="@id/btnThemeToggle"', 'app:layout_constraintEnd_toEndOf="parent"')
    
    with open(path, 'w') as f:
        f.write(content)
        
    print(f"Processed {filename}")
