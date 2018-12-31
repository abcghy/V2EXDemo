package info.gaohuiyu.v2exdemo.data.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import info.gaohuiyu.v2exdemo.data.dao.MemberDao
import info.gaohuiyu.v2exdemo.data.dao.NodeDao
import info.gaohuiyu.v2exdemo.data.dao.TopicDao
import info.gaohuiyu.v2exdemo.data.model.Member
import info.gaohuiyu.v2exdemo.data.model.Node
import info.gaohuiyu.v2exdemo.data.model.Topic

@Database(entities = arrayOf(
    Topic::class, Member::class, Node::class
), version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun memberDao(): MemberDao
    abstract fun nodeDao(): NodeDao
}