package models

import controllers.Glicko2

/**
 * Created by josiah on 14-10-28.
 */
case class Player(
  val uid: String,
  var rating: Double,
  var ratingDeviation: Double,
  var volatility: Double)
