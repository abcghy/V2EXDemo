package info.gaohuiyu.v2exdemo.data.model

import org.jsoup.Jsoup

data class Topic(val title: String?, val url: String?, val publisher: Member?,
                 val node: Node?, val lastReplyTime: String?, val lastReplyMember: Member?,
                 val replyCount: Int)
data class Member(val avatar: String?, val name: String?)
data class Node(val name: String?)

fun main(args: Array<String>) {
    val doc = Jsoup.connect("https://www.v2ex.com").get()
    println("title: ${doc.title()}")
//    val doc = Jsoup.parse(File("index.html"), "UTF-8", "https://www.v2ex.com/")
//    println("title: ${doc.title()}")
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
        val topicUrl = titleA.attr("href")

        val member: Member = memberA.let {
            val memberName = it.attr("href")
            val memberAvatar = it.select("img").attr("src")
            Member(memberAvatar, memberName)
        }

        val nodeA = tr.select("a.node")
        val node = Node(nodeA.html())

        return@MutableList Topic(title, topicUrl, member, node, lastReplyTime, lastReplyMember, replyCount)
    }

    topics.forEach {
        println(it)
    }
}