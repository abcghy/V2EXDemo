package info.gaohuiyu.v2exdemo.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import info.gaohuiyu.v2exdemo.R
import kotlinx.android.synthetic.main.view_photo.view.*

class PhotoView: FrameLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        View.inflate(context, R.layout.view_photo, this)
    }

    fun show(url: String) {
        if (visibility == View.GONE) {
            visibility = View.VISIBLE
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
                .into(pvReal)
    }

    fun hide() {
        visibility = View.GONE
    }
}