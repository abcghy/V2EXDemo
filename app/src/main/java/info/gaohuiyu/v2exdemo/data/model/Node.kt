package info.gaohuiyu.v2exdemo.data.model

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey

@Entity(
//    foreignKeys = arrayOf(
//        ForeignKey(
//            entity = Member::class,
//            parentColumns = arrayOf("name"),
//            childColumns = arrayOf("publisher")
//        ),
//        ForeignKey(
//            entity = Node::class,
//            parentColumns = arrayOf("name"),
//            childColumns = arrayOf("node")
//        ),
//        ForeignKey(
//            entity = Member::class,
//            parentColumns = arrayOf("name"),
//            childColumns = arrayOf("lastReplyMember")
//        )
//    )
)
data class Topic(
    @PrimaryKey
    val id: Long,
    val title: String?,
    @Embedded(prefix = "publisher_")
    val publisher: Member?,
    @Embedded(prefix = "node_")
    val node: Node?,
    val lastReplyTime: String?,
    @Embedded(prefix = "lastReplyMember_")
    val lastReplyMember: Member?,
    val replyCount: Int,
    val order: Int
)

@Entity
data class Member(
    @PrimaryKey val name: String, val avatar: String?
)

@Entity
data class Node(@PrimaryKey val name: String)

data class Comment(
    val content: String,
    val commenter: Member,
    val loveCount: String?,
    val time: String,
    val order: String
)

data class Subtitle(val content: String, val time: String)
data class TopicDetail(
    val title: String, val content: String?, val lastReplyTime: String?,
    val replyCount: String?, val node: String, val publisher: Member?,
    val publishTime: String?, val clickCount: String?
)

data class CommentHeader(val replyCount: String?, val filter: Boolean)
data class CommentResponse(val comments: List<Comment>, val currentPage: Int, val totalPage: Int)

//data class TopicListResponse(
//    val topics: List<Topic>
//)

data class TopicDetailResponse(
    val topicDetail: TopicDetail,
    val subtitles: List<Subtitle>,
    val commentResponse: CommentResponse
) {
    fun toList(): MutableList<Any> {
        val list = ArrayList<Any>()
        list.add(topicDetail)
        list.addAll(subtitles)
        list.add(CommentHeader(topicDetail.replyCount, true))
        list.addAll(commentResponse.comments)
        return list
    }
}

data class Tab(val name: String, val suffix: String)

data class CheckedWrapper<T>(var isChecked: Boolean, val data: T) {
    companion object {
        fun <T> transform(data: T): CheckedWrapper<T> {
            return CheckedWrapper(false, data)
        }

        fun <T> transform(datas: List<T>): List<CheckedWrapper<T>> {
            return datas.map {
                transform(it)
            }
        }
    }
}