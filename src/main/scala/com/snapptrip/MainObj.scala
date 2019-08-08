package com.snapptrip
import java.util

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Base64

import scala.util.Success
object MainObj {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("MyAkkaSystem")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    val pref=new Preference
    pref.readFile("preferences.csv").onComplete{
      case Success(r)=>println(r)
      case _=>println("error")
    }

  }
}
