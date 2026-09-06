$CurrentDir = Split-Path -Parent $MyInvocation.MyCommand.Path
& "C:\miniconda\python.exe" "C:\Users\long\.gemini\antigravity\scratch\rigorous-dev-workflow\cli.py" $args --target-dir $CurrentDir
