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
    val matches = List(new Game(player1, player2, false), new Game(player3, player1, false), new Game(player4, player1, false))
    println(player1)
    player1 = ratingSystem.calculateNewRating(player1, List(player1,player2,player3,player4), matches)
    println(player1)
  }
  def Test2(): Unit = {
    val ratingSystem = new Glicko2(0.5)
    var player1 = new Player("one", 1200, 200, 0.06)
    var player2 = new Player("two", 1200, 200, 0.06)
    val matches = List(new Game(player1, player2, false), new Game(player1, player2, false), new Game(player1, player2, false), new Game(player1, player2, false), new Game(player1, player2, false))
    println(player1)
    println(player2)
    player1 = ratingSystem.calculateNewRating(player1, List(player1,player2), matches)
    player2 = ratingSystem.calculateNewRating(player2, List(player1,player2), matches)
    println(player1)
    println(player2)
  }
}
