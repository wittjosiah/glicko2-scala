package glicko2.models

/**
 * Created by josiah on 14-10-28.
 */
case class PlayerRating(
  gameType: String,
  rating: Double = 1500,
  ratingDeviation: Double = 200,
  volatility: Double = 0.06)
