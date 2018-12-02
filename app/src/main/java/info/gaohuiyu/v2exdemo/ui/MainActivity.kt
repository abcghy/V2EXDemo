package info.gaohuiyu.v2exdemo.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Rect
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.util.DiffUtil
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.TextView
import info.gaohuiyu.v2exdemo.R
import info.gaohuiyu.v2exdemo.data.api.V2EXApi
import info.gaohuiyu.v2exdemo.data.model.Topic
import info.gaohuiyu.v2exdemo.data.model.TopicDetail
import info.gaohuiyu.v2exdemo.data.repository.TopicRepository
import info.gaohuiyu.v2exdemo.ui.detail.TopicDetailActivity
import kotlinx.android.synthetic.main.activity_main.*

fun Context.dp2px(dp: Float): Int = (resources.displayMetrics.density * dp + 0.5f).toInt()

class MainActivity : AppCompatActivity() {

    private lateinit var mAdapter: MainAdapter

    private lateinit var mViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAdapter = MainAdapter(object: OnSelectListener {
            override fun onSelect(topic: Topic?) {
                if (topic != null) {
                    TopicDetailActivity.openActivity(this@MainActivity, topicId = topic.id)
                }
            }

        })
        rv.adapter = mAdapter

        rv.layoutManager = GridLayoutManager(this, 2)
        rv.addItemDecoration(GridItemDecoration())

        mViewModel = ViewModelProviders.of(this, object: ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//                if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
//                }
                return MainViewModel(TopicRepository(V2EXApi())) as T
            }

        }).get(MainViewModel::class.java)

        subscribeUI(mViewModel)
        fetchData()
    }

    private fun subscribeUI(viewModel: MainViewModel) {
        viewModel.topics.observe(this, Observer {
            if (it != null) {
                mAdapter.setList(it)
            } else {

            }
        })
    }

    private fun fetchData() {
        mViewModel.loadFirstPage()
    }
}

class GridItemDecoration: RecyclerView.ItemDecoration {
    constructor() : super()

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        if (position < 2) {
            outRect.top = parent.context.dp2px(16f)
        }
        outRect.bottom = parent.context.dp2px(16f)
        if (position % 2 == 0) {
            outRect.left = parent.context.dp2px(16f)
        }
        outRect.right = parent.context.dp2px(16f)
    }
}

interface OnSelectListener {
    fun onSelect(topic: Topic?)
}

class MainAdapter(val onSelectListener: OnSelectListener): RecyclerView.Adapter<MainViewHolder>() {

    private var topics: List<Topic>? = null

    fun setList(topics: List<Topic>) {
        if (this.topics == null) {
            this.topics = topics
            notifyItemRangeInserted(0, topics.size)
        } else {
            val diffResult = DiffUtil.calculateDiff(object: DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return this@MainAdapter.topics!!.size
                }

                override fun getNewListSize(): Int {
                    return topics.size
                }

                override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                    return this@MainAdapter.topics!![oldPosition] == topics[newPosition]
                }

                override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                    return this@MainAdapter.topics!![oldPosition].id == topics[newPosition].id
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

class MainViewHolder : RecyclerView.ViewHolder{

    private var tvTitle: TextView? = null
    private var tvTime: TextView? = null
    private var tvPublisherName: TextView? = null
    private var tvCommentCount: TextView? = null
    var topic: Topic? = null

    constructor(parent: ViewGroup) : super(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_main, parent, false)) {
        tvTitle = itemView.findViewById(R.id.tvTitle)
        tvTime = itemView.findViewById(R.id.tvTime)
        tvPublisherName = itemView.findViewById(R.id.tvPublisherName)
        tvCommentCount = itemView.findViewById(R.id.tvCommentCount)
    }

    fun bindTo(topic: Topic) {
        this.topic = topic

        tvTitle?.text = topic.title
        tvTime?.text = topic.lastReplyTime
        tvPublisherName?.text = topic.publisher?.name
        tvCommentCount?.text = topic.replyCount.toString()
    }
}