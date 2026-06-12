# ============================================================
# ProRF 模拟器启动脚本
# 用法：
#   .\start-emulator.ps1              # 只启动模拟器
#   .\start-emulator.ps1 -Install     # 启动 + 安装最新 debug APK
#   .\start-emulator.ps1 -Install -Launch  # 启动 + 安装 + 打开 App
#
# 说明：
#   - 每次通过此脚本启动模拟器，避免残留进程导致的失败
#   - 脚本会自动等待开机完成，再执行后续操作
#   - 日志写到 emulator-run.log，方便排查
# ============================================================

param(
    [switch]$Install,   # 安装 debug APK
    [switch]$Launch     # 安装后自动打开 App
)

$SDK   = "$env:LOCALAPPDATA\Android\Sdk"
$ADB   = "$SDK\platform-tools\adb.exe"
$EMU   = "$SDK\emulator\emulator.exe"
$AVD   = "ProRF_Test"
$APK   = "$PSScriptRoot\android\app\build\outputs\apk\debug\app-debug.apk"
$LOG   = "$PSScriptRoot\emulator-run.log"
$PKG   = "com.prorf.app"

# ── 1. 清理残留进程 ──────────────────────────────────────────
Write-Host "[1/4] 清理残留的模拟器进程..." -ForegroundColor Cyan
Get-Process -Name "qemu-system-x86_64","emulator" -ErrorAction SilentlyContinue |
    Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Milliseconds 800

& $ADB kill-server 2>$null | Out-Null
Start-Sleep -Milliseconds 500
& $ADB start-server 2>$null | Out-Null

# ── 2. 启动模拟器（后台） ────────────────────────────────────
Write-Host "[2/4] 启动 AVD: $AVD ..." -ForegroundColor Cyan
$emuArgs = @(
    "-avd", $AVD,
    "-no-snapshot-save",      # 不保存快照，但可读取已有快照加速启动
    "-gpu", "swiftshader_indirect",  # 纯软件渲染，最稳定（避免 GPU 驱动崩溃）
    "-no-boot-anim",          # 跳过开机动画，节省时间
    "-no-audio",              # 禁用音频，避免 Windows 音频驱动问题
    "-feature", "-Vulkan,-GLDirectMem,-VulkanNativeSwapChain"  # 禁用 Vulkan，防止 CPU 线程挂起
)
Start-Process -FilePath $EMU -ArgumentList $emuArgs `
    -RedirectStandardOutput $LOG -RedirectStandardError "$LOG.err" `
    -NoNewWindow -PassThru | Out-Null

# ── 3. 等待开机完成 ──────────────────────────────────────────
Write-Host "[3/4] 等待模拟器开机..." -ForegroundColor Cyan
& $ADB wait-for-device | Out-Null

$maxWait = 120   # 最长等待 120 秒
$elapsed = 0
while ($elapsed -lt $maxWait) {
    $boot = & $ADB shell getprop sys.boot_completed 2>$null
    if ($boot.Trim() -eq "1") { break }
    Start-Sleep -Seconds 5
    $elapsed += 5
    Write-Host "  ... 已等待 ${elapsed}s" -ForegroundColor DarkGray
}

if ($elapsed -ge $maxWait) {
    Write-Host "[ERROR] 模拟器开机超时，请检查 $LOG" -ForegroundColor Red
    exit 1
}
Write-Host "  模拟器已就绪 (${elapsed}s)" -ForegroundColor Green

# ── 4. 安装 APK（可选） ──────────────────────────────────────
if ($Install) {
    if (-not (Test-Path $APK)) {
        Write-Host "[ERROR] 找不到 APK: $APK" -ForegroundColor Red
        Write-Host "        请先执行: gradle assembleDebug" -ForegroundColor Yellow
        exit 1
    }
    Write-Host "[4/4] 安装 APK（等待 package manager 就绪）..." -ForegroundColor Cyan
    Start-Sleep -Seconds 5   # 等 package manager 完全启动，避免 Broken pipe
    $result = & $ADB install -r $APK 2>&1
    if ($result -match "Success") {
        Write-Host "  安装成功" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] 安装失败: $result" -ForegroundColor Red
        exit 1
    }

    if ($Launch) {
        Write-Host "  启动 App..." -ForegroundColor Cyan
        & $ADB shell monkey -p $PKG -c android.intent.category.LAUNCHER 1 | Out-Null
        Write-Host "  App 已启动" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "完成！模拟器已运行在 emulator-5554" -ForegroundColor Green
Write-Host "  截图: adb exec-out screencap -p > shot.png" -ForegroundColor DarkGray
Write-Host "  日志: $LOG" -ForegroundColor DarkGray
