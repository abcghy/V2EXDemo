package info.gaohuiyu.v2exdemo.data.api

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.Gson
import info.gaohuiyu.v2exdemo.AppExecutors
import info.gaohuiyu.v2exdemo.data.model.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class V2EXApi {
    fun getHotTopics(): LiveData<List<Topic>> {
        val topicListLiveData = MutableLiveData<List<Topic>>()
        AppExecutors.networkIO().execute {
            val url = "https://www.v2ex.com/?tab=hot"

            val doc = Jsoup.connect(url).get()

            val cellItems = doc.select("#Main > div.box > div.cell.item")

            val topics = MutableList(cellItems.size) {
                val tr = cellItems[it].select("tr")[0]
                val memberA = tr.select("td:first-child > a")

                val thirdTD = tr.select("td:nth-child(3)")

                val titleA = thirdTD.select("span.item_title > a")
                val topicInfo = thirdTD.select("span.topic_info")[0]

                val lastReplyTime: String? = topicInfo.ownText().split("•").run {
                    if (this.size > 2) {
                        this[2]
                    } else {
                        null
                    }?.trim()
                }

                val lastReplyName: String? = topicInfo.run {
                    if (this.children().size > 3) {
                        child(3).select("strong > a").html()
                    } else {
                        null
                    }
                }
                val lastReplyMember = Member(null, lastReplyName)

                val replyCount = tr.select("a.count_livid").text().run {
                    if (this.isNullOrEmpty()) {
                        0
                    } else {
                        this.toInt()
                    }
                }

                val title = titleA.html()
                val topicId = Regex("/t/(\\d*)")
                        .find(titleA.attr("href"))?.groups?.get(1)?.value!!
                        .run {
                    this.toLong()
                }

                val member: Member = memberA.let {
                    val memberName = it.attr("href")
                    val memberAvatar = it.select("img").attr("src")
                    Member(memberAvatar, memberName)
                }

                val nodeA = tr.select("a.node")
                val node = Node(nodeA.html())

                return@MutableList Topic(title, topicId, member, node, lastReplyTime, lastReplyMember, replyCount)
            }
            Log.d("httpLog", Gson().toJson(topics))
            topicListLiveData.postValue(topics)
        }
        return topicListLiveData
    }

    fun getTopicDetail(topicId: Long, page: Int): LiveData<TopicDetail> {
        val topicDetailLiveData = MutableLiveData<TopicDetail>()
        AppExecutors.networkIO().execute {
            topicDetailLiveData.postValue(get(topicId, page))
        }
        return topicDetailLiveData
    }

    fun get(topicId: Long, page: Int = 1): TopicDetail {
        val url = "https://www.v2ex.com/t/$topicId?p=$page"
        val doc = Jsoup.connect(url).get()
        val node = doc.select("div#Main > div.box > div.header > a")[1].html()
        val title = doc.select("div#Main > div.box > div.header > h1")[0].html()

        val publisherAvatar = doc.select("#Main > div:nth-child(2) > div.header > div.fr > a > img.avatar")[0].attr("src")
        val publisherName = doc.select("#Main > div:nth-child(2) > div.header > small > a")[0].html()

        val publisher = Member(publisherAvatar, publisherName)

        var publishTime: String? = null
        var clickCount: String? = null
        doc.select("#Main > div:nth-child(2) > div.header > small")[0].ownText().split("·")
                .apply {
                    this.map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .forEachIndexed { index, s ->
                                when (index) {
                                    0 -> publishTime = s
                                    1 -> clickCount = s
                                }
                            }
                }
        val content = doc.select("#Main > div:nth-child(2) > div.cell > div.topic_content").run {
            return@run if (this.size > 0) {
                this[0].html()
            } else {
                null
            }
        }
        val subtitles: MutableList<Subtitle> = ArrayList()
        doc.select("div.subtle").run {
            if (this.size > 0) {
                this.forEach {
                    val subtitleContent = it.select(".topic_content")[0].html()
                    val subtitleTime = it.select("span")[0].text()
                            .split("·")[1].trim()

                    subtitles.add(Subtitle(subtitleContent, subtitleTime))
                }
            }
        }
//    val favouriteCount = doc.select("#Main > div.topic_buttons > div.fr.topic_stats")[0]
//    println(favouriteCount)
        var replyCount: String? = null
        var lastReplyTime: String? = null
        doc.select("#Main > div:nth-child(4) > div:nth-child(1) > span")[0].text()
                .apply {
                    split("|")
                            .map { it.trim() }
                            .mapIndexed { index, s ->
                                when (index) {
                                    0 -> replyCount = s
                                    1 -> lastReplyTime = s
                                }
                            }
                }
        var comments: MutableList<Comment> = ArrayList()
        doc.getElementsByAttributeValueMatching("id", "r_(.*)").forEach {
            val commentContent = it.select("div.reply_content")[0].text()
            val commenterAvatar = it.select("img.avatar")[0].attr("src")
            val commenterName = it.select("tr > td:nth-child(3) > strong > a")[0].html()
            val commentTime = it.select("span.ago")[0].html()
            val loveCount = it.select("tr > td:nth-child(3) > span.small.fade").run {
                return@run if (this.size > 0) {
                    this[0].html()
                } else {
                    null
                }
            }
            val commenter = Member(commenterAvatar, commenterName)
            comments.add(Comment(commentContent, commenter, loveCount, commentTime))
        }

        val pages = getPages(doc)

        val topicDetail = TopicDetail(title, content, lastReplyTime, replyCount, node, publisher, publishTime, clickCount, comments, subtitles, pages.first, pages.second)
        Log.d("httpLog", Gson().toJson(topicDetail))
        return topicDetail
    }

    fun getPages(doc: Document): Pair<Int, Int> {
        val currentPage = doc.select("a.page_current").run {
            return@run if (this.size > 0) {
                this[0].html().toInt()
            } else {
                1
            }
        }
        val totalPage = doc.select("a.page_normal").run {
            var _totalPage = currentPage
            this.forEach {
                val pageNormal = it.html().toInt()
                if (pageNormal > _totalPage) {
                    _totalPage = pageNormal
                }
            }
            return@run _totalPage
        }
        return Pair(currentPage, totalPage)
    }
}