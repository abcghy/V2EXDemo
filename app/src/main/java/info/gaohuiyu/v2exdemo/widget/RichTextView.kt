package info.gaohuiyu.v2exdemo.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition

class RichTextView: TextView {
    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        movementMethod = LinkMovementMethod.getInstance()
    }

    fun setHtml(htmlSource: String?, width: Int) {
        val html = Html.fromHtml(htmlSource, ImageGetter(context, this@RichTextView, width), null)
        setImageClickable(html as SpannableStringBuilder)
        text = html
    }

    private var imageSpans: Array<ImageSpan>? = null
    private var imageUrls: Array<String>? = null

    private var imageClickListener: ImageClickListener? = null

    fun setOnImageClickListener(imageClickListener: ImageClickListener) {
        this.imageClickListener = imageClickListener
    }

    fun setImageClickable(spannableStringBuilder: SpannableStringBuilder) {
        imageSpans = spannableStringBuilder.getSpans(0, spannableStringBuilder.length, ImageSpan::class.java)
        imageUrls = Array(imageSpans!!.size) {
            imageSpans?.get(it)?.source!!
        }

        for (i in 0 until imageSpans?.size!!) {
            val imageSpan = imageSpans!![i]

            val start = spannableStringBuilder.getSpanStart(imageSpan)
            val end = spannableStringBuilder.getSpanEnd(imageSpan)

            spannableStringBuilder.setSpan(object: ClickableSpan() {
                override fun onClick(widget: View) {
                    Log.d("test", "${i}")
                    imageClickListener?.onImageClick(imageUrls!![i])
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    interface ImageClickListener {
        fun onImageClick(url: String)
    }
}

class ImageGetter: Html.ImageGetter {

    val context: Context
    val textView: RichTextView
    val textViewWidth: Float
//    val screenWidth: Float

    constructor(context: Context, textView: RichTextView, width: Int) {
        this.context = context
        this.textView = textView
        // todo 修改成tv宽度
        this.textViewWidth = width.toFloat()
//        this.screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
//        Log.d("test", "width: ${this.textView.width}")
    }

    override fun getDrawable(source: String?): Drawable {
        var urlDrawable = URLDrawable()

        Glide.with(context)
                .asBitmap()
                .load(source)
                .into(object: SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val scale = textViewWidth / resource.width

                        val matrix = Matrix()
                        matrix.postScale(scale, scale)

                        val scaledBitmap = Bitmap.createBitmap(resource, 0, 0, resource.width, resource.height, matrix, true)

                        urlDrawable.mBitmap = scaledBitmap
                        urlDrawable.setBounds(0, 0, scaledBitmap.width, scaledBitmap.height)
                        textView.text = textView.text
                        textView.invalidate()
                    }
                })
        return urlDrawable
    }

}

class URLDrawable : BitmapDrawable() {
    var mBitmap: Bitmap? = null

    override fun draw(canvas: Canvas) {
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0f, 0f, getPaint())
        }
    }
}