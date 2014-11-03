package glicko2scala

import glicko2scala.models._

/**
 * Created by josiah on 14-10-28.
 */

// Based off of Mark Glickman's rating system as specified in http://www.glicko.net/glicko/glicko2.pdf
object Glicko2Client {
  //  ___ _____ ___ ___
  // |___   |   |_  |__|  /|
  //  ___|  |   |__ |     _|_

  val DefaultRating =  1500.0
  val DefaultDeviation =  350
  val DefaultVolatility =  0.06
  val ConversionConstant =  173.7178
  val ConvergenceTolerance =  0.000001
  val tau = 0.5

  //  ___ _____ ___ ___   ___
  // |___   |   |_  |__|  ___|
  //  ___|  |   |__ |    |___

  def playerToGlicko2Scale(player: Player) {
    player.rating = (player.rating - 1500) / ConversionConstant
    player.ratingDeviation = player.ratingDeviation / ConversionConstant
  }
  def playersToGlicko2Scale(players: List[Player]) {
    if (players.nonEmpty) {
      playerToGlicko2Scale(players.head)
      playersToGlicko2Scale(players.tail)
    }
  }

  //  ___ _____ ___ ___   ___
  // |___   |   |_  |__|   __|
  //  ___|  |   |__ |     ___|

  def g(deviation: Double) = 1.0 / Math.sqrt(1.0 + (3.0 * Math.pow(deviation, 2) / Math.pow(Math.PI, 2)))
  def E(playerRating: Double, opponentRating: Double, opponentDeviation: Double) = 1.0 / (1.0 + Math.exp(-1.0 * g(opponentDeviation) * (playerRating - opponentRating)))
  def getOpponent(player: Player, thisMatch: Game) = if (player == thisMatch.winner) thisMatch.loser else thisMatch.winner
  def inverse_v(player: Player, matches: List[Game]): Double = {
    if (matches.isEmpty) 0 else {
      val opponent = getOpponent(player, matches.head)
      Math.pow(g(opponent.ratingDeviation), 2) *
        E(player.rating, opponent.rating, opponent.ratingDeviation) *
        (1.0 - E(player.rating, opponent.rating, opponent.ratingDeviation)) +
        inverse_v(player, matches.tail)
    }
  }
  def calculateV(player: Player, matches: List[Game]) = 1.0 / inverse_v(player, matches)

  //  ___ _____ ___ ___
  // |___   |   |_  |__|  |__|
  //  ___|  |   |__ |        |

  def getScore(player: Player, thisMatch: Game) = if (thisMatch.draw) 0.5 else if (player == thisMatch.winner) 1.0 else 0.0
  def outcomeRating(player: Player, matches: List[Game]): Double = {
    if (matches.isEmpty) 0 else {
      val opponent = getOpponent(player, matches.head)
      val score = getScore(player, matches.head)
      g(opponent.ratingDeviation) * (score - E(player.rating, opponent.rating, opponent.ratingDeviation)) + outcomeRating(player, matches.tail)
    }
  }
  def calculateDelta(player: Player, matches: List[Game]) = calculateV(player, matches) * outcomeRating(player, matches)

  //  ___ _____ ___ ___   ___
  // |___   |   |_  |__|  |__
  //  ___|  |   |__ |     __|

  def f(x: Double, delta: Double, phi: Double, v: Double, a: Double) =
    ((Math.exp(x) * (Math.pow(delta, 2) - Math.pow(phi, 2) - v - Math.exp(x))) /
      2.0 * Math.pow(Math.pow(phi, 2) + v + Math.exp(x), 2)) - ((x - a) / Math.pow(tau, 2))
  def getK(k: Double, delta: Double, phi: Double, v: Double, a: Double): Double = if (f(a - k * tau, delta, phi, v, a) < 0) getK(k + 1, delta, phi, v, a) else k
  def getB(delta: Double, phi: Double, v: Double, a: Double): Double = {
    if (Math.pow(delta, 2) > Math.pow(phi, 2) + v) Math.log(Math.pow(delta, 2) - Math.pow(phi, 2) - v)
    else {
      val k = getK(1, delta, phi, v, a)
      a - k * tau
    }
  }
  def converge(A: Double, B: Double, f_A: Double, f_B: Double, delta: Double, phi: Double, v: Double, a: Double, epsilon: Double): Double = {
    val C = A + (A - B) * f_A /(f_B - f_A)
    val f_C = f(C, delta, phi, v, a)
    val newA = if (f_C * f_B < 0) B else A
    val newF_A = if (f_C * f_B < 0) f_B else f_A/2
    val newB = C
    val newF_B = f_C
    if (Math.abs(newB - newA) > epsilon) converge(newA, newB, newF_A, newF_B, delta, phi, v, a, epsilon)
    else A
  }
  def calculateNewRating(player: Player, players: List[Player], matches: List[Game]) = {
    playersToGlicko2Scale(players)

    val phi = player.ratingDeviation
    val sigma = player.volatility
    val a = Math.log(Math.pow(sigma, 2))
    val v = calculateV(player, matches)
    val delta = calculateDelta(player, matches)
    val epsilon = ConvergenceTolerance

    val A = a
    val B = getB(delta, phi, v, a)
    val f_A = f(A, delta, phi, v, a)
    val f_B = f(B, delta, phi, v, a)

    val finalA = converge(A, B, f_A, f_B, delta, phi, v, a, epsilon)

    player.volatility = Math.exp(finalA/2)

  //  ___ _____ ___ ___   ___
  // |___   |   |_  |__|  |__
  //  ___|  |   |__ |     |__|

    player.ratingDeviation = Math.sqrt(Math.pow(player.ratingDeviation, 2) + Math.pow(player.volatility, 2))

  //  ___ _____ ___ ___   ___
  // |___   |   |_  |__|  |  |
  //  ___|  |   |__ |        |

    player.ratingDeviation = 1.0 / Math.sqrt(1.0/Math.pow(player.ratingDeviation, 2) + 1.0/v)
    player.rating = player.rating + Math.pow(player.ratingDeviation, 2) * outcomeRating(player, matches)

    playersToGlickoScale(players)
    player
  }

  //  ___ _____ ___ ___   ___
  // |___   |   |_  |__|  |__|
  //  ___|  |   |__ |     |__|

  def playerToGlickoScale(player: Player) {
    player.rating = player.rating * ConversionConstant + 1500
    player.ratingDeviation = player.ratingDeviation * ConversionConstant
  }
  def playersToGlickoScale(players: List[Player]) {
    if (players.nonEmpty) {
      playerToGlickoScale(players.head)
      playersToGlickoScale(players.tail)
    }
  }
}
