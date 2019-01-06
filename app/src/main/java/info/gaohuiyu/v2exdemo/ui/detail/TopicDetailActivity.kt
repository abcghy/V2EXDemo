package info.gaohuiyu.v2exdemo.ui.detail

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import info.gaohuiyu.v2exdemo.R
import info.gaohuiyu.v2exdemo.data.api.V2EXApi
import info.gaohuiyu.v2exdemo.data.config.baseUrl
import info.gaohuiyu.v2exdemo.data.config.memberSuffix
import info.gaohuiyu.v2exdemo.data.config.pageSuffix
import info.gaohuiyu.v2exdemo.data.config.photoExtension
import info.gaohuiyu.v2exdemo.data.db.AppDatabase
import info.gaohuiyu.v2exdemo.data.model.Comment
import info.gaohuiyu.v2exdemo.data.model.CommentHeader
import info.gaohuiyu.v2exdemo.data.model.Subtitle
import info.gaohuiyu.v2exdemo.data.model.TopicDetail
import info.gaohuiyu.v2exdemo.data.repository.TopicRepository
import info.gaohuiyu.v2exdemo.ui.dp2px
import info.gaohuiyu.v2exdemo.util.getValueByRegex
import info.gaohuiyu.v2exdemo.widget.RichTextView
import kotlinx.android.synthetic.main.activity_topic_detail.*

class TopicDetailActivity : AppCompatActivity(), OnRefreshListener, OnLoadMoreListener {
    companion object {
        fun openActivity(context: Context, topicId: Long) {
            context.startActivity(Intent(context, TopicDetailActivity::class.java).apply {
                putExtra("topicId", topicId)
            })
        }
    }

    private lateinit var mAdapter: TopicDetailAdapter
    private lateinit var mViewModel: TopicViewModel
    private lateinit var itemDecoration: TopicDetailItemDecoration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_detail)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        supportActionBar?.elevation = 0f

        mAdapter = TopicDetailAdapter(object: OnPhotoShowListener {
            override fun onShow(url: String) {
                pv.show(url)
            }

        })
        rv.adapter = mAdapter

        itemDecoration = TopicDetailItemDecoration(this)
        rv.addItemDecoration(itemDecoration)

        rv.layoutManager = LinearLayoutManager(this)

        srl.setOnRefreshListener(this)
        srl.setOnLoadMoreListener(this)

        val topicId =
                intent.getLongExtra("topicId", -1).apply {
                    if (this == -1L) {
                        finish()
                        return
                    }
                }
        mViewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val db = Room.databaseBuilder(this@TopicDetailActivity, AppDatabase::class.java, "V2ex.db")
                        .fallbackToDestructiveMigration()
                        .build()
                return TopicViewModel(TopicRepository(V2EXApi(), db), topicId) as T
            }

        }).get(TopicViewModel::class.java)

        subscribeToUI(mViewModel)

        srl.autoRefresh()
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        mViewModel.refresh()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        mViewModel.loadMore()
    }

    private fun subscribeToUI(viewModel: TopicViewModel) {
        viewModel.topicLiveData.observe(this, Observer {

            if (it != null) {
                srl.finishRefresh()
//                srl.finishLoadMore()

                itemDecoration.subtitleCount = getSubtitleCount(it)
                Log.d("test", "it.data: ${it}")
                mAdapter.setData(it)
            } else {
                Toast.makeText(this@TopicDetailActivity, "Empty", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.isMore.observe(this, Observer {
            if (it != null) {
//                srl.setEnableLoadMore(it)
                if (it) {
                    srl.finishLoadMore()
                } else {
                    srl.finishLoadMoreWithNoMoreData()
                }
            }
        })
    }

    private fun getSubtitleCount(list: List<Any>): Int {
        return list.count {
            it is Subtitle
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (pv.visibility == View.VISIBLE) {
            pv.hide()
        } else {
            super.onBackPressed()
        }
    }
}

interface OnPhotoShowListener {
    fun onShow(url: String)
}

class TopicDetailItemDecoration : RecyclerView.ItemDecoration {
    val dividerPaint: Paint
    val commentPaint: Paint
    var subtitleCount: Int

    constructor(context: Context, subtitleCount: Int = 0) : super() {
        this.subtitleCount = subtitleCount

        dividerPaint = Paint()
        dividerPaint.isAntiAlias = true
        dividerPaint.color = context.resources.getColor(R.color.Lina)
        dividerPaint.strokeWidth = context.dp2px(1f).toFloat()
        dividerPaint.style = Paint.Style.STROKE

        commentPaint = Paint()
        commentPaint.isAntiAlias = true
        commentPaint.color = context.resources.getColor(R.color.Line)
        commentPaint.strokeWidth = context.dp2px(1f).toFloat()
        commentPaint.style = Paint.Style.STROKE
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount

        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            when {
                position < subtitleCount -> {
                    val bottom = child.bottom
                    c.drawLine(0f, bottom.toFloat(), parent.width.toFloat(), bottom.toFloat(), dividerPaint)
                }
                position == subtitleCount -> {
                    // nothing
                }
                position == subtitleCount + 1 -> {
                    val bottom = child.bottom
                    c.drawLine(parent.context.dp2px(16f).toFloat(), bottom.toFloat(),
                            parent.width.toFloat(), bottom.toFloat(), commentPaint)
                }
                else -> {
                    val bottom = child.bottom
                    c.drawLine(parent.context.dp2px(68f).toFloat(), bottom.toFloat(),
                            parent.width.toFloat(), bottom.toFloat(), commentPaint)
                }
            }
        }
    }
}

class TopicDetailAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    val onPhotoShowListener: OnPhotoShowListener

    constructor(onPhotoShowListener: OnPhotoShowListener) : super() {
        this.onPhotoShowListener = onPhotoShowListener
    }

    companion object {
        val HEADER = 0
        val SUBTITLE = 1
        val COMMENT_HEADER = 2
        val COMMENT = 3
    }

    private var datas: List<Any>? = null

    fun setData(list: List<Any>) {
        if (datas == null) {
            datas = list
            notifyItemRangeInserted(0, datas?.size!!)
        } else {
            val newDatas = list
//            Log.d("test", "oldCount: ${datas?.size}, newCount: ${newDatas.size}")
//            val diffResult = DiffUtil.calculateDiff(object: DiffUtil.Callback() {
//                override fun getOldListSize(): Int {
//                    return this@TopicDetailAdapter.datas!!.size
//                }
//
//                override fun getNewListSize(): Int {
//                    return newDatas.size
//                }
//
//                override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
//                    val oldItem = datas?.get(oldPosition)
//                    val newItem = newDatas[newPosition]
//                    return oldItem == newItem
//                }
//
//                override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
//                    val oldItem = datas?.get(oldPosition)
//                    val newItem = newDatas[newPosition]
//                    return oldItem == newItem
//                }
//            })
            datas = newDatas
            notifyDataSetChanged()
//            diffResult.dispatchUpdatesTo(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER -> {
                HeaderViewHolder(parent, onPhotoShowListener)
            }
            SUBTITLE -> {
                SubtitleViewHolder(parent, onPhotoShowListener)
            }
            COMMENT_HEADER -> {
                CommentHeaderViewHolder(parent)
            }
            COMMENT -> {
                CommentViewHolder(parent, onPhotoShowListener)
            }
            else -> {
                HeaderViewHolder(parent, onPhotoShowListener)
            }
        }
    }

    override fun getItemCount(): Int {
        return datas?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                holder.bindTo(datas?.get(holder.adapterPosition) as TopicDetail)
            }
            is SubtitleViewHolder -> {
                holder.bindTo(datas?.get(holder.adapterPosition) as Subtitle)
            }
            is CommentHeaderViewHolder -> {
                holder.bindTo(datas?.get(holder.adapterPosition) as CommentHeader)
            }
            is CommentViewHolder -> {
                holder.bindTo(datas?.get(holder.adapterPosition) as Comment)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (datas?.get(position)) {
            is TopicDetail -> HEADER
            is Subtitle -> SUBTITLE
            is CommentHeader -> COMMENT_HEADER
            is Comment -> COMMENT
            else -> COMMENT_HEADER
        }
    }
}

class HeaderViewHolder : RecyclerView.ViewHolder {

    private var tvNode: TextView? = null
    private var tvTitle: TextView? = null
    private var tvContent: RichTextView? = null
    private var tvPublisherName: TextView? = null
    private var tvPublishTime: TextView? = null
    private var tvClickCount: TextView? = null

    constructor(parent: ViewGroup, onPhotoShowListener: OnPhotoShowListener) : super(
            LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_topic_detail_header, parent, false)
    ) {
        tvNode = itemView.findViewById(R.id.tvNode)
        tvTitle = itemView.findViewById(R.id.tvTitle)
        tvContent = itemView.findViewById(R.id.tvContent)
        tvPublisherName = itemView.findViewById(R.id.tvPublisherName)
        tvPublishTime = itemView.findViewById(R.id.tvPublishTime)
        tvClickCount = itemView.findViewById(R.id.tvClickCount)

        tvContent?.setOnRichTextClickListener(object : RichTextView.RichTextClickListener {
            override fun onUrlClick(url: String) {
                if (url.startsWith(baseUrl + pageSuffix) or url.startsWith(pageSuffix)) {
                    val topicId = getValueByRegex(url, "/t/(\\d*)").run {
                        this.toLong()
                    }
                    TopicDetailActivity.openActivity(itemView.context, topicId)
                    return
                }
                if (url.startsWith(memberSuffix)) {
                    //
                    Toast.makeText(itemView.context, "跳转到成员页面", Toast.LENGTH_SHORT).show()
                    return
                }
                if (photoExtension.any {
                            url.endsWith(it)
                        }) {
                    onPhotoShowListener.onShow(url)
                    return
                }
                val uri = Uri.parse(url)
                itemView.context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }

            override fun onImageClick(url: String) {
                onPhotoShowListener.onShow(url)
            }
        })
    }

    fun bindTo(topicDetail: TopicDetail) {
        tvNode?.text = topicDetail.node
        tvTitle?.text = topicDetail.title
        topicDetail.content?.apply {
            tvContent?.setHtml(this, itemView.context.let {
                return@let it.resources.displayMetrics.widthPixels - it.dp2px(32f)
            })
        }
        tvPublisherName?.text = topicDetail.publisher?.name
        tvPublishTime?.text = topicDetail.publishTime
        tvClickCount?.text = topicDetail.clickCount
    }
}

class SubtitleViewHolder : RecyclerView.ViewHolder {
    private var tvOrder: TextView? = null
    private var tvDate: TextView? = null
    private var tvContent: RichTextView? = null

    constructor(parent: ViewGroup, onPhotoShowListener: OnPhotoShowListener)
            : super(LayoutInflater.from(parent.context).inflate(R.layout.item_topic_subtitle, parent, false)) {
        tvOrder = itemView.findViewById(R.id.tvOrder)
        tvDate = itemView.findViewById(R.id.tvDate)
        tvContent = itemView.findViewById(R.id.tvContent)

        tvContent?.setOnRichTextClickListener(object : RichTextView.RichTextClickListener {
            override fun onUrlClick(url: String) {
                if (url.startsWith(baseUrl + pageSuffix) or url.startsWith(pageSuffix)) {
                    val topicId = getValueByRegex(url, "/t/(\\d*)").run {
                        this.toLong()
                    }
                    TopicDetailActivity.openActivity(itemView.context, topicId)
                    return
                }
                if (url.startsWith(memberSuffix)) {
                    //
                    Toast.makeText(itemView.context, "跳转到成员页面", Toast.LENGTH_SHORT).show()
                    return
                }
                if (photoExtension.any {
                            url.endsWith(it)
                        }) {
                    onPhotoShowListener.onShow(url)
                    return
                }
                val uri = Uri.parse(url)
                itemView.context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }

            override fun onImageClick(url: String) {
                onPhotoShowListener.onShow(url)
            }
        })
    }

    fun bindTo(subtitle: Subtitle) {
        tvOrder?.text = "第${adapterPosition}条附言"
        tvDate?.text = subtitle.time
        tvContent?.setHtml(subtitle.content, itemView.context.let {
            return@let it.resources.displayMetrics.widthPixels - it.dp2px(32f)
        })
    }
}

class CommentHeaderViewHolder : RecyclerView.ViewHolder {
    private var tvCommentCount: TextView? = null
    private var ivSort: ImageView? = null

    constructor(parent: ViewGroup) : super(LayoutInflater.from(parent.context).inflate(R.layout.item_topic_comment_header, parent, false)) {
        tvCommentCount = itemView.findViewById(R.id.tvCommentCount)
        ivSort = itemView.findViewById(R.id.ivSort)
    }

    fun bindTo(commentHeader: CommentHeader) {
        tvCommentCount?.text = "回复(${commentHeader.replyCount})"
    }
}

class CommentViewHolder : RecyclerView.ViewHolder {
    private var ivAvatar: ImageView? = null
    private var tvName: TextView? = null
    private var tvReply: TextView? = null
    private var tvOrder: TextView? = null
    private var tvDate: TextView? = null
    private var tvContent: RichTextView? = null

    constructor(parent: ViewGroup, onPhotoShowListener: OnPhotoShowListener) : super(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_topic_comment, parent, false)) {
        ivAvatar = itemView.findViewById(R.id.ivAvatar)
        tvName = itemView.findViewById(R.id.tvName)
        tvContent = itemView.findViewById(R.id.tvContent)
        tvReply = itemView.findViewById(R.id.tvReply)
        tvOrder = itemView.findViewById(R.id.tvOrder)
        tvDate = itemView.findViewById(R.id.tvDate)

        tvContent?.setOnRichTextClickListener(object : RichTextView.RichTextClickListener {
            override fun onUrlClick(url: String) {
                if (url.startsWith(baseUrl + pageSuffix) or url.startsWith(pageSuffix)) {
                    val topicId = getValueByRegex(url, "/t/(\\d*)").run {
                        this.toLong()
                    }
                    TopicDetailActivity.openActivity(itemView.context, topicId)
                    return
                }
                if (url.startsWith(memberSuffix)) {
                    //
                    Toast.makeText(itemView.context, "跳转到成员页面", Toast.LENGTH_SHORT).show()
                    return
                }
                if (photoExtension.any {
                            url.endsWith(it)
                        }) {
                    onPhotoShowListener.onShow(url)
                    return
                }
                val uri = Uri.parse(url)
                itemView.context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }

            override fun onImageClick(url: String) {
                onPhotoShowListener.onShow(url)
            }
        })
    }

    fun bindTo(comment: Comment) {
        Glide.with(itemView.context)
                .load(comment.commenter.avatar)
                .apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(itemView.context.dp2px(4f))))
                .into(ivAvatar!!)
        tvOrder?.text = comment.order
        tvName?.text = comment.commenter.name
        tvContent?.setHtml(comment.content, itemView.context.let {
            return@let it.resources.displayMetrics.widthPixels - it.dp2px(84f)
        })

        tvDate?.text = comment.time
    }
}