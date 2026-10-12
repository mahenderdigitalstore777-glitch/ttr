package com.example.engine

object CommentaryGenerator {
    private val dotPhrases = listOf(
        "defended solidly back to the bowler.",
        "pushed gently to mid-on, no run.",
        "beaten outside off stump! Good delivery.",
        "tapped towards point, fielder is quick to gather.",
        "left alone outside off stump safely to the keeper.",
        "driven straight to extra cover, couldn't pierce the gap."
    )

    private val singlePhrases = listOf(
        "worked away to deep square leg for a single.",
        "tucked off the pads towards mid-wicket for one.",
        "nudged down to third man to rotate the strike.",
        "guided into the covers and they scamper through for a quick single."
    )

    private val twoPhrases = listOf(
        "placed nicely into the gap in the deep, they turn hard and come back for two!",
        "driven firmly through mid-off, easy two taken.",
        "chipped over the infield, safely lands and they hustle for a couple."
    )

    private val threePhrases = listOf(
        "magnificent placement in the deep! Sweeper chases and keeps it down to three.",
        "driven past extra cover, long chase to the fence, they run three!"
    )

    private val fourPhrases = listOf(
        "FOUR! Cracking shot through covers, timed to absolute perfection!",
        "FOUR! Pulled away with authority into the square leg fence!",
        "FOUR! Steered elegantly past backward point to the boundary ropes!",
        "FOUR! Lofted over mid-on, bounces once and crosses the boundary!"
    )

    private val sixPhrases = listOf(
        "SIX! That is massive! Launched high into the night sky and into the stands!",
        "SIX! What a strike! Dispatched way over long-on for maximum!",
        "SIX! Pure timing and power, clears the boundary with ease!",
        "SIX! Picked up effortlessly off the hips and sailed over deep fine leg!"
    )

    fun generate(
        bowlerName: String,
        strikerName: String,
        runs: Int,
        extrasType: String,
        extrasRuns: Int,
        isWicket: Boolean,
        wicketType: String
    ): String {
        val bName = bowlerName.ifBlank { "Bowler" }
        val sName = strikerName.ifBlank { "Batsman" }

        if (isWicket) {
            return when (wicketType.uppercase()) {
                "BOWLED" -> "OUT! BOWLED HIM! $bName crashes through $sName's defense and knocks the stumps out of the ground!"
                "CAUGHT" -> "OUT! CAUGHT! $sName tries to clear the ropes but holes out to the fielder! Great catch off $bName!"
                "LBW" -> "OUT! LBW! Trapped right in front of the middle stump! Huge appeal and the umpire raises the finger! $bName strikes!"
                "RUN_OUT" -> "OUT! RUN OUT! Direct hit! Miscommunication between the wickets and $sName falls short of the crease!"
                "STUMPED" -> "OUT! STUMPED! $sName steps out of the crease, beaten in flight by $bName, and the keeper whips the bails off in a flash!"
                "HIT_WICKET" -> "OUT! HIT WICKET! In an attempt to make room, $sName dislodges the bails! Unfortunate way to go!"
                "RETIRED_HURT" -> "$sName has retired hurt and walks back to the pavilion."
                "RETIRED_OUT" -> "$sName is retired out."
                else -> "OUT! $wicketType! $sName departs off the bowling of $bName!"
            }
        }

        when (extrasType.uppercase()) {
            "WIDE" -> {
                val totalWd = 1 + extrasRuns
                return if (extrasRuns > 0) {
                    "WIDE + $extrasRuns! Fired down the leg side, goes past the keeper for extra runs! $totalWd wides."
                } else {
                    "Wide ball! Strays outside the tramlines, called wide by the umpire."
                }
            }
            "NO_BALL" -> {
                val offBat = runs
                return if (offBat > 0) {
                    "NO BALL! Overstepping by $bName and $sName hammers it for $offBat! Free hit coming up!"
                } else {
                    "NO BALL! Front-foot no ball called by the umpire! Next delivery is a FREE HIT!"
                }
            }
            "BYE" -> return "Byes! Beats both the batsman and the keeper, they sneak $extrasRuns bye(s)."
            "LEG_BYE" -> return "Leg byes! Deflected off the pads into the leg side, they take $extrasRuns leg bye(s)."
            "PENALTY" -> return "Penalty runs awarded to the team ($extrasRuns runs)."
        }

        return when (runs) {
            0 -> "$bName to $sName, ${dotPhrases.random()}"
            1 -> "$bName to $sName, 1 run, ${singlePhrases.random()}"
            2 -> "$bName to $sName, 2 runs, ${twoPhrases.random()}"
            3 -> "$bName to $sName, 3 runs, ${threePhrases.random()}"
            4 -> fourPhrases.random() + " ($sName)"
            6 -> sixPhrases.random() + " ($sName)"
            else -> "$bName to $sName, $runs runs scored! Good running between the wickets."
        }
    }
}
