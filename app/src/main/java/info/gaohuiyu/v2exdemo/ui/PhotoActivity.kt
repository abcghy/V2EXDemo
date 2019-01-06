package info.gaohuiyu.v2exdemo.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import info.gaohuiyu.v2exdemo.R
import kotlinx.android.synthetic.main.activity_photo.*

class PhotoActivity: AppCompatActivity() {
    companion object {
        fun openActivity(context: Context, url: String) {
            context.startActivity(Intent(context, PhotoActivity::class.java).apply {
                putExtra("url", url)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_photo)

        val url = intent.getStringExtra("url")
        if (url == null) {
            finish()
            return
        }

        pb.visibility = View.VISIBLE
        Glide.with(this)
                .load(url)
                .addListener(object: RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>,
                                              isFirstResource: Boolean): Boolean {
                        pb.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>,
                                                 dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        pb.visibility = View.GONE
                        return false
                    }

                })
                .into(pv)

    }
}