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

    suspend fun initializeNewCareer(
        managerName: String,
        selectedClubId: Int,
        careerMode: String = "Manager",
        playerPosition: String = "ATT",
        leagueCode: String = "SPL"
    ) {
        careerDao.clearCareerData()

        // 1. Predefined 48+ Clubs across Leagues (Saudi Pro League + Big Five European Leagues + Championship/League One)
        val clubs = listOf(
            // SPL (Saudi Pro League)
            ClubEntity(id = 1, name = "Al-Hilal", nameAr = "الهلال 👑", shortName = "HIL", primaryColor = 0xFF0D47A1, secondaryColor = 0xFF1976D2, reputation = 87, budget = 140_000_000L, league = "SPL"),
            ClubEntity(id = 2, name = "Al-Nassr", nameAr = "النصر 💛", shortName = "NAS", primaryColor = 0xFFFFEB3B, secondaryColor = 0xFF004D40, reputation = 85, budget = 120_000_000L, league = "SPL"),
            ClubEntity(id = 3, name = "Al-Ittihad", nameAr = "الاتحاد 🖤", shortName = "ITT", primaryColor = 0xFF212121, secondaryColor = 0xFFFFD700, reputation = 83, budget = 110_000_000L, league = "SPL"),
            ClubEntity(id = 4, name = "Al-Ahli", nameAr = "الأهلي 💚", shortName = "AHL", primaryColor = 0xFF1B5E20, secondaryColor = 0xFFFFFFFF, reputation = 82, budget = 95_000_000L, league = "SPL"),
            ClubEntity(id = 5, name = "Al-Shabab", nameAr = "الشباب 🤍", shortName = "SHB", primaryColor = 0xFF757575, secondaryColor = 0xFF212121, reputation = 78, budget = 65_000_000L, league = "SPL"),
            ClubEntity(id = 6, name = "Al-Ettifaq", nameAr = "الاتفاق ❤️", shortName = "ETF", primaryColor = 0xFFD84315, secondaryColor = 0xFF1B5E20, reputation = 77, budget = 50_000_000L, league = "SPL"),
            ClubEntity(id = 49, name = "Al-Taawoun", nameAr = "التعاون 🐺", shortName = "TAW", primaryColor = 0xFFFFD700, secondaryColor = 0xFF0D47A1, reputation = 75, budget = 35_000_000L, league = "SPL"),
            ClubEntity(id = 50, name = "Al-Fateh", nameAr = "الفتح 🟢💙", shortName = "FAT", primaryColor = 0xFF1E88E5, secondaryColor = 0xFF2E7D32, reputation = 73, budget = 25_000_000L, league = "SPL"),

            // EPL (English Premier League)
            ClubEntity(id = 7, name = "Manchester City", nameAr = "مانشستر سيتي 🩵", shortName = "MCI", primaryColor = 0xFF80D8FF, secondaryColor = 0xFFECEFF1, reputation = 92, budget = 190_000_000L, league = "EPL"),
            ClubEntity(id = 8, name = "Arsenal", nameAr = "أرسنال ❤️", shortName = "ARS", primaryColor = 0xFFE53935, secondaryColor = 0xFFFFFFFF, reputation = 90, budget = 150_000_000L, league = "EPL"),
            ClubEntity(id = 9, name = "Liverpool", nameAr = "ليفربول 🔴", shortName = "LIV", primaryColor = 0xFFB71C1C, secondaryColor = 0xFFFFD54F, reputation = 89, budget = 140_000_000L, league = "EPL"),
            ClubEntity(id = 10, name = "Chelsea", nameAr = "تشيلسي 💙", shortName = "CHE", primaryColor = 0xFF0D47A1, secondaryColor = 0xFFFFFFFF, reputation = 86, budget = 125_000_000L, league = "EPL"),
            ClubEntity(id = 11, name = "Manchester United", nameAr = "مانشستر يونايتد 👹", shortName = "MUN", primaryColor = 0xFFC62828, secondaryColor = 0xFF212121, reputation = 85, budget = 135_000_000L, league = "EPL"),
            ClubEntity(id = 12, name = "Tottenham Hotspur", nameAr = "توتنهام ⚪", shortName = "TOT", primaryColor = 0xFFECEFF1, secondaryColor = 0xFF1A237E, reputation = 84, budget = 95_000_000L, league = "EPL"),
            ClubEntity(id = 51, name = "Newcastle United", nameAr = "نيوكاسل يونايتد ⚫⚪", shortName = "NEW", primaryColor = 0xFF211D1D, secondaryColor = 0xFFFFFFFF, reputation = 83, budget = 110_000_000L, league = "EPL"),
            ClubEntity(id = 52, name = "Aston Villa", nameAr = "أستون فيلا 🦁", shortName = "AVL", primaryColor = 0xFF800020, secondaryColor = 0xFF95B9E2, reputation = 82, budget = 85_000_000L, league = "EPL"),

            // LAL (La Liga)
            ClubEntity(id = 13, name = "Real Madrid", nameAr = "ريال مدريد 👑", shortName = "RMA", primaryColor = 0xFFECEFF1, secondaryColor = 0xFF1A237E, reputation = 94, budget = 195_000_000L, league = "LAL"),
            ClubEntity(id = 14, name = "Barcelona", nameAr = "برشلونة 🔵🔴", shortName = "FCB", primaryColor = 0xFFB71C1C, secondaryColor = 0xFF0D47A1, reputation = 91, budget = 130_000_000L, league = "LAL"),
            ClubEntity(id = 15, name = "Atletico Madrid", nameAr = "أتلتيكو مدريد 🔴⚪", shortName = "ATM", primaryColor = 0xFFE53935, secondaryColor = 0xFF0D47A1, reputation = 87, budget = 105_000_000L, league = "LAL"),
            ClubEntity(id = 16, name = "Real Sociedad", nameAr = "ريال سوسيداد 🔵", shortName = "RSO", primaryColor = 0xFF1E88E5, secondaryColor = 0xFFFFFFFF, reputation = 82, budget = 65_000_000L, league = "LAL"),
            ClubEntity(id = 17, name = "Real Betis", nameAr = "ريال بيتيس 🟢", shortName = "BET", primaryColor = 0xFF2E7D32, secondaryColor = 0xFFFFFFFF, reputation = 81, budget = 55_000_000L, league = "LAL"),
            ClubEntity(id = 18, name = "Sevilla", nameAr = "إشبيلية ⚪🔴", shortName = "SEV", primaryColor = 0xFFD32F2F, secondaryColor = 0xFFFFFFFF, reputation = 79, budget = 50_000_000L, league = "LAL"),
            ClubEntity(id = 53, name = "Girona", nameAr = "جيرونا ❤️⚪", shortName = "GIR", primaryColor = 0xFFE53935, secondaryColor = 0xFFFFFFFF, reputation = 82, budget = 60_000_000L, league = "LAL"),
            ClubEntity(id = 54, name = "Athletic Bilbao", nameAr = "أتلتيك بيلباو 🔴⚪🦁", shortName = "ATH", primaryColor = 0xFFC62828, secondaryColor = 0xFFFFFFFF, reputation = 81, budget = 50_000_000L, league = "LAL"),

            // SER (Serie A)
            ClubEntity(id = 19, name = "Inter Milan", nameAr = "إنتر ميلان 🖤💙", shortName = "INT", primaryColor = 0xFF0D47A1, secondaryColor = 0xFF212121, reputation = 89, budget = 115_000_000L, league = "SER"),
            ClubEntity(id = 20, name = "AC Milan", nameAr = "إي سي ميلان 🔴🖤", shortName = "ACM", primaryColor = 0xFFC62828, secondaryColor = 0xFF212121, reputation = 86, budget = 90_000_000L, league = "SER"),
            ClubEntity(id = 21, name = "Juventus", nameAr = "يوفنتوس ⚪🖤", shortName = "JUV", primaryColor = 0xFFE0E0E0, secondaryColor = 0xFF212121, reputation = 87, budget = 105_000_000L, league = "SER"),
            ClubEntity(id = 22, name = "Napoli", nameAr = "نابولي 🩵", shortName = "NAP", primaryColor = 0xFF2196F3, secondaryColor = 0xFFFFFFFF, reputation = 84, budget = 80_000_000L, league = "SER"),
            ClubEntity(id = 23, name = "AS Roma", nameAr = "روما 💛❤️", shortName = "ROM", primaryColor = 0xFF880E4F, secondaryColor = 0xFFFFB300, reputation = 82, budget = 65_000_000L, league = "SER"),
            ClubEntity(id = 24, name = "Atalanta", nameAr = "أتالانتا 💙🖤", shortName = "ATA", primaryColor = 0xFF0288D1, secondaryColor = 0xFF212121, reputation = 83, budget = 60_000_000L, league = "SER"),
            ClubEntity(id = 55, name = "Lazio", nameAr = "لاتسيو 🦅🌤️", shortName = "LAZ", primaryColor = 0xFF4FC3F7, secondaryColor = 0xFFFFFFFF, reputation = 80, budget = 50_000_000L, league = "SER"),
            ClubEntity(id = 56, name = "Fiorentina", nameAr = "فيورنتينا 💜⚜️", shortName = "FIO", primaryColor = 0xFF4A148C, secondaryColor = 0xFFFFFFFF, reputation = 79, budget = 45_000_000L, league = "SER"),

            // BUN (Bundesliga)
            ClubEntity(id = 25, name = "Bayern Munich", nameAr = "بايرن ميونخ 🔴❤️", shortName = "FCB", primaryColor = 0xFFE53935, secondaryColor = 0xFFECEFF1, reputation = 91, budget = 140_000_000L, league = "BUN"),
            ClubEntity(id = 26, name = "Bayer Leverkusen", nameAr = "باير ليفركوزن ❤️🖤", shortName = "LEV", primaryColor = 0xFFC62828, secondaryColor = 0xFF212121, reputation = 88, budget = 95_000_000L, league = "BUN"),
            ClubEntity(id = 27, name = "Borussia Dortmund", nameAr = "بوروسيا دورتموند 💛🖤", shortName = "BVB", primaryColor = 0xFFFFD54F, secondaryColor = 0xFF212121, reputation = 85, budget = 80_000_000L, league = "BUN"),
            ClubEntity(id = 28, name = "RB Leipzig", nameAr = "لايبزيغ 🔴⚪", shortName = "RBL", primaryColor = 0xFFD32F2F, secondaryColor = 0xFF1A237E, reputation = 84, budget = 75_000_000L, league = "BUN"),
            ClubEntity(id = 29, name = "Eintracht Frankfurt", nameAr = "آينتراخت فرانكفورت 🦅", shortName = "SGE", primaryColor = 0xFF212121, secondaryColor = 0xFFE53935, reputation = 80, budget = 50_000_000L, league = "BUN"),
            ClubEntity(id = 30, name = "Stuttgart", nameAr = "شتوتغارت ⚪🔴", shortName = "VFB", primaryColor = 0xFFE53935, secondaryColor = 0xFFFFFFFF, reputation = 80, budget = 48_000_000L, league = "BUN"),
            ClubEntity(id = 57, name = "Wolfsburg", nameAr = "فولفسبورغ 🟢🟢", shortName = "WOB", primaryColor = 0xFF4CAF50, secondaryColor = 0xFFFFFFFF, reputation = 78, budget = 40_000_000L, league = "BUN"),
            ClubEntity(id = 58, name = "Freiburg", nameAr = "فرايبورغ 🦅🔴", shortName = "SCF", primaryColor = 0xFFB71C1C, secondaryColor = 0xFFFFFFFF, reputation = 77, budget = 35_000_000L, league = "BUN"),

            // FRA (Ligue 1)
            ClubEntity(id = 31, name = "Paris Saint-Germain", nameAr = "باريس سان جيرمان 🔵🇫🇷", shortName = "PSG", primaryColor = 0xFF0F172A, secondaryColor = 0xFFE2E8F0, reputation = 89, budget = 155_000_000L, league = "FRA"),
            ClubEntity(id = 32, name = "Marseille", nameAr = "مارسيليا 🩵⚪", shortName = "OM", primaryColor = 0xFF4FC3F7, secondaryColor = 0xFFFFFFFF, reputation = 82, budget = 70_000_000L, league = "FRA"),
            ClubEntity(id = 33, name = "Monaco", nameAr = "موناكو 🔴⚪", shortName = "ASM", primaryColor = 0xFFE53935, secondaryColor = 0xFFFFFFFF, reputation = 81, budget = 65_000_000L, league = "FRA"),
            ClubEntity(id = 34, name = "Lyon", nameAr = "ليون 🔵🔴⚪", shortName = "OL", primaryColor = 0xFF0D47A1, secondaryColor = 0xFFE53935, reputation = 80, budget = 60_000_000L, league = "FRA"),
            ClubEntity(id = 35, name = "Lille", nameAr = "ليل 🔴🐶", shortName = "LOS", primaryColor = 0xFFB71C1C, secondaryColor = 0xFF1A237E, reputation = 79, budget = 50_000_000L, league = "FRA"),
            ClubEntity(id = 36, name = "Nice", nameAr = "نيس ⚫🔴", shortName = "NIC", primaryColor = 0xFF212121, secondaryColor = 0xFFD32F2F, reputation = 78, budget = 45_000_000L, league = "FRA"),
            ClubEntity(id = 59, name = "Lens", nameAr = "لانس 🔴💛", shortName = "RCL", primaryColor = 0xFFFFD54F, secondaryColor = 0xFFC62828, reputation = 78, budget = 42_000_000L, league = "FRA"),
            ClubEntity(id = 60, name = "Rennes", nameAr = "رين 🔴⚫🦁", shortName = "REN", primaryColor = 0xFFD32F2F, secondaryColor = 0xFF212121, reputation = 77, budget = 38_000_000L, league = "FRA"),

            // ECHA (English Championship)
            ClubEntity(id = 37, name = "Leicester City", nameAr = "ليستر سيتي 🦊", shortName = "LEI", primaryColor = 0xFF0D47A1, secondaryColor = 0xFFFFFFFF, reputation = 78, budget = 45_000_000L, league = "ECHA"),
            ClubEntity(id = 38, name = "Leeds United", nameAr = "ليدز يونايتد 🤍", shortName = "LEE", primaryColor = 0xFFECEFF1, secondaryColor = 0xFFFFD54F, reputation = 76, budget = 38_000_000L, league = "ECHA"),
            ClubEntity(id = 39, name = "Southampton", nameAr = "ساوثهامبتون 🔴⚪", shortName = "SOU", primaryColor = 0xFFE53935, secondaryColor = 0xFF212121, reputation = 75, budget = 35_000_000L, league = "ECHA"),
            ClubEntity(id = 40, name = "Ipswich Town", nameAr = "إيبسويتش تاون 🚜", shortName = "IPS", primaryColor = 0xFF1976D2, secondaryColor = 0xFFFFFFFF, reputation = 74, budget = 30_000_000L, league = "ECHA"),
            ClubEntity(id = 41, name = "West Brom", nameAr = "وست بروميتش 🔵⚪", shortName = "WBA", primaryColor = 0xFF002F6C, secondaryColor = 0xFFFFFFFF, reputation = 72, budget = 25_000_000L, league = "ECHA"),
            ClubEntity(id = 42, name = "Norwich City", nameAr = "نورويتش سيتي 🪵", shortName = "NOR", primaryColor = 0xFFFBC02D, secondaryColor = 0xFF388E3C, reputation = 71, budget = 24_000_000L, league = "ECHA"),

            // EL1 (English League One)
            ClubEntity(id = 43, name = "Portsmouth", nameAr = "بورتسموث ⚓", shortName = "POR", primaryColor = 0xFF1E3A8A, secondaryColor = 0xFFFFFFFF, reputation = 65, budget = 12_000_000L, league = "EL1"),
            ClubEntity(id = 44, name = "Derby County", nameAr = "ديربي كاونتي 🐏", shortName = "DER", primaryColor = 0xFF1F2937, secondaryColor = 0xFFFFFFFF, reputation = 64, budget = 11_000_000L, league = "EL1"),
            ClubEntity(id = 45, name = "Bolton Wanderers", nameAr = "بولتون 🐘", shortName = "BOL", primaryColor = 0xFFF8FAFC, secondaryColor = 0xFF1E3A8A, reputation = 63, budget = 10_000_000L, league = "EL1"),
            ClubEntity(id = 46, name = "Peterborough", nameAr = "بيتربورو 🔵", shortName = "PET", primaryColor = 0xFF2563EB, secondaryColor = 0xFFFFFFFF, reputation = 62, budget = 8_500_000L, league = "EL1"),
            ClubEntity(id = 47, name = "Barnsley", nameAr = "بارنسلي 🔴", shortName = "BAR", primaryColor = 0xFFDC2626, secondaryColor = 0xFFFFFFFF, reputation = 61, budget = 8_000_000L, league = "EL1"),
            ClubEntity(id = 48, name = "Oxford United", nameAr = "أكسفورد يونايتد 🐂", shortName = "OXF", primaryColor = 0xFFFACC15, secondaryColor = 0xFF1E3A8A, reputation = 60, budget = 7_500_000L, league = "EL1")
        ).map {
            if (it.id == selectedClubId) it.copy(isUser = true) else it
        }

        careerDao.insertClubs(clubs)

        // 2. Dynamically generate rosters matching real football leagues
        val playersDatabaseList = mutableListOf<PlayerEntity>()

        val firstNames = mapOf(
            "SPL" to listOf("سالم", "عبدالرحمن", "فراس", "محمد", "علي", "سلطان", "فيصل", "حسن", "عبدالله", "صالح", "سلمان", "ياسر", "سعد", "خالد", "أحمد", "نواف"),
            "EPL" to listOf("James", "John", "Harry", "Jack", "George", "Oliver", "William", "Charlie", "Thomas", "Mason", "Cole", "Declan", "Marcus", "Trent"),
            "LAL" to listOf("Carlos", "Luis", "Javier", "Manuel", "Sergio", "Diego", "Andres", "Alejandro", "Pedro", "Felipe", "Pablo", "Mateo", "Hugo"),
            "SER" to listOf("Giovanni", "Marco", "Alessandro", "Luca", "Lorenzo", "Andrea", "Francesco", "Matteo", "Davide", "Giuseppe", "Nicolo", "Federico"),
            "BUN" to listOf("Lukas", "Leon", "Jonas", "Max", "Paul", "Felix", "Maximilian", "Tim", "Tobias", "Florian", "Thomas", "Sebastian", "Elias"),
            "FRA" to listOf("Lucas", "Arthur", "Mathis", "Hugo", "Enzo", "Clement", "Nathan", "Romain", "Damien", "Julien", "Antoine", "Pierre", "Jean")
        )
        val lastNames = mapOf(
            "SPL" to listOf("الدوسري", "غريب", "البريكان", "العويس", "البليهي", "الغنام", "الغامدي", "كادش", "الحمدان", "الشهري", "الفرج", "الحربي", "المولد", "البقمي"),
            "EPL" to listOf("Smith", "Jones", "Taylor", "Brown", "Wilson", "Evans", "Thomas", "Roberts", "Walker", "Palmer", "Mount", "Kane", "Saka", "Foden"),
            "LAL" to listOf("Gomez", "Lopez", "Garcia", "Fernandez", "Rodriguez", "Torres", "Martinez", "Sanchez", "Perez", "Navas", "Carvajal", "Alba"),
            "SER" to listOf("Rossi", "Ferrari", "Russo", "Bianchi", "Gallo", "Costa", "Barella", "Chiesa", "Bastoni", "Donnarumma", "Locatelli", "Dimarco"),
            "BUN" to listOf("Müller", "Schmidt", "Schneider", "Fischer", "Weber", "Meyer", "Wagner", "Becker", "Schulz", "Kroos", "Neuer", "Musiala"),
            "FRA" to listOf("Martin", "Bernard", "Thomas", "Petit", "Dubois", "Michel", "Laurent", "Dembele", "Mbappe", "Griezmann", "Giroud", "Barcola")
        )

        val predefinedSquads = mapOf(
            1 to listOf(
                Pair("Yassine Bounou", "GK"), Pair("Saud Abdulhamid", "DEF"), Pair("Kalidou Koulibaly", "DEF"),
                Pair("Ali Al-Bulaihi", "DEF"), Pair("Renan Lodi", "DEF"), Pair("Rúben Neves", "MID"),
                Pair("S. Milinković-Savić", "MID"), Pair("Mohamed Kanno", "MID"), Pair("Neymar Jr", "ATT"),
                Pair("Aleksandar Mitrović", "ATT"), Pair("Salem Al-Dawsari", "ATT")
            ),
            2 to listOf(
                Pair("David Ospina", "GK"), Pair("Sultan Al-Ghannam", "DEF"), Pair("Aymeric Laporte", "DEF"),
                Pair("Alex Telles", "DEF"), Pair("Ali Lajami", "DEF"), Pair("Marcelo Brozović", "MID"),
                Pair("Otávio", "MID"), Pair("Seko Fofana", "MID"), Pair("Cristiano Ronaldo", "ATT"),
                Pair("Sadio Mané", "ATT"), Pair("Abdulrahman Ghareeb", "ATT")
            ),
            3 to listOf(
                Pair("Marcelo Grohe", "GK"), Pair("Luiz Felipe", "DEF"), Pair("Ahmed Hegazi", "DEF"),
                Pair("Hassan Kadesh", "DEF"), Pair("Ahmed Bamasoud", "DEF"), Pair("N'Golo Kanté", "MID"),
                Pair("Fabinho", "MID"), Pair("Faisal Al-Ghamdi", "MID"), Pair("Karim Benzema", "ATT"),
                Pair("Abderrazak Hamdallah", "ATT"), Pair("Romarinho", "ATT")
            ),
            4 to listOf(
                Pair("Edouard Mendy", "GK"), Pair("Roger Ibañez", "DEF"), Pair("Merih Demiral", "DEF"),
                Pair("Ali Majrashi", "DEF"), Pair("Abdullah Al-Ammar", "DEF"), Pair("Franck Kessié", "MID"),
                Pair("Gabri Veiga", "MID"), Pair("Sumayhan Al-Nabit", "MID"), Pair("Riyad Mahrez", "ATT"),
                Pair("Allan Saint-Maximin", "ATT"), Pair("Firas Al-Buraikan", "ATT")
            ),
            7 to listOf(
                Pair("Ederson Moraes", "GK"), Pair("Rúben Dias", "DEF"), Pair("John Stones", "DEF"),
                Pair("Kyle Walker", "DEF"), Pair("Josko Gvardiol", "DEF"), Pair("Rodri", "MID"),
                Pair("Kevin De Bruyne", "MID"), Pair("Phil Foden", "MID"), Pair("Erling Haaland", "ATT"),
                Pair("Bernardo Silva", "ATT"), Pair("Jeremy Doku", "ATT")
            ),
            8 to listOf(
                Pair("David Raya", "GK"), Pair("William Saliba", "DEF"), Pair("Gabriel Magalhães", "DEF"),
                Pair("Ben White", "DEF"), Pair("Oleksandr Zinchenko", "DEF"), Pair("Declan Rice", "MID"),
                Pair("Martin Ødegaard", "MID"), Pair("Thomas Partey", "MID"), Pair("Bukayo Saka", "ATT"),
                Pair("Gabriel Martinelli", "ATT"), Pair("Kai Havertz", "ATT")
            ),
            9 to listOf(
                Pair("Alisson Becker", "GK"), Pair("Virgil van Dijk", "DEF"), Pair("Ibrahima Konatè", "DEF"),
                Pair("Trent Alex-Arnold", "DEF"), Pair("Andy Robertson", "DEF"), Pair("Alexis Mac Allister", "MID"),
                Pair("Dominik Szoboszlai", "MID"), Pair("Wataru Endo", "MID"), Pair("Mohamed Salah", "ATT"),
                Pair("Luis Díaz", "ATT"), Pair("Darwin Núñez", "ATT")
            ),
            10 to listOf(
                Pair("Robert Sánchez", "GK"), Pair("Reece James", "DEF"), Pair("Levi Colwill", "DEF"),
                Pair("Axel Disasi", "DEF"), Pair("Marc Cucurella", "DEF"), Pair("Enzo Fernández", "MID"),
                Pair("Moisés Caicedo", "MID"), Pair("Conor Gallagher", "MID"), Pair("Cole Palmer", "ATT"),
                Pair("Nicolas Jackson", "ATT"), Pair("Mykhailo Mudryk", "ATT")
            ),
            11 to listOf(
                Pair("André Onana", "GK"), Pair("Lisandro Martínez", "DEF"), Pair("Harry Maguire", "DEF"),
                Pair("Luke Shaw", "DEF"), Pair("Diogo Dalot", "DEF"), Pair("Bruno Fernandes", "MID"),
                Pair("Kobbie Mainoo", "MID"), Pair("Casemiro", "MID"), Pair("Marcus Rashford", "ATT"),
                Pair("Rasmus Højlund", "ATT"), Pair("Alejandro Garnacho", "ATT")
            ),
            13 to listOf(
                Pair("Thibaut Courtois", "GK"), Pair("Antonio Rüdiger", "DEF"), Pair("Éder Militão", "DEF"),
                Pair("Dani Carvajal", "DEF"), Pair("Ferland Mendy", "DEF"), Pair("Federico Valverde", "MID"),
                Pair("Jude Bellingham", "MID"), Pair("A. Tchouaméni", "MID"), Pair("Kylian Mbappé", "ATT"),
                Pair("Vinícius Jr", "ATT"), Pair("Rodrygo Goes", "ATT")
            ),
            14 to listOf(
                Pair("M. ter Stegen", "GK"), Pair("Ronald Araújo", "DEF"), Pair("Jules Koundé", "DEF"),
                Pair("Andreas Christensen", "DEF"), Pair("Alejandro Balde", "DEF"), Pair("Frenkie de Jong", "MID"),
                Pair("Pedri Gonzalez", "MID"), Pair("Gavi", "MID"), Pair("R. Lewandowski", "ATT"),
                Pair("Lamine Yamal", "ATT"), Pair("Raphinha Dias", "ATT")
            ),
            15 to listOf(
                Pair("Jan Oblak", "GK"), Pair("José María Giménez", "DEF"), Pair("Nahuel Molina", "DEF"),
                Pair("Mario Hermoso", "DEF"), Pair("César Azpilicueta", "DEF"), Pair("Koke Resurrección", "MID"),
                Pair("Rodrigo De Paul", "MID"), Pair("Marcos Llorente", "MID"), Pair("Antoine Griezmann", "ATT"),
                Pair("Álvaro Morata", "ATT"), Pair("Memphis Depay", "ATT")
            ),
            19 to listOf(
                Pair("Yann Sommer", "GK"), Pair("Alessandro Bastoni", "DEF"), Pair("Benjamin Pavard", "DEF"),
                Pair("Federico Dimarco", "DEF"), Pair("Francesco Acerbi", "DEF"), Pair("Nicolò Barella", "MID"),
                Pair("Hakan Çalhanoğlu", "MID"), Pair("Davide Frattesi", "MID"), Pair("Lautaro Martínez", "ATT"),
                Pair("Marcus Thuram", "ATT"), Pair("Alexis Sánchez", "ATT")
            ),
            20 to listOf(
                Pair("Mike Maignan", "GK"), Pair("Fikayo Tomori", "DEF"), Pair("Theo Hernandez", "DEF"),
                Pair("Davide Calabria", "DEF"), Pair("Malick Thiaw", "DEF"), Pair("Ismaël Bennacer", "MID"),
                Pair("Tijjani Reijnders", "MID"), Pair("R. Loftus-Cheek", "MID"), Pair("Rafael Leão", "ATT"),
                Pair("Christian Pulisic", "ATT"), Pair("Olivier Giroud", "ATT")
            ),
            21 to listOf(
                Pair("W. Szczęsny", "GK"), Pair("Gleison Bremer", "DEF"), Pair("Danilo", "DEF"),
                Pair("Andrea Cambiaso", "DEF"), Pair("Federico Gatti", "DEF"), Pair("Adrien Rabiot", "MID"),
                Pair("Manuel Locatelli", "MID"), Pair("Weston McKennie", "MID"), Pair("Dušan Vlahović", "ATT"),
                Pair("Federico Chiesa", "ATT"), Pair("Moise Kean", "ATT")
            ),
            25 to listOf(
                Pair("Manuel Neuer", "GK"), Pair("Kim Min-jae", "DEF"), Pair("Dayot Upamecano", "DEF"),
                Pair("Alphonso Davies", "DEF"), Pair("Matthijs de Ligt", "DEF"), Pair("Joshua Kimmich", "MID"),
                Pair("Leon Goretzka", "MID"), Pair("Konrad Laimer", "MID"), Pair("Harry Kane", "ATT"),
                Pair("Leroy Sané", "ATT"), Pair("Jamal Musiala", "ATT")
            ),
            26 to listOf(
                Pair("Lukas Hradecky", "GK"), Pair("Jonathan Tah", "DEF"), Pair("Piero Hincapié", "DEF"),
                Pair("Edmond Tapsoba", "DEF"), Pair("Alex Grimaldo", "DEF"), Pair("Granit Xhaka", "MID"),
                Pair("Exequiel Palacios", "MID"), Pair("Florian Wirtz", "MID"), Pair("Victor Boniface", "ATT"),
                Pair("Jeremie Frimpong", "ATT"), Pair("Patrik Schick", "ATT")
            ),
            31 to listOf(
                Pair("G. Donnarumma", "GK"), Pair("Marquinhos", "DEF"), Pair("Achraf Hakimi", "DEF"),
                Pair("Lucas Hernandez", "DEF"), Pair("Nuno Mendes", "DEF"), Pair("Vitinha", "MID"),
                Pair("Warren Zaïre-Emery", "MID"), Pair("Fabián Ruiz", "MID"), Pair("Ousmane Dembélé", "ATT"),
                Pair("Randal Kolo Muani", "ATT"), Pair("Bradley Barcola", "ATT")
            )
        )

        val customRatings = mapOf(
            "Erling Haaland" to 92, "Kylian Mbappé" to 93, "Vinícius Jr" to 91,
            "Kevin De Bruyne" to 90, "Jude Bellingham" to 90, "Harry Kane" to 90,
            "Mohamed Salah" to 89, "Lautaro Martínez" to 89, "Nicolò Barella" to 87,
            "Declan Rice" to 87, "Bukayo Saka" to 88, "Florian Wirtz" to 89,
            "Rafael Leão" to 87, "Cristiano Ronaldo" to 87, "Karim Benzema" to 86,
            "Neymar Jr" to 86, "Virgil van Dijk" to 89, "Rodri" to 92,
            "Thibaut Courtois" to 90, "Alisson Becker" to 89, "M. ter Stegen" to 87,
            "G. Donnarumma" to 87, "Manuel Neuer" to 86, "Yassine Bounou" to 84,
            "William Saliba" to 89, "Rúben Dias" to 89, "Phil Foden" to 89,
            "S. Milinković-Savić" to 84, "Rúben Neves" to 83, "Aleksandar Mitrović" to 83,
            "Bruno Fernandes" to 87, "Sadio Mané" to 83, "Aymeric Laporte" to 83,
            "Antoine Griezmann" to 87, "Martin Ødegaard" to 89, "N'Golo Kanté" to 84,
            "Riyad Mahrez" to 83, "Jan Oblak" to 86, "Mike Maignan" to 86
        )

        for (c in clubs) {
            val hasPredefined = predefinedSquads.containsKey(c.id)
            if (hasPredefined) {
                val list = predefinedSquads[c.id]!!
                for (item in list) {
                    val pName = item.first
                    val role = item.second
                    val baseRat = customRatings[pName] ?: when {
                        c.reputation >= 90 -> 84 + Random.nextInt(0, 5)
                        c.reputation >= 85 -> 81 + Random.nextInt(0, 5)
                        else -> 77 + Random.nextInt(0, 5)
                    }
                    val age = if (pName == "Cristiano Ronaldo") 39 else if (pName == "Karim Benzema" || pName == "Lionel Messi" || pName == "Luka Modrić") 36 else Random.nextInt(19, 31)
                    val valAmt = getPlayerValue(baseRat, age, role)
                    val wageAmt = getPlayerWage(baseRat)

                    playersDatabaseList.add(
                        PlayerEntity(
                            name = pName,
                            position = role,
                            rating = baseRat,
                            shooting = if (role == "ATT") baseRat + Random.nextInt(2, 6) else if (role == "GK") 12 else (baseRat - 15).coerceAtLeast(30),
                            passing = if (role == "MID") baseRat + Random.nextInt(2, 6) else (baseRat - 10).coerceAtLeast(30),
                            speed = (baseRat + Random.nextInt(-6, 8)).coerceIn(55, 99),
                            defending = if (role == "DEF") baseRat + Random.nextInt(2, 6) else if (role == "GK") 8 else 30,
                            goalkeeper = if (role == "GK") baseRat + Random.nextInt(2, 6) else 8,
                            age = age,
                            clubId = c.id,
                            value = valAmt,
                            wage = wageAmt,
                            onTransferList = false
                        )
                    )
                }
            } else {
                val lang = when (c.league) {
                    "SPL" -> "SPL"
                    "EPL", "ECHA", "EL1" -> "EPL"
                    "LAL" -> "LAL"
                    "SER" -> "SER"
                    "BUN" -> "BUN"
                    else -> "FRA"
                }
                val firsts = firstNames[lang] ?: firstNames["EPL"]!!
                val lasts = lastNames[lang] ?: lastNames["EPL"]!!

                val roles = listOf("GK", "DEF", "DEF", "DEF", "DEF", "MID", "MID", "MID", "ATT", "ATT", "ATT")
                for (role in roles) {
                    val pName = firsts.shuffled().first() + " " + lasts.shuffled().first()
                    val baseRat = (c.reputation + Random.nextInt(-4, 4)).coerceAtLeast(55)
                    val age = Random.nextInt(18, 34)
                    val valAmt = getPlayerValue(baseRat, age, role)
                    val wageAmt = getPlayerWage(baseRat)

                    playersDatabaseList.add(
                        PlayerEntity(
                            name = pName,
                            position = role,
                            rating = baseRat,
                            shooting = if (role == "ATT") baseRat + Random.nextInt(2, 5) else if (role == "GK") 12 else (baseRat - 15).coerceAtLeast(25),
                            passing = if (role == "MID") baseRat + Random.nextInt(2, 5) else (baseRat - 10).coerceAtLeast(25),
                            speed = (baseRat + Random.nextInt(-5, 7)).coerceIn(45, 98),
                            defending = if (role == "DEF") baseRat + Random.nextInt(2, 5) else if (role == "GK") 8 else 30,
                            goalkeeper = if (role == "GK") baseRat + Random.nextInt(2, 5) else 8,
                            age = age,
                            clubId = c.id,
                            value = valAmt,
                            wage = wageAmt,
                            onTransferList = false
                        )
                    )
                }
            }
        }

        // If Player Career mode is selected, add the customizable player into the database
        if (careerMode == "Player") {
            playersDatabaseList.add(
                PlayerEntity(
                    name = managerName,
                    position = playerPosition,
                    rating = 75,
                    shooting = if (playerPosition == "ATT") 78 else 45,
                    passing = if (playerPosition == "MID") 77 else 48,
                    speed = 82,
                    defending = if (playerPosition == "DEF") 77 else 32,
                    goalkeeper = if (playerPosition == "GK") 78 else 8,
                    age = 18,
                    clubId = selectedClubId,
                    value = getPlayerValue(75, 18, playerPosition),
                    wage = 25_000L,
                    onTransferList = false
                )
            )
        }

        // Add marquee iconic transfer star free agents
        val transferMarketStars = listOf(
            Triple("Lionel Messi", "ATT", 88),
            Triple("Luka Modrić", "MID", 83),
            Triple("Toni Kroos", "MID", 84),
            Triple("Sergio Ramos", "DEF", 81),
            Triple("David De Gea", "GK", 80),
            Triple("Paulo Dybala", "ATT", 83)
        )

        for (item in transferMarketStars) {
            val age = Random.nextInt(32, 38)
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

        // 3. Generate Double Round-Robin Fixtures specifically for the 6 teams of the user's selected league
        val leagueClubs = clubs.filter { it.league == leagueCode }
        val teamList = leagueClubs.map { it.id }.toMutableList()
        val numTeams = teamList.size
        val firstLegFixtures = mutableListOf<FixtureEntity>()

        // 5 Weeks for Round Robin Leg 1
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

        // Generate Leg 2 (Rounds 6 to 10) by flipping home and away
        val allFixtures = mutableListOf<FixtureEntity>()
        allFixtures.addAll(firstLegFixtures)

        for (f in firstLegFixtures) {
            allFixtures.add(
                FixtureEntity(
                    season = 1,
                    week = f.week + (numTeams - 1),
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
            week = 1,
            careerMode = careerMode,
            playerName = managerName,
            playerPosition = playerPosition,
            selectedLeagueCode = leagueCode
        )
        careerDao.insertCareer(career)

        // 5. Place inbox welcoming news letter
        val welcomeNews = NewsEntity(
            title = "Inauguration Announcement!",
            titleAr = if (careerMode == "Player") "⚽ انطلاق مسيرة تخليد اللاعب الجديد!" else "👑 تعيين القيادة التكتيكية الجديدة للمدرب!",
            content = if (careerMode == "Player") {
                "Exciting! $managerName has signed a professional player contract with ${userClubRef.name}! Fans are ready to see the talent rise."
            } else {
                "Breaking News! $managerName has been announced as the official head coach of ${userClubRef.name}! The board expects solid glory with a budget of $${userClubRef.budget / 1_000_000}M."
            },
            contentAr = if (careerMode == "Player") {
                "ساعة الحسم! وقّعت الجوهرة الكروية $managerName عقداً احترافياً رسمياً مع نادي ${userClubRef.nameAr} لدعم الفريق هذا الموسم كلاعب أساسي واعد بـ ${playerPosition}!"
            } else {
                "أخبار عاجلة! تم الإعلان عن تعيين المدرب القدير $managerName كقائد فني وتكتيكي لنادي ${userClubRef.nameAr}! تترقب الإدارة وعشاق الفريق تحقيق البطولات هذا الموسم بميزانية قدرها $${userClubRef.budget / 1_000_000} مليون دولار."
            },
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
