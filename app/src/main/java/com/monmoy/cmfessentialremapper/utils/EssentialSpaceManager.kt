package com.monmoy.cmfessentialremapper.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

enum class PackageInstallStatus(val label: String) {
    ENABLED("Active / Enabled"),
    DISABLED("Disabled"),
    NOT_INSTALLED("Not Installed")
}

data class EssentialSpaceStatus(
    val spacePackageStatus: PackageInstallStatus,
    val recorderPackageStatus: PackageInstallStatus,
    val isShizukuInstalled: Boolean,
    val isShizukuRunning: Boolean,
    val isShizukuPermissionGranted: Boolean
) {
    val areBothDisabled: Boolean
        get() {
            val hasDisabled = spacePackageStatus == PackageInstallStatus.DISABLED ||
                    recorderPackageStatus == PackageInstallStatus.DISABLED
            val noneInstalled = spacePackageStatus == PackageInstallStatus.NOT_INSTALLED &&
                    recorderPackageStatus == PackageInstallStatus.NOT_INSTALLED
            return hasDisabled || (noneInstalled && isShizukuReady)
        }

    val isTakeoverReady: Boolean
        get() = areBothDisabled

    val isShizukuReady: Boolean
        get() = isShizukuRunning && isShizukuPermissionGranted
}

/**
 * Handles package queries, manual App Info routing, and optional Shizuku
 * package management for Nothing OS Essential Space components.
 */
class EssentialSpaceManager(private val context: Context) {

    companion object {
        const val PKG_ESSENTIAL_SPACE = "com.nothing.ntessentialspace"
        const val PKG_ESSENTIAL_RECORDER = "com.nothing.ntessentialrecorder"
        const val PKG_SHIZUKU = "moe.shizuku.privileged.api"
    }

    fun getPackageStatus(packageName: String): PackageInstallStatus {
        return try {
            val flags = PackageManager.MATCH_DISABLED_COMPONENTS or PackageManager.MATCH_UNINSTALLED_PACKAGES
            val appInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(flags.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getApplicationInfo(
                    packageName,
                    flags
                )
            }

            if (appInfo.enabled) {
                PackageInstallStatus.ENABLED
            } else {
                PackageInstallStatus.DISABLED
            }
        } catch (_: PackageManager.NameNotFoundException) {
            PackageInstallStatus.NOT_INSTALLED
        }
    }

    fun isPackageInstalled(packageName: String): Boolean {
        return getPackageStatus(packageName) != PackageInstallStatus.NOT_INSTALLED
    }

    fun isShizukuInstalled(): Boolean {
        return isPackageInstalled(PKG_SHIZUKU)
    }

    fun isShizukuRunning(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }
    }

    fun isShizukuPermissionGranted(): Boolean {
        return try {
            if (isShizukuRunning()) {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            } else {
                false
            }
        } catch (_: Throwable) {
            false
        }
    }

    fun requestShizukuPermission(requestCode: Int) {
        try {
            if (isShizukuRunning() && !isShizukuPermissionGranted()) {
                Shizuku.requestPermission(requestCode)
            }
        } catch (_: Throwable) {}
    }

    fun getOverallStatus(): EssentialSpaceStatus {
        return EssentialSpaceStatus(
            spacePackageStatus = getPackageStatus(PKG_ESSENTIAL_SPACE),
            recorderPackageStatus = getPackageStatus(PKG_ESSENTIAL_RECORDER),
            isShizukuInstalled = isShizukuInstalled(),
            isShizukuRunning = isShizukuRunning(),
            isShizukuPermissionGranted = isShizukuPermissionGranted()
        )
    }

    fun createOpenAppInfoIntent(packageName: String): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun createDownloadShizukuIntent(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$PKG_SHIZUKU")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun createOpenShizukuIntent(): Intent? {
        return context.packageManager.getLaunchIntentForPackage(PKG_SHIZUKU)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    suspend fun setPackageEnabledShizuku(packageName: String, enable: Boolean): Result<Boolean> =
        withContext(Dispatchers.IO) {
            if (!isShizukuRunning()) {
                return@withContext Result.failure(IllegalStateException("Shizuku service is not running."))
            }
            if (!isShizukuPermissionGranted()) {
                return@withContext Result.failure(SecurityException("Shizuku permission not granted."))
            }

            if (!isPackageInstalled(packageName)) {
                return@withContext Result.success(true)
            }

            try {
                val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                    "newProcess",
                    Array<String>::class.java,
                    Array<String>::class.java,
                    String::class.java
                ).apply { isAccessible = true }

                fun executeCommand(cmd: Array<String>): Pair<Int, String> {
                    val process = newProcessMethod.invoke(null, cmd, null, null) as Process
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val output = reader.readText()
                    val exitCode = process.waitFor()
                    val errorReader = BufferedReader(InputStreamReader(process.errorStream))
                    val err = errorReader.readText()
                    return Pair(exitCode, "$output $err".trim())
                }

                val primaryCommand = if (enable) {
                    arrayOf("pm", "enable", packageName)
                } else {
                    arrayOf("pm", "disable-user", "--user", "0", packageName)
                }

                var (exitCode, output) = executeCommand(primaryCommand)

                var isTargetState = if (enable) {
                    getPackageStatus(packageName) == PackageInstallStatus.ENABLED
                } else {
                    getPackageStatus(packageName) != PackageInstallStatus.ENABLED
                }

                // If primary disable command did not work, try fallback command: pm disable
                if (!enable && !isTargetState && exitCode != 0) {
                    val fallback = executeCommand(arrayOf("pm", "disable", packageName))
                    exitCode = fallback.first
                    output = fallback.second
                    isTargetState = getPackageStatus(packageName) != PackageInstallStatus.ENABLED
                }

                if (exitCode == 0 || isTargetState) {
                    Result.success(true)
                } else {
                    Result.failure(RuntimeException("Command failed ($exitCode): $output"))
                }
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }

    suspend fun disableBothEssentialPackages(): Result<Boolean> = withContext(Dispatchers.IO) {
        val r1 = setPackageEnabledShizuku(PKG_ESSENTIAL_SPACE, false)
        val r2 = setPackageEnabledShizuku(PKG_ESSENTIAL_RECORDER, false)
        if (r1.isSuccess || r2.isSuccess) {
            Result.success(true)
        } else {
            r1
        }
    }

    suspend fun enableBothEssentialPackages(): Result<Boolean> = withContext(Dispatchers.IO) {
        val r1 = setPackageEnabledShizuku(PKG_ESSENTIAL_SPACE, true)
        val r2 = setPackageEnabledShizuku(PKG_ESSENTIAL_RECORDER, true)
        if (r1.isSuccess && r2.isSuccess) {
            Result.success(true)
        } else {
            r1
        }
    }
}

