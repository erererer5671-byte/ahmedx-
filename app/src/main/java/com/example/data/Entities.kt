package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "career")
data class CareerEntity(
    @PrimaryKey val id: Int = 1,
    val managerName: String,
    val clubId: Int, // Managed club
    val budget: Long, // Current budget in Dollars
    val season: Int = 1,
    val week: Int = 1, // 1 to 10 or 18
    val reputation: Int = 50,
    val difficulty: String = "Normal",
    val careerMode: String = "Manager", // "Manager" or "Player"
    val playerName: String = "",
    val playerPosition: String = "ATT",
    val playerRating: Int = 75,
    val playerSpeed: Int = 75,
    val playerShooting: Int = 75,
    val playerPassing: Int = 75,
    val playerDefending: Int = 40,
    val selectedLeagueCode: String = "SPL", // "SPL", "EPL", "LAL", "SER", "BUN", "FRA"
    val academyLevel: Int = 1, // Youth Academy tier (1 to 5)
    val scoutLevel: Int = 1, // Hired scout quality tier (1 to 5)
    val scoutingMissionActive: Boolean = false, // Current Active status
    val scoutingMissionPosition: String = "", // Target scouting position
    val scoutingWeeksPassed: Int = 0, // Counting duration of Scouting
    val selectedSponsor: String = "None", // Persistent active jersey sponsor
    val managerCoins: Int = 100, // In-game currency for customization
    val managerXp: Int = 0, // Manager XP level tracking
    val selectedTheme: String = "Classic", // Active customization theme
    val unlockedThemes: String = "Classic", // Comma-separated list of unlocked themes, e.g. "Classic,Emerald"
    val dailyTask1Progress: Int = 0, // Play 1 Match (0/1)
    val dailyTask1Max: Int = 1,
    val dailyTask1Completed: Boolean = false,
    val dailyTask1Claimed: Boolean = false,
    val dailyTask2Progress: Int = 0, // Score 2 goals (0/2)
    val dailyTask2Max: Int = 2,
    val dailyTask2Completed: Boolean = false,
    val dailyTask2Claimed: Boolean = false,
    val dailyTask3Progress: Int = 0, // Buy or Sell Player (0/1)
    val dailyTask3Max: Int = 1,
    val dailyTask3Completed: Boolean = false,
    val dailyTask3Claimed: Boolean = false,
    val nationalTeamId: Int = 0, // 0 if none, else codes 101 to 108 representing Saudi Arabia, Egypt, France, England etc.
    val hasNationalJob: Boolean = false,
    val activeScoutingType: String = "SENIOR" // "SENIOR" (discover wonderkids) vs "YOUTH" (discover 12-17 year olds for Academy)
)

@Entity(tableName = "clubs")
data class ClubEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val nameAr: String,
    val shortName: String,
    val primaryColor: Long, // Color Hex code
    val secondaryColor: Long,
    val reputation: Int, // 1 to 100
    val budget: Long,
    val isUser: Boolean = false,
    // League stats
    val played: Int = 0,
    val wins: Int = 0,
    val draws: Int = 0,
    val losses: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val points: Int = 0,
    val formation: String = "4-4-2", // e.g. "4-4-2", "4-3-3", "3-5-2"
    val tactic: String = "Balanced", // "Defensive", "Balanced", "Offensive"
    val league: String = "SPL"
)

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val position: String, // "GK", "DEF", "MID", "ATT"
    val rating: Int,
    val shooting: Int,
    val passing: Int,
    val speed: Int,
    val defending: Int,
    val goalkeeper: Int,
    val age: Int,
    var clubId: Int,
    val value: Long,
    val wage: Long,
    val energy: Int = 100,
    val form: Int = 5, // 1 - 10
    val goals: Int = 0,
    val assists: Int = 0,
    val injuryWeeks: Int = 0,
    val onTransferList: Boolean = false,
    val isOnLoan: Boolean = false,
    val loanWeeksLeft: Int = 0,
    val originalClubId: Int = 0,
    val onLoanList: Boolean = false,
    val potential: Int = 80,
    val parentClubId: Int = 0,
    val sellOnClausePercent: Int = 0,
    val loanOptionBuyPrice: Long = 0L,
    val hasLoanOptionToBuy: Boolean = false
)

@Entity(tableName = "fixtures")
data class FixtureEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val season: Int,
    val week: Int, // Match day 1-18
    val homeTeamId: Int,
    val awayTeamId: Int,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val isPlayed: Boolean = false
)

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val titleAr: String,
    val content: String,
    val contentAr: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "Global" // "Global", "TransferOffer", "Board"
)

@Entity(tableName = "journals")
data class JournalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val season: Int,
    val week: Int,
    val title: String,
    val titleAr: String,
    val content: String,
    val contentAr: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isAuto: Boolean = true // auto-generated vs manager manual diary
)

@Entity(tableName = "history_records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val season: Int,
    val championTeam: String,
    val championTeamAr: String,
    val topScorer: String,
    val topScorerAr: String,
    val topScorerGoals: Int,
    val ballonDorWinner: String = "Mohamed Salah",
    val ballonDorWinnerAr: String = "محمد صلاح",
    val bestGoalkeeper: String = "Yassine Bounou",
    val bestGoalkeeperAr: String = "ياسين بونو"
)
