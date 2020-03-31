package `in`.canews.dynamic_feature_assets

import android.content.Context
import com.google.android.play.core.splitcompat.SplitCompat
import io.flutter.app.FlutterApplication


internal class MyApplication : FlutterApplication(){
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        SplitCompat.install(base)
    }
}