package com.example.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class CareerRepository(private val careerDao: CareerDao) {

    // Career Flow
    val careerFlow: Flow<CareerEntity?> = careerDao.getCareerFlow()
    val clubsFlow: Flow<List<ClubEntity>> = careerDao.getClubsFlow()
    val newsFlow: Flow<List<NewsEntity>> = careerDao.getNewsFlow()
    val fixturesFlow: Flow<List<FixtureEntity>> = careerDao.getFixturesFlow()
    val journalsFlow: Flow<List<JournalEntity>> = careerDao.getJournalsFlow()

    fun getPlayersByClubFlow(clubId: Int): Flow<List<PlayerEntity>> = careerDao.getPlayersByClubFlow(clubId)
    val transferPlayersFlow: Flow<List<PlayerEntity>> = careerDao.getTransferListPlayersFlow()

    suspend fun getCareer() = careerDao.getCareer()
    suspend fun getUserClub() = careerDao.getUserClub()

    // Value Pricing Formula
    fun getPlayerValue(rating: Int, age: Int, position: String): Long {
        val baseFactor = when {
            rating >= 90 -> 90_000_000L + (rating - 90) * 15_000_000L
            rating >= 85 -> 40_000_000L + (rating - 85) * 10_000_000L
            rating >= 80 -> 12_000_000L + (rating - 80) * 5_000_000L
            rating >= 75 -> 4_000_000L + (rating - 75) * 1_500_000L
            else -> 800_000L + (rating - 70).coerceAtLeast(0) * 400_000L
        }
        val ageFactor = when {
            age < 23 -> 1.3 // Young talent premium
            age > 33 -> 0.4 // Veteran discount
            age > 30 -> 0.7
            else -> 1.0
        }
        return (baseFactor * ageFactor).toLong()
    }

    fun getPlayerWage(rating: Int): Long {
        return when {
            rating >= 90 -> 350_000L
            rating >= 85 -> 180_000L
            rating >= 80 -> 90_000L
            else -> 30_000L
        }
    }

    suspend fun updateCareer(career: CareerEntity) = careerDao.insertCareer(career)
    suspend fun updateClub(club: ClubEntity) = careerDao.updateClub(club)
    suspend fun updatePlayer(player: PlayerEntity) = careerDao.updatePlayer(player)
    suspend fun updateFixture(fixture: FixtureEntity) = careerDao.updateFixture(fixture)
    suspend fun insertNews(news: NewsEntity) = careerDao.insertNews(news)
    suspend fun insertJournal(journal: JournalEntity) = careerDao.insertJournal(journal)

    suspend fun getPlayersByClub(clubId: Int) = careerDao.getPlayersByClub(clubId)
    suspend fun getPlayerById(playerId: Int) = careerDao.getPlayerById(playerId)
    suspend fun getClubById(clubId: Int) = careerDao.getClubById(clubId)

    suspend fun initializeNewCareer(managerName: String, selectedClubId: Int) {
        careerDao.clearCareerData()

        // 1. Predefined Clubs list
        val clubs = listOf(
            ClubEntity(id = 1, name = "Al-Hilal", nameAr = "الهلال", shortName = "HIL", primaryColor = 0xFF0D47A1, secondaryColor = 0xFF1976D2, reputation = 86, budget = 120_000_000L),
            ClubEntity(id = 2, name = "Al-Nassr", nameAr = "النصر", shortName = "NAS", primaryColor = 0xFFFFEB3B, secondaryColor = 0xFF004D40, reputation = 84, budget = 110_000_000L),
            ClubEntity(id = 3, name = "Al-Ittihad", nameAr = "الاتحاد", shortName = "ITT", primaryColor = 0xFF212121, secondaryColor = 0xFFFFD700, reputation = 82, budget = 100_000_000L),
            ClubEntity(id = 4, name = "Al-Ahli", nameAr = "الأهلي", shortName = "AHL", primaryColor = 0xFF1B5E20, secondaryColor = 0xFFFFFFFF, reputation = 81, budget = 90_000_000L),
            ClubEntity(id = 5, name = "Real Madrid", nameAr = "ريال مدريد", shortName = "RMA", primaryColor = 0xFFECEFF1, secondaryColor = 0xFF1A237E, reputation = 93, budget = 180_000_000L),
            ClubEntity(id = 6, name = "Barcelona", nameAr = "برشلونة", shortName = "FCB", primaryColor = 0xFFB71C1C, secondaryColor = 0xFF0D47A1, reputation = 90, budget = 130_000_000L),
            ClubEntity(id = 7, name = "Manchester City", nameAr = "مانشستر سيتي", shortName = "MCI", primaryColor = 0xFF80D8FF, secondaryColor = 0xFFECEFF1, reputation = 92, budget = 190_000_000L),
            ClubEntity(id = 8, name = "Liverpool", nameAr = "ليفربول", shortName = "LIV", primaryColor = 0xFFD84315, secondaryColor = 0xFFFFD54F, reputation = 89, budget = 140_000_000L),
            ClubEntity(id = 9, name = "Bayern Munich", nameAr = "بايرن ميونخ", shortName = "FCB", primaryColor = 0xFFE53935, secondaryColor = 0xFFECEFF1, reputation = 88, budget = 135_000_000L),
            ClubEntity(id = 10, name = "Paris Saint-Germain", nameAr = "باريس سان جيرمان", shortName = "PSG", primaryColor = 0xFF0F172A, secondaryColor = 0xFFE2E8F0, reputation = 87, budget = 150_000_000L)
        ).map {
            if (it.id == selectedClubId) it.copy(isUser = true) else it
        }

        careerDao.insertClubs(clubs)

        // 2. Define roster of real playmakers
        val rawPlayers = listOf(
            // Al-Hilal
            Triple("Bounou", "GK", 85), Triple("Koulibaly", "DEF", 84),
            Triple("Rúben Neves", "MID", 83), Triple("Malcom", "ATT", 82), Triple("Mitrović", "ATT", 85),
            // Al-Nassr
            Triple("Bento", "GK", 80), Triple("Laporte", "DEF", 83),
            Triple("Otávio", "MID", 82), Triple("Sadio Mané", "ATT", 84), Triple("Cristiano Ronaldo", "ATT", 88),
            // Al-Ittihad
            Triple("Rajković", "GK", 79), Triple("Luiz Felipe", "DEF", 80),
            Triple("Kanté", "MID", 84), Triple("Diaby", "ATT", 82), Triple("Karim Benzema", "ATT", 87),
            // Al-Ahli
            Triple("Mendy", "GK", 81), Triple("Demiral", "DEF", 80),
            Triple("Kessié", "MID", 82), Triple("Mahrez", "ATT", 84), Triple("Firmino", "ATT", 81),
            // Real Madrid
            Triple("Courtois", "GK", 89), Triple("Rüdiger", "DEF", 87),
            Triple("Bellingham", "MID", 89), Triple("Vinícius Jr", "ATT", 91), Triple("Mbappé", "ATT", 92),
            // Barcelona
            Triple("Ter Stegen", "GK", 86), Triple("Araujo", "DEF", 85),
            Triple("Pedri", "MID", 86), Triple("Lamine Yamal", "ATT", 88), Triple("Lewandowski", "ATT", 87),
            // Man City
            Triple("Ederson", "GK", 86), Triple("Rúben Dias", "DEF", 88),
            Triple("Rodri", "MID", 91), Triple("De Bruyne", "MID", 90), Triple("Haaland", "ATT", 91),
            // Liverpool
            Triple("Alisson", "GK", 88), Triple("Van Dijk", "DEF", 89),
            Triple("Szoboszlai", "MID", 83), Triple("Luis Díaz", "ATT", 84), Triple("Mohamed Salah", "ATT", 89),
            // Bayern Munich
            Triple("Neuer", "GK", 85), Triple("Kim Min-jae", "DEF", 83),
            Triple("Musiala", "MID", 87), Triple("Sané", "ATT", 84), Triple("Harry Kane", "ATT", 90),
            // PSG
            Triple("Donnarumma", "GK", 85), Triple("Marquinhos", "DEF", 84),
            Triple("Vitinha", "MID", 82), Triple("Dembélé", "ATT", 84), Triple("Barcola", "ATT", 81)
        )

        // Convert raw players matching their club IDs
        val playersDatabaseList = mutableListOf<PlayerEntity>()
        var clubIndex = 1
        var count = 0
        for (item in rawPlayers) {
            val age = Random.nextInt(18, 36)
            val valAmt = getPlayerValue(item.third, age, item.second)
            val wageAmt = getPlayerWage(item.third)

            playersDatabaseList.add(
                PlayerEntity(
                    name = item.first,
                    position = item.second,
                    rating = item.third,
                    shooting = if (item.second == "GK") 15 else item.third + Random.nextInt(-4, 4),
                    passing = if (item.second == "GK") 20 else item.third + Random.nextInt(-4, 4),
                    speed = if (item.second == "GK") 45 else item.third + Random.nextInt(-6, 6),
                    defending = if (item.second == "DEF") item.third + 5 else if (item.second == "MID") item.third - 5 else 30,
                    goalkeeper = if (item.second == "GK") item.third + 5 else 10,
                    age = age,
                    clubId = clubIndex,
                    value = valAmt,
                    wage = wageAmt,
                    onTransferList = false
                )
            )
            count++
            if (count % 5 == 0) {
                clubIndex++
            }
        }

        // Add some FREE AGENTS / TRANSFER STAR PLAYERS (clubId = 0)
        val transferMarketStars = listOf(
            Triple("Lionel Messi", "ATT", 88),
            Triple("Luka Modrić", "MID", 83),
            Triple("Toni Kroos", "MID", 84),
            Triple("Sergio Ramos", "DEF", 81),
            Triple("David De Gea", "GK", 80),
            Triple("Paulo Dybala", "ATT", 83),
            Triple("Paul Pogba", "MID", 78),
            Triple("Thiago Silva", "DEF", 79)
        )

        for (item in transferMarketStars) {
            val age = Random.nextInt(28, 38)
            val valAmt = getPlayerValue(item.third, age, item.second)
            val wageAmt = getPlayerWage(item.third)

            playersDatabaseList.add(
                PlayerEntity(
                    name = item.first,
                    position = item.second,
                    rating = item.third,
                    shooting = if (item.second == "GK") 12 else item.third + Random.nextInt(-4, 4),
                    passing = if (item.second == "GK") 25 else item.third + Random.nextInt(-4, 4),
                    speed = if (item.second == "GK") 40 else item.third + Random.nextInt(-6, 6),
                    defending = if (item.second == "DEF") item.third + 5 else if (item.second == "MID") item.third - 5 else 25,
                    goalkeeper = if (item.second == "GK") item.third + 5 else 10,
                    age = age,
                    clubId = 0, // No club initially
                    value = valAmt,
                    wage = wageAmt,
                    onTransferList = true
                )
            )
        }

        careerDao.insertPlayers(playersDatabaseList)

        // 3. Generate Double Round-Robin Fixtures for 10 teams
        val teamList = (1..10).toMutableList()
        val numTeams = teamList.size
        val firstLegFixtures = mutableListOf<FixtureEntity>()

        // 9 Weeks for Round Robin Leg 1
        for (round in 0 until numTeams - 1) {
            for (i in 0 until numTeams / 2) {
                val home = teamList[i]
                val away = teamList[numTeams - 1 - i]
                if (round % 2 == 0) {
                    firstLegFixtures.add(
                        FixtureEntity(
                            season = 1,
                            week = round + 1,
                            homeTeamId = home,
                            awayTeamId = away
                        )
                    )
                } else {
                    firstLegFixtures.add(
                        FixtureEntity(
                            season = 1,
                            week = round + 1,
                            homeTeamId = away,
                            awayTeamId = home
                        )
                    )
                }
            }
            // Rotate team list (keep element 0 stationary)
            val last = teamList.last()
            for (idx in teamList.size - 1 downTo 2) {
                teamList[idx] = teamList[idx - 1]
            }
            teamList[1] = last
        }

        // Generate Leg 2 (Rounds 10 to 18) by flipping home and away
        val allFixtures = mutableListOf<FixtureEntity>()
        allFixtures.addAll(firstLegFixtures)

        for (f in firstLegFixtures) {
            allFixtures.add(
                FixtureEntity(
                    season = 1,
                    week = f.week + 9,
                    homeTeamId = f.awayTeamId,
                    awayTeamId = f.homeTeamId
                )
            )
        }

        careerDao.insertFixtures(allFixtures)

        // 4. Initialize Career Entity
        val userClubRef = clubs.first { it.id == selectedClubId }
        val career = CareerEntity(
            managerName = managerName,
            clubId = selectedClubId,
            budget = userClubRef.budget,
            season = 1,
            week = 1
        )
        careerDao.insertCareer(career)

        // 5. Inbox News
        val welcomeNews = NewsEntity(
            title = "Welcome Coach!",
            titleAr = "مرحباً بك يا كوتش!",
            content = "Breaking News! $managerName has been announced as the official head coach of ${userClubRef.name}! The board expects a solid performance this season with a budget of $${userClubRef.budget / 1_000_000}M.",
            contentAr = "أخبار عاجلة! تم الإعلان عن تعيين المدرب $managerName كمدير فني رسمي لنادي ${userClubRef.nameAr}! تترقب الإدارة نجاحاً كبيراً هذا الموسم بميزانية قدرها $${userClubRef.budget / 1_000_000} مليون دولار.",
            type = "Board"
        )
        careerDao.insertNews(welcomeNews)
    }

    suspend fun getFixturesByWeek(week: Int): List<FixtureEntity> = careerDao.getFixturesByWeek(week)

    // Simulate rest of matches of the current week (those not involving user team)
    suspend fun simulateRestOfMatches(week: Int, userClubId: Int) {
        val fixtures = careerDao.getFixturesByWeek(week)
        for (f in fixtures) {
            if (f.isPlayed) continue
            if (f.homeTeamId == userClubId || f.awayTeamId == userClubId) {
                // Must be played manually by user, skip simulation here
                continue
            }

            // Simple rating-based simulation
            val homeClub = careerDao.getClubById(f.homeTeamId)
            val awayClub = careerDao.getClubById(f.awayTeamId)

            if (homeClub != null && awayClub != null) {
                val homePlayers = careerDao.getPlayersByClub(f.homeTeamId)
                val awayPlayers = careerDao.getPlayersByClub(f.awayTeamId)

                val homeRating = homePlayers.map { it.rating }.average().toInt().takeIf { it > 0 } ?: homeClub.reputation
                val awayRating = awayPlayers.map { it.rating }.average().toInt().takeIf { it > 0 } ?: awayClub.reputation

                val diff = homeRating - awayRating

                // Pre-weighted score generation
                val homeGoalsChance = (Random.nextInt(0, 4) + (diff / 5).coerceIn(-2, 3)).coerceIn(0, 5)
                val awayGoalsChance = (Random.nextInt(0, 4) - (diff / 5).coerceIn(-3, 2)).coerceIn(0, 5)

                val (hs, asScore) = homeGoalsChance to awayGoalsChance
                val updatedFixture = f.copy(homeScore = hs, awayScore = asScore, isPlayed = true)
                careerDao.updateFixture(updatedFixture)

                // Update their league stats
                updateClubStats(homeClub, hs, asScore)
                updateClubStats(awayClub, asScore, hs)
            }
        }
    }

    private suspend fun updateClubStats(club: ClubEntity, gf: Int, ga: Int) {
        val played = club.played + 1
        val win = if (gf > ga) 1 else 0
        val draw = if (gf == ga) 1 else 0
        val loss = if (gf < ga) 1 else 0
        val points = club.points + (win * 3) + draw

        val updatedClub = club.copy(
            played = played,
            wins = club.wins + win,
            draws = club.draws + draw,
            losses = club.losses + loss,
            goalsFor = club.goalsFor + gf,
            goalsAgainst = club.goalsAgainst + ga,
            points = points
        )
        careerDao.updateClub(updatedClub)
    }

    // Sell User Player to Transfer list or general AI bids
    suspend fun putPlayerOnTransferList(playerId: Int, state: Boolean) {
        val p = careerDao.getPlayerById(playerId) ?: return
        careerDao.updatePlayer(p.copy(onTransferList = state))
    }

    // Process a transfer buy offer
    suspend fun buyPlayerFromMarket(player: PlayerEntity, userClub: ClubEntity, career: CareerEntity): Boolean {
        if (career.budget < player.value) return false

        // Update player's clubId to User club, and reset transfer status
        var updatedPlayer = player.copy(clubId = userClub.id, onTransferList = false)
        careerDao.updatePlayer(updatedPlayer)

        // Deduct budget
        val updatedCareer = career.copy(budget = career.budget - player.value)
        careerDao.insertCareer(updatedCareer)

        // Record news letter
        val news = NewsEntity(
            title = "New Signing Announcement!",
            titleAr = "صفقة جديدة للنادي!",
            content = "Fantastic acquisition! ${userClub.name} has signed defender/midfielder/forward ${player.name} from the transfer market for a record fee of $${player.value / 1_000_000}M.",
            contentAr = "صفقة رائعة! أعلن نادي ${userClub.nameAr} عن التعاقد مع اللاعب ${player.name} بصفقة بلغت قيمتها $${player.value / 1_000_000} مليون دولار.",
            type = "Global"
        )
        careerDao.insertNews(news)
        return true
    }

    // Sell player to an AI club
    suspend fun sellPlayerToAI(player: PlayerEntity, userClub: ClubEntity, career: CareerEntity): Boolean {
        // Find a random AI club
        val clubsList = careerDao.getClubs().filter { !it.isUser }
        if (clubsList.isEmpty()) return false
        val buyer = clubsList.random()

        // Give player to buyer, remove transfer list status
        val updatedPlayer = player.copy(clubId = buyer.id, onTransferList = false)
        careerDao.updatePlayer(updatedPlayer)

        // Add to user budget
        val updatedCareer = career.copy(budget = career.budget + player.value)
        careerDao.insertCareer(updatedCareer)

        val news = NewsEntity(
            title = "Player Sold!",
            titleAr = "تم بيع لاعب!",
            content = "Deal complete! ${player.name} has been sold by ${userClub.name} to ${buyer.name} for $${player.value / 1_000_000}M.",
            contentAr = "تمت الصفقة بنجاح! وافق نادي ${userClub.nameAr} على بيع اللاعب ${player.name} إلى نادي ${buyer.nameAr} مقابل $${player.value / 1_000_000} مليون دولار.",
            type = "Global"
        )
        careerDao.insertNews(news)
        return true
    }

    // Train energy / upgrade skills of players
    suspend fun upgradePlayerSkill(player: PlayerEntity, statType: String, cost: Long, userClubId: Int, career: CareerEntity): Boolean {
        if (career.budget < cost) return false

        val updatedPlayer = when (statType) {
            "shooting" -> player.copy(shooting = (player.shooting + 1).coerceAtMost(99), rating = (player.rating + 1).coerceAtMost(99))
            "passing" -> player.copy(passing = (player.passing + 1).coerceAtMost(99), rating = (player.rating + 1).coerceAtMost(99))
            "speed" -> player.copy(speed = (player.speed + 1).coerceAtMost(99), rating = (player.rating + 1).coerceAtMost(99))
            "defending" -> player.copy(defending = (player.defending + 1).coerceAtMost(99), rating = (player.rating + 1).coerceAtMost(99))
            "goalkeeper" -> player.copy(goalkeeper = (player.goalkeeper + 1).coerceAtMost(99), rating = (player.rating + 1).coerceAtMost(99))
            "energy" -> player.copy(energy = 100)
            else -> player
        }

        careerDao.updatePlayer(updatedPlayer)

        // Deduct budget
        val updatedCareer = career.copy(budget = career.budget - cost)
        careerDao.insertCareer(updatedCareer)
        return true
    }

    // Replenish entire squad energy with a training budget cost
    suspend fun recoverSquadEnergy(cost: Long, userClubId: Int, career: CareerEntity): Boolean {
        if (career.budget < cost) return false

        val squad = careerDao.getPlayersByClub(userClubId)
        for (p in squad) {
            careerDao.updatePlayer(p.copy(energy = 100))
        }

        // Deduct cost
        val updatedCareer = career.copy(budget = career.budget - cost)
        careerDao.insertCareer(updatedCareer)
        return true
    }
}
