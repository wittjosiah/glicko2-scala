package controllers

import models._

/**
 * Created by josiah on 14-10-28.
 */

// Based off of Mark Glickman's rating system as specified in http://www.glicko.net/glicko/glicko2.pdf
class Glicko2(tau: Double = 0.75) {
  //  ___ _____ ___ ___
  // |___   |   |_  |__|  /|
  //  ___|  |   |__ |     _|_

  val DefaultRating =  1500.0
  val DefaultDeviation =  350
  val DefaultVolatility =  0.06
  val ConversionConstant =  173.7178
  val ConvergenceTolerance =  0.000001

  //  ___ _____ ___ ___   ___
  // |___   |   |_  |__|  ___|
  //  ___|  |   |__ |    |___

  def playerToGlicko2Scale(player: Player) = {
    player.rating = (player.rating - 1500) / ConversionConstant
    player.ratingDeviation = player.ratingDeviation / ConversionConstant
    player
  }
  def matchesToGlicko2Scale(matches: List[Match]): List[Match] = {
    if (matches.isEmpty) List()
    else {
      val glicko2Winner = playerToGlicko2Scale(matches.head.winner)
      val glicko2Loser = playerToGlicko2Scale(matches.head.loser)
      val glicko2Match = new Match(glicko2Winner, glicko2Loser, matches.head.draw)
      List(glicko2Match) ::: matchesToGlicko2Scale(matches.tail)
    }
  }

  //  ___ _____ ___ ___   ___
  // |___   |   |_  |__|   __|
  //  ___|  |   |__ |     ___|

  def g(deviation: Double) = 1.0 / Math.sqrt(1.0 + (3.0 * Math.pow(deviation, 2) / Math.pow(Math.PI, 2)))
  def E(playerRating: Double, opponentRating: Double, opponentDeviation: Double) = 1.0 / (1.0 + Math.exp(-1.0 * g(opponentDeviation) * (playerRating - opponentRating)))
  def getOpponent(player: Player, thisMatch: Match) = if (player == thisMatch.winner) thisMatch.loser else thisMatch.winner
  def inverse_v(player: Player, matches: List[Match]): Double = {
    if (matches.isEmpty) 0 else {
      val opponent = getOpponent(player, matches.head)
      Math.pow(g(opponent.ratingDeviation), 2) *
        E(player.rating, opponent.rating, opponent.ratingDeviation) *
        (1.0 - E(player.rating, opponent.rating, opponent.ratingDeviation)) +
        inverse_v(player, matches.tail)
    }
  }
  def calculateV(player: Player, matches: List[Match]) = 1.0 / inverse_v(player, matches)

  //  ___ _____ ___ ___
  // |___   |   |_  |__|  |__|
  //  ___|  |   |__ |        |

  def getScore(player: Player, thisMatch: Match) = if (thisMatch.draw) 0.5 else if (player == thisMatch.winner) 1.0 else 0.0
  def outcomeRating(player: Player, matches: List[Match]): Double = {
    if (matches.isEmpty) 0 else {
      val opponent = getOpponent(player, matches.head)
      val score = getScore(player, matches.head)
      g(opponent.ratingDeviation) * (score - E(player.rating, opponent.rating, opponent.ratingDeviation)) + outcomeRating(player, matches.tail)
    }
  }
  def calculateDelta(player: Player, matches: List[Match]) = calculateV(player, matches) * outcomeRating(player, matches)

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
  def calculateNewRating(player: Player, matches: List[Match]) = {
    val glicko2Player = playerToGlicko2Scale(player)
    val glicko2Matches = matchesToGlicko2Scale(matches)

    val phi = glicko2Player.ratingDeviation
    val sigma = glicko2Player.volatility
    val a = Math.log(Math.pow(sigma, 2))
    val v = calculateV(glicko2Player, glicko2Matches)
    val delta = calculateDelta(glicko2Player, glicko2Matches)
    val epsilon = ConvergenceTolerance

    val A = a
    val B = getB(delta, phi, v, a)
    val f_A = f(A, delta, phi, v, a)
    val f_B = f(B, delta, phi, v, a)

    val finalA = converge(A, B, f_A, f_B, delta, phi, v, a, epsilon)

    glicko2Player.volatility = Math.exp(finalA/2)

  //  ___ _____ ___ ___   ___
  // |___   |   |_  |__|  |__
  //  ___|  |   |__ |     |__|

    glicko2Player.ratingDeviation = Math.sqrt(Math.pow(glicko2Player.ratingDeviation, 2) + Math.pow(glicko2Player.volatility, 2))

  //  ___ _____ ___ ___   ___
  // |___   |   |_  |__|  |  |
  //  ___|  |   |__ |        |

    glicko2Player.ratingDeviation = 1.0 / Math.sqrt(1.0/Math.pow(glicko2Player.ratingDeviation, 2) + 1.0/v)
    glicko2Player.rating = glicko2Player.rating + Math.pow(glicko2Player.ratingDeviation, 2) * outcomeRating(glicko2Player, glicko2Matches)

    playerToGlickoScale(glicko2Player)
  }

  //  ___ _____ ___ ___   ___
  // |___   |   |_  |__|  |__|
  //  ___|  |   |__ |     |__|

  def playerToGlickoScale(player: Player) = {
    player.rating = player.rating * ConversionConstant + 1500
    player.ratingDeviation = player.ratingDeviation * ConversionConstant
    player
  }
  def matchesToGlickoScale(matches: List[Match]): List[Match] = {
    if (matches.isEmpty) List()
    else {
      val glickoWinner = playerToGlickoScale(matches.head.winner)
      val glickoLoser = playerToGlickoScale(matches.head.loser)
      val glickoMatch = new Match(glickoWinner, glickoLoser, matches.head.draw)
      List(glickoMatch) ::: matchesToGlicko2Scale(matches.tail)
    }
  }
}
