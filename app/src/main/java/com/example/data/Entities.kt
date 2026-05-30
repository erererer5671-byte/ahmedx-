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
    val week: Int = 1, // 1 to 18
    val reputation: Int = 50,
    val difficulty: String = "Normal"
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
    val tactic: String = "Balanced" // "Defensive", "Balanced", "Offensive"
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
    val onLoanList: Boolean = false
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
