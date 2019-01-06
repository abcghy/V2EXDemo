package info.gaohuiyu.v2exdemo.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.arch.persistence.room.Room
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import info.gaohuiyu.v2exdemo.R
import info.gaohuiyu.v2exdemo.data.api.V2EXApi
import info.gaohuiyu.v2exdemo.data.db.AppDatabase
import info.gaohuiyu.v2exdemo.data.model.Topic
import info.gaohuiyu.v2exdemo.data.repository.TopicRepository
import info.gaohuiyu.v2exdemo.domain.Status
import info.gaohuiyu.v2exdemo.ui.detail.TopicDetailActivity
import info.gaohuiyu.v2exdemo.widget.OnSelectedListener
import kotlinx.android.synthetic.main.activity_main.*

fun Context.dp2px(dp: Float): Int = (resources.displayMetrics.density * dp + 0.5f).toInt()

class MainActivity : AppCompatActivity(), OnRefreshListener {

    private lateinit var mAdapter: MainAdapter

    private lateinit var mViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        toolbar.setOnClickListener {
            ddvTag.toggle()
        }

        mAdapter = MainAdapter(object : OnSelectListener {
            override fun onSelect(topic: Topic?) {
                if (topic != null) {
                    TopicDetailActivity.openActivity(this@MainActivity, topicId = topic.id)
                }
            }

        })
        rv.adapter = mAdapter

        rv.layoutManager = LinearLayoutManager(this)
        rv.addItemDecoration(LinearItemDecoration(this))

        mViewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val db = Room.databaseBuilder(this@MainActivity, AppDatabase::class.java, "V2ex.db")
                        .fallbackToDestructiveMigration()
                        .build()
                return MainViewModel(TopicRepository(V2EXApi(), db)) as T
            }
        }).get(MainViewModel::class.java)

        srl.setEnableLoadMore(false)
        srl.setOnRefreshListener(this)

        subscribeUI(mViewModel)

        initData()
        setListener()
        srl.autoRefresh()
    }

    fun initData() {
        ddvTag.setDatas(mViewModel.tabs)
    }

    fun setListener() {
        ddvTag.setOnSelectListener(object : OnSelectedListener {
            override fun onSelected(pos: Int) {
                mViewModel.selectTag(pos)
            }
        })
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        mViewModel.refresh()
    }

    private fun subscribeUI(viewModel: MainViewModel) {
        viewModel.topics.observe(this, Observer {
            if (it != null) {
                when (it.status) {
                    Status.SUCCESS -> {
                        Log.d("test", "it.data: ${it.data.hashCode()}")
                        mAdapter.setList(it.data!!)
                        srl.finishRefresh()
                        srl.finishLoadMore()
                    }
                    Status.ERROR -> {
                        Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show()
                        srl.finishRefresh()
                        srl.finishLoadMore()
                    }
                }
            }
        })

        viewModel.currentTab.observe(this, Observer {
            if (it != null) {
                supportActionBar?.title = it.name
            }
        })
    }

    private fun fetchData() {
        mViewModel.refresh()
    }
}

class LinearItemDecoration : RecyclerView.ItemDecoration {
    val dividerPaint: Paint

    constructor(context: Context) : super() {
        dividerPaint = Paint()
        dividerPaint.isAntiAlias = true
        dividerPaint.color = context.resources.getColor(R.color.tag_light)
        dividerPaint.strokeWidth = context.dp2px(1f).toFloat()
        dividerPaint.style = Paint.Style.STROKE
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val bottom = child.bottom
            c.drawLine(0f, bottom.toFloat(), parent.width.toFloat(), bottom.toFloat(), dividerPaint)
        }
    }
}

interface OnSelectListener {
    fun onSelect(topic: Topic?)
}

class MainAdapter(val onSelectListener: OnSelectListener) : RecyclerView.Adapter<MainViewHolder>() {

    private var topics: List<Topic>? = null

    fun setList(topics: List<Topic>) {
        if (this.topics == null) {
            this.topics = topics
            notifyItemRangeInserted(0, topics.size)
        } else {
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return this@MainAdapter.topics!!.size
                }

                override fun getNewListSize(): Int {
                    return topics.size
                }

                override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                    return this@MainAdapter.topics!![oldPosition].id == topics[newPosition].id
                }

                override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                    return this@MainAdapter.topics!![oldPosition] == topics[newPosition]
                }
            })
            this.topics = topics
            diffResult.dispatchUpdatesTo(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(parent).apply {
            itemView.setOnClickListener {
                onSelectListener.onSelect(topic)
            }
        }
    }

    override fun getItemCount(): Int {
        return topics?.size ?: 0
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bindTo(topics?.get(position)!!)
    }
}

class MainViewHolder : RecyclerView.ViewHolder {

    private var tvTitle: TextView? = null
    private var tvTime: TextView? = null
    private var tvPublisherName: TextView? = null
    private var tvCommentCount: TextView? = null
    private var tvNodeName: TextView? = null
    var topic: Topic? = null

    constructor(parent: ViewGroup) : super(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_main, parent, false)) {
        tvTitle = itemView.findViewById(R.id.tvTitle)
        tvTime = itemView.findViewById(R.id.tvTime)
        tvPublisherName = itemView.findViewById(R.id.tvPublisherName)
        tvCommentCount = itemView.findViewById(R.id.tvCommentCount)
        tvNodeName = itemView.findViewById(R.id.tvNodeName)
    }

    fun bindTo(topic: Topic) {
        this.topic = topic

        tvTitle?.text = topic.title
        tvTime?.text = topic.lastReplyTime
        tvPublisherName?.text = topic.publisher?.name
        topic.replyCount.apply {
            if (this == 0) {
                tvCommentCount?.visibility = View.GONE
            } else {
                tvCommentCount?.visibility = View.VISIBLE
                tvCommentCount?.text = topic.replyCount.toString()
            }
        }
        tvNodeName?.text = topic.node?.name
    }
}