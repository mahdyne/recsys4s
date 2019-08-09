package com.snapptrip

import scala.collection.immutable

object Similarity {
  def jaccardSim(preferences: Seq[Pref]): Set[Sim] = {
    case class JD1(itemId: Int, userId1: Int, userId2: Int)
    val userItemCount = preferences.groupBy(_.userId).mapValues(_.size)
    val itemToUserMap = preferences.groupBy(_.itemId).map { case (k, v) => k -> v.map(_.userId) }
    val jd1 = preferences.flatMap(e =>
      itemToUserMap(e.itemId).map(u => JD1(e.itemId, e.userId, u)).filter(jd => jd.userId1 < jd.userId2)
    )
    val similarities=jd1.groupBy(e => (e.userId1, e.userId2)).map { case (k, v) =>
      val intersectionCount = v.size
      val sim = intersectionCount.toDouble / (userItemCount(k._1) + userItemCount(k._2) - intersectionCount)
      Sim(k._1,k._2,sim)
    }.toSet
    similarities
  }
}

case class Sim(user_a: Int, user_b: Int, similarity: Double)
