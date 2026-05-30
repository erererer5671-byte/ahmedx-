package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CareerDao {
    @Query("SELECT * FROM career WHERE id = 1 LIMIT 1")
    fun getCareerFlow(): Flow<CareerEntity?>

    @Query("SELECT * FROM career WHERE id = 1 LIMIT 1")
    suspend fun getCareer(): CareerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCareer(career: CareerEntity)

    @Query("DELETE FROM career")
    suspend fun deleteCareer()

    // Clubs
    @Query("SELECT * FROM clubs ORDER BY points DESC, (goalsFor - goalsAgainst) DESC, goalsFor DESC")
    fun getClubsFlow(): Flow<List<ClubEntity>>

    @Query("SELECT * FROM clubs")
    suspend fun getClubs(): List<ClubEntity>

    @Query("SELECT * FROM clubs WHERE id = :id LIMIT 1")
    suspend fun getClubById(id: Int): ClubEntity?

    @Query("SELECT * FROM clubs WHERE isUser = 1 LIMIT 1")
    suspend fun getUserClub(): ClubEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClubs(clubs: List<ClubEntity>)

    @Update
    suspend fun updateClub(club: ClubEntity)

    @Query("DELETE FROM clubs")
    suspend fun deleteAllClubs()

    // Players
    @Query("SELECT * FROM players")
    fun getPlayersFlow(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE clubId = :clubId")
    fun getPlayersByClubFlow(clubId: Int): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE clubId = :clubId")
    suspend fun getPlayersByClub(clubId: Int): List<PlayerEntity>

    @Query("SELECT * FROM players WHERE onTransferList = 1")
    fun getTransferListPlayersFlow(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :id LIMIT 1")
    suspend fun getPlayerById(id: Int): PlayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>)

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Query("DELETE FROM players")
    suspend fun deleteAllPlayers()

    // Fixtures
    @Query("SELECT * FROM fixtures ORDER BY week ASC, id ASC")
    fun getFixturesFlow(): Flow<List<FixtureEntity>>

    @Query("SELECT * FROM fixtures WHERE week = :week")
    suspend fun getFixturesByWeek(week: Int): List<FixtureEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixtures(fixtures: List<FixtureEntity>)

    @Update
    suspend fun updateFixture(fixture: FixtureEntity)

    @Query("DELETE FROM fixtures")
    suspend fun deleteAllFixtures()

    // News
    @Query("SELECT * FROM news ORDER BY timestamp DESC")
    fun getNewsFlow(): Flow<List<NewsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: NewsEntity)

    @Delete
    suspend fun deleteNews(news: NewsEntity)

    @Query("DELETE FROM news")
    suspend fun deleteAllNews()

    // Journals
    @Query("SELECT * FROM journals ORDER BY timestamp DESC")
    fun getJournalsFlow(): Flow<List<JournalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: JournalEntity)

    @Query("DELETE FROM journals")
    suspend fun deleteAllJournals()

    @Transaction
    suspend fun clearCareerData() {
        deleteCareer()
        deleteAllClubs()
        deleteAllPlayers()
        deleteAllFixtures()
        deleteAllNews()
        deleteAllJournals()
    }
}
