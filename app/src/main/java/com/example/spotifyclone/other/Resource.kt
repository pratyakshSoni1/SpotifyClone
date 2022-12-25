package com.example.spotifyclone.other

data class Resource<out T>(val status:Status, val data:T?, val message:String?){

    companion object{

        fun <T> loading(data: T) = Resource(Status.LOADING, data, null)
        fun <T> success(data: T) = Resource(Status.SUCCESS, data, null)
        fun <T> erorr(data: T, message: String?) = Resource(Status.ERROR, data, message)

    }


}

enum class Status{
    LOADING,
    SUCCESS,
    ERROR
}