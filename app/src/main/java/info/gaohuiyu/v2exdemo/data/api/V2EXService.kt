package info.gaohuiyu.v2exdemo.data.api

import info.gaohuiyu.v2exdemo.data.model.Node
import io.reactivex.Observable
import retrofit2.http.GET

interface V2EXService {

    @GET("nodes/all.json")
    fun getNode(): Observable<List<Node>>


}