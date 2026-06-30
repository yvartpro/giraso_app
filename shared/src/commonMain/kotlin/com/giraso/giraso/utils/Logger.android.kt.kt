package com.giraso.giraso.utils

expect object Logger {
  fun d(subTag: String, body: String)
  fun e(subTag: String, body: String, throwable: Throwable? = null)
  fun w(subTag: String, body: String)
  fun i(subTag: String, body: String)

}