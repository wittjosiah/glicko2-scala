package controllers

import models._

/**
 * Created by josiah on 14-10-29.
 */
class Glicko2Test {
  def Test1() {
    val ratingSystem = new Glicko2(0.5)
    var player1 = new Player("one", 1500, 200, 0.06)
    val player2 = new Player("two", 1400, 30, 0.06)
    val player3 = new Player("three", 1550, 100, 0.06)
    val player4 = new Player("four", 1700, 300, 0.06)
    val matches = List(new Match(player1, player2, false), new Match(player3, player1, false), new Match(player4, player1, false))
    println(player1)
    player1 = ratingSystem.calculateNewRating(player1, matches)
    println(player1)
  }
}
