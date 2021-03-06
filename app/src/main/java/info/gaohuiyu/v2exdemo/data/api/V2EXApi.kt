package info.gaohuiyu.v2exdemo.data.api

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.support.annotation.IntRange
import android.util.Log
import com.google.gson.Gson
import info.gaohuiyu.v2exdemo.AppExecutors
import info.gaohuiyu.v2exdemo.data.model.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.lang.Exception

class V2EXApi {
    fun getTopicsByTab(tab: Tab): LiveData<ApiResponse<List<Topic>>> {
        val topicListLiveData = MutableLiveData<ApiResponse<List<Topic>>>()
        AppExecutors.networkIO().execute {
            val url = "https://www.v2ex.com/?tab=${tab.suffix}"
            val request = ApiRequest(url)
            var doc: Document
            try {
                doc = Jsoup.connect(request.url).get()
            } catch (e: Throwable) {
                e.printStackTrace()
                topicListLiveData.postValue(ApiResponse.create(e))
                return@execute
            }

            val cellItems = doc.select("#Main > div.box > div.cell.item")

            val topics = List(cellItems.size) {
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
                val lastReplyMember = lastReplyName?.run {
                    Member(lastReplyName, null)
                }

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
                    val memberName = it.attr("href").replace("/member/", "").trim()
                    val memberAvatar = it.select("img").attr("src")
                    Member(memberName, memberAvatar)
                }

                val nodeA = tr.select("a.node")
                val node = Node(nodeA.html())

                return@List Topic(topicId, title, member, node, lastReplyTime, lastReplyMember, replyCount, it)
            }

            logRequestAndResponse(request, topics)

            val apiResponse = ApiResponse.create(topics)
            topicListLiveData.postValue(apiResponse)
        }
        return topicListLiveData
    }

    fun getTopicDetailResponse(topicId: Long): LiveData<ApiResponse<TopicDetailResponse>> {
        val topicDetailLiveData = MutableLiveData<ApiResponse<TopicDetailResponse>>()
        AppExecutors.networkIO().execute {
            val topicDetailResponse: TopicDetailResponse?
            try {
                topicDetailResponse = get(topicId, 1)
            } catch (e: Exception) {
                e.printStackTrace()
                topicDetailLiveData.postValue(ApiResponse.create(e))
                return@execute
            }
            topicDetailLiveData.postValue(ApiResponse.create(topicDetailResponse))
        }
        return topicDetailLiveData
    }

    fun getTopicComments(topicId: Long, @IntRange(from = 2, to = Long.MAX_VALUE) page: Int): LiveData<ApiResponse<CommentResponse>> {
        val topicDetailLiveData = MutableLiveData<ApiResponse<CommentResponse>>()
        AppExecutors.networkIO().execute {
            val topicDetailResponse: TopicDetailResponse?
            try {
                topicDetailResponse = get(topicId, page)
            } catch (e: Exception) {
                e.printStackTrace()
                topicDetailLiveData.postValue(ApiResponse.create(e))
                return@execute
            }
            topicDetailLiveData.postValue(ApiResponse.create(topicDetailResponse?.commentResponse))
        }
        return topicDetailLiveData
    }

    private fun get(topicId: Long, page: Int = 1): TopicDetailResponse? {
        val url = "https://www.v2ex.com/t/$topicId?p=$page"
        val request = ApiRequest(url)
        var doc = Jsoup.connect(request.url).get()

        if (doc.select("#Main > .box > .header > h1").size == 0) {
            return null
        }

        val topicDetail = getTopicDetailByDoc(doc)

        val subtitles = getSubtitlesByDoc(doc)

        var commentResponse = getCommentResponseByDoc(doc)

        val topicDetailResponse = TopicDetailResponse(topicDetail, subtitles, commentResponse)

        logRequestAndResponse(request, topicDetailResponse)

        return topicDetailResponse
    }

    private fun getTopicDetailByDoc(doc: Document): TopicDetail {
        val node = doc.select("div#Main > div.box > div.header > a")[1].html()
        val title = doc.select("div#Main > div.box > div.header > h1")[0].html()

        val publisherAvatar = doc.select("#Main > div:nth-child(2) > div.header > div.fr > a > img.avatar")[0].attr("src")
        val publisherName = doc.select("#Main > div:nth-child(2) > div.header > small > a")[0].html()

        val publisher = Member(publisherName, publisherAvatar)

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

        var replyCount: String? = null
        var lastReplyTime: String? = null
        doc.select("#Main > div:nth-child(4) > div:nth-child(1) > span").apply {
            if (this.size == 0) {
                replyCount = "0"
//                lastReplyTime = s
            } else {
                this[0].text().split("|")
                        .map { it.trim() }
                        .mapIndexed { index, s ->
                            when (index) {
                                0 -> replyCount = s.replace("回复", "").trim()
                                1 -> lastReplyTime = s
                            }
                        }
            }
        }

        return TopicDetail(title, content, lastReplyTime, replyCount, node, publisher, publishTime, clickCount)
    }

    private fun getSubtitlesByDoc(doc: Document): List<Subtitle> {
        var subtitles: MutableList<Subtitle> = ArrayList()
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
        return subtitles
    }

    private fun getCommentResponseByDoc(doc: Document): CommentResponse {
        var comments: MutableList<Comment> = ArrayList()
        doc.getElementsByAttributeValueMatching("id", "r_(.*)").forEach {
            val commentContent = it.select("div.reply_content")[0].html()
            val commenterAvatar = "https:" + it.select("img.avatar")[0].attr("src")
            val commenterName = it.select("tr > td:nth-child(3) > strong > a")[0].html()
            val commentTime = it.select("span.ago")[0].html()
            val commentOrder = it.select("span.no")[0].html()
            val loveCount = it.select("tr > td:nth-child(3) > span.small.fade").run {
                return@run if (this.size > 0) {
                    this[0].html()
                } else {
                    null
                }
            }
            val commenter = Member(commenterName, commenterAvatar)
            comments.add(Comment(commentContent, commenter, loveCount, commentTime, commentOrder))
        }
        val pages = getPages(doc)
        return CommentResponse(comments, pages.first, pages.second)
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

    val tag: String = "httpLog"
    val gson: Gson = Gson()

    private fun logRequestAndResponse(request: ApiRequest, response: Any) {
        Log.d(tag, gson.toJson(request))
        Log.d(tag, gson.toJson(response))
    }
}