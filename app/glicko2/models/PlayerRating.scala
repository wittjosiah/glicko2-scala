package glicko2.models

/**
 * Created by josiah on 14-10-28.
 */
case class PlayerRating(
  playerID: Int,
  gameType: String,
  rating: Double = 1500,
  ratingDeviation: Double = 350,
  volatility: Double = 0.06)
