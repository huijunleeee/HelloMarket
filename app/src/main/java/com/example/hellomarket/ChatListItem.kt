package com.example.hellomarket

data class ChatListItem(
    val buyerId: String,
    val sellerId : String,
    val key: Long
){

    constructor(): this("","",0)
}