package com.giraso.giraso

interface Platform {
  val name: String
}

expect fun getPlatform(): Platform