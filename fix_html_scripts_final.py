import os

assets_dir = '/home/darlin-pafra/AndroidStudioProjects/Save/Front-end/app/src/main/assets'
html_files = [
    'stash_dashboard.html',
    'support_live_chat.html',
    'voting_hub.html',
    'liquidity_dashboard.html',
    'payout_audit.html',
]

CLEAN_SCRIPT = """
    <script>
        function applyThemeFromAndroid() {
            console.log("Applying theme from Android bridge...");
            var isDark = false;
            try {
                if (window.Android && typeof window.Android.isDarkMode === 'function') {
                    isDark = window.Android.isDarkMode();
                } else if (window.AndroidBridge && typeof window.AndroidBridge.isDarkMode === 'function') {
                    isDark = window.AndroidBridge.isDarkMode();
                } else {
                    isDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
                }
            } catch(e) {
                console.error("Error reading theme from bridge:", e);
                isDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
            }
            document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light');
            document.body.style.display = 'block'; // Ensure body is shown after theme applied
        }

        // Call immediately
        applyThemeFromAndroid();

        // Also call on load to be sure
        window.addEventListener('load', applyThemeFromAndroid);
        
        // Listen for system theme changes as backup
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', applyThemeFromAndroid);
    </script>
"""

for filename in html_files:
    filepath = os.path.join(assets_dir, filename)
    if not os.path.exists(filepath):
        continue

    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find the start of the script tag and the end of the script tag
    import re
    # Remove all existing script tags at the bottom
    new_content = re.sub(r'<script>.*?</script>', '', content, flags=re.DOTALL)
    
    # Add back the clean script before </body>
    if '</body>' in new_content:
        new_content = new_content.replace('</body>', CLEAN_SCRIPT + '\n</body>')
    else:
        new_content += CLEAN_SCRIPT

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print(f"Cleaned up script in {filename}")
