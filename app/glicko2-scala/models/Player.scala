package glicko2scala.models

/**
 * Created by josiah on 14-10-28.
 */
case class Player(
  var rating: Double = 1500,
  var ratingDeviation: Double = 200,
  var volatility: Double = 0.06,
  clientId: String)
