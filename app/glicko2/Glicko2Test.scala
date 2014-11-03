package glicko2

import glicko2.models._

/**
 * Created by josiah on 14-10-29.
 */

//TODO: write real tests
object Glicko2Test {
  def Test1() {
    var player1 = new PlayerRating("one", 1500, 200, 0.06)
    val player2 = new PlayerRating("two", 1400, 30, 0.06)
    val player3 = new PlayerRating("three", 1550, 100, 0.06)
    val player4 = new PlayerRating("four", 1700, 300, 0.06)
    val matches = List(new Game(player1, player2, false), new Game(player3, player1, false), new Game(player4, player1, false))
    println(player1)
    player1 = RatingSystem.calculateNewRating(player1, matches)
    println(player1)
  }
  def Test2() {
    var player1 = new PlayerRating("one", 1200, 200, 0.06)
    var player2 = new PlayerRating("two", 1200, 200, 0.06)
    val matches = List(new Game(player1, player2, false), new Game(player1, player2, false), new Game(player1, player2, false), new Game(player1, player2, false), new Game(player1, player2, false))
    println(player1)
    println(player2)
    player1 = RatingSystem.calculateNewRating(player1, matches)
    player2 = RatingSystem.calculateNewRating(player2, matches)
    println(player1)
    println(player2)
  }
}
