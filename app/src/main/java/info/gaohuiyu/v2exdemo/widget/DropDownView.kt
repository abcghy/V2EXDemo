package info.gaohuiyu.v2exdemo.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.CheckedTextView
import android.widget.FrameLayout
import com.google.android.flexbox.FlexboxLayoutManager
import com.zhy.view.flowlayout.TagFlowLayout
import info.gaohuiyu.v2exdemo.R
import info.gaohuiyu.v2exdemo.data.model.CheckedWrapper
import info.gaohuiyu.v2exdemo.data.model.Tab
import info.gaohuiyu.v2exdemo.ui.dp2px
import kotlinx.android.synthetic.main.view_drop_down.view.*

class DropDownView : FrameLayout {
    val mDuration = 300L

    val HIDING  = 0
    val LOADING = 1
    val SHOWING = 2

    var showStatus: Int = HIDING

    private lateinit var mAdapter: TabAdapter

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        View.inflate(context, R.layout.view_drop_down, this)

        mAdapter = TabAdapter()
        rv.adapter = mAdapter

        val layoutManager = FlexboxLayoutManager(context)
        rv.layoutManager = layoutManager

        rbvBG.setOnClickListener {
            hide()
        }
    }

    fun setDatas(list: List<Tab>) {
        mAdapter.setDatas(list)
    }

    fun setOnSelectListener(onSelectedListener: OnSelectedListener) {
        mAdapter.onSelectedListener = onSelectedListener
    }

    fun show() {
        if ((showStatus == LOADING) or (showStatus == SHOWING)) {
            return
        }
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            visibility = View.VISIBLE
        }
        showStatus = LOADING
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(rbvBG, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(llTagContent, "translationY", -context.dp2px(200f).toFloat(), 0f)
        )
        animatorSet.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                showStatus = SHOWING
            }

            override fun onAnimationCancel(animation: Animator?) {
                super.onAnimationCancel(animation)
                showStatus = SHOWING
            }
        })
        animatorSet.interpolator = LinearInterpolator()
        animatorSet.duration = mDuration
        animatorSet.start()
    }

    fun toggle() {
        if (showStatus == LOADING) {
            return
        } else if (showStatus == SHOWING) {
            hide()
        } else if (showStatus == HIDING) {
            show()
        }
    }

    fun hide() {
        if ((showStatus == LOADING) or (showStatus == HIDING)) {
            return
        }
        showStatus = LOADING
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(rbvBG, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(llTagContent, "translationY", 0f, -context.dp2px(200f).toFloat())
        )
        animatorSet.interpolator = LinearInterpolator()
        animatorSet.duration = mDuration
        animatorSet.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                visibility = GONE
                showStatus = HIDING
            }

            override fun onAnimationCancel(animation: Animator?) {
                super.onAnimationCancel(animation)
                visibility = View.GONE
                showStatus = HIDING
            }
        })
        animatorSet.start()
    }
}

class TabAdapter: RecyclerView.Adapter<TabViewHolder>() {
    private var datas: List<Tab>? = null
    private var mCheckedPos: Int = -1

    var onSelectedListener: OnSelectedListener? = null

    fun setDatas(datas: List<Tab>) {
        this.datas = datas
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val viewHolder = TabViewHolder(parent)
        viewHolder.tvTagName.setOnClickListener {
            val thePos = viewHolder.adapterPosition
            mCheckedPos = thePos
            notifyDataSetChanged()
            onSelectedListener?.onSelected(mCheckedPos)
        }
        return viewHolder
    }

    override fun getItemCount(): Int {
        return datas?.size ?: 0
    }

    override fun onBindViewHolder(holder: TabViewHolder, pos: Int) {
        holder.bindTo(datas?.get(pos)!!, mCheckedPos == pos)
    }

}

class TabViewHolder: RecyclerView.ViewHolder {
    var tvTagName: CheckedTextView
    var tab: Tab? = null

    constructor(parent: ViewGroup) : super(LayoutInflater.from(parent.context).inflate(R.layout.view_tag, parent, false)) {
        tvTagName = itemView.findViewById(R.id.tvTagName)
    }

    fun bindTo(tab: Tab, isChecked: Boolean) {
        this.tab = tab

        tvTagName.text = tab.name
        tvTagName.isChecked = isChecked
    }
}

interface OnSelectedListener {
    fun onSelected(pos: Int)
}