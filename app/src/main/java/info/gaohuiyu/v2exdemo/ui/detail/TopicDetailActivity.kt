package info.gaohuiyu.v2exdemo.ui.detail

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import info.gaohuiyu.v2exdemo.R
import info.gaohuiyu.v2exdemo.data.api.V2EXApi
import info.gaohuiyu.v2exdemo.data.model.Comment
import info.gaohuiyu.v2exdemo.data.model.Subtitle
import info.gaohuiyu.v2exdemo.data.model.TopicDetail
import info.gaohuiyu.v2exdemo.data.repository.TopicRepository
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        mAdapter = TopicDetailAdapter()
        rv.adapter = mAdapter

        rv.layoutManager = LinearLayoutManager(this)

        srl.setOnRefreshListener(this)
        srl.setOnLoadMoreListener(this)

        val topicId = intent.getLongExtra("topicId", -1).apply {
            if (this == -1L) {
                finish()
                return
            }
        }
        mViewModel = ViewModelProviders.of(this, object: ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return TopicViewModel(TopicRepository(V2EXApi()), topicId) as T
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
                srl.finishLoadMore()

                mAdapter.setData(it)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

class TopicDetailAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        val HEADER = 0
        val SUBTITLE = 1
        val COMMENT = 2
    }

    private var datas: List<Any>? = null

    fun setData(list: List<Any>) {
        if (datas == null) {
            datas = list
            notifyItemRangeInserted(0, datas?.size!!)
        } else {
            val newDatas = list
            val diffResult = DiffUtil.calculateDiff(object: DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return this@TopicDetailAdapter.datas!!.size
                }

                override fun getNewListSize(): Int {
                    return newDatas.size
                }

                override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                    val oldItem = datas?.get(oldPosition)
                    val newItem = newDatas[newPosition]
                    return oldItem == newItem
                }

                override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                    val oldItem = datas?.get(oldPosition)
                    val newItem = newDatas[newPosition]
                    return oldItem == newItem
                }
            })
            datas = newDatas
            diffResult.dispatchUpdatesTo(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER -> {
                HeaderViewHolder(parent)
            }
            SUBTITLE -> {
                SubtitleViewHolder(parent)
            }
            COMMENT -> {
                CommentViewHolder(parent)
            }
            else -> {
                HeaderViewHolder(parent)
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
            is CommentViewHolder -> {
                holder.bindTo(datas?.get(holder.adapterPosition) as Comment)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (datas?.get(position)) {
            is TopicDetail -> HEADER
            is Subtitle -> SUBTITLE
            is Comment -> COMMENT
            else -> HEADER
        }
    }
}

class HeaderViewHolder : RecyclerView.ViewHolder {

    private var tvNode: TextView? = null
    private var tvTitle: TextView? = null
    private var tvContent: TextView? = null
    private var tvPublisherName: TextView? = null
    private var tvPublishTime: TextView? = null
    private var tvClickCount: TextView? = null

    constructor(parent: ViewGroup) : super(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_topic_detail_header, parent, false)
    ) {
        tvNode = itemView.findViewById(R.id.tvNode)
        tvTitle = itemView.findViewById(R.id.tvTitle)
        tvContent = itemView.findViewById(R.id.tvContent)
        tvPublisherName = itemView.findViewById(R.id.tvPublisherName)
        tvPublishTime = itemView.findViewById(R.id.tvPublishTime)
        tvClickCount = itemView.findViewById(R.id.tvClickCount)
    }

    fun bindTo(topicDetail: TopicDetail) {
        tvNode?.text = topicDetail.node
        tvTitle?.text = topicDetail.title
        tvContent?.text = topicDetail.content
        tvPublisherName?.text = topicDetail.publisher?.name
        tvPublishTime?.text = topicDetail.publishTime
        tvClickCount?.text = topicDetail.clickCount
    }
}

class SubtitleViewHolder : RecyclerView.ViewHolder {
    private var tvDate: TextView? = null
    private var tvContent: TextView? = null

    constructor(parent: ViewGroup) : super(LayoutInflater.from(parent.context).inflate(R.layout.item_topic_subtitle, parent, false)) {
        tvDate = itemView.findViewById(R.id.tvDate)
        tvContent = itemView.findViewById(R.id.tvContent)
    }

    fun bindTo(subtitle: Subtitle) {
        tvDate?.text = subtitle.time
        tvContent?.text = subtitle.content
    }
}

class CommentViewHolder : RecyclerView.ViewHolder {
    private var tvName: TextView? = null
    private var tvContent: TextView? = null

    constructor(parent: ViewGroup) : super(LayoutInflater.from(parent.context).inflate(R.layout.item_topic_comment, parent, false)) {
        tvName = itemView.findViewById(R.id.tvName)
        tvContent = itemView.findViewById(R.id.tvContent)
    }

    fun bindTo(comment: Comment) {
        tvName?.text = comment.commenter.name
        tvContent?.text = comment.content
    }
}