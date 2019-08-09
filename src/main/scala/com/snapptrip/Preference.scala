package com.snapptrip

import java.nio.charset.StandardCharsets
import java.nio.file.Paths

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.actor.Status.Success
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{FileIO, Flow, Framing, Sink, Source}
import akka.util.ByteString

import scala.collection.immutable
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

case class Pref(userId: Int, itemId: Int)

class Preference(implicit ec: ExecutionContext, materializer: ActorMaterializer, system: ActorSystem) {
  def getStream(filePath: String): (Source[Pref, Future[IOResult]]) forSome {type source >: Source[ByteString, Future[IOResult]] <: Source[ByteString, Future[IOResult]]} = {
    val path = Paths.get(filePath)
    val source = FileIO.fromPath(path)
    val toMapFlow: Flow[ByteString, Map[String, String], NotUsed] = CsvParsing.lineScanner()
      .via(CsvToMap.toMapAsStrings())
    val filterFlow: Flow[Map[String, String], Option[Pref], NotUsed] =
      Flow[Map[String, String]]
        .map { case m =>
          m.get("user_id") match {
            case Some(uid) => Try(uid.toInt).toOption match {
              case Some(uidInt) => m.get("item_id") match {
                case Some(iid) => Try(iid.toInt).toOption match {
                  case Some(iidInt) => Some(Pref(uidInt, iidInt))
                  case None => None
                }
              }
              case None => None
            }
          }
        }
    val collectSome = Flow[Option[Pref]].collect { case Some(pref) => pref }
    val result=source
      .via(toMapFlow)
      .via(filterFlow)
      .via(collectSome)
      result
  }

  def getSeq(filePath: String): Future[immutable.Seq[Pref]] = {
    getStream(filePath).runWith(Sink.seq)
      .andThen {
        case _ =>
          system.terminate()
          Await.ready(system.whenTerminated, 1 seconds)
      }
  }
}
