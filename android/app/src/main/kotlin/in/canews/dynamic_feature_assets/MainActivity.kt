package `in`.canews.dynamic_feature_assets

import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PersistableBundle
import android.util.Log
import androidx.annotation.NonNull;
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.jeppeman.locallydynamic.LocallyDynamicSplitInstallManagerFactory
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import java.util.*
import java.util.logging.StreamHandler
import kotlin.collections.HashMap

class MainActivity : FlutterActivity() {

    private var archName = ""
    private var splitInstallManager: SplitInstallManager? = null
    private var mSessionId = -1;

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        LocallyDynamicSplitInstallManagerFactory.create(this)
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine)
        EventChannel(flutterEngine.dartExecutor,"moduleInstall").setStreamHandler(
                object : EventChannel.StreamHandler {
                    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                        loadAndLaunchModule("arm64",events);
                    }

                    override fun onCancel(arguments: Any?) {

                    }

                }
        )
    }

    /**
     * Load a feature by module name.
     * @param name The name of the feature module to load.
     */
    private fun loadAndLaunchModule(name: String, eventSink: EventChannel.EventSink?) {
        if (isModuleInstalled(name) == true)
            return
        val request = SplitInstallRequest.newBuilder()
                .addModule(name)
                .build()
        splitInstallManager?.startInstall(request)?.addOnSuccessListener { sessionId ->
            mSessionId = sessionId
        }
        splitInstallManager?.registerListener { state ->
            if (state.sessionId() == mSessionId) {
                when (state.status()) {
                    SplitInstallSessionStatus.DOWNLOADING -> {

                        val status: Int = state.status()
                        val downloaded = state.bytesDownloaded()
                        val total = state.totalBytesToDownload()

                        val map: HashMap<String, Any> = HashMap()
                        map["status"] = status
                        map["downloaded"] = downloaded
                        map["total"] = total
                        Log.d("MainActivity>Download:", "\n\n" + map.toMap() + "\n\n")
                        eventSink?.success(map)
                    }
                    SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                        Log.d("MainActivity>LoadModule", "\n\n" + "SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION" + "\n\n")
                        startIntentSender(state.resolutionIntent()?.intentSender, null, 0, 0, 0)
                    }
                    SplitInstallSessionStatus.CANCELED -> {
                        Log.d("MainActivity>LoadModule", "\n\n" + "SplitInstallSessionStatus.CANCELED" + "\n\n")
                    }
                    SplitInstallSessionStatus.CANCELING -> {
                        Log.d("MainActivity>LoadModule", "\n\n" + "SplitInstallSessionStatus.CANCELING" + "\n\n")
                    }
                    SplitInstallSessionStatus.DOWNLOADED -> {
                        Log.d("MainActivity>LoadModule", "\n\n" + "SplitInstallSessionStatus.DOWNLOADED" + "\n\n")
                    }
                    SplitInstallSessionStatus.FAILED -> {
                        Log.d("MainActivity>LoadModule", "\n\n" + "SplitInstallSessionStatus.FAILED" + "\n\n")
                    }
                    SplitInstallSessionStatus.INSTALLED -> {
                        Log.d("MainActivity>LoadModule", "\n\n" + "SplitInstallSessionStatus.INSTALLED" + "\n\n")
                    }
                    SplitInstallSessionStatus.INSTALLING -> {
                        Log.d("MainActivity>LoadModule", "\n\n" + "SplitInstallSessionStatus.INSTALLING" + "\n\n")
                    }
                    SplitInstallSessionStatus.PENDING -> {
                        Log.d("MainActivity>LoadModule", "\n\n" + "SplitInstallSessionStatus.PENDING" + "\n\n")
                    }
                    SplitInstallSessionStatus.UNKNOWN -> {
                        Log.d("MainActivity>LoadModule", "\n\n" + "SplitInstallSessionStatus.UNKNOWN" + "\n\n")
                    }
                }
            }
        }
    }

    private fun isModuleInstalled(name: String): Boolean? =
            splitInstallManager?.installedModules?.contains(name)

    private fun isRequiredModulesInstalled(): Boolean = isModuleInstalled("common") == true &&
            isModuleInstalled(archName) == true

    private fun getArchName() {
        val arch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Build.SUPPORTED_ABIS
        } else {
            TODO("VERSION.SDK_INT < LOLLIPOP")
        }
        archName = if (arch.contains("arm64-v8a")) {
            "arm64"
        } else if (arch.contains("armeabi-v7a")) {
            "arm"
        } else if (arch.contains("x86_64")) {
            "x86_64"
        } else {
            "x86"
        }
    }

}