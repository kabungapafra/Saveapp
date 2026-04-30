import re
import os

assets_dir = '/home/darlin-pafra/AndroidStudioProjects/Save/Front-end/app/src/main/assets'
html_files = [
    'stash_dashboard.html',
    'support_live_chat.html',
    'voting_hub.html',
    'liquidity_dashboard.html',
    'payout_audit.html',
]

# The JS to inject: reads Android SharedPreferences theme via Android bridge,
# falls back to prefers-color-scheme, but removes the interactive toggle button
INJECT_THEME_JS = """
        // ── Android Theme Sync ──────────────────────────────────────
        function applyThemeFromAndroid() {
            var isDark = false;
            try {
                // Try to read from Android bridge if available
                if (window.Android && typeof window.Android.isDarkMode === 'function') {
                    isDark = window.Android.isDarkMode();
                } else {
                    // Fallback: match system preference
                    isDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
                }
            } catch(e) {
                isDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
            }
            document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light');
        }
        applyThemeFromAndroid();
        // ─────────────────────────────────────────────────────────────
"""

for filename in html_files:
    filepath = os.path.join(assets_dir, filename)
    if not os.path.exists(filepath):
        print(f"SKIP (not found): {filename}")
        continue

    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    original = content

    # 1. Remove the theme-toggle button element (button tag with id themeToggle)
    content = re.sub(
        r'<button[^>]*id=["\']themeToggle["\'][^>]*>.*?</button>',
        '',
        content,
        flags=re.DOTALL | re.IGNORECASE
    )

    # 2. Remove .theme-toggle CSS block
    content = re.sub(
        r'\.theme-toggle\s*\{[^}]*\}',
        '',
        content,
        flags=re.DOTALL
    )

    # 3. Remove the toggleTheme() function
    content = re.sub(
        r'function toggleTheme\(\)\s*\{[^}]*\}',
        '',
        content,
        flags=re.DOTALL
    )

    # 4. Remove updateThemeIcon() function
    content = re.sub(
        r'function updateThemeIcon\([^)]*\)\s*\{.*?\n\s*\}',
        '',
        content,
        flags=re.DOTALL
    )

    # 5. Remove initTheme() function
    content = re.sub(
        r'function initTheme\(\)\s*\{[^}]*\}',
        '',
        content,
        flags=re.DOTALL
    )

    # 6. Remove initTheme() call
    content = re.sub(r'\s*initTheme\(\);', '', content)

    # 7. Remove themeIcon / themeToggle variable declarations
    content = re.sub(r'\s*const themeIcon\s*=\s*document\.getElementById\([\'"]themeIcon[\'"]\);\s*', '\n', content)
    content = re.sub(r'\s*const themeToggle\s*=\s*document\.getElementById\([\'"]themeToggle[\'"]\);\s*', '\n', content)

    # 8. Remove localStorage theme reads that reference the old toggle
    content = re.sub(r"\s*const savedTheme\s*=\s*localStorage\.getItem\('theme'\)[^;]*;\s*", '\n', content)
    content = re.sub(r"\s*localStorage\.setItem\('theme'[^;]*\);\s*", '\n', content)

    # 9. Inject the Android-sync theme script before </script> closing of the last script tag
    # Find the last </script> and inject before it
    inject = INJECT_THEME_JS
    # Insert before the last </script>
    last_script_close = content.rfind('</script>')
    if last_script_close != -1:
        content = content[:last_script_close] + inject + content[last_script_close:]

    if content != original:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed: {filename}")
    else:
        print(f"No change: {filename}")
