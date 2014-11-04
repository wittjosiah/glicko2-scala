import glicko2.RatingSystem
import glicko2.models._
import org.scalatest.FunSuite

/**
 * Created by josiah on 14-10-29.
 */

class Glicko2Test extends FunSuite {
  test("Simple rating round") {
    val player1 = PlayerRating("one", 1500, 200, 0.06)
    val player2 = PlayerRating("two", 1400, 30, 0.06)
    val player3 = PlayerRating("three", 1550, 100, 0.06)
    val player4 = PlayerRating("four", 1700, 300, 0.06)
    val matches = List(Game(player1, player2, draw = false), Game(player3, player1, draw = false), Game(player4, player1, draw = false))
    assert(RatingSystem.calculateNewRating(player1, matches) == PlayerRating("one",1464.0513448250845,151.51510315274723,0.06))
  }
}
