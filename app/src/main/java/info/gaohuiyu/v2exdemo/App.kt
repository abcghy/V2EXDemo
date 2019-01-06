package info.gaohuiyu.v2exdemo

import android.app.Application
import com.blankj.utilcode.util.Utils

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        Utils.init(this)
    }
}