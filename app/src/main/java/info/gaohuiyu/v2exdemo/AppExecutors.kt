package info.gaohuiyu.v2exdemo

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class AppExecutors {

    companion object {
        private val diskIO = Executors.newSingleThreadExecutor()
        private val networkIO = Executors.newFixedThreadPool(3)
        private val mainThread = MainThreadExecutor()

        fun diskIO() = diskIO
        fun networkIO() = networkIO
        fun mainThread() = mainThread
    }

    class MainThreadExecutor: Executor {
        private val mainThreadHandler= Handler(Looper.getMainLooper())

        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}