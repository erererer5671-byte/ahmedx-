package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

import kotlinx.coroutines.Job
import androidx.room.Room

data class SlotInfo(
    val slotId: Int,
    val hasData: Boolean,
    val managerName: String = "",
    val clubName: String = "",
    val clubNameAr: String = "",
    val league: String = "",
    val season: Int = 1,
    val week: Int = 1
)

data class ClubJobOffer(
    val clubId: Int,
    val clubName: String,
    val clubNameAr: String,
    val budget: Long,
    val league: String,
    val primaryColor: Long,
    val reputation: Int,
    val contractWage: Long
)

class CareerViewModel(application: Application) : AndroidViewModel(application) {

    private val fallbackDb = AppDatabase.getDatabase(application)
    private val fallbackDao = fallbackDb.careerDao()
    private val fallbackRepository = CareerRepository(fallbackDao)

    // Dynamic Database slot tracking
    private val _activeSlot = MutableStateFlow<Int?>(null)
    val activeSlotId = _activeSlot.asStateFlow()

    private val _slotsInfo = MutableStateFlow<List<SlotInfo>>(emptyList())
    val slotsInfo = _slotsInfo.asStateFlow()

    var currentDao: CareerDao? = null
    var currentRepo: CareerRepository? = null

    val repository: CareerRepository
        get() = currentRepo ?: fallbackRepository

    val dao: CareerDao
        get() = currentDao ?: fallbackDao

    // State Collection Job
    private var activeCollectionJob: Job? = null

    // Mutable states that are populated dynamically once a slot is active
    private val _careerState = MutableStateFlow<CareerEntity?>(null)
    val careerState = _careerState.asStateFlow()

    private val _clubsState = MutableStateFlow<List<ClubEntity>>(emptyList())
    val clubsState = _clubsState.asStateFlow()

    private val _newsState = MutableStateFlow<List<NewsEntity>>(emptyList())
    val newsState = _newsState.asStateFlow()

    private val _fixturesState = MutableStateFlow<List<FixtureEntity>>(emptyList())
    val fixturesState = _fixturesState.asStateFlow()

    private val _transferPlayersState = MutableStateFlow<List<PlayerEntity>>(emptyList())
    val transferPlayersState = _transferPlayersState.asStateFlow()

    private val _journalsState = MutableStateFlow<List<JournalEntity>>(emptyList())
    val journalsState = _journalsState.asStateFlow()

    private val _loanedOutPlayers = MutableStateFlow<List<PlayerEntity>>(emptyList())
    val loanedOutPlayers = _loanedOutPlayers.asStateFlow()

    // Scout Discoveries Flow
    private val _scoutDiscoveries = MutableStateFlow<List<PlayerEntity>>(emptyList())
    val scoutDiscoveries = _scoutDiscoveries.asStateFlow()

    // Resignation and Job Offers tracking
    private val _isResignedState = MutableStateFlow(false)
    val isResignedState = _isResignedState.asStateFlow()

    private val _jobOffers = MutableStateFlow<List<ClubJobOffer>>(emptyList())
    val jobOffers = _jobOffers.asStateFlow()

    fun getDatabaseForSlot(slot: Int): AppDatabase {
        return Room.databaseBuilder(
            getApplication(),
            AppDatabase::class.java,
            "soccer_manager_career_database_slot_$slot"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    fun loadSlotsInfo() {
        viewModelScope.launch {
            val list = mutableListOf<SlotInfo>()
            for (slot in 1..3) {
                try {
                    val db = getDatabaseForSlot(slot)
                    val career = db.careerDao().getCareer()
                    if (career != null) {
                        val dbClub = db.careerDao().getClubById(career.clubId)
                        list.add(
                            SlotInfo(
                                slotId = slot,
                                hasData = true,
                                managerName = career.managerName,
                                clubName = dbClub?.name ?: "Unknown",
                                clubNameAr = dbClub?.nameAr ?: "غير معروف",
                                league = career.selectedLeagueCode,
                                season = career.season,
                                week = career.week
                            )
                        )
                    } else {
                        list.add(SlotInfo(slotId = slot, hasData = false))
                    }
                    db.close()
                } catch (e: Exception) {
                    list.add(SlotInfo(slotId = slot, hasData = false))
                }
            }
            _slotsInfo.value = list
        }
    }

    fun selectSaveSlot(slotId: Int) {
        _activeSlot.value = slotId
        val dbForSlot = getDatabaseForSlot(slotId)
        val daoForSlot = dbForSlot.careerDao()
        val repoForSlot = CareerRepository(daoForSlot)

        currentDao = daoForSlot
        currentRepo = repoForSlot

        // Cancel old collector
        activeCollectionJob?.cancel()

        _isResignedState.value = false
        _jobOffers.value = emptyList()

        // Sync and launch job to retrieve database updates reactively
        activeCollectionJob = viewModelScope.launch {
            launch {
                repoForSlot.careerFlow.collect { career ->
                    _careerState.value = career
                    if (career != null) {
                        val userC = daoForSlot.getClubById(career.clubId)
                        _userClub.value = userC

                        val squad = repoForSlot.getPlayersByClub(career.clubId)
                        _userSquad.value = squad

                        val fixtures = repoForSlot.getFixturesByWeek(career.week)
                        val nextF = fixtures.firstOrNull {
                            it.homeTeamId == career.clubId || it.awayTeamId == career.clubId
                        }
                        _matchFixture.value = nextF
                        if (nextF != null) {
                            val oppId = if (nextF.homeTeamId == career.clubId) nextF.awayTeamId else nextF.homeTeamId
                            _opponentClub.value = daoForSlot.getClubById(oppId)
                        }
                    } else {
                        // Reset when database gets deleted
                        _userClub.value = null
                        _userSquad.value = emptyList()
                        _matchFixture.value = null
                        _opponentClub.value = null
                    }
                }
            }

            launch {
                repoForSlot.clubsFlow.collect { list ->
                    _clubsState.value = list
                }
            }

            launch {
                repoForSlot.newsFlow.collect { list ->
                    _newsState.value = list
                }
            }

            launch {
                repoForSlot.fixturesFlow.collect { list ->
                    _fixturesState.value = list
                }
            }

            launch {
                repoForSlot.journalsFlow.collect { list ->
                    _journalsState.value = list
                }
            }

            launch {
                repoForSlot.transferPlayersFlow.collect { list ->
                    _transferPlayersState.value = list
                }
            }

            launch {
                daoForSlot.getPlayersFlow().collect { list ->
                    val userC = _userClub.value
                    _loanedOutPlayers.value = if (userC != null) {
                        list.filter { it.originalClubId == userC.id && it.isOnLoan && it.clubId != userC.id }
                    } else {
                        emptyList()
                    }
                    _scoutDiscoveries.value = list.filter { it.clubId == -2 }
                }
            }
        }
    }

    fun exitSaveSlot() {
        activeCollectionJob?.cancel()
        currentDao = null
        currentRepo = null
        _activeSlot.value = null
        _careerState.value = null
        _userClub.value = null
        _userSquad.value = emptyList()
        _matchFixture.value = null
        _opponentClub.value = null
        loadSlotsInfo()
    }

    fun deleteSaveSlot(slotId: Int) {
        viewModelScope.launch {
            try {
                val db = getDatabaseForSlot(slotId)
                db.clearAllTables()
                db.close()
                getApplication<Application>().deleteDatabase("soccer_manager_career_database_slot_$slotId")
            } catch (e: Exception) {
                // Ignore failure
            }
            loadSlotsInfo()
        }
    }

    // Resignation
    fun resignFromCurrentClub() {
        val career = careerState.value ?: return
        val uClub = userClub.value ?: return
        viewModelScope.launch {
            // Remove user ownership from current club
            dao.updateClub(uClub.copy(isUser = false))

            // Compilation of 4 random Club job offers
            val allClubs = dao.getClubs().filter { it.id != uClub.id }
            val selectedOffers = allClubs.shuffled().take(4).map { c ->
                ClubJobOffer(
                    clubId = c.id,
                    clubName = c.name,
                    clubNameAr = c.nameAr,
                    budget = c.budget,
                    league = c.league,
                    primaryColor = c.primaryColor,
                    reputation = c.reputation,
                    contractWage = (c.reputation * 1000L + Random.nextInt(-5000, 10000)).coerceAtLeast(12000L)
                )
            }
            _jobOffers.value = selectedOffers
            _isResignedState.value = true

            // Send board notification
            val resignNews = NewsEntity(
                title = "Sensational Manager Resignation!",
                titleAr = "عاجل: تقديم استقالة المدرب ⚡",
                content = "${career.managerName} has officially resigned from ${uClub.name} and is looking for a new tactical vacancy in global soccer leagues.",
                contentAr = "أخبار عاجلة وصادمة! قدّم المدرب الفني والقدير ${career.managerName} استقالته الرسمية من تدريب نادي ${uClub.nameAr}. يُقال أن وكيل أعماله يُناقش بعض العروض المحلية والدولية الجديدة الكبرى والمتاحة حالياً!",
                type = "Board"
            )
            repository.insertNews(resignNews)
        }
    }

    fun acceptJobOffer(offer: ClubJobOffer) {
        val career = careerState.value ?: return
        viewModelScope.launch {
            val newClub = dao.getClubById(offer.clubId) ?: return@launch

            // Set new club isUser to true
            dao.updateClub(newClub.copy(isUser = true))

            // Clear squad cache before loading
            _userSquad.value = emptyList()

            // Update Career attributes
            val updatedCareer = career.copy(
                clubId = newClub.id,
                budget = newClub.budget,
                selectedLeagueCode = newClub.league
            )
            repository.updateCareer(updatedCareer)

            val newSquad = repository.getPlayersByClub(newClub.id)
            _userSquad.value = newSquad
            _userClub.value = newClub

            // Re-identify match fixture
            val fixtures = repository.getFixturesByWeek(career.week)
            val nextF = fixtures.firstOrNull {
                it.homeTeamId == newClub.id || it.awayTeamId == newClub.id
            }
            _matchFixture.value = nextF
            if (nextF != null) {
                val oppId = if (nextF.homeTeamId == newClub.id) nextF.awayTeamId else nextF.homeTeamId
                _opponentClub.value = dao.getClubById(oppId)
            }

            // News announcement
            val welcomeNews = NewsEntity(
                title = "New Appointment Announcement!",
                titleAr = "رسمياً: تعيين المدرب الجديد لنادي ${newClub.nameAr}! ✍️",
                content = "Sensational! ${career.managerName} has officially signed a contract with ${newClub.name} with a total transfer budget of $${newClub.budget / 1_000_000}M.",
                contentAr = "تم حسم الأمور! وقّع المدرب الفذ ${career.managerName} عقداً رسمياً جديداً ليقود الدفة الفنية لنادي ${newClub.nameAr}! وقد أبدى تطلعه لتحقيق البطولات بميزانية تعاقدات قدرها $${newClub.budget / 1_000_000} مليون دولار.",
                type = "Board"
            )
            repository.insertNews(welcomeNews)

            // Logging welcomes in journal
            val apptJournal = JournalEntity(
                season = career.season,
                week = career.week,
                title = "New Club Takeover",
                titleAr = "تولّي القيادة الفنية في نادي ${newClub.nameAr} 🌟",
                content = "Signed contracts with ${newClub.name}.",
                contentAr = "تم توقيع عقد تدريب القيادة الكروية في نادي ${newClub.nameAr} رسمياً وبداية المعسكر التدريبي الأول لتهيئة واستقطاب اللاعبين الجدد!",
                isAuto = true
            )
            repository.insertJournal(apptJournal)

            // Clear Resignation state
            _isResignedState.value = false
            _jobOffers.value = emptyList()
            _currentTab.value = 0 // Return to home screen
        }
    }


    // UI state holder
    private val _currentTab = MutableStateFlow(0) // 0: Home, 1: Squad, 2: Table, 3: Transfers, 4: Inbox
    val currentTab = _currentTab.asStateFlow()

    // Squad details reactive flow
    private val _userSquad = MutableStateFlow<List<PlayerEntity>>(emptyList())
    val userSquad = _userSquad.asStateFlow()

    fun selectTab(tab: Int) {
        _currentTab.value = tab
    }

    // Bidding states represent negotiations
    sealed class BidState {
        object None : BidState()
        data class Submitted(val isAccepted: Boolean, val messageAr: String, val counterOffer: Long = 0L) : BidState()
    }

    private val _activeBidPlayer = MutableStateFlow<PlayerEntity?>(null)
    val activeBidPlayer = _activeBidPlayer.asStateFlow()

    private val _bidDialogState = MutableStateFlow<BidState>(BidState.None)
    val bidDialogState = _bidDialogState.asStateFlow()

    // Training Schedule selection: "Balanced", "Intensive", "Tactical", "Recovery"
    private val _selectedTrainingSchedule = MutableStateFlow("Balanced")
    val selectedTrainingSchedule = _selectedTrainingSchedule.asStateFlow()

    private val _trainingFeedback = MutableStateFlow<String?>(null)
    val trainingFeedback = _trainingFeedback.asStateFlow()

    // Match Speed multiplier state for simulation
    private val _matchSpeed = MutableStateFlow(1f) // 1f, 2f, 4f
    val matchSpeed = _matchSpeed.asStateFlow()

    fun changeMatchSpeed(speed: Float) {
        _matchSpeed.value = speed
    }

    // Dynamic Team Chemistry Flow calculated on the fly based on roster positions
    val teamChemistry: StateFlow<Int> = _userSquad.map { squad ->
        if (squad.isEmpty()) return@map 0
        var gkCount = 0
        var defCount = 0
        var midCount = 0
        var attCount = 0
        for (p in squad) {
            when (p.position) {
                "GK" -> gkCount++
                "DEF" -> defCount++
                "MID" -> midCount++
                "ATT" -> attCount++
            }
        }
        var chem = 100
        if (gkCount < 1) chem -= 25
        if (defCount < 3) chem -= (3 - defCount).coerceAtLeast(0) * 10
        if (midCount < 3) chem -= (3 - midCount).coerceAtLeast(0) * 10
        if (attCount < 2) chem -= (2 - attCount).coerceAtLeast(0) * 10
        chem.coerceIn(20, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 80)

    // Opponent dynamic tactical state during match simulation
    private val _opponentCurrentTactic = MutableStateFlow("Balanced")
    val opponentCurrentTactic = _opponentCurrentTactic.asStateFlow()

    fun selectTrainingSchedule(schedule: String) {
        _selectedTrainingSchedule.value = schedule
    }

    fun clearTrainingFeedback() {
        _trainingFeedback.value = null
    }

    fun selectPlayerForBid(player: PlayerEntity) {
        _activeBidPlayer.value = player
        _bidDialogState.value = BidState.None
    }

    fun closeBidDialog() {
        _activeBidPlayer.value = null
        _bidDialogState.value = BidState.None
    }

    fun submitTransferBid(player: PlayerEntity, bidAmount: Long) {
        val career = careerState.value ?: return
        val uClub = userClub.value ?: return
        if (career.budget < bidAmount) {
            _bidDialogState.value = BidState.Submitted(false, "عفواً! ميزانية النادي لا تكفي لتقديم هذا العرض المالي.")
            return
        }

        viewModelScope.launch {
            val playerVal = player.value
            val ratio = bidAmount.toDouble() / playerVal

            if (ratio >= 0.95) {
                // Success! Buy player directly
                val success = repository.buyPlayerFromMarket(player.copy(value = bidAmount), uClub, career)
                if (success) {
                    _bidDialogState.value = BidState.Submitted(true, "تم قبول العرض والتعاقد بنجاح! اللاعب ${player.name} سعيد بالاتفاق المالي المبرم ويرتدي قميص ناديك رسمياً.")
                    _userSquad.value = repository.getPlayersByClub(career.clubId)
                } else {
                    _bidDialogState.value = BidState.Submitted(false, "عفواً، فشلت الصفقة لسبب فني طارئ.")
                }
            } else if (ratio >= 0.75) {
                // Suggest counter-offer
                val counter = (playerVal * Random.nextDouble(0.95, 1.05)).toLong()
                _bidDialogState.value = BidState.Submitted(false, "النادي البائع يقابل عرضك بالمفاوضة: يطلب النادي مبلغ $${counter / 1_000_000}M للموافقة الرسمية على الانتقال.", counter)
            } else {
                // Rejected
                _bidDialogState.value = BidState.Submitted(false, "مرفوض تماماً! وكيل اللاعب يعتبر عرضه هزيلاً جداً ويرفض استكمال جلسة المفاوضات.")
            }
        }
    }

    fun acceptCounterOffer(player: PlayerEntity, counterAmount: Long) {
        val career = careerState.value ?: return
        val uClub = userClub.value ?: return
        if (career.budget < counterAmount) {
            _bidDialogState.value = BidState.Submitted(false, "رصيد النادي غير كافٍ لتلبية طلب النادي البائع.")
            return
        }
        viewModelScope.launch {
            val success = repository.buyPlayerFromMarket(player.copy(value = counterAmount), uClub, career)
            if (success) {
                _bidDialogState.value = BidState.None
                _activeBidPlayer.value = null
                _userSquad.value = repository.getPlayersByClub(career.clubId)
            }
        }
    }

    fun processIncomingTransferOffer(news: NewsEntity, accept: Boolean) {
        val career = careerState.value ?: return
        val uClub = userClub.value ?: return
        viewModelScope.launch {
            if (accept) {
                val squad = _userSquad.value
                val player = squad.firstOrNull { news.contentAr.contains(it.name) || news.titleAr.contains(it.name) || news.content.contains(it.name) || news.title.contains(it.name) }
                if (player != null && squad.size > 5) {
                    if (news.titleAr.contains("طلب استعارة") || news.contentAr.contains("استعارة")) {
                        // This is a LOAN offer! AI pays 10% premium upfront fee
                        val loanFee = (player.value * 0.10).toLong()
                        val biddingClubs = dao.getClubs().filter { !it.isUser }
                        val recipientId = biddingClubs.randomOrNull()?.id ?: 0

                        val updated = player.copy(
                            isOnLoan = true,
                            loanWeeksLeft = 6,
                            originalClubId = uClub.id,
                            clubId = recipientId,
                            onLoanList = false
                        )
                        repository.updatePlayer(updated)

                        // Inject loan bonus to budget
                        repository.updateCareer(career.copy(budget = career.budget + loanFee))

                        val dealNews = NewsEntity(
                            title = "Loan Approved!",
                            titleAr = "إعارة للخارج: موافقة النادي 👍",
                            content = "Approved 6 weeks loan of ${player.name}.",
                            contentAr = "تمت الموافقة الرسمية على إعارة جوهرتك ${player.name} لخصوم دورينا لمدة 6 أسابيع، ودخل خزينة النادي رسوم إعارة قدرها $${loanFee / 1_000_000}M.",
                            type = "Global"
                        )
                        repository.insertNews(dealNews)

                        logSeasonJournal(
                            titleAr = "🌐 إعارة لاعب للخارج",
                            contentAr = "تمت الموافقة على إعارة نجم الفريق ${player.name} لتجربة كروية خارجية وتوفير عبء راتبه مع جني $${loanFee / 1_000_000}M رسوم إعارة."
                        )
                    } else {
                        // Extract randomized price based on news text if possible, else standard formula
                        val offerValue = (player.value * Random.nextDouble(0.95, 1.15)).toLong()
                        val success = repository.sellPlayerToAI(player.copy(value = offerValue), uClub, career)
                        if (success) {
                            _userSquad.value = repository.getPlayersByClub(career.clubId)
                        }
                    }
                }
            }
            dao.deleteNews(news)
        }
    }

    // Interactive Match Elements State
    private val _matchFixture = MutableStateFlow<FixtureEntity?>(null)
    val matchFixture = _matchFixture.asStateFlow()

    private val _userClub = MutableStateFlow<ClubEntity?>(null)
    val userClub = _userClub.asStateFlow()

    private val _opponentClub = MutableStateFlow<ClubEntity?>(null)
    val opponentClub = _opponentClub.asStateFlow()

    private val _matchState = MutableStateFlow<MatchPlayState>(MatchPlayState.PreMatch)
    val matchState = _matchState.asStateFlow()

    private val _isSkippingSeason = MutableStateFlow(false)
    val isSkippingSeason = _isSkippingSeason.asStateFlow()

    init {
        loadSlotsInfo()
    }

    // User Career Creation
    fun createNewCareer(
        managerName: String,
        clubId: Int,
        careerMode: String = "Manager",
        playerPosition: String = "ATT",
        leagueCode: String = "SPL"
    ) {
        viewModelScope.launch {
            repository.initializeNewCareer(
                managerName = managerName,
                selectedClubId = clubId,
                careerMode = careerMode,
                playerPosition = playerPosition,
                leagueCode = leagueCode
            )
            _currentTab.value = 0

            // Logging initial journal entry
            val welcomeJournal = JournalEntity(
                season = 1,
                week = 1,
                title = "Inauguration Day",
                titleAr = if (careerMode == "Player") "⚽ انطلاق مسيرة اللاعب $managerName" else "👑 بداية المسيرة الكروية للمدرب $managerName",
                content = "Profile officially started.",
                contentAr = if (careerMode == "Player") {
                    "انطلقت المسيرة الرياضية الاحترافية للاعب المحترف $managerName. الآمال عريضة والجميع ينتظر تطوره وتدريباته وتألقه في المباريات للتنافس على الانتقال لأفضل الأندية العالمية!"
                } else {
                    "تم تعيين المدرب القدير $managerName رسمياً على رأس القيادة الفنية لناديكم المختار. الآمال معقودة والجميع يتطلعون لتقديم أداء تكتيكي أسطوري هذا الموسم الكروي الجديد!"
                },
                isAuto = true
            )
            repository.insertJournal(welcomeJournal)
        }
    }

    // Buy Player from Transfer List
    fun purchasePlayer(player: PlayerEntity) {
        val career = careerState.value ?: return
        val uClub = userClub.value ?: return
        viewModelScope.launch {
            val success = repository.buyPlayerFromMarket(player, uClub, career)
            if (success) {
                // Refresh squad
                _userSquad.value = repository.getPlayersByClub(career.clubId)
            }
        }
    }

    // Toggle status of a player on transfer list
    fun toggleTransferStatus(player: PlayerEntity) {
        viewModelScope.launch {
            repository.putPlayerOnTransferList(player.id, !player.onTransferList)
        }
    }

    // --- LOAN SYSTEM AND JOURNALING METHODS ---

    fun writeManualJournal(titleAr: String, contentAr: String) {
        val career = careerState.value ?: return
        viewModelScope.launch {
            val journal = JournalEntity(
                season = career.season,
                week = career.week,
                title = "Coach Diary Entry",
                titleAr = titleAr,
                content = "Manual entry drafted by head coach.",
                contentAr = contentAr,
                isAuto = false
            )
            repository.insertJournal(journal)
        }
    }

    fun logSeasonJournal(titleAr: String, contentAr: String) {
        val career = careerState.value ?: return
        viewModelScope.launch {
            val journal = JournalEntity(
                season = career.season,
                week = career.week,
                title = "Automated Milestone",
                titleAr = titleAr,
                content = "System auto logged event",
                contentAr = contentAr,
                isAuto = true
            )
            repository.insertJournal(journal)
        }
    }

    fun loanInPlayer(player: PlayerEntity, weeks: Int = 6) {
        val career = careerState.value ?: return
        val uClub = userClub.value ?: return
        val loanFee = (player.value * 0.05).toLong() // 5% upfront fee

        if (career.budget < loanFee) {
            _bidDialogState.value = BidState.Submitted(false, "رصيد ناديكم عاجز عن سداد تكلفة الإعارة البالغة $${loanFee / 1_000_000}M.")
            return
        }

        viewModelScope.launch {
            // Update player entity
            val updated = player.copy(
                isOnLoan = true,
                loanWeeksLeft = weeks,
                originalClubId = player.clubId,
                clubId = uClub.id,
                onTransferList = false,
                onLoanList = false
            )
            repository.updatePlayer(updated)

            // Deduct loan fee
            repository.updateCareer(career.copy(budget = career.budget - loanFee))

            // Post news
            val news = NewsEntity(
                title = "Player Loaned In!",
                titleAr = "إعارة ناجحة: انضمام ${player.name}! ✍️",
                content = "Formalized deal! ${player.name} joins on loan for $weeks weeks.",
                contentAr = "أعلن نادي ${uClub.nameAr} رسمياً عن إبرام صفقة إعارة مع اللاعب ${player.name} لمدة $weeks أسابيع مقابل رسوم إعارة أولية قدرها $${loanFee / 1_000_000}M.",
                type = "Global"
            )
            repository.insertNews(news)

            // Auto log in season journal
            logSeasonJournal(
                titleAr = "✍️ تعاقد بنظام الإعارة",
                contentAr = "تم استعارة اللاعب المميز ${player.name} لتدعيم خطوط الفريق لمدة $weeks أسابيع بقيمة $${loanFee / 1_000_000}M."
            )

            // Refresh squad
            _userSquad.value = repository.getPlayersByClub(career.clubId)
        }
    }

    fun toggleLoanStatus(player: PlayerEntity) {
        viewModelScope.launch {
            val updated = player.copy(onLoanList = !player.onLoanList)
            repository.updatePlayer(updated)
            _userSquad.value = repository.getPlayersByClub(player.clubId)
        }
    }

    fun terminateOrRecallLoan(player: PlayerEntity) {
        val career = careerState.value ?: return
        val uClub = userClub.value ?: return

        viewModelScope.launch {
            if (player.originalClubId == uClub.id) {
                // User is recalling their own player loaned to AI. Cost: 500,000$ recall fee
                val recallCost = 500_000L
                if (career.budget >= recallCost) {
                    val updated = player.copy(
                        isOnLoan = false,
                        loanWeeksLeft = 0,
                        clubId = uClub.id,
                        onLoanList = false
                    )
                    repository.updatePlayer(updated)
                    repository.updateCareer(career.copy(budget = career.budget - recallCost))

                    val news = NewsEntity(
                        title = "Player Recalled!",
                        titleAr = "استدعاء لاعب من الإعارة ↩️",
                        content = "Recalled ${player.name} back to squad.",
                        contentAr = "قررت إدارة نادي ${uClub.nameAr} قطع إعارة اللاعب ${player.name} واستدعائه مبكراً للفريق مع تحمل غرامة مالية قدرها 500,000$.",
                        type = "Global"
                    )
                    repository.insertNews(news)

                    logSeasonJournal(
                        titleAr = "↩️ استدعاء لاعب",
                        contentAr = "تم استدعاء اللاعب ${player.name} من فترة إعارته مبكراً لدعم الفريق في المباريات الحاسمة."
                    )
                }
            } else {
                // User is returning a borrowed player back to their parent AI club early. Free of charge
                val parentClub = repository.getClubById(player.originalClubId)
                val updated = player.copy(
                    isOnLoan = false,
                    loanWeeksLeft = 0,
                    clubId = player.originalClubId,
                    onLoanList = false
                )
                repository.updatePlayer(updated)

                val news = NewsEntity(
                    title = "Loan Terminated Early",
                    titleAr = "إنهاء إعارة مبكّر للوافد 🚪",
                    content = "Returned loaned player ${player.name} early.",
                    contentAr = "قرر المدرب إنهاء إعارة اللاعب ${player.name} مبكراً وإعادته لصفوف ناديه السابق ${parentClub?.nameAr ?: "الأصلي"}.",
                    type = "Global"
                )
                repository.insertNews(news)

                logSeasonJournal(
                    titleAr = "🚪 إنهاء إعارة وافد",
                    contentAr = "تمت تسوية وإنهاء كشف استعارة اللاعب ${player.name} وإعادته إلى ناديه الأصلي."
                )
            }
            _userSquad.value = repository.getPlayersByClub(career.clubId)
        }
    }

    // Upgrade player credentials (Train) with dynamic success & exhaustion rates
    fun trainPlayer(player: PlayerEntity, stat: String) {
        val career = careerState.value ?: return
        val uClub = userClub.value ?: return
        
        if (stat == "energy") {
            viewModelScope.launch {
                val success = repository.upgradePlayerSkill(player, "energy", 500_000L, uClub.id, career)
                if (success) {
                    _userSquad.value = repository.getPlayersByClub(career.clubId)
                }
            }
            return
        }

        val cost = 1_000_000L + (player.rating - 70).coerceAtLeast(0) * 500_000L
        if (career.budget < cost) {
            _trainingFeedback.value = "ميزانية النادي غير كافية لدعم تكاليف هذه الحصة التدريبية الشاقة."
            return
        }

        viewModelScope.launch {
            if (Random.nextDouble() < 0.70) {
                // Success
                val success = repository.upgradePlayerSkill(player, stat, cost, uClub.id, career)
                if (success) {
                    _userSquad.value = repository.getPlayersByClub(career.clubId)
                    _trainingFeedback.value = "نجح التدريب! لقد تحسن مستوى اللاعب ${player.name} بمقدار (+1) في الإحصائية المستهدفة بنجاح 📈"
                } else {
                    _trainingFeedback.value = "حدث خطأ أثناء إجراء التدريب المالي."
                }
            } else {
                // Failed due to exhaustion but penalty cost applies
                val fatigue = Random.nextInt(10, 18)
                val updatedP = player.copy(energy = (player.energy - fatigue).coerceAtLeast(10))
                repository.updatePlayer(updatedP)

                // Substantial partial cost deduction to pay trainers
                val fine = cost / 2
                repository.updateCareer(career.copy(budget = career.budget - fine))

                _userSquad.value = repository.getPlayersByClub(career.clubId)
                _trainingFeedback.value = "أخفق اللاعب ${player.name} في إكمال المناورة التدريبية بداعي الإجهاد العضلي، وخسر ${fatigue}% من لياقته البدنية ⚠️"
            }
        }
    }

    // Apply Weekly Schedule Training automatically at advanceWeek
    private suspend fun applyWeeklyTrainingSchedule(squad: List<PlayerEntity>, career: CareerEntity) {
        val schedule = _selectedTrainingSchedule.value
        var upgradedCount = 0
        
        for (p in squad) {
            var updatedP = p
            when (schedule) {
                "Intensive" -> {
                    val newEnergy = (p.energy - 18).coerceAtLeast(10)
                    if (Random.nextDouble() < 0.75) {
                        val statToUpgrade = if (p.position == "GK") "goalkeeper" else listOf("shooting", "passing", "speed").random()
                        updatedP = when (statToUpgrade) {
                            "shooting" -> p.copy(shooting = (p.shooting + 1).coerceAtMost(99))
                            "passing" -> p.copy(passing = (p.passing + 1).coerceAtMost(99))
                            "speed" -> p.copy(speed = (p.speed + 1).coerceAtMost(99))
                            "goalkeeper" -> p.copy(goalkeeper = (p.goalkeeper + 1).coerceAtMost(99))
                            else -> p
                        }
                        if (updatedP.rating < 99) {
                            updatedP = updatedP.copy(rating = updatedP.rating + 1)
                        }
                        upgradedCount++
                    }
                    updatedP = updatedP.copy(energy = newEnergy)
                    repository.updatePlayer(updatedP)
                }
                "Tactical" -> {
                    val newEnergy = (p.energy - 10).coerceAtLeast(12)
                    if (Random.nextDouble() < 0.65) {
                        val statToUpgrade = if (p.position == "GK") "passing" else listOf("passing", "defending").random()
                        updatedP = when (statToUpgrade) {
                            "passing" -> p.copy(passing = (p.passing + 1).coerceAtMost(99))
                            "defending" -> p.copy(defending = (p.defending + 1).coerceAtMost(99))
                            else -> p
                        }
                        if (updatedP.rating < 99) {
                            updatedP = updatedP.copy(rating = updatedP.rating + 1)
                        }
                        upgradedCount++
                    }
                    updatedP = updatedP.copy(energy = newEnergy)
                    repository.updatePlayer(updatedP)
                }
                "Recovery" -> {
                    val newEnergy = (p.energy + 25).coerceAtMost(100)
                    updatedP = p.copy(energy = newEnergy)
                    repository.updatePlayer(updatedP)
                }
                else -> { // Balanced
                    val newEnergy = (p.energy + 5).coerceAtMost(100)
                    if (Random.nextDouble() < 0.35) {
                        val statToUpgrade = if (p.position == "GK") "goalkeeper" else listOf("shooting", "passing", "defending").random()
                        updatedP = when (statToUpgrade) {
                            "shooting" -> p.copy(shooting = (p.shooting + 1).coerceAtMost(99))
                            "passing" -> p.copy(passing = (p.passing + 1).coerceAtMost(99))
                            "defending" -> p.copy(defending = (p.defending + 1).coerceAtMost(99))
                            "goalkeeper" -> p.copy(goalkeeper = (p.goalkeeper + 1).coerceAtMost(99))
                            else -> p
                        }
                        if (updatedP.rating < 99) {
                            updatedP = updatedP.copy(rating = updatedP.rating + 1)
                        }
                        upgradedCount++
                    }
                    updatedP = updatedP.copy(energy = newEnergy)
                    repository.updatePlayer(updatedP)
                }
            }
        }
        
        // Cost deduction
        if (schedule == "Recovery") {
            val cost = 250_000L
            if (career.budget >= cost) {
                repository.updateCareer(career.copy(budget = career.budget - cost))
            }
        }

        val scheduleAr = when (schedule) {
            "Intensive" -> "البدني الشاق المكثف ⚡"
            "Tactical" -> "التكتيك الفني والمناورات ⚽"
            "Recovery" -> "الاستشفاء الطبي والراحة 🥤"
            else -> "المتوازن واللعب المعتاد ⚖️"
        }

        val newsReport = NewsEntity(
            title = "Weekly Training Session Report",
            titleAr = "تقرير الحصة التدريبية المجدولة الأسبوعية 📋",
            content = "Completed scheduled weekly training program [$schedule]. Upgraded $upgradedCount players stats.",
            contentAr = "أتم الفريق الحصة التدريبية المجدولة لهذا الأسبوع بنجاح تحت نظام: [$scheduleAr]. تحسنت مهارات عدد ($upgradedCount) من لاعبي فريقك استعداداً للمنافس القادم في الدوري الممتاز.",
            type = "Board"
        )
        repository.insertNews(newsReport)
    }

    // Replenish entire squad energy
    fun recoverSquadEnergy() {
        val career = careerState.value ?: return
        val uClub = userClub.value ?: return
        val cost = 2_500_000L
        viewModelScope.launch {
            val success = repository.recoverSquadEnergy(cost, uClub.id, career)
            if (success) {
                _userSquad.value = repository.getPlayersByClub(career.clubId)
            }
        }
    }

    // Advanced week
    fun advanceWeek() {
        val career = careerState.value ?: return
        val matchF = matchFixture.value ?: return
        if (!matchF.isPlayed) return // User must play the match first!

        viewModelScope.launch {
            // 1. Simulate all other fixtures
            repository.simulateRestOfMatches(career.week, career.clubId)

            // Handle Scouting Mission progression (World Soccer Champs style)
            if (career.scoutingMissionActive) {
                val updatedWeeks = career.scoutingWeeksPassed + 1
                if (updatedWeeks >= 1) {
                    // Mission Finished! Find 3 wonderkids
                    val discoveredPlayers = mutableListOf<PlayerEntity>()
                    val positions = if (career.scoutingMissionPosition.isEmpty() || career.scoutingMissionPosition == "ANY") {
                        listOf("GK", "DEF", "MID", "ATT")
                    } else {
                        listOf(career.scoutingMissionPosition)
                    }

                    val firstNames = listOf("Jack", "Harry", "Thomas", "George", "Oliver", "William", "James", "Mason", "Jude", "Leo", "Zinedine", "Cristiano", "Kylian", "Erling", "Bukayo", "Lamine")
                    val lastNames = listOf("Champs", "Wunderkind", "Prodigy", "Wizard", "Talent", "Henderson", "Walker", "Alexander", "Green", "White", "Cole", "Bellingham", "Mainoo", "Guler")
                    
                    for (i in 0 until 3) {
                        val chosenPos = positions.shuffled().first()
                        val starRating = (career.scoutLevel * 6 + 55 + Random.nextInt(0, 8)).coerceIn(58, 88)
                        val age = Random.nextInt(17, 20)
                        val value = repository.getPlayerValue(starRating, age, chosenPos)
                        
                        val nameStr = "${firstNames.shuffled().first()} ${lastNames.shuffled().first()} (صيد الكشافة)"
                        discoveredPlayers.add(
                            PlayerEntity(
                                name = nameStr,
                                position = chosenPos,
                                rating = starRating,
                                shooting = if (chosenPos == "ATT") starRating + Random.nextInt(2, 6) else if (chosenPos == "GK") 10 else starRating - 10,
                                passing = if (chosenPos == "MID") starRating + Random.nextInt(2, 6) else starRating - 10,
                                speed = starRating + Random.nextInt(-4, 9),
                                defending = if (chosenPos == "DEF") starRating + Random.nextInt(2, 6) else if (chosenPos == "GK") 8 else 30,
                                goalkeeper = if (chosenPos == "GK") starRating + Random.nextInt(2, 6) else 8,
                                age = age,
                                clubId = -2, // Scout recruit candidate
                                value = value,
                                wage = 8_000L,
                                form = 7,
                                energy = 100
                            )
                        )
                    }

                    // Delete old scout discoveries (set to -9)
                    val allPlayers = dao.getPlayersFlow().first()
                    for (p in allPlayers) {
                        if (p.clubId == -2) {
                            repository.updatePlayer(p.copy(clubId = -9))
                        }
                    }

                    // Save new discoveries
                    dao.insertPlayers(discoveredPlayers)

                    // Update career
                    val finishedCareer = career.copy(
                        scoutingMissionActive = false,
                        scoutingWeeksPassed = 0,
                        scoutingMissionPosition = ""
                    )
                    repository.updateCareer(finishedCareer)

                    // News alerts
                    val scoutAlert = NewsEntity(
                        title = "Scout Dispatch Finished",
                        titleAr = "تقرير كشافة النادي جاهز! 🕵️‍♂️",
                        content = "Scout returned with prospective stars.",
                        contentAr = "عاد المندوب الكروي بقائمة استطلاع حصرية من (3) مواهب شابة Wonderkids تناسب احتياجات الفريق بأسعار تصفية مغرية. تصفح صفحة الكشافة لاستعراضهم والتعاقد معهم الآن!",
                        type = "Board"
                    )
                    repository.insertNews(scoutAlert)
                } else {
                    repository.updateCareer(career.copy(scoutingWeeksPassed = updatedWeeks))
                }
            }

            // 2. Reduce squad energy slightly (match fatigue)
            val squad = _userSquad.value
            for (p in squad) {
                val matchFatigue = if (p.position == "GK") 5 else Random.nextInt(10, 18)
                val newEnergy = (p.energy - matchFatigue).coerceAtLeast(15)
                repository.updatePlayer(p.copy(energy = newEnergy))
            }

            // Execute scheduled weekly training
            applyWeeklyTrainingSchedule(squad, career)

            // 3. Handle random transfer offers for our squad of players
            triggerRandomSellOffers(career, squad)

            // 3b. Loan system week countdown
            val allPlayers = dao.getPlayersFlow().first()
            for (p in allPlayers) {
                if (p.isOnLoan) {
                    val weeksLeft = p.loanWeeksLeft - 1
                    if (weeksLeft <= 0) {
                        // Loan ended! Return to parent club
                        val returned = p.copy(
                            isOnLoan = false,
                            loanWeeksLeft = 0,
                            clubId = p.originalClubId,
                            onLoanList = false
                        )
                        repository.updatePlayer(returned)

                        // Notify in inbox
                        val originalClub = repository.getClubById(p.originalClubId)
                        val returnNews = NewsEntity(
                            title = "Loan Concluded",
                            titleAr = "انتهى عقد الإعارة للاعب ${p.name} 🔚",
                            content = "Loan period ended. Player returns to original club.",
                            contentAr = "انتهى عقد الإعارة الرسمي بنجاح للاعب ${p.name} وعاد أدراجه لصفوف ناديه السابق ${originalClub?.nameAr ?: "الأصلي"}.",
                            type = "Global"
                        )
                        repository.insertNews(returnNews)

                        // Journal log
                        val journalTitle = "🔚 انتهاء الإعارة وعودة ${p.name}"
                        val journalContent = "انتهت فترة إعارة اللاعب ${p.name} وعاد رسمياً لناديه المالك لعقده."
                        val journal = JournalEntity(
                            season = career.season,
                            week = career.week,
                            title = "Loan Ended",
                            titleAr = journalTitle,
                            content = "Loan ended.",
                            contentAr = journalContent,
                            isAuto = true
                        )
                        repository.insertJournal(journal)
                    } else {
                        val updated = p.copy(loanWeeksLeft = weeksLeft)
                        repository.updatePlayer(updated)
                    }
                }
            }

            // 3c. Generate random AI loan offers
            triggerRandomLoanOffers(career, squad)

            // 3d. Auto Milestone Journal log for the match played this week
            val isUserHome = matchF.homeTeamId == career.clubId
            val userScore = if (isUserHome) matchF.homeScore ?: 0 else matchF.awayScore ?: 0
            val oppScore = if (isUserHome) matchF.awayScore ?: 0 else matchF.homeScore ?: 0
            val oppClubName = if (isUserHome) repository.getClubById(matchF.awayTeamId)?.nameAr ?: "الخصم" else repository.getClubById(matchF.homeTeamId)?.nameAr ?: "الخصم"

            val journalTitle = if (userScore > oppScore) "🟢 انتصار حاسم في الأسبوع ${career.week}" 
                              else if (userScore == oppScore) "🟡 تعادل تكتيكي في الأسبوع ${career.week}" 
                              else "🔴 تعثر وخسارة في الأسبوع ${career.week}"

            val journalContent = if (userScore > oppScore) {
                "أداء فني خارق وممتع! تفوقنا على $oppClubName بنجاح ساحق بنتيجة $userScore-$oppScore. احتفل المدير الفني والجمهور في المدرجات بالتكتيك المنظم!"
            } else if (userScore == oppScore) {
                "مباراة متوازنة وتكتيك صلب، انتهى لقائنا ضد $oppClubName بالتعادل العادل بنتيجة $userScore-$oppScore. سنعمل على زيادة الحسم الهجومي في التدوينات المقبلة."
            } else {
                "تعرضنا لكبوة مفاجئة وخسرنا اللقاء بنتيجة $userScore-$oppScore أمام $oppClubName. سنقوم بتصحيح الأخطاء الدفاعية ونعود لنغمة الانتصارات سريعاً."
            }

            val matchJournal = JournalEntity(
                season = career.season,
                week = career.week,
                title = "Match recap",
                titleAr = journalTitle,
                content = "Match concluded with score $userScore-$oppScore",
                contentAr = journalContent,
                isAuto = true
            )
            repository.insertJournal(matchJournal)

            // 3e. Apply weekly Jersey Sponsor Payment if any
            var sponsorIncome = 0L
            val sponsorName = career.selectedSponsor
            if (sponsorName != "None") {
                val basePay = when (sponsorName) {
                    "Aramco" -> 350_000L
                    "Emirates" -> 250_000L
                    "Spotify" -> 180_000L
                    "Nike" -> 120_000L
                    else -> 0L
                }
                var bonusPay = 0L
                if (sponsorName == "Spotify" && userScore > oppScore) {
                    bonusPay = 100_000L // Win Bonus
                }
                sponsorIncome = basePay + bonusPay

                // Deposit news alert in inbox
                val transSponsor = when (sponsorName) {
                    "Aramco" -> "أرامكو السعودية 🇸🇦"
                    "Emirates" -> "طيران الإمارات ✈️"
                    "Spotify" -> "سبوتيفاي 🎵"
                    else -> "نايكي ✔️"
                }
                val sponsorAlert = NewsEntity(
                    title = "Sponsor Payout",
                    titleAr = "حوالة ومستحقات رعاية [$transSponsor]! 👕💰",
                    content = "Weekly payout deposited.",
                    contentAr = "أودعت الشركة الراعية [ $transSponsor ] الدفعة الأسبوعية لعقد رعاية قميص النادي بقيمة $${sponsorIncome / 1_000f}k في رصيد النادي لمواصلة التدعيم وتحسين الأكاديمية والمقرات!",
                    type = "Board"
                )
                repository.insertNews(sponsorAlert)
            }

            // 4. Increment week index or end season
            if (career.week < 10) {
                val updatedCareer = career.copy(
                    week = career.week + 1,
                    budget = career.budget + sponsorIncome
                )
                repository.updateCareer(updatedCareer)
            } else {
                // End of season! Reset week stats, increase season rating, pay prize money
                val uClub = userClub.value ?: return@launch
                val rank = getClubsRank(uClub.id)
                val userPrize = (15_000_000L - (rank - 1) * 1_500_000L).coerceAtLeast(3_000_000L)
                
                // Reset clubs standings
                val allClubs = repository.clubsFlow.first()
                for (c in allClubs) {
                    val updatedC = c.copy(
                        played = 0, wins = 0, draws = 0, losses = 0,
                        goalsFor = 0, goalsAgainst = 0, points = 0
                    )
                    repository.updateClub(updatedC)
                }

                val updatedCareer = career.copy(
                    week = 1,
                    season = career.season + 1,
                    budget = career.budget + userPrize + sponsorIncome
                )
                repository.updateCareer(updatedCareer)

                // Add championship news
                val championshipNews = NewsEntity(
                    title = "Season ${career.season} Concluded!",
                    titleAr = "انتهى الموسم رقم ${career.season}!",
                    content = "The season has officially finished. Your club finished in Rank #$rank! You have received a massive cash payout of $${userPrize / 1_000_000}M for recruitment.",
                    contentAr = "انتهى الموسم الكروي رسمياً. حقق فريقك المركز #$rank في الترتيب العام! حصل النادي على تمويل إضافي بقيمة $${userPrize / 1_000_000} مليون دولار للتطوير.",
                    type = "Board"
                )
                repository.insertNews(championshipNews)
            }

            // Sync players
            _userSquad.value = repository.getPlayersByClub(career.clubId)
        }
    }

    fun skipEntireSeason() {
        val uClub = userClub.value ?: return

        viewModelScope.launch {
            _isSkippingSeason.value = true
            try {
                // Loop to simulate all remaining weeks of the current season
                while (true) {
                    val career = repository.careerFlow.first() ?: break
                    val currentWeek = career.week

                    // Fetch the fixture for the current week
                    val fixtures = repository.getFixturesByWeek(currentWeek)
                    val userFixture = fixtures.firstOrNull {
                        it.homeTeamId == career.clubId || it.awayTeamId == career.clubId
                    } ?: break

                    // 1. Simulate the user's match if not played
                    if (!userFixture.isPlayed) {
                        val oppId = if (userFixture.homeTeamId == career.clubId) userFixture.awayTeamId else userFixture.homeTeamId
                        val oppClub = repository.getClubById(oppId) ?: break
                        val uSquad = repository.getPlayersByClub(career.clubId)
                        val oppSquad = repository.getPlayersByClub(oppId)

                        val uRating = uSquad.map { it.rating }.average().toInt()
                        val oppRating = oppSquad.map { it.rating }.average().toInt()

                        val uDefending = uSquad.filter { it.position == "DEF" }.map { it.defending }.average().takeIf { !it.isNaN() }?.toInt() ?: 70
                        val uEnergyAvg = uSquad.map { it.energy }.average().takeIf { !it.isNaN() }?.toInt() ?: 100

                        val diff = uRating - oppRating

                        var uGoalsBonus = 0
                        var oppGoalsBonus = 0

                        if (uDefending < 75) {
                            oppGoalsBonus += 1
                        }
                        if (uEnergyAvg < 65) {
                            oppGoalsBonus += 1
                        }

                        val uGoalsChance = (Random.nextInt(0, 4) + (diff / 6)).coerceIn(0, 5) + uGoalsBonus
                        val oppGoalsChance = (Random.nextInt(0, 4) - (diff / 6)).coerceIn(0, 5) + oppGoalsBonus

                        val updatedFixture = userFixture.copy(
                            homeScore = if (userFixture.homeTeamId == uClub.id) uGoalsChance else oppGoalsChance,
                            awayScore = if (userFixture.awayTeamId == uClub.id) uGoalsChance else oppGoalsChance,
                            isPlayed = true
                        )
                        repository.updateFixture(updatedFixture)

                        // Update tables
                        repository.updateClub(
                            updateClubStatsLocal(uClub, updatedFixture.homeScore!!, updatedFixture.awayScore!!)
                        )
                        repository.updateClub(
                            updateClubStatsLocal(oppClub, updatedFixture.awayScore!!, updatedFixture.homeScore!!)
                        )
                    }

                    // 2. Simulate other matches
                    repository.simulateRestOfMatches(currentWeek, career.clubId)

                    // 3. Squad fatigue
                    val squad = repository.getPlayersByClub(career.clubId)
                    for (p in squad) {
                        val matchFatigue = if (p.position == "GK") 5 else Random.nextInt(10, 18)
                        val newEnergy = (p.energy - matchFatigue).coerceAtLeast(15)
                        repository.updatePlayer(p.copy(energy = newEnergy))
                    }

                    // 4. Apply scheduled training
                    applyWeeklyTrainingSchedule(squad, career)

                    // 5. Trigger random sell/loan list countdown
                    triggerRandomSellOffers(career, squad)

                    val allPlayers = dao.getPlayersFlow().first()
                    for (p in allPlayers) {
                        if (p.isOnLoan) {
                            val weeksLeft = p.loanWeeksLeft - 1
                            if (weeksLeft <= 0) {
                                val returned = p.copy(
                                    isOnLoan = false,
                                    loanWeeksLeft = 0,
                                    clubId = p.originalClubId,
                                    onLoanList = false
                                )
                                repository.updatePlayer(returned)
                            } else {
                                val updated = p.copy(loanWeeksLeft = weeksLeft)
                                repository.updatePlayer(updated)
                            }
                        }
                    }

                    // 6. Advance week or end season
                    if (currentWeek < 10) {
                        val updatedCareer = career.copy(week = currentWeek + 1)
                        repository.updateCareer(updatedCareer)
                    } else {
                        // End of season! Reset standings, increase budget
                        val rank = getClubsRank(uClub.id)
                        val userPrize = (15_000_000L - (rank - 1) * 1_500_000L).coerceAtLeast(3_000_000L)

                        val allClubs = repository.clubsFlow.first()
                        for (c in allClubs) {
                            val updatedC = c.copy(
                                played = 0, wins = 0, draws = 0, losses = 0,
                                goalsFor = 0, goalsAgainst = 0, points = 0
                            )
                            repository.updateClub(updatedC)
                        }

                        val updatedCareer = career.copy(
                            week = 1,
                            season = career.season + 1,
                            budget = career.budget + userPrize
                        )
                        repository.updateCareer(updatedCareer)

                        // Add championship news
                        val championshipNews = NewsEntity(
                            title = "Season ${career.season} Concluded (Simulated)!",
                            titleAr = "انتهى الموسم رقم ${career.season} (تخطي الموسم)! 🏁",
                            content = "The season has officially finished. Your club finished in Rank #$rank! You have received a payout of $${userPrize / 1_000_000}M.",
                            contentAr = "انتهى الموسم الكروي بالكامل عبر المحاكاة السريعة. حقق فريقك المركز #$rank في الترتيب العام للدوري الممتاز! حصل النادي على ميزانية إضافية قيمتها $${userPrize / 1_000_000} مليون دولار للتطوير والاستعداد للموسم الجديد.",
                            type = "Board"
                        )
                        repository.insertNews(championshipNews)

                        // Add journal entry
                        val seasonJournal = JournalEntity(
                            season = career.season,
                            week = 10,
                            title = "Season Concluded via Quick Simulation",
                            titleAr = "🏁 انتهاء الموسم رقم ${career.season} بالمحاكاة الكاملة",
                            content = "Completed season skip.",
                            contentAr = "تم تخطي وتسمية الموسم الكروي رقم ${career.season} بمحاكاة كاملة وتفصيلية، وحلّ الفريق في المركز #$rank بجدول الدوري التنافسي.",
                            isAuto = true
                        )
                        repository.insertJournal(seasonJournal)

                        break
                    }
                }

                // Sync user squad
                _userSquad.value = repository.getPlayersByClub(uClub.id)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSkippingSeason.value = false
            }
        }
    }

    private suspend fun getClubsRank(clubId: Int): Int {
        val clubs = dao.getClubs()
        val index = clubs.indexOfFirst { it.id == clubId }
        return if (index != -1) index + 1 else 5
    }

    // Sell player selected
    fun sellPlayer(player: PlayerEntity) {
        val career = careerState.value ?: return
        val uClub = userClub.value ?: return
        if (_userSquad.value.size <= 5) return // Must keep at least 5 players

        viewModelScope.launch {
            repository.sellPlayerToAI(player, uClub, career)
            _userSquad.value = repository.getPlayersByClub(career.clubId)
        }
    }

    // Trigger AI offers to purchase user's stars
    private suspend fun triggerRandomSellOffers(career: CareerEntity, squad: List<PlayerEntity>) {
        if (squad.size > 5 && Random.nextDouble() < 0.4) {
            val targetedPlayer = squad.filter { it.rating > 70 }.randomOrNull() ?: return
            val biddingClubs = dao.getClubs().filter { !it.isUser }
            if (biddingClubs.isNotEmpty()) {
                val buyer = biddingClubs.random()
                val valueOfOffer = (targetedPlayer.value * Random.nextDouble(0.9, 1.25)).toLong()
                
                val transferMail = NewsEntity(
                    title = "Transfer Offer: ${targetedPlayer.name}",
                    titleAr = "عرض انتقال للاعب: ${targetedPlayer.name}",
                    content = "${buyer.name} is offering official bid of $${valueOfOffer / 1_000_000}M to purchase ${targetedPlayer.name}. Make your decision in the main roster dashboard.",
                    contentAr = "أرسل نادي ${buyer.nameAr} عرضاً رسمياً بقيمة $${valueOfOffer / 1_000_000} مليون دولار لضم لاعبك ${targetedPlayer.name}. يمكنك بيعه وقبول العرض فوراً من صندوق الوارد وصيانة الميزانية.",
                    type = "TransferOffer"
                )
                repository.insertNews(transferMail)
            }
        }
    }

    // Trigger AI loan offer for a player on user's loan list
    private suspend fun triggerRandomLoanOffers(career: CareerEntity, squad: List<PlayerEntity>) {
        val loanCandidates = squad.filter { it.onLoanList && !it.isOnLoan }
        if (loanCandidates.isNotEmpty() && Random.nextDouble() < 0.35) {
            val targetedPlayer = loanCandidates.random()
            val biddingClubs = dao.getClubs().filter { !it.isUser }
            if (biddingClubs.isNotEmpty()) {
                val borrower = biddingClubs.random()
                val loanFee = (targetedPlayer.value * 0.10).toLong() // AI will compensate 10% premium upfront fee

                val loanOfferMail = NewsEntity(
                    title = "Loan Offer: ${targetedPlayer.name}",
                    titleAr = "طلب استعارة للاعب: ${targetedPlayer.name} 📋",
                    content = "${borrower.name} requests to loan ${targetedPlayer.name} for 6 weeks, paying upfront fee of $${loanFee / 1_000_000}M.",
                    contentAr = "يرغب نادي ${borrower.nameAr} في تقديم عرض فوري لاستعارة مدافعنا/مهاجمنا المميز ${targetedPlayer.name} لمدة 6 أسابيع مجدولة مع التكفل بـ 100% من أجوره الأسبوعية ودفع بدل إعارة إضافي قدره $${loanFee / 1_000_000} مليون دولار للتنشيط.",
                    type = "TransferOffer" // Reuses the binary Accept/Reject flow flawlessly
                )
                repository.insertNews(loanOfferMail)
            }
        }
    }

    // Match Highlight Active State variables
    private var matchTime = 0
    private var logs = mutableListOf<String>()
    private var homeGoals = 0
    private var awayGoals = 0

    // Quick instant simulation with Smart Tactical AI and squad metrics
    fun quickSimulateMatch() {
        val fixture = matchFixture.value ?: return
        val career = careerState.value ?: return
        val uClub = userClub.value ?: return
        val oppClub = opponentClub.value ?: return

        viewModelScope.launch {
            val uSquad = _userSquad.value
            val oppSquad = repository.getPlayersByClub(oppClub.id)

            val uRating = uSquad.map { it.rating }.average().toInt()
            val oppRating = oppSquad.map { it.rating }.average().toInt()

            val uDefending = uSquad.filter { it.position == "DEF" }.map { it.defending }.average().takeIf { !it.isNaN() }?.toInt() ?: 70
            val uEnergyAvg = uSquad.map { it.energy }.average().takeIf { !it.isNaN() }?.toInt() ?: 100

            var diff = uRating - oppRating

            var uGoalsBonus = 0
            var oppGoalsBonus = 0

            // Opponent AI analyzes user weaknesses
            if (uDefending < 75) {
                oppGoalsBonus += 1 // Exploit low defense average
            }
            if (uEnergyAvg < 65) {
                oppGoalsBonus += 1 // Exploit high exhaustion
            }

            val uGoalsChance = (Random.nextInt(0, 4) + (diff / 6)).coerceIn(0, 5) + uGoalsBonus
            val oppGoalsChance = (Random.nextInt(0, 4) - (diff / 6)).coerceIn(0, 5) + oppGoalsBonus

            val updatedFixture = fixture.copy(
                homeScore = if (fixture.homeTeamId == uClub.id) uGoalsChance else oppGoalsChance,
                awayScore = if (fixture.awayTeamId == uClub.id) uGoalsChance else oppGoalsChance,
                isPlayed = true
            )
            repository.updateFixture(updatedFixture)

            // Update tables stats
            repository.updateClub(
                updateClubStatsLocal(uClub, updatedFixture.homeScore!!, updatedFixture.awayScore!!)
            )
            repository.updateClub(
                updateClubStatsLocal(oppClub, updatedFixture.awayScore!!, updatedFixture.homeScore!!)
            )

            // Match is completed, update match state
            val scorersList = mutableListOf<String>()
            val uScorers = uSquad.filter { it.position == "ATT" || it.position == "MID" }
            val oppScorers = oppSquad.filter { it.position == "ATT" || it.position == "MID" }

            if (uDefending < 75) {
                scorersList.add("🧠 ذكاء تكتيكي: استغل الخصم تدني اللياقة الدفاعية للفريق في المراقبة والضغط.")
            }
            if (uEnergyAvg < 65) {
                scorersList.add("⚠️ تقرير فني: ظهر الإجهاد البدني على لاعبينا مما سمح للمنافس بفرض سيطرته.")
            }

            for (i in 0 until uGoalsChance) {
                val scorer = uScorers.randomOrNull()?.name ?: "Striker"
                scorersList.add("⚽ ${uClub.nameAr}: $scorer")
            }
            for (i in 0 until oppGoalsChance) {
                val scorer = oppScorers.randomOrNull()?.name ?: "Striker"
                scorersList.add("⚽ ${oppClub.nameAr}: $scorer")
            }

            _matchState.value = MatchPlayState.PostMatch(
                homeScore = updatedFixture.homeScore!!,
                awayScore = updatedFixture.awayScore!!,
                scorers = scorersList
            )
            _matchFixture.value = updatedFixture
        }
    }

    private fun updateClubStatsLocal(club: ClubEntity, myGoals: Int, oppGoals: Int): ClubEntity {
        val played = club.played + 1
        val isWin = myGoals > oppGoals
        val isDraw = myGoals == oppGoals
        val isLoss = myGoals < oppGoals

        return club.copy(
            played = played,
            wins = club.wins + (if (isWin) 1 else 0),
            draws = club.draws + (if (isDraw) 1 else 0),
            losses = club.losses + (if (isLoss) 1 else 0),
            goalsFor = club.goalsFor + myGoals,
            goalsAgainst = club.goalsAgainst + oppGoals,
            points = club.points + (if (isWin) 3 else if (isDraw) 1 else 0)
        )
    }

    // Start Interactive Highlights Game Match!
    fun startInteractiveMatch() {
        matchTime = 0
        logs = mutableListOf()
        homeGoals = 0
        awayGoals = 0
        _opponentCurrentTactic.value = "Balanced"

        val uClub = userClub.value ?: return
        val oppClub = opponentClub.value ?: return

        logs.add("🏁 انطلاق صافرة بداية المباراة بين ${uClub.nameAr} و ${oppClub.nameAr}!")
        
        _matchState.value = MatchPlayState.Simulating(
            minute = matchTime,
            homeScore = homeGoals,
            awayScore = awayGoals,
            logs = logs.toList(),
            currentEvent = null
        )

        // Kick off loop ticks
        tickMatchTime()
    }

    private fun tickMatchTime() {
        viewModelScope.launch {
            while (matchTime < 90) {
                val speedDelay = (1000 / _matchSpeed.value).toLong()
                kotlinx.coroutines.delay(speedDelay) // Ticking minutes quickly
                matchTime += Random.nextInt(8, 15)
                if (matchTime >= 90) matchTime = 90

                val uClub = userClub.value ?: break
                val oppClub = opponentClub.value ?: break
                val squad = _userSquad.value
                val oppSquad = repository.getPlayersByClub(oppClub.id)

                // 🧠 Evaluative Strategy Shifts according to tactical score status & exhaustion
                val uEnergyAvg = if (squad.isNotEmpty()) squad.map { it.energy }.average().toInt() else 100
                val prevTactic = _opponentCurrentTactic.value
                val newTactic = when {
                    awayGoals < homeGoals -> "Ultra-Offensive" // Losing back
                    awayGoals > homeGoals && matchTime >= 65 -> "Defensive" // Leading late
                    uEnergyAvg < 65 -> "High-Press" // Exploit user physical drops
                    else -> "Balanced"
                }

                if (newTactic != prevTactic) {
                    _opponentCurrentTactic.value = newTactic
                    val tacticAr = when (newTactic) {
                        "Ultra-Offensive" -> "هجوم كاسح (Ultra-Offensive) لتعويض النتيجة وسد الفارق"
                        "Defensive" -> "دفاع المنطقة المحكم (Park the Bus) لتأمين الفوز والمحافظة على النتيجة"
                        "High-Press" -> "الضغط العالي والتاكلينج الشرس (High Press) مستغلاً إرهاق فريقنا"
                        else -> "الأسلوب المتوازن والانتشار الجيد في الملعب"
                    }
                    logs.add("🧠 ذكاء المنافس: غيّر نادِ ${oppClub.nameAr} تكتيكه الرياضي إلى [$tacticAr].")
                }

                // Chance to trigger highlight events
                val rand = Random.nextDouble()
                if (rand < 0.45 && matchTime < 90) {
                    // Ultra-offensive increases opp attack frequency slightly
                    val oppAttackBias = if (newTactic == "Ultra-Offensive") 0.60 else 0.45
                    val isUserAttack = Random.nextDouble() > oppAttackBias

                    val event = if (isUserAttack) {
                        generateUserAttackEvent(squad, oppSquad)
                    } else {
                        generateOpponentAttackEvent(squad, oppSquad)
                    }

                    _matchState.value = MatchPlayState.Simulating(
                        minute = matchTime,
                        homeScore = homeGoals,
                        awayScore = awayGoals,
                        logs = logs.toList(),
                        currentEvent = event
                    )
                    // Halted waiting for user input choice!
                    return@launch
                } else {
                    val phrase = listOf(
                        "صراع شرس ومراوغات تكتيكية متبادلة في دائرة المنتصف.",
                        "الفريقان يتبادلان الكرات العرضية في محاولة لتطبيق تعليمات المدربين.",
                        "المدافعون مستيقظون لقطع التمريرات الطويلة وإحباط الهجمات العشوائية."
                    ).random()
                    logs.add("${matchTime}' دقيقة - $phrase")
                    _matchState.value = MatchPlayState.Simulating(
                        minute = matchTime,
                        homeScore = homeGoals,
                        awayScore = awayGoals,
                        logs = logs.toList(),
                        currentEvent = null
                    )
                }
            }

            // Finish match
            finishInteractiveMatch()
        }
    }

    // Generate offensive event for user squad with dynamic tactical counter-impacts
    private fun generateUserAttackEvent(userSquad: List<PlayerEntity>, oppSquad: List<PlayerEntity>): MatchEvent {
        val striker = userSquad.filter { it.position == "ATT" }.randomOrNull() ?: userSquad.random()
        val defender = oppSquad.filter { it.position == "DEF" }.randomOrNull() ?: oppSquad.random()

        var baseShoot = Math.min(65, striker.shooting - defender.defending + 40)
        var basePass = Math.min(80, striker.passing - defender.defending + 50)
        var baseDribble = Math.min(60, striker.speed - defender.speed + 35)

        // Dynamic tactic changes modifier
        val currentOppTactic = _opponentCurrentTactic.value
        val detailsAr: String
        if (currentOppTactic == "Defensive") {
            baseShoot -= 15
            basePass -= 10
            baseDribble -= 15
            detailsAr = " [الخصم متكتل دفاعياً -15% نجاح]"
        } else if (currentOppTactic == "Ultra-Offensive") {
            baseShoot += 15
            basePass += 15
            baseDribble += 12
            detailsAr = " [اندفاع الخصم يترك مساحات مرتدة +15% نجاح]"
        } else if (currentOppTactic == "High-Press") {
            basePass -= 12
            baseDribble -= 12
            detailsAr = " [تحت ضغط الخصم العكسي المربك -12% نجاح]"
        } else {
            detailsAr = ""
        }

        val options = listOf(
            MatchOption("shoot", "تسديدة مباشرة على المرمى$detailsAr", "Direct Shoot", baseShoot.coerceIn(10, 95), 10),
            MatchOption("pass", "تمريرة حاسمة لصانع اللعب$detailsAr", "Direct Assist Pass", basePass.coerceIn(10, 95), 5),
            MatchOption("dribble", "مراوغة الدفاع لفتح زاوية$detailsAr", "Skill Dribble", baseDribble.coerceIn(10, 95), 15)
        )

        return MatchEvent(
            minute = matchTime,
            type = EventType.USER_ATTACK,
            description = "We got an Attack! ${striker.name} has breached the final line defenses, but Defender ${defender.name} is closing down!",
            descriptionAr = "فرصة هجومية في الثلث الأخير! اخترق ${striker.name} الحطوط الشرسة، المدافع ${defender.name} يقابله مباشرة! اختر الاستجابة التكتيكية المناسبة:",
            activePlayer = striker,
            opponentPlayer = defender,
            options = options,
            pitchBallX = Random.nextFloat() * 0.3f + 0.6f, // attack zone (right side)
            pitchBallY = Random.nextFloat() * 0.6f + 0.2f
        )
    }

    // Generate defensive highlight event for user squad with dynamic weakness exploitation
    private fun generateOpponentAttackEvent(userSquad: List<PlayerEntity>, oppSquad: List<PlayerEntity>): MatchEvent {
        val oppStriker = oppSquad.filter { it.position == "ATT" }.randomOrNull() ?: oppSquad.random()
        val userDefender = userSquad.filter { it.position == "DEF" }.randomOrNull() ?: userSquad.random()

        var baseBlock = Math.min(75, userDefender.defending - oppStriker.speed + 45)
        var basePress = Math.min(65, userDefender.defending - oppStriker.shooting + 35)
        var baseSlide = Math.min(50, userDefender.defending + 15)

        // Evaluate user's tactical defensive weakness & exhaustion
        val uDefendingAvg = userSquad.filter { it.position == "DEF" }.map { it.defending }.average().takeIf { !it.isNaN() }?.toInt() ?: 70
        val uEnergyAvg = userSquad.map { it.energy }.average().takeIf { !it.isNaN() }?.toInt() ?: 100

        val weaknessAr: String
        if (uDefendingAvg < 75) {
            baseBlock -= 12
            basePress -= 10
            baseSlide -= 10
            weaknessAr = " [ثغرة دفاعنا الضعيف تساند الخصم]"
        } else if (uEnergyAvg < 65) {
            baseBlock -= 15
            basePress -= 15
            baseSlide -= 12
            weaknessAr = " [إجهاد المدافع البدني يقلل فاعليته]"
        } else {
            weaknessAr = ""
        }

        val options = listOf(
            MatchOption("block", "تغطية دفاعية جيدة لسد زاوية التسديد $weaknessAr", "Defensive Block", baseBlock.coerceIn(10, 95), 5),
            MatchOption("press", "ضغط بدني مباشر لافتكاك الكرة $weaknessAr", "Aggressive Press", basePress.coerceIn(10, 95), 12),
            MatchOption("slide", "زحلقة تكتيكية لعرقلة الانفراد $weaknessAr", "Tactical Slide Tackle", baseSlide.coerceIn(10, 95), 18)
        )

        return MatchEvent(
            minute = matchTime,
            type = EventType.OPPONENT_ATTACK,
            description = "${oppStriker.name} has intercepted ball and charging on! ${userDefender.name} is backtracking to defend.",
            descriptionAr = "هجوم مرتد معاكس مباغت! انفلت صانع ألعاب الهجوم ${oppStriker.name} باتجاه منطقة جزائنا، المدافع ${userDefender.name} هو حصنك الأخير! احسم طريقة الاعتراض المتين:",
            activePlayer = oppStriker,
            opponentPlayer = userDefender,
            options = options,
            pitchBallX = Random.nextFloat() * 0.3f + 0.1f, // defensive zone (left side)
            pitchBallY = Random.nextFloat() * 0.6f + 0.2f
        )
    }

    // Resolve user's decision choice
    fun resolveEventChoice(option: MatchOption) {
        val state = _matchState.value
        if (state !is MatchPlayState.Simulating || state.currentEvent == null) return

        val event = state.currentEvent
        val rnd = Random.nextInt(100)
        // Chemistry impact on option success: high chemistry grants up to +6%, poor synergy penalizes up to -10%
        val chemValue = teamChemistry.value
        val chemBonus = ((chemValue - 70) / 5).coerceIn(-10, 6)
        val success = rnd < (option.successChance + chemBonus)
        val isUserAttack = event.type == EventType.USER_ATTACK

        val details: String
        val detailsAr: String

        if (isUserAttack) {
            if (success) {
                homeGoals++
                details = "GOAL! Absolute beauty! Choice [${option.labelAr}] was clean, and ${event.activePlayer?.name} scored!"
                detailsAr = "جوووووول! خيار ذكي للغاية لمداهمة المدافعين! نجح تكتيك [${option.label}] وسدد ${event.activePlayer?.name} الكرة لتسكن يسار حارس المرمى بنجاح!"
                logs.add("⚽ ${matchTime}' دقيقة - هددددف! مهاجم ناديك يسجل بدقة عالية.")
            } else {
                details = "Attack failed. Goal keeper caught it cleanly."
                detailsAr = "ضاعت الفرصة! تمكن جدار الدفاع البشري للخصم من التصدي للكرة بنجاح وقطعها بالرغم من خيار المحاولة الرياضية [${option.label}]."
                logs.add("🧤 ${matchTime}' دقيقة - الخصم يستخلص الكرة ويبدد خطر هجمتنا.")
            }
        } else {
            // Opponent attacking
            if (success) {
                // Defender wins the duel, opponent fails
                details = "Brilliant tackle! Defender ${event.opponentPlayer?.name} intercepted successfully."
                detailsAr = "عمل أسطوري مذهل! نجح صخرة الدفاع ${event.opponentPlayer?.name} في استعادة الاستحواذ بنجاح عن طريق قرار [${option.label}]."
                logs.add("🛡️ ${matchTime}' دقيقة - افتكاك صلد للكرة ينهي الخطر.")
            } else {
                awayGoals++
                details = "Goal Opponent. Defender was bypassed and target converted."
                detailsAr = "هدف في مرمانا للأسف! تراجع التوفيق في مناورة [${option.label}]، وتجاوزنا هداف الخصم ${event.activePlayer?.name} محرزاً هدفاً مباغتاً."
                logs.add("⚽ ${matchTime}' دقيقة - هدف في شباكنا للأسف الشديد بواسطة المهاجم المنافس.")
            }
        }

        // Add details to logs
        logs.add(detailsAr)

        _matchState.value = MatchPlayState.Simulating(
            minute = matchTime,
            homeScore = homeGoals,
            awayScore = awayGoals,
            logs = logs.toList(),
            currentEvent = null // Reset
        )

        // Continue clock ticking loop
        tickMatchTime()
    }

    private suspend fun finishInteractiveMatch() {
        val fixture = matchFixture.value ?: return
        val uClub = userClub.value ?: return
        val oppClub = opponentClub.value ?: return

        // Save scores depending on whether we were Home or Away
        val isUserHome = fixture.homeTeamId == uClub.id
        val finalHScore = if (isUserHome) homeGoals else awayGoals
        val finalAScore = if (isUserHome) awayGoals else homeGoals

        val updatedFixture = fixture.copy(
            homeScore = finalHScore,
            awayScore = finalAScore,
            isPlayed = true
        )
        repository.updateFixture(updatedFixture)

        // Update tables
        repository.updateClub(
            updateClubStatsLocal(uClub, if (isUserHome) finalHScore else finalAScore, if (isUserHome) finalAScore else finalHScore)
        )
        repository.updateClub(
            updateClubStatsLocal(oppClub, if (isUserHome) finalAScore else finalHScore, if (isUserHome) finalHScore else finalAScore)
        )

        _matchFixture.value = updatedFixture

        _matchState.value = MatchPlayState.PostMatch(
            homeScore = finalHScore,
            awayScore = finalAScore,
            scorers = listOf("🏁 انتهت المباراة الحماسية بنتيجة $finalHScore - $finalAScore")
        )
    }

    // Directly skip interactive simulation to final scores instantly
    fun skipRemainingMatch() {
        val state = _matchState.value
        if (state !is MatchPlayState.Simulating) return

        val uClub = userClub.value ?: return
        val oppClub = opponentClub.value ?: return
        val squad = _userSquad.value
        
        // Quick mathematical model of what would happen in remaining duration
        val remainingMinutes = 90 - matchTime
        if (remainingMinutes > 0) {
            val potentialAttacks = (remainingMinutes / 15).coerceAtLeast(1)
            for (i in 0 until potentialAttacks) {
                // Bias according to rating difference
                val diffFactor = (squad.map { it.rating }.average() - 75f) / 10f
                if (Random.nextFloat() < 0.22f + (diffFactor * 0.02f)) {
                    homeGoals++
                    logs.add("⚽ دقيقة ${90 - i * 3}' - هدف محقق لصالح أصحاب الأرض بعد كسر مصيدة التسلل!")
                }
                if (Random.nextFloat() < 0.20f - (diffFactor * 0.02f)) {
                    awayGoals++
                    logs.add("⚽ دقيقة ${90 - i * 3}' - هدف لصالح الضيوف من هجمة مرتدة ساحقة!")
                }
            }
        }
        
        matchTime = 90
        logs.add("⏱️ إنهاء فوري بالاتفاق مع طاقم الحكام تكتيكياً ومحاكاة الدقائق المتبقية.")

        _matchState.value = MatchPlayState.Simulating(
            minute = 90,
            homeScore = homeGoals,
            awayScore = awayGoals,
            logs = logs.toList(),
            currentEvent = null
        )

        viewModelScope.launch {
            finishInteractiveMatch()
        }
    }

    // Pick dynamic sponsor of choice (Aramco, Emirates, Spotify, Nike) and claim weekly payouts
    fun chooseSponsor(sponsorName: String) {
        val career = careerState.value ?: return
        viewModelScope.launch {
            val updatedCareer = career.copy(selectedSponsor = sponsorName)
            repository.updateCareer(updatedCareer)

            // Newsletter of signed contracts
            val translation = when (sponsorName) {
                "Aramco" -> "أرامكو السعودية 🇸🇦"
                "Emirates" -> "طيران الإمارات ✈️"
                "Spotify" -> "سبوتيفاي الموسيقية 🎵"
                "Nike" -> "نايكي للمستلزمات الرياضية ✔️"
                else -> "None"
            }
            if (sponsorName != "None") {
                val valStr = when (sponsorName) {
                    "Aramco" -> "$3.5M لكل موسم تكتيكي"
                    "Emirates" -> "$2.5M لكل موسم بدون شروط"
                    "Spotify" -> "$1.8M لكل موسم + مكافأة انتصار"
                    else -> "$1.2M لكل موسم + تمويل براعم"
                }
                val joinNews = NewsEntity(
                    title = "Sponsor Signed",
                    titleAr = "إعلان رسمي: رعاية جديدة للفريق مع [$translation]! 👕",
                    content = "Club signed a new Jersey Shirt Sponsor contract.",
                    contentAr = "أعلن مجلس إدارة النادي رسمياً عن توقيع عقد شراكة استراتيجية لرعاية قميص الفريق الأول مع [$translation] بعائد مالي قيمته [$valStr]. ستدعم هذه الأموال مباشرة جهود المدرب في تطوير الأكاديمية وجلب النجوم!",
                    type = "Board"
                )
                repository.insertNews(joinNews)
            }
        }
    }

    // Quit Match screen to return to Lobby
    fun finishMatchLobby() {
        _matchState.value = MatchPlayState.PreMatch
    }

    // --- ACADEMY & SCOUTING SYSTEM METHODS (World Soccer Champs style) ---

    // Upgrade Youth Academy
    fun upgradeAcademy() {
        val career = careerState.value ?: return
        if (career.academyLevel >= 5) return
        val upgradeCost = when (career.academyLevel) {
            1 -> 1_500_000L
            2 -> 3_000_000L
            3 -> 6_000_000L
            4 -> 12_000_000L
            else -> 0L
        }
        if (career.budget >= upgradeCost) {
            viewModelScope.launch {
                val updatedCareer = career.copy(
                    budget = career.budget - upgradeCost,
                    academyLevel = career.academyLevel + 1
                )
                repository.updateCareer(updatedCareer)

                // Notify in inbox
                val academyNews = NewsEntity(
                    title = "Academy Upgraded",
                    titleAr = "تمت ترقية أكاديمية الشباب لمستواك الجديد! 🎓",
                    content = "Academy Level is now Lvl ${updatedCareer.academyLevel}.",
                    contentAr = "تهانينا للمدير الفني! استثمر النادي مبلغ $${upgradeCost / 1_000_000}M لرفع كفاءة ومرافق البنية التحتية لأكاديمية الشباب لتصبح بالمستوى الكاسح (${updatedCareer.academyLevel}/5). يضمن ذلك إنجاب نجوم ذات OVR وجاهزية فنية أفضل بكثير للتصعيد الفوري للفريق الكروي الأول!",
                    type = "Board"
                )
                repository.insertNews(academyNews)
            }
        }
    }

    // Promote Academy Prodigy
    fun promoteAcademyProdigy() {
        val career = careerState.value ?: return
        val uClub = userClub.value ?: return
        val signFee = 250_000L
        if (career.budget < signFee) return

        viewModelScope.launch {
            // Generate customized star
            val age = Random.nextInt(16, 18)
            val pos = listOf("GK", "DEF", "MID", "ATT").shuffled().first()
            val baseRating = (career.academyLevel * 5 + 50 + Random.nextInt(0, 6)).coerceIn(52, 85)
            
            val arabFirstNames = listOf("فهد", "يوسف", "تركي", "نواف", "فيصل", "سلطان", "مهند", "بندر", "سعود", "ريان")
            val arabLastNames = listOf("الدوسري", "العازمي", "الجهني", "الشمري", "القحطاني", "الحربي", "العتيبي", "سالم", "جمال", "خالد")
            val engFirstNames = listOf("Jack", "Harry", "Thomas", "George", "Oliver", "William", "James", "Mason", "Jude")
            val engLastNames = listOf("Henderson", "Champs", "Walker", "Alexander", "Green", "White", "Cole", "Bellingham")
            
            val isArab = career.selectedLeagueCode == "SPL"
            val randomName = if (isArab) {
                "${arabFirstNames.shuffled().first()} ${arabLastNames.shuffled().first()}"
            } else {
                "${engFirstNames.shuffled().first()} ${engLastNames.shuffled().first()}"
            } + " (براعمالأكاديمية)"

            val freshPlayer = PlayerEntity(
                name = randomName,
                position = pos,
                rating = baseRating,
                shooting = if (pos == "ATT") baseRating + Random.nextInt(2, 6) else if (pos == "GK") 10 else baseRating - 10,
                passing = if (pos == "MID") baseRating + Random.nextInt(2, 6) else baseRating - 10,
                speed = baseRating + Random.nextInt(-4, 9),
                defending = if (pos == "DEF") baseRating + Random.nextInt(2, 6) else if (pos == "GK") 8 else 30,
                goalkeeper = if (pos == "GK") baseRating + Random.nextInt(2, 6) else 8,
                age = age,
                clubId = uClub.id,
                value = repository.getPlayerValue(baseRating, age, pos),
                wage = 5_000L,
                form = 8,
                energy = 100
            )

            dao.insertPlayers(listOf(freshPlayer))
            
            val updatedCareer = career.copy(budget = career.budget - signFee)
            repository.updateCareer(updatedCareer)

            // Refresh user squad
            _userSquad.value = repository.getPlayersByClub(uClub.id)

            // Notify in inbox
            val news = NewsEntity(
                title = "Academy Star Promoted",
                titleAr = "توقيع موهوب صاعد من أكاديمية الشباب! 🎓",
                content = "Graduated talent signed successfully.",
                contentAr = "زف الكشافة الخبر السعيد! تم التوقيع رسمياً وتصعيد الموهبة الصاعدة [${randomName}] بمركز [${pos}] وبتقييم أساسي واعد [${baseRating} OVR] بسن الـ ${age} عاماً فقط لشعار الفريق مقابل رسوم مالية وتأمين $${signFee / 1_000}K.",
                type = "Board"
            )
            repository.insertNews(news)
        }
    }

    // Upgrade Hired Scout
    fun upgradeScout() {
        val career = careerState.value ?: return
        if (career.scoutLevel >= 3) return
        val cost = when (career.scoutLevel) {
            1 -> 2_500_000L
            2 -> 5_000_000L
            else -> 0L
        }
        if (career.budget >= cost) {
            viewModelScope.launch {
                val updatedCareer = career.copy(
                    budget = career.budget - cost,
                    scoutLevel = career.scoutLevel + 1
                )
                repository.updateCareer(updatedCareer)

                val news = NewsEntity(
                    title = "Scout Upgraded",
                    titleAr = "التعاقد مع كشاف كروي عالمي المستوى! 🔍",
                    content = "Scout level increased.",
                    contentAr = "رسمياً، وافقت إدارة النادي على رفع سقف التوظيف للتعاقد مع خبير استكشاف كروي استراتيجي بالمستوى الجديد (${updatedCareer.scoutLevel}/3). هذا الخبير متمرس في الأسواق الأوروبية وسيضمن لك العثور على صيد ثمين من المواهب بمجرد تحديد المواصفات المطلوبة!",
                    type = "Board"
                )
                repository.insertNews(news)
            }
        }
    }

    // Launch Scouting Mission
    fun launchScoutMission(position: String) {
        val career = careerState.value ?: return
        if (career.scoutingMissionActive) return
        val missionFee = 200_000L
        if (career.budget < missionFee) return

        viewModelScope.launch {
            val updatedCareer = career.copy(
                budget = career.budget - missionFee,
                scoutingMissionActive = true,
                scoutingMissionPosition = position,
                scoutingWeeksPassed = 0
            )
            repository.updateCareer(updatedCareer)

            // News post
            val joinNews = NewsEntity(
                title = "Scout Dispatched",
                titleAr = "انطلاق بعثة الكشافة للتنقيب الميداني ✈️",
                content = "Scout sent out inside global leagues.",
                contentAr = "تم تفويض طاقم الاستكشاف بقيادة الكشاف برتبة ليفل (${career.scoutLevel}) بمهمة طيران رسمية للتفتيش عن صفقات Wonderkids بمركز [${position}] وتوفير قائمة بـ 3 ترشيحات جاهزة بحلول الأسبوع القادم في الدوري.",
                type = "Board"
            )
            repository.insertNews(joinNews)
        }
    }

    // Sign Discovered Scout Candidate
    fun signScoutProdigy(prodigy: PlayerEntity) {
        val career = careerState.value ?: return
        val uClub = userClub.value ?: return
        val price = prodigy.value
        if (career.budget < price) return

        viewModelScope.launch {
            // Update candidate to be on user's team
            val signed = prodigy.copy(
                clubId = uClub.id,
                onTransferList = false
            )
            repository.updatePlayer(signed)

            // Deduct money from budget
            val updatedCareer = career.copy(budget = career.budget - price)
            repository.updateCareer(updatedCareer)

            // Refresh user squad
            _userSquad.value = repository.getPlayersByClub(uClub.id)

            // News post
            val joinNews = NewsEntity(
                title = "Scouted Star Signed",
                titleAr = "حسم صفقة الاستكشاف رسمياً! 💎",
                content = "Wonderkid scouted and signed.",
                contentAr = "بناءً على مشورة الكشافين، وقع المدرب رسمياً مع الموهبة الصاعدة المكتشفة [${prodigy.name}] بمركز [${prodigy.position}] وتقييم [${prodigy.rating} OVR] بقيمة $${price / 1_000_000}M. ستقوم هذه الصفقة بإشعال الحماس التنافسي في الخطوط الخلفية والأمامية للفريق!",
                type = "Board"
            )
            repository.insertNews(joinNews)
        }
    }
}

// Custom UI Match classes
sealed class MatchPlayState {
    object PreMatch : MatchPlayState()
    data class Simulating(
        val minute: Int,
        val homeScore: Int,
        val awayScore: Int,
        val logs: List<String>,
        val currentEvent: MatchEvent? = null
    ) : MatchPlayState()
    data class PostMatch(
        val homeScore: Int,
        val awayScore: Int,
        val scorers: List<String>
    ) : MatchPlayState()
}

data class MatchEvent(
    val minute: Int,
    val type: EventType, // USER_ATTACK, OPPONENT_ATTACK
    val description: String,
    val descriptionAr: String,
    val activePlayer: PlayerEntity?,
    val opponentPlayer: PlayerEntity?,
    val options: List<MatchOption>,
    val pitchBallX: Float,
    val pitchBallY: Float
)

data class MatchOption(
    val id: String,
    val label: String,
    val labelAr: String,
    val successChance: Int,
    val costEnergy: Int
)

enum class EventType {
    USER_ATTACK,
    OPPONENT_ATTACK,
    NEUTRAL
}

