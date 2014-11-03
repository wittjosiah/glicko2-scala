package glicko2.models

/**
 * Created by josiah on 14-10-28.
 */
case class Game (
  winner: PlayerRating,
  loser: PlayerRating,
  draw: Boolean)
