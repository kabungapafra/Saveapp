import os
import re
import glob

layout_dir = '/home/darlin-pafra/AndroidStudioProjects/Save/Front-end/app/src/main/res/layout'

# Mapping of hardcoded hex colors → theme-aware color references
# Only backgrounds, card backgrounds, and text backgrounds are replaced (not accent/brand colors)
# White/near-white backgrounds → card_surface (adapts in dark mode)
# Very dark backgrounds (#1E293B, #0F172A etc) → v_page_bg (adapts in dark mode)

WHITE_HEX = {
    '#FFFFFF', '#ffffff', '#FFF', '#fff',
    '#FAFAFA', '#fafafa',
    '#F9FAFB', '#f9fafb',
    '#F8FAFC', '#f8fafc',
    '#F1F5F9', '#f1f5f9',
}

DARK_BG_HEX = {
    '#1E293B', '#1e293b',
    '#0F172A', '#0f172a',
    '#111827', '#111827',
    '#1a1a2e', '#1A1A2E',
}

files = glob.glob(os.path.join(layout_dir, '*.xml'))
changed = []

for f in files:
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()

    original = content

    # --- Replace card backgrounds ---
    # app:cardBackgroundColor="@android:color/white" → @color/card_surface
    content = re.sub(
        r'app:cardBackgroundColor="@android:color/white"',
        'app:cardBackgroundColor="@color/card_surface"',
        content
    )
    # app:cardBackgroundColor="@color/white" → @color/card_surface
    content = re.sub(
        r'app:cardBackgroundColor="@color/white"',
        'app:cardBackgroundColor="@color/card_surface"',
        content
    )
    # app:cardBackgroundColor="#FFFFFF" etc → @color/card_surface
    for hex_val in WHITE_HEX:
        content = re.sub(
            rf'app:cardBackgroundColor="{re.escape(hex_val)}"',
            'app:cardBackgroundColor="@color/card_surface"',
            content
        )

    # --- Replace layout/view backgrounds (white) ---
    for hex_val in WHITE_HEX:
        # android:background="..."
        content = re.sub(
            rf'android:background="{re.escape(hex_val)}"',
            'android:background="@color/card_surface"',
            content
        )

    # --- Replace layout backgrounds (dark) with v_page_bg ---
    for hex_val in DARK_BG_HEX:
        content = re.sub(
            rf'android:background="{re.escape(hex_val)}"',
            'android:background="@color/v_page_bg"',
            content
        )
        content = re.sub(
            rf'app:cardBackgroundColor="{re.escape(hex_val)}"',
            'app:cardBackgroundColor="@color/v_page_bg"',
            content
        )

    # --- Replace @android:color/white references in backgrounds ---
    content = content.replace(
        'android:background="@android:color/white"',
        'android:background="@color/card_surface"'
    )

    # --- Replace drawableTint hardcoded dark color ---
    content = re.sub(
        r'app:drawableTint="#1E293B"',
        'app:drawableTint="@color/v_text_dark"',
        content, flags=re.IGNORECASE
    )
    content = re.sub(
        r'app:drawableTint="#1e293b"',
        'app:drawableTint="@color/v_text_dark"',
        content
    )

    if content != original:
        with open(f, 'w', encoding='utf-8') as file:
            file.write(content)
        changed.append(os.path.basename(f))

print(f"Updated {len(changed)} files:")
for name in changed:
    print(f"  - {name}")
