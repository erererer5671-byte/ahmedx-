package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.viewmodel.CareerViewModel
import com.example.viewmodel.MatchPlayState
import com.example.viewmodel.MatchEvent
import com.example.viewmodel.MatchOption
import com.example.viewmodel.EventType
import kotlin.random.Random

@Composable
fun SoccerAppMainView(viewModel: CareerViewModel) {
    val career by viewModel.careerState.collectAsStateWithLifecycle()
    val clubs by viewModel.clubsState.collectAsStateWithLifecycle()
    val news by viewModel.newsState.collectAsStateWithLifecycle()
    val fixtures by viewModel.fixturesState.collectAsStateWithLifecycle()
    val transferPlayers by viewModel.transferPlayersState.collectAsStateWithLifecycle()
    val userClub by viewModel.userClub.collectAsStateWithLifecycle()
    val opponentClub by viewModel.opponentClub.collectAsStateWithLifecycle()
    val matchState by viewModel.matchState.collectAsStateWithLifecycle()
    val activeTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val userSquad by viewModel.userSquad.collectAsStateWithLifecycle()
    val activeBidPlayer by viewModel.activeBidPlayer.collectAsStateWithLifecycle()
    val bidDialogState by viewModel.bidDialogState.collectAsStateWithLifecycle()
    val selectedTrainingSchedule by viewModel.selectedTrainingSchedule.collectAsStateWithLifecycle()
    val trainingFeedback by viewModel.trainingFeedback.collectAsStateWithLifecycle()
    val journals by viewModel.journalsState.collectAsStateWithLifecycle()
    val loanedOutPlayers by viewModel.loanedOutPlayers.collectAsStateWithLifecycle()
    val isSkippingSeason by viewModel.isSkippingSeason.collectAsStateWithLifecycle()

    if (isSkippingSeason) {
        Dialog(onDismissRequest = {}) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E252E)),
                border = BorderStroke(1.dp, Color(0xFF38495C)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFD1E4FF),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "جاري محاكاة وتخطي الموسم بالكامل... ⚽",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "يتم محاكاة بقية مباريات الدوري، تطبيق التدريبات، وتحديث وضعيات اللاعبين والإعارات للانتقال فوراً للموسم المُقبل.",
                        color = Color(0xFFC2C7CF),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF111318) // Eye-safe premium dark background
    ) {
        if (career == null) {
            // First run: Career Selection Setup
            CareerSetupScreen { name, clubId, mode, pos, league ->
                viewModel.createNewCareer(name, clubId, mode, pos, league)
            }
        } else {
            // Check if user is currently playing a match
            if (matchState != MatchPlayState.PreMatch) {
                MatchSimScreen(
                    viewModel = viewModel,
                    matchPlayState = matchState,
                    userClub = userClub,
                    opponentClub = opponentClub,
                    onOptionSelected = { viewModel.resolveEventChoice(it) },
                    onFinishMatch = { viewModel.finishMatchLobby() }
                )
            } else {
                // Main Coach Career dashboard
                Scaffold(
                    bottomBar = {
                        val sophAccent = Color(0xFFD1E4FF)
                        val sophGray = Color(0xFFC2C7CF)
                        val sophHeaderBg = Color(0xFF1A1C1E)
                        val sophPill = Color(0xFF38495C)
                        NavigationBar(
                            containerColor = sophHeaderBg,
                            tonalElevation = 8.dp,
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            val tabs = listOf(
                                Triple(0, "الرئيسية\nHome", Icons.Default.Home),
                                Triple(1, "التشكيلة\nSquad", Icons.Default.Person),
                                Triple(2, "الترتيب\nTable", Icons.Default.Star),
                                Triple(3, "الانتقالات\nTransfers", Icons.Default.ShoppingCart),
                                Triple(4, "البريد\nInbox", Icons.Default.Email),
                                Triple(5, "المذكرات\nDiary", Icons.Default.Create),
                                Triple(6, "الكشافة\nScout", Icons.Default.Search),
                                Triple(7, "الأكاديمية\nAcademy", Icons.Default.Face)
                            )
                            tabs.forEach { (index, label, icon) ->
                                val isSelected = activeTab == index
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { viewModel.selectTab(index) },
                                    icon = { 
                                        Icon(
                                            icon, 
                                            contentDescription = label, 
                                            tint = if (isSelected) sophAccent else sophGray
                                        ) 
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            lineHeight = 12.sp,
                                            color = if (isSelected) sophAccent else sophGray
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = sophPill
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color(0xFF111318))
                    ) {
                        when (activeTab) {
                            0 -> HomeScreen(
                                career = career!!,
                                userClub = userClub,
                                opponentClub = opponentClub,
                                userSquad = userSquad,
                                fixtures = fixtures,
                                onStartMatch = { viewModel.startInteractiveMatch() },
                                onQuickSim = { viewModel.quickSimulateMatch() },
                                onAdvanceWeek = { viewModel.advanceWeek() },
                                onSkipSeason = { viewModel.skipEntireSeason() },
                                onSelectSponsor = { viewModel.chooseSponsor(it) }
                            )
                            1 -> SquadScreen(
                                userClub = userClub,
                                userSquad = userSquad,
                                career = career!!,
                                selectedSchedule = selectedTrainingSchedule,
                                trainingFeedback = trainingFeedback,
                                loanedOutPlayers = loanedOutPlayers,
                                onToggleLoan = { viewModel.toggleLoanStatus(it) },
                                onTerminateLoan = { viewModel.terminateOrRecallLoan(it) },
                                onSelectSchedule = { viewModel.selectTrainingSchedule(it) },
                                onClearFeedback = { viewModel.clearTrainingFeedback() },
                                onTrainPlayer = { p, s -> viewModel.trainPlayer(p, s) },
                                onSellPlayer = { viewModel.sellPlayer(it) },
                                onToggleTransfer = { viewModel.toggleTransferStatus(it) },
                                onRecoverEnergy = { viewModel.recoverSquadEnergy() }
                            )
                            2 -> StandingsScreen(
                                clubs = clubs,
                                userClub = userClub,
                                currentWeek = career!!.week,
                                fixtures = fixtures
                            )
                            3 -> TransfersScreen(
                                career = career!!,
                                userClub = userClub!!,
                                availablePlayers = transferPlayers,
                                activeBidPlayer = activeBidPlayer,
                                bidDialogState = bidDialogState,
                                onBuyPlayer = { viewModel.purchasePlayer(it) },
                                onLoanPlayer = { viewModel.loanInPlayer(it) },
                                onSelectBidPlayer = { viewModel.selectPlayerForBid(it) },
                                onCancelBid = { viewModel.closeBidDialog() },
                                onSubmitBid = { p, bid -> viewModel.submitTransferBid(p, bid) },
                                onAcceptCounter = { p, counter -> viewModel.acceptCounterOffer(p, counter) }
                            )
                            4 -> InboxScreen(
                                newsList = news,
                                onProcessIncomingOffer = { item, accept -> viewModel.processIncomingTransferOffer(item, accept) }
                            )
                            5 -> SeasonJournalScreen(
                                journals = journals,
                                career = career!!,
                                onAddManualNote = { viewModel.writeManualJournal("خاطرة المدرب ✏️", it) }
                            )
                            6 -> {
                                val scoutDiscoveries by viewModel.scoutDiscoveries.collectAsStateWithLifecycle()
                                ScoutingScreen(
                                    career = career!!,
                                    discoveries = scoutDiscoveries,
                                    onUpgradeScout = { viewModel.upgradeScout() },
                                    onLaunchMission = { viewModel.launchScoutMission(it) },
                                    onSignProdigy = { viewModel.signScoutProdigy(it) }
                                )
                            }
                            7 -> {
                                AcademyScreen(
                                    career = career!!,
                                    onUpgradeAcademy = { viewModel.upgradeAcademy() },
                                    onPromoteProdigy = { viewModel.promoteAcademyProdigy() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- SQUAD PROFILE CONFIGURATION SETUP ----------------
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CareerSetupScreen(onSetupFinished: (String, Int, String, String, String) -> Unit) {
    var nameInput by remember { mutableStateOf("") }
    var careerMode by remember { mutableStateOf("Manager") } // "Manager" or "Player"
    var playerPosition by remember { mutableStateOf("ATT") } // "ATT", "MID", "DEF", "GK"
    var selectedLeague by remember { mutableStateOf("SPL") } // "SPL", "EPL", "LAL", "SER", "BUN", "FRA"
    var selectedClubId by remember { mutableStateOf(1) } // Dynamic based on league selection

    // 8 select-able leagues definitions
    val leaguesList = listOf(
        Triple("SPL", "الدوري السعودي 🇸🇦", "Saudi Pro League"),
        Triple("EPL", "الدوري الإنجليزي 🏴󠁧󠁢󠁥󠁮󠁧󠁿", "Premier League"),
        Triple("ECHA", "دوري الأولى الإنجليزي 🏴󠁧󠁢󠁥󠁮󠁧󠁿", "Championship"),
        Triple("EL1", "دوري الثانية الإنجليزي 🏴󠁧󠁢󠁥󠁮󠁧󠁿", "League One"),
        Triple("LAL", "الدوري الإسباني 🇪🇸", "LaLiga"),
        Triple("SER", "الدوري الإيطالي 🇮🇹", "Serie A"),
        Triple("BUN", "الدوري الألماني 🇩🇪", "Bundesliga"),
        Triple("FRA", "الدوري الفرنسي 🇫🇷", "Ligue 1")
    )

    // Complete index mapping for clubs across leagues
    val clubsByLeague = mapOf(
        "SPL" to listOf(
            Triple(1, "Al-Hilal (الهلال) 👑", "الميزانية: $120M - النخبة الأزرق"),
            Triple(2, "Al-Nassr (النصر) 💛", "الميزانية: $110M - نجد العالمي"),
            Triple(3, "Al-Ittihad (الاتحاد) 🖤", "الميزانية: $100M - العميد الجداوي"),
            Triple(4, "Al-Ahli (الأهلي) 💚", "الميزانية: $90M - الراقي التاريخي"),
            Triple(5, "Al-Shabab (الشباب) 🤍", "الميزانية: $65M - الليث الشبابي"),
            Triple(6, "Al-Ettifaq (الاتفاق) ❤️", "الميزانية: $50M - فارس الدهناء")
        ),
        "EPL" to listOf(
            Triple(7, "Manchester City (سيتي) 🩵", "الميزانية: $190M - حامل اللقب البطل"),
            Triple(8, "Arsenal (أرسنال) ❤️", "الميزانية: $150M - المدفعجية اللندني"),
            Triple(9, "Liverpool (ليفربول) 🔴", "الميزانية: $140M - الريدز العريق"),
            Triple(10, "Chelsea (تشيلسي) 💙", "الميزانية: $120M - البلوز اللندني"),
            Triple(11, "Manchester United (يونايتد) 👹", "الميزانية: $130M - الشياطين الحمر"),
            Triple(12, "Tottenham Hotspur (توتنهام) ⚪", "الميزانية: $90M - السبيرز المميز")
        ),
        "ECHA" to listOf(
            Triple(37, "Leicester City (ليستر سيتي) 🦊", "الميزانية: $45M - أوراق برتبة الثعالب"),
            Triple(38, "Leeds United (ليدز يونايتد) 🤍", "الميزانية: $38M - فخر ليدز الأبيض"),
            Triple(39, "Southampton (ساوثهامبتون) 🔴⚪", "الميزانية: $35M - القديسين العنيدين"),
            Triple(40, "Ipswich Town (إيبسويتش) 🚜", "الميزانية: $30M - تراكتورز الصاعد"),
            Triple(41, "West Brom (وست بروميتش) 🔵⚪", "الميزانية: $25M - الباغيز الخصم الصعب"),
            Triple(42, "Norwich City (نورويتش) 🪵", "الميزانية: $24M - جزر الكناري الهجومية")
        ),
        "EL1" to listOf(
            Triple(43, "Portsmouth (بورتسموث) ⚓", "الميزانية: $12M - البحارة اللندني التاريخي"),
            Triple(44, "Derby County (ديربي كاونتي) 🐏", "الميزانية: $11M - الكباش العنيدة والصلبة"),
            Triple(45, "Bolton Wanderers (بولتون) 🐘", "الميزانية: $10M - فخر بولتون القديم"),
            Triple(46, "Peterborough (بيتربورو) 🔵", "الميزانية: $8.5M - النادي الهجومي البارع"),
            Triple(47, "Barnsley (بارنسلي) 🔴", "الميزانية: $8M - التايكس الحاد والمندفع"),
            Triple(48, "Oxford United (أكسفورد) 🐂", "الميزانية: $7.5M - الثيران العريقة المتأهبة")
        ),
        "LAL" to listOf(
            Triple(13, "Real Madrid (ريال مدريد) 👑", "الميزانية: $180M - سيد أوروبا والملكي"),
            Triple(14, "Barcelona (برشلونة) 🔵🔴", "الميزانية: $130M - البلوغرانا الكتالوني"),
            Triple(15, "Atletico Madrid (أتلتيكو) 🔴⚪", "الميزانية: $100M - الروخي بلانكوس الصخرة"),
            Triple(16, "Real Sociedad (سوسيداد) 🔵", "الميزانية: $60M - الفريق الباسكي الصاعد"),
            Triple(17, "Real Betis (بيتيس) 🟢", "الميزانية: $55M - النادي الأندلسي الأخضر"),
            Triple(18, "Sevilla (إشبيلية) ⚪🔴", "الميزانية: $50M - ملك الدوري الأوروبي")
        ),
        "SER" to listOf(
            Triple(19, "Inter Milan (إنتر ميلان) 🖤💙", "الميزانية: $110M - النيراتزوري البطل"),
            Triple(20, "AC Milan (إي سي ميلان) 🔴🖤", "الميزانية: $85M - الروسونيري العريق"),
            Triple(21, "Juventus (يوفنتوس) ⚪🖤", "الميزانية: $100M - السيدة العجوز والبيانكونيري"),
            Triple(22, "Napoli (نابولي) 🩵", "الميزانية: $80M - البارتينوبي الجنوبي"),
            Triple(23, "AS Roma (روما) 💛❤️", "الميزانية: $65M - الذئاب الرومانية العريقة"),
            Triple(24, "Atalanta (أتالانتا) 💙🖤", "الميزانية: $55M - قاهر الكبار والإعصار")
        ),
        "BUN" to listOf(
            Triple(25, "Bayern Munich (بايرن ميونخ) 🔴❤️", "الميزانية: $135M - البافاري المرعب"),
            Triple(26, "Bayer Leverkusen (ليفركوزن) ❤️🖤", "الميزانية: $90M - بطل الثنائية الذهبي"),
            Triple(27, "Borussia Dortmund (دورتموند) 💛🖤", "الميزانية: $80M - أسود الفيستفاليا"),
            Triple(28, "RB Leipzig (لايبزيغ) 🔴⚪", "الميزانية: $75M - ثيران لايبزيغ الحمراء"),
            Triple(29, "Eintracht Frankfurt (فرانكفورت) 🦅", "الميزانية: $50M - النسور الألمانية الصعبة"),
            Triple(30, "Stuttgart (شتوتغارت) ⚪🔴", "الميزانية: $45M - فخر شتوتغارت")
        ),
        "FRA" to listOf(
            Triple(31, "Paris Saint-Germain (باريس) 🔵🇫🇷", "الميزانية: $150M - الملياردير الباريسي"),
            Triple(32, "Marseille (مارسيليا) 🩵⚪", "الميزانية: $70M - فخر الجنوب الفرنسي"),
            Triple(33, "Monaco (موناكو) 🔴⚪", "الميزانية: $65M - نادي الإمارة الراقي"),
            Triple(34, "Lyon (ليون) 🔵🔴⚪", "الميزانية: $60M - العرين الفرنسي والتاريخي"),
            Triple(35, "Lille (ليل) 🔴🐶", "الميزانية: $50M - كلاب الصيد العنيدة"),
            Triple(36, "Nice (نيس) ⚫🔴", "الميزانية: $45M - الصقور الفرنسية الجنوبية")
        )
    )

    // Dynamic selection update when league toggles
    LaunchedEffect(selectedLeague) {
        val filtered = clubsByLeague[selectedLeague] ?: emptyList()
        if (filtered.isNotEmpty()) {
            selectedClubId = filtered.first().first
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF131B24), Color(0xFF0C0E12))
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual decorative football header
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Color(0xFF1E2F40), CircleShape)
                    .border(2.dp, Color(0xFF90CAF9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "soccer",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "اختر نمط مسيرتك الاحترافية",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Assemble Your Football Legacy Profile",
                fontSize = 12.sp,
                color = Color(0xFF90A4AE),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 1. SELECT CAREER MODE UI (Manager vs Player)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A232E), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val modes = listOf(
                    "Manager" to "Manager (مدرب) 👑",
                    "Player" to "Player (لاعب) ⚽"
                )
                modes.forEach { (modeVal, labelText) ->
                    val isSelected = careerMode == modeVal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF0D47A1) else Color.Transparent)
                            .clickable { careerMode = modeVal }
                            .padding(12.dp)
                            .testTag("mode_tab_$modeVal"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = labelText,
                            color = if (isSelected) Color.White else Color(0xFFB0BEC5),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. NAME INPUT FIELD
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = {
                    Text(
                        text = if (careerMode == "Player") "اسم اللاعب الفنية الكروية / Player Name" else "اسم المدير الفني / Manager Name",
                        color = Color(0xFF90A4AE)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF90CAF9),
                    unfocusedBorderColor = Color(0xFF37474F)
                ),
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_name_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3. IF PLAYER MODE IS ACTIVE: SHOW POSITION CHANGER & INITIAL PREVIEW ATTRIBUTE CARD
            AnimatedVisibility(
                visible = careerMode == "Player",
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "اختر مركزك الأساسي بالفريق:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF90CAF9),
                        modifier = Modifier.align(Alignment.End)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val positions = listOf(
                            "ATT" to "🎯 مهاجم (ATT)",
                            "MID" to "🧠 وسط (MID)",
                            "DEF" to "🛡️ دفاع (DEF)",
                            "GK" to "🧤 حارس (GK)"
                        )
                        positions.forEach { (posCode, label) ->
                            val isSelected = playerPosition == posCode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF1B5E20) else Color(0xFF1E252E))
                                    .border(1.dp, if (isSelected) Color(0xFF4CAF50) else Color(0xFF37474F), RoundedCornerShape(8.dp))
                                    .clickable { playerPosition = posCode }
                                    .padding(8.dp)
                                    .testTag("pos_tab_$posCode"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFFB0BEC5),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Player Starting Stats Cards
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF142416)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF2E7D32))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🏆 مواصفات بدايتك الاحترافية (تقييم 75 OVR):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF81C784),
                                modifier = Modifier.align(Alignment.End)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "سوف تبدأ بعمر 18 سنة لتكسب طاقات سريعة وتدريبية! قدراتك تشتمل على طاقة لياقة تامة 100%، وسرعة انطلاق 82، وقدرة تكتيكية خاصة بالمركز المختار.",
                                fontSize = 11.sp,
                                color = Color(0xFFC8E6C9),
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // 4. LEAGUES HORIZONTAL SELECTION PILLS
            Text(
                text = "اختر دوري التنافس الممتاز المفضل:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF90CAF9),
                modifier = Modifier.align(Alignment.End)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                leaguesList.forEach { (code, nameAr, nameEn) ->
                    val isSelected = selectedLeague == code
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFF0288D1) else Color(0xFF1E252E))
                            .border(1.dp, if (isSelected) Color(0xFF29B6F6) else Color(0xFF37474F), RoundedCornerShape(20.dp))
                            .clickable { selectedLeague = code }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("league_tab_$code"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = nameAr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = nameEn, color = Color(0xFFB0BEC5), fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. CLUBS FROM FILTERED LEAGUE
            Text(
                text = "اختر النادي البداية المفضل:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF90CAF9),
                modifier = Modifier.align(Alignment.End)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val currentClubsList = clubsByLeague[selectedLeague] ?: emptyList()
            currentClubsList.forEach { (id, name, desc) ->
                val isSelected = selectedClubId == id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedClubId = id }
                        .testTag("club_select_$id"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF1E2C3B) else Color(0xFF161C22)
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) Color(0xFF90CAF9) else Color(0xFF263238)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = desc, fontSize = 11.sp, color = Color(0xFF90A4AE))
                        }
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedClubId = id },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF90CAF9),
                                unselectedColor = Color(0xFF37474F)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6. ACTION LAUNCH BUTTON
            Button(
                onClick = {
                    if (nameInput.trim().isNotEmpty()) {
                        onSetupFinished(
                            nameInput.trim(),
                            selectedClubId,
                            careerMode,
                            playerPosition,
                            selectedLeague
                        )
                    }
                },
                enabled = nameInput.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFB8C00),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF263238),
                    disabledContentColor = Color(0xFF546E7A)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("setup_submit_btn")
            ) {
                Text(
                    text = if (careerMode == "Player") "اصنع بطل الملاعب الذهبي وأبرم عقدك! ⚽✍️" else "توقيع الميزانية والبدء كمدرب تكتيكي! 👑✍️",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ---------------- TABS: SCREEN IMPLEMENTATIONS ----------------

// 1. HOME SCREEN (Lobby)
@Composable
fun HomeScreen(
    career: CareerEntity,
    userClub: ClubEntity?,
    opponentClub: ClubEntity?,
    userSquad: List<PlayerEntity>,
    fixtures: List<FixtureEntity>,
    onStartMatch: () -> Unit,
    onQuickSim: () -> Unit,
    onAdvanceWeek: () -> Unit,
    onSkipSeason: () -> Unit,
    onSelectSponsor: (String) -> Unit
) {
    if (userClub == null) return

    var showSkipConfirmation by remember { mutableStateOf(false) }

    val currentWeekFixture = fixtures.firstOrNull {
        it.week == career.week && (it.homeTeamId == userClub.id || it.awayTeamId == userClub.id)
    }

    val avgRating = if (userSquad.isNotEmpty()) userSquad.map { it.rating }.average().toInt() else 80

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            // Adaptive Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E252E)),
                border = BorderStroke(1.dp, Color(0xFF37474F)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val initials = if (career.managerName.isNotBlank()) {
                        career.managerName.split(" ").filter { it.isNotBlank() }.map { it.take(1) }.joinToString("").take(2).uppercase()
                    } else "PL"

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(0xFF90CAF9), CircleShape)
                            .border(1.5.dp, Color(0xFF1565C0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D47A1)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = career.managerName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (career.careerMode == "Player") "لاعب نادي ${userClub.nameAr} (${career.playerPosition}) ⚽" else "مدرب نادي ${userClub.nameAr} 👑",
                            fontSize = 12.sp,
                            color = Color(0xFFB0BEC5),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Budget / Salary Capsule
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF0D47A1), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF1976D2), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (career.careerMode == "Player") "راتب: $25K" else "$${career.budget / 1_000_000}M",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Season Badge
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF455A64), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "موسم ${career.season}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("jersey_sponsor_panel"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B232D)),
                border = BorderStroke(1.2.dp, Color(0xFF42A5F5)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF0288D1), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = "الرعاة والاستشهار 👕", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text(
                            text = "عقد رعاية قميص النادي / Jersey Shirt Sponsor",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF90CAF9)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "وقع عقد رعاية سنوي لقميص فريقك للحصول على دفعات تمويل مالية أسبوعية في ميزانيتك، تساعدك على الاستثمار في شراء نجوم دوري World Soccer Champs الممتازين أو ترقية مرافق النادي!",
                        fontSize = 11.sp,
                        color = Color(0xFFECEFF1),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val activeSponsor = career.selectedSponsor
                    Spacer(modifier = Modifier.height(10.dp))

                    // Current status banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF111318), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFF37474F), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = when (activeSponsor) {
                                    "Aramco" -> "أرامكو السعودية 🇸🇦 ($350k/أسبوع)"
                                    "Emirates" -> "طيران الإمارات ✈️ ($250k/أسبوع)"
                                    "Spotify" -> "سبوتيفاي الموسيقية 🎵 ($180k/أسبوع + $100k بونص فوز)"
                                    "Nike" -> "نايكي الرياضية ✔️ ($120k/أسبوع)"
                                    else -> "بدون راعي نشط حالياً 🛑"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeSponsor != "None") Color(0xFF81C784) else Color(0xFFFFB74D)
                            )
                            Text(
                                text = "الراعي الرسمي لقميص الفريق:",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "العروض والخيارات الاستثمارية المتاحة للتوقيع الفوري:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF90CAF9),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Sparking offer list rows layout
                    val sponsorsList = listOf(
                        Triple("Aramco", "أرامكو السعودية 🇸🇦", "تمويل أسبوعي بقيمة $350k دفعة نقدية."),
                        Triple("Emirates", "طيران الإمارات ✈️", "تمويل أسبوعي بقيمة $250k مضمون بدون حدود."),
                        Triple("Spotify", "سبوتيفاي الموسيقية 🎵", "$180k أسبوعياً + $100k حافز فوز بالمباريات."),
                        Triple("Nike", "نايكي الرياضية ✔️", "$120k أسبوعياً دفعة أسبوعية فنية مخفضة.")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        sponsorsList.forEach { (key, name, desc) ->
                            val isCurrent = activeSponsor == key
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isCurrent) Color(0xFF0D47A1) else Color(0xFF161C24),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isCurrent) Color(0xFF42A5F5) else Color(0xFF37474F),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectSponsor(key) }
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isCurrent) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF2E7D32), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = "موقَّع حالياً ✔️", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF00E676), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = "توقيع العقد ✍️", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111318))
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(text = desc, fontSize = 10.sp, color = Color.LightGray, textAlign = TextAlign.Right)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            // Interactive Penalty Shootout Training Mini-Game Block
            var penaltyGameOpen by remember { mutableStateOf(false) }
            var penaltyStage by remember { mutableStateOf("start") } // "start", "playing", "result"
            var shootoutUserScore by remember { mutableStateOf(0) }
            var shootoutAiScore by remember { mutableStateOf(0) }
            var currentRound by remember { mutableStateOf(1) }
            var roundResultText by remember { mutableStateOf("") }
            var lastShotDirection by remember { mutableStateOf("") }
            var keeperSaved by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF112213)),
                border = BorderStroke(1.2.dp, Color(0xFF2E7D32)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF2E7D32), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "تمارين حيوية 🎮", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text(
                            text = "ضربات جزاء تكتيكية 🎯",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF81C784)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "جيم كروي سريع! تسدد ركلات الترجيح والتغلب على الحارس الذكي لربح التحدي وتطوير مستواك كلاعب أو كمدرب للتسجيل التاريخي!",
                        fontSize = 11.sp,
                        color = Color(0xFFC8E6C9),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!penaltyGameOpen) {
                        Button(
                            onClick = {
                                penaltyGameOpen = true
                                penaltyStage = "start"
                                shootoutUserScore = 0
                                shootoutAiScore = 0
                                currentRound = 1
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("العب ضربات تدريبية سريعة ⚽", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        HorizontalDivider(color = Color(0xFF2E7D32).copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 10.dp))
                        
                        when (penaltyStage) {
                            "start" -> {
                                Text(
                                    text = "الركلة رقم ($currentRound من 5) - اختر زاوية التسديد الآن لمخادعة الحارس المتربص:",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val directions = listOf(
                                        "Left" to "الزاوية اليسرى 🥅⬅️",
                                        "Center" to "المنتصف المضمون 🥅⬆️",
                                        "Right" to "الزاوية اليمنى 🥅➡️"
                                    )
                                    directions.forEach { (dirCode, label) ->
                                        Button(
                                            onClick = {
                                                lastShotDirection = label
                                                val keeperChoice = listOf("Left", "Center", "Right").random()
                                                if (keeperChoice == dirCode) {
                                                    keeperSaved = true
                                                    roundResultText = "تصدي أسطوري! توقع حارس الذكاء الاصطناعي زاويتك بدقة كاسحة وطار لها باحتراف! 🧤"
                                                    shootoutAiScore += 1
                                                } else {
                                                    keeperSaved = false
                                                    roundResultText = "ججججووووول! سددت كرة خيالية دائرية مزقت الشباك والحارس قفز للاتجاه الآخر! ⚽🥅"
                                                    shootoutUserScore += 1
                                                }
                                                penaltyStage = "result"
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(vertical = 10.dp)
                                        ) {
                                            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }
                            "result" -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (keeperSaved) Color(0xFFC62828).copy(alpha = 0.15f) else Color(0xFF2E7D32).copy(alpha = 0.15f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = if (keeperSaved) "❌ ركلة ضائعة" else "🥅 هدف لا يصد ولا يرد!",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (keeperSaved) Color(0xFFE57373) else Color(0xFF81C784)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = roundResultText,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "النتيجة: المسدد $shootoutUserScore - الحارس $shootoutAiScore",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Button(
                                        onClick = {
                                            if (currentRound < 5) {
                                                currentRound += 1
                                                penaltyStage = "start"
                                            } else {
                                                penaltyStage = "completed"
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (currentRound < 5) "الركلة التالية ➡️" else "رؤية النتيجة النهائية 🏁", fontSize = 11.sp)
                                    }
                                }
                            }
                            "completed" -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🏁 انتهى الجيم البسيط والتحدي السريع!",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (shootoutUserScore > shootoutAiScore) {
                                            "فوز تكتيكي باهر! 🏆 تفوقت على الحارس بكل ثقة واستحققت إشادة المدرب والمشجعين!"
                                        } else if (shootoutUserScore == shootoutAiScore) {
                                            "التعادل سيد الأحكام المثير! أداء ممتاز وقدرة تكتيكية رائعة بالإنهاء كالمحترفين."
                                        } else {
                                            "للأسف، تصدى الحارس لمعظم كراتك الرائعة هذه المرة! استمر في التمرين لتحسّن مهارات التسديد الدقيق 👊"
                                        },
                                        fontSize = 12.sp,
                                        color = Color(0xFFC8E6C9),
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                penaltyStage = "start"
                                                shootoutUserScore = 0
                                                shootoutAiScore = 0
                                                currentRound = 1
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("لعب مرة ثانية 🔁", fontSize = 11.sp)
                                        }
                                        Button(
                                            onClick = { penaltyGameOpen = false },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242)),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("إنهاء الجيم ❌", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            // Next Match & Calendar card
            Text(
                text = "المباراة القادمة / Next Match",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                border = BorderStroke(1.dp, Color(0xFF43474E)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Matchday Tag
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFD1E4FF), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "الجولة ${career.week}",
                            fontSize = 11.sp,
                            color = Color(0xFF003355),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (opponentClub != null && currentWeekFixture != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Home Team
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (currentWeekFixture.homeTeamId == userClub.id) userClub.nameAr else opponentClub.nameAr,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = if (currentWeekFixture.homeTeamId == userClub.id) "قوة: $avgRating" else "قوة: ${opponentClub.reputation}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFC2C7CF)
                                )
                            }

                            // VS Divider
                            Text(
                                text = "VS",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFD1E4FF)
                            )

                            // Away Team
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (currentWeekFixture.awayTeamId == userClub.id) userClub.nameAr else opponentClub.nameAr,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = if (currentWeekFixture.awayTeamId == userClub.id) "قوة: $avgRating" else "قوة: ${opponentClub.reputation}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFC2C7CF)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Actions Panel
                        if (!currentWeekFixture.isPlayed) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Simulation button
                                Button(
                                    onClick = onQuickSim,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1E252E),
                                        contentColor = Color.White
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFF43474E)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .testTag("quick_simulation_btn")
                                ) {
                                    Text("محاكاة فورية ⚡", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                // Interactive play button
                                Button(
                                    onClick = onStartMatch,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD1E4FF),
                                        contentColor = Color(0xFF003355)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(50.dp)
                                        .testTag("interactive_play_btn")
                                ) {
                                    Text("العب التكتيك 🎮", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // If played, enable progressing to next matchday
                            Button(
                                onClick = onAdvanceWeek,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD1E4FF),
                                    contentColor = Color(0xFF003355)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("advance_week_btn")
                            ) {
                                Text(
                                    text = "إنهاء الجولة والتقدم للجولة القادمة ⏭️",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { showSkipConfirmation = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x22FF9800),
                                contentColor = Color(0xFFFFB74D)
                            ),
                            border = BorderStroke(1.dp, Color(0x66FF9800)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("skip_season_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB74D),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تخطي ومحاكاة الموسم بأكمله ⏩",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Text(text = "موسم ناجح جداً! لقد أكملت مسابقتك كلياً.", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }

        item {
            // Dialogue rendering
            if (showSkipConfirmation) {
                AlertDialog(
                    onDismissRequest = { showSkipConfirmation = false },
                    title = {
                        Text(
                            text = "تخطي الموسم بأكمله؟ ⚠️",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    text = {
                        Text(
                            text = "سيقوم النظام بمحاكاة جميع الجولات المتبقية وتحديث ترتيب فريقك تلقائياً للعبور مباشرة للموسم القادم. لن تتمكن من التراجع عن هذه الخطوة.",
                            fontSize = 13.sp,
                            color = Color(0xFFC2C7CF)
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSkipConfirmation = false
                                onSkipSeason()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE53935),
                                contentColor = Color.White
                            )
                        ) {
                            Text("نعم، تخطي الموسم ⏩", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSkipConfirmation = false }) {
                            Text("إلغاء", color = Color(0xFFC2C7CF), fontSize = 12.sp)
                        }
                    },
                    containerColor = Color(0xFF1E252E)
                )
            }
        }

        item {
            // Squad stats glance
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                border = BorderStroke(1.dp, Color(0xFF43474E)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "إحصائيات المعنويات واللياقة / Team Status",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "معدل طاقة التشكيلة", fontSize = 13.sp, color = Color(0xFFC2C7CF))
                        val avgEnergy = if (userSquad.isNotEmpty()) userSquad.map { it.energy }.average().toInt() else 100
                        Text(
                            text = "$avgEnergy%",
                            fontSize = 13.sp,
                            color = if (avgEnergy > 60) Color(0xFFD1E4FF) else Color(0xFFFF5555),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "قوة الفريق العامة", fontSize = 13.sp, color = Color(0xFFC2C7CF))
                        Text(
                            text = "$avgRating ⭐",
                            fontSize = 13.sp,
                            color = Color(0xFFD1E4FF),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// 2. SQUAD SCREEN (Manage formation, play training upgrades, sell player deals)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SquadScreen(
    userClub: ClubEntity?,
    userSquad: List<PlayerEntity>,
    career: CareerEntity,
    selectedSchedule: String,
    trainingFeedback: String?,
    loanedOutPlayers: List<PlayerEntity>,
    onToggleLoan: (PlayerEntity) -> Unit,
    onTerminateLoan: (PlayerEntity) -> Unit,
    onSelectSchedule: (String) -> Unit,
    onClearFeedback: () -> Unit,
    onTrainPlayer: (PlayerEntity, String) -> Unit,
    onSellPlayer: (PlayerEntity) -> Unit,
    onToggleTransfer: (PlayerEntity) -> Unit,
    onRecoverEnergy: () -> Unit
) {
    if (userClub == null) return

    val tacticalFormations = listOf("4-4-2", "4-3-3", "3-5-2", "5-4-1")
    var selectedFormation by remember { mutableStateOf(userClub.formation) }

    if (trainingFeedback != null) {
        AlertDialog(
            onDismissRequest = onClearFeedback,
            confirmButton = {
                TextButton(onClick = onClearFeedback) {
                    Text("حسناً / Understand", fontWeight = FontWeight.Bold, color = Color(0xFFD1E4FF))
                }
            },
            title = {
                Text("تقرير المدرب الفني 🏃‍♂️", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Text(trainingFeedback, fontSize = 14.sp, color = Color(0xFFC2C7CF))
            },
            containerColor = Color(0xFF1E252E),
            shape = RoundedCornerShape(16.dp)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            // General info & Energy heal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "إدارة تفاصيل التشكيلة", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "Tactical Formation & Training", fontSize = 12.sp, color = Color.Gray)
                }

                Button(
                    onClick = onRecoverEnergy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E252E),
                        contentColor = Color(0xFFD1E4FF)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF43474E)),
                    modifier = Modifier.testTag("heal_squad_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("شحن اللياقة $2.5M 🥤", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Team-wide Weekly Training Scheduler UI block
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E252E)),
                border = BorderStroke(1.dp, Color(0xFF43474E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "مركز جدولة التدريب الرياضي الأسبوعي 📋",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "يتم تطبيق البرنامج تلقائياً على التشكيلة نهاية كل جولة عند التقدم:",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val schedules = listOf(
                        "Balanced" to "متوازن ⚖️",
                        "Intensive" to "بدني مكثف ⚡",
                        "Tactical" to "تكتيكي ومناورات ⚽",
                        "Recovery" to "استشفاء وراحة 🥤"
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        schedules.forEach { (key, label) ->
                            val isChosen = selectedSchedule == key
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isChosen) Color(0xFFD1E4FF) else Color(0xFF111318),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isChosen) Color(0xFFD1E4FF) else Color(0xFF43474E),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectSchedule(key) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChosen) Color(0xFF003355) else Color(0xFFC2C7CF)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (selectedSchedule) {
                            "Intensive" -> "التأثير: +75% فرصة لتحديث المهارات (+1)، لكن يستهلك طاقة اللياقة إضافياً -18% مجهود."
                            "Tactical" -> "التأثير: +65% فرصة تفوق مهارات التمرير والدفاع، تفقد اللياقة -10%."
                            "Recovery" -> "التأثير: استرداد سريع ونشاط لجميع اللاعبين بمقدار +25% طاقة. الرسوم: $250k."
                            else -> "التأثير: برنامج متوازن، يزيد طاقة اللياقة بمقدار +5%، مع فرصة 35% لنمو مهارات عشوائية."
                        },
                        fontSize = 11.sp,
                        color = Color(0xFFD1E4FF)
                    )
                }
            }
        }

        item {
            val teamChemistry = remember(userSquad) {
                if (userSquad.isEmpty()) 0
                else {
                    var gkCount = 0
                    var defCount = 0
                    var midCount = 0
                    var attCount = 0
                    for (p in userSquad) {
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
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("team_chemistry_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131F2E)),
                border = BorderStroke(1.2.dp, if (teamChemistry >= 70) Color(0xFF81C784) else Color(0xFFE57373)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .background(if (teamChemistry >= 70) Color(0xFF1B5E20) else Color(0xFFB71C1C), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = "مؤشر التناغم 🧪", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text(
                            text = "تجانس التشكيلة الكروية / Team Chemistry",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (teamChemistry >= 70) Color(0xFF81C784) else Color(0xFFE57373)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "يقيس هذا المؤشر توازن مراكز تشكيلة لاعبيك (GK, DEF, MID, ATT). الكيمياء المرتفعة تمنحك أفضلية حاسمة لترجيح كفة خياراتك وإحراز الأهداف أثناء هجمات اللقاء!",
                        fontSize = 11.sp,
                        color = Color(0xFFCFD8DC),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "$teamChemistry%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        LinearProgressIndicator(
                            progress = teamChemistry / 100f,
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (teamChemistry >= 70) Color(0xFF81C784) else Color(0xFFE57373),
                            trackColor = Color(0xFF2C3949)
                        )
                    }
                }
            }
        }

        item {
            // New Feature: Elegant Squad Form & Ranking Dashboard Board
            var boardSortType by remember { mutableStateOf("Form") } // "Form", "Rating", "Stats"

            val sortedPlayers = remember(userSquad, boardSortType) {
                when (boardSortType) {
                    "Form" -> userSquad.sortedByDescending { (it.rating * 0.4f) + (it.form * 4f) + (it.energy * 0.3f) }
                    "Rating" -> userSquad.sortedByDescending { it.rating }
                    else -> userSquad.sortedByDescending { it.goals * 10 + it.assists * 5 + it.rating * 0.1f }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("squad_ranking_board"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131F2E)),
                border = BorderStroke(1.2.dp, Color(0xFF1E88E5)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF0D47A1), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = "لوحة تكتيكية 📊", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text(
                            text = "لوحة تقييم وجاهزية الفريق",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF90CAF9)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "نظرة شمولية احترافية للمدير الفني لفلترة أوراق الفريق الرابحة وتحديد النجوم والمساهمين الأكثر جاهزية للمواجهات القادمة.",
                        fontSize = 11.sp,
                        color = Color(0xFFCFD8DC),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mode Toggles
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E2836), RoundedCornerShape(10.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val boardOptions = listOf(
                            "Form" to "⚡ الجاهزية والبدن",
                            "Rating" to "⭐ الأفضل تقييماً",
                            "Stats" to "⚽ المساهمات والأهداف"
                        )
                        boardOptions.forEach { (typeKey, labelStr) ->
                            val isSelected = boardSortType == typeKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF0D47A1) else Color.Transparent)
                                    .clickable { boardSortType = typeKey }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = labelStr,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF90A4AE),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // List top 4 ranked players in this view
                    Text(
                        text = "ترتيب لاعبي فريقك الحالي حسب اختيارك:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF90CAF9),
                        modifier = Modifier.align(Alignment.End)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    sortedPlayers.take(4).forEachIndexed { index, p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(Color(0xFF172435), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left side stats / info
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Position capsule
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF263238), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = p.position, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB0BEC5))
                                }
                                Spacer(modifier = Modifier.width(6.dp))

                                Column {
                                    Text(text = p.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "طاقة: ${p.energy}% | فورم: ${p.form}/10",
                                            fontSize = 10.sp,
                                            color = Color(0xFF90A4AE)
                                        )
                                        if (p.goals > 0 || p.assists > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "⚽ ${p.goals} | 🅰️ ${p.assists}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFFB300)
                                            )
                                        }
                                    }
                                }
                            }

                            // Right side rank indicators
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (index) {
                                                0 -> Color(0xFFFFD54F) // Gold
                                                1 -> Color(0xFFB0BEC5) // Silver
                                                2 -> Color(0xFFFFB74D) // Bronze
                                                else -> Color(0xFF37474F)
                                            },
                                            CircleShape
                                        )
                                        .size(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (index < 3) Color.Black else Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "OVR ${p.rating}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF90CAF9)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tactical Advisory Corner
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF101B26)),
                        border = BorderStroke(1.dp, Color(0xFF1E88E5).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "info",
                                    tint = Color(0xFFFFCC00),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "توصية المستشار التكتيكي للمدير الفني 🧠",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when (boardSortType) {
                                    "Form" -> {
                                        if (sortedPlayers.isNotEmpty()) {
                                            "التحليل الفني: ننصحك بالاعتماد على اللاعب [${sortedPlayers.first().name}] كمحرك أساسي للمباراة القادمة لامتلاكه طاقة كروية كاملة (${sortedPlayers.first().energy}%) ومستويات تصاعدية ممتازة!"
                                        } else "البيانات غير كافية حالياً."
                                    }
                                    "Rating" -> {
                                        if (sortedPlayers.isNotEmpty()) {
                                            "رؤية فنية: النجم الأرفع والأعلى مهارة بفريقك هو [${sortedPlayers.first().name}] بتقييم كاسح (${sortedPlayers.first().rating} OVR). ابني خطتك التكتيكية لتمرير الكرات بشكل يبرز موهبته وصناعته للأهداف."
                                        } else "البيانات غير كافية."
                                    }
                                    else -> {
                                        val topScorer = sortedPlayers.maxByOrNull { it.goals }
                                        if (topScorer != null && topScorer.goals > 0) {
                                            "تقرير التسجيل: القناص [${topScorer.name}] يقدم مستويات تهديفية مذهلة بمجموع (${topScorer.goals}) أهداف حاسمة! استمر في إشراكه هجومياً للحفاظ على الزخم التهديفي للمجموعة."
                                        } else {
                                            "لم يبدأ أي لاعب بالتسجيل في الدوري بعد. حفّز لاعبي الهجوم في التمرين والتدريبات بالاعتماد على تسديدات دقيقة!"
                                        }
                                    }
                                },
                                fontSize = 11.sp,
                                color = Color(0xFFCFD8DC),
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        item {
            // Formation Picker Section
            Text(
                text = "الرسم التكتيكي / Formation:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD1E4FF),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tacticalFormations.forEach { form ->
                    val isSelected = selectedFormation == form
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) Color(0xFFD1E4FF) else Color(0xFF1E252E),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFD1E4FF) else Color(0xFF43474E),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedFormation = form }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = form,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFF003355) else Color(0xFFC2C7CF)
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "قائمة اللاعبين وحصص التدريب:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        items(userSquad) { player ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                border = BorderStroke(1.dp, Color(0xFF43474E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF38495C), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(text = player.position, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = player.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Text(text = "العمر: ${player.age} عاماً | القيمة المالية: $${player.value / 1_000_000}M", fontSize = 12.sp, color = Color(0xFFC2C7CF))
                            if (player.isOnLoan) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFFEB3B), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "لاعب معار (مستأجر) - متبقي ${player.loanWeeksLeft} جولات ⏳",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF004D40)
                                    )
                                }
                            } else if (player.onLoanList) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF4CAF50), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "متاح على لائحة الإعارات 🌐",
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Rating badge
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFD1E4FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = player.rating.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF003355))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Fatigue and Energy stats bar
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "الطاقة / Energy:", fontSize = 12.sp, color = Color(0xFFC2C7CF))
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .background(Color(0xFF1E252E), RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(player.energy / 100f)
                                    .background(
                                        if (player.energy > 60) Color(0xFFD1E4FF) else Color(0xFFFF5555),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "${player.energy}%", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Training Upgrades / Sell buttons panel
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (player.isOnLoan) {
                            // Borrowed player. Show only recall or return early button
                            Button(
                                onClick = { onTerminateLoan(player) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFC0392B),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("terminate_loan_${player.id}"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text("إنهاء عقد الإعارة مبكراً 🚪", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Training buttons
                            if (player.position != "GK") {
                                Button(
                                    onClick = { onTrainPlayer(player, "shooting") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1E252E),
                                        contentColor = Color(0xFFC2C7CF)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFF43474E)),
                                    modifier = Modifier.testTag("train_shoot_${player.id}"),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("تسديد 🎯 (+1)", fontSize = 10.sp)
                                }

                                Button(
                                    onClick = { onTrainPlayer(player, "passing") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1E252E),
                                        contentColor = Color(0xFFC2C7CF)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFF43474E)),
                                    modifier = Modifier.testTag("train_pass_${player.id}"),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("تمرير ⚽ (+1)", fontSize = 10.sp)
                                }
                            } else {
                                Button(
                                    onClick = { onTrainPlayer(player, "goalkeeper") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1E252E),
                                        contentColor = Color(0xFFC2C7CF)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFF43474E)),
                                    modifier = Modifier.testTag("train_gk_${player.id}"),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("تصديات 🧤 (+1)", fontSize = 10.sp)
                                }
                            }

                            // Sell AI Offer button
                            Button(
                                onClick = { onSellPlayer(player) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF5C0000),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("sell_ai_${player.id}"),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("بيع للنادي المهتم 💰", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            // Transfer switch
                            val stateTxt = if (player.onTransferList) "إلغاء البيع" else "عرض بالسوق"
                            Button(
                                onClick = { onToggleTransfer(player) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (player.onTransferList) Color.Gray else Color(0x33D1E4FF),
                                    contentColor = if (player.onTransferList) Color.White else Color(0xFFD1E4FF)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(text = "$stateTxt 📢", fontSize = 10.sp)
                            }

                            // Loan toggle switch
                            val onLoanTxt = if (player.onLoanList) "إلغاء عرض الإعارة" else "عرض للإعارة"
                            Button(
                                onClick = { onToggleLoan(player) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (player.onLoanList) Color(0xFF273746) else Color(0xFF196F3D),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(text = "$onLoanTxt 🌐", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        if (loanedOutPlayers.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "نجوم معارون خارج النادي (تحت الملاحظة):",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFCC00),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            items(loanedOutPlayers) { player ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14171E)),
                    border = BorderStroke(1.dp, Color(0xFFE6A23C)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFFFEEBB), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = player.position, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = player.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Text(text = "العمر: ${player.age} عاماً | القيمة المالية: $${player.value / 1_000_000}M", fontSize = 12.sp, color = Color.Gray)
                            }

                            // Rating badge
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFFFEEBB), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = player.rating.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "مُعار لأحد الفرق المنافسة | المتبقي: ${player.loanWeeksLeft} جولات كروية ⏳",
                            color = Color(0xFFE6A23C),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val canAffordRecall = career.budget >= 500_000L
                        Button(
                            onClick = { onTerminateLoan(player) },
                            enabled = canAffordRecall,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFC107),
                                contentColor = Color.Black,
                                disabledContainerColor = Color(0xFF2C2F36),
                                disabledContentColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp).testTag("recall_loan_${player.id}"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (canAffordRecall) "استدعاء مبكر وغرامة قطيعة ↩️ ($500k)" else "عاجز عن سداد تكلفة الاستدعاء ($500k)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// 3. TABLE & SCHEDULER VIEW
@Composable
fun StandingsScreen(
    clubs: List<ClubEntity>,
    userClub: ClubEntity?,
    currentWeek: Int,
    fixtures: List<FixtureEntity>
) {
    var showStandings by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Toggle view
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E252E), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF43474E), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { showStandings = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showStandings) Color(0xFF38495C) else Color.Transparent,
                    contentColor = if (showStandings) Color.White else Color(0xFFC2C7CF)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("جدول الدوري / Standings", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { showStandings = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!showStandings) Color(0xFF38495C) else Color.Transparent,
                    contentColor = if (!showStandings) Color.White else Color(0xFFC2C7CF)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("المباريات / Fixtures", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showStandings) {
            // Table Header layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1C1E), RoundedCornerShape(8.dp))
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "#", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2C7CF), modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                Text(text = "الفريق / Club", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2C7CF), modifier = Modifier.weight(1f))
                Text(text = "لعب", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2C7CF), modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                Text(text = "فارق", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2C7CF), modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                Text(text = "نقاط", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD1E4FF), modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(clubs.size) { index ->
                    val club = clubs[index]
                    val isMyself = userClub != null && club.id == userClub.id
                    val diff = club.goalsFor - club.goalsAgainst

                    val rowBg = if (isMyself) Color(0xFFD1E4FF) else Color.Transparent
                    val rowTextColor = if (isMyself) Color(0xFF003355) else Color.White
                    val rowSecTextColor = if (isMyself) Color(0xFF003355) else Color(0xFFC2C7CF)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(rowBg, RoundedCornerShape(12.dp))
                            .drawBehind {
                                if (!isMyself) {
                                    val strokeWidth = 1.dp.toPx()
                                    val y = size.height - strokeWidth / 2
                                    drawLine(
                                        color = Color(0xFF43474E),
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = strokeWidth
                                    )
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMyself) rowTextColor else if (index < 3) Color(0xFFD1E4FF) else Color(0xFFC2C7CF),
                            modifier = Modifier.width(30.dp),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = if (isMyself) "${club.nameAr} (أنت)" else club.nameAr,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = rowTextColor,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(text = club.played.toString(), fontSize = 13.sp, color = rowTextColor, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                        Text(text = (if (diff >= 0) "+$diff" else "$diff"), fontSize = 13.sp, color = if (isMyself) rowTextColor else if (diff >= 0) Color(0xFF4CAF50) else Color(0xFFFF5555), modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                        Text(
                            text = club.points.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = rowTextColor,
                            modifier = Modifier.width(42.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Scrollable match schedule of current seasons
            Text(
                text = "جدول مباريات الجولة $currentWeek / Week $currentWeek Matches",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val weekFixtures = fixtures.filter { it.week == currentWeek }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(weekFixtures) { f ->
                    val homeRef = clubs.firstOrNull { it.id == f.homeTeamId }
                    val awayRef = clubs.firstOrNull { it.id == f.awayTeamId }

                    if (homeRef != null && awayRef != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                            border = BorderStroke(1.dp, Color(0xFF43474E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Text(
                                    text = homeRef.nameAr,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Right
                                )

                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF38495C), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val scoreTxt = if (f.isPlayed) "${f.homeScore} - ${f.awayScore}" else "VS"
                                    Text(
                                        text = scoreTxt,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (f.isPlayed) Color(0xFFD1E4FF) else Color.White
                                    )
                                }

                                Text(
                                    text = awayRef.nameAr,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Left
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4. TRANSFERS SCREEN (Bidding on superstar free agents)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransfersScreen(
    career: CareerEntity,
    userClub: ClubEntity,
    availablePlayers: List<PlayerEntity>,
    activeBidPlayer: PlayerEntity?,
    bidDialogState: CareerViewModel.BidState,
    onBuyPlayer: (PlayerEntity) -> Unit,
    onLoanPlayer: (PlayerEntity) -> Unit,
    onSelectBidPlayer: (PlayerEntity) -> Unit,
    onCancelBid: () -> Unit,
    onSubmitBid: (PlayerEntity, Long) -> Unit,
    onAcceptCounter: (PlayerEntity, Long) -> Unit
) {
    var bidAmountStr by remember { mutableStateOf("") }
    var selectedPositionFilter by remember { mutableStateOf("ALL") }

    val filteredPlayers = remember(availablePlayers, selectedPositionFilter) {
        if (selectedPositionFilter == "ALL") {
            availablePlayers
        } else {
            availablePlayers.filter { it.position == selectedPositionFilter }
        }
    }

    // Propose Contract Bid dialog helper
    if (activeBidPlayer != null) {
        AlertDialog(
            onDismissRequest = onCancelBid,
            title = {
                Text(
                    text = "مفاوضة التعاقد: ${activeBidPlayer.name} 🤝",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    val potential = activeBidPlayer.rating + (28 - activeBidPlayer.age).coerceIn(0, 8)
                    Text(
                        text = "القوة البدنية والفنية: ${activeBidPlayer.rating} ⭐ | القدرة الكامنة: $potential ⭐",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "العمر: ${activeBidPlayer.age} عاماً | القيمة السوقية: $$((activeBidPlayer.value / 1_000_000).toDouble())M",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "ميزانية النادي: $${career.budget / 1_000_000}M",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD1E4FF),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    when (bidDialogState) {
                        is CareerViewModel.BidState.None -> {
                            Text(
                                text = "أدخل قيمة العرض المالي المراد تقديمه للنادي البائع:",
                                fontSize = 12.sp,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = bidAmountStr,
                                onValueChange = { bidAmountStr = it.filter { char -> char.isDigit() } },
                                label = { Text("قيمة المبلغ المقترح بالدولار ($)", color = Color.Gray) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD1E4FF),
                                    unfocusedBorderColor = Color(0xFF43474E),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                        is CareerViewModel.BidState.Submitted -> {
                            val msg = bidDialogState.messageAr
                            Text(
                                text = msg,
                                fontSize = 14.sp,
                                color = if (bidDialogState.isAccepted) Color(0xFFD1E4FF) else Color(0xFFFF5555),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            if (bidDialogState.counterOffer > 0L) {
                                Text(
                                    text = "العرض البديل النهائي من النادي: $${bidDialogState.counterOffer / 1_000_000}M",
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (bidDialogState) {
                        is CareerViewModel.BidState.None -> {
                            Button(
                                onClick = {
                                    val bidAmt = bidAmountStr.toLongOrNull()
                                    if (bidAmt != null) {
                                        onSubmitBid(activeBidPlayer, bidAmt)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1E4FF)),
                                modifier = Modifier.weight(1.dp.value)
                            ) {
                                Text("إرسال العرض 📨", color = Color(0xFF003355), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        is CareerViewModel.BidState.Submitted -> {
                            if (bidDialogState.counterOffer > 0L) {
                                val canAffordCounter = career.budget >= bidDialogState.counterOffer
                                Button(
                                    onClick = {
                                        onAcceptCounter(activeBidPlayer, bidDialogState.counterOffer)
                                    },
                                    enabled = canAffordCounter,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1E4FF)),
                                    modifier = Modifier.weight(1.dp.value)
                                ) {
                                    Text("شراء بالسعر البديل ✍️", color = Color(0xFF003355), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    
                    TextButton(
                        onClick = onCancelBid,
                        modifier = Modifier.weight(1.dp.value)
                    ) {
                        Text("إغلاق / إلغاء", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            },
            containerColor = Color(0xFF1E252E),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Transfer Market Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E252E)),
            border = BorderStroke(1.dp, Color(0xFF43474E)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "سوق انتقالات اللاعبين العالمي 🌍", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    text = "الرصيد المتاح للتعاقدات الرسمية: $${career.budget / 1_000_000}M",
                    fontSize = 13.sp,
                    color = Color(0xFFD1E4FF),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Position Filtering Bar
        Text(text = "تصفية حسب مركز اللاعب:", fontSize = 11.sp, color = Color.Gray)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val filters = listOf(
                "ALL" to "كل المراكز 🏟️",
                "GK" to "حارس مرمى GK",
                "DEF" to "مدافع DEF",
                "MID" to "لاعب وسط MID",
                "ATT" to "مهاجم ATT"
            )

            filters.forEach { (key, label) ->
                val isSelected = selectedPositionFilter == key
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) Color(0xFFD1E4FF) else Color(0xFF1E252E),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFFD1E4FF) else Color(0xFF43474E),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedPositionFilter = key }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF003355) else Color(0xFFC2C7CF)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "لاعبون متاحون للتفاوض والشراء:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))

        if (filteredPlayers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "لا يوجد لاعبون متاحون للمركز المختار.", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredPlayers) { player ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                        border = BorderStroke(1.dp, Color(0xFF43474E)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF38495C), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = player.position, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = player.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    
                                    val potential = player.rating + (28 - player.age).coerceIn(0, 8)
                                    Text(
                                        text = "القوة الحالية: ${player.rating} ⭐ | القدرة المتوقعة: $potential ⭐",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFFC107),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                    
                                    Text(text = "العمر: ${player.age} عاماً", fontSize = 11.sp, color = Color(0xFFC2C7CF))
                                    Text(
                                        text = "قيمة الصفقة: $${player.value / 1_000_000}M",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD1E4FF)
                                    )
                                    if (player.onLoanList) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF2E7D32), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "متاح للإعارة الفورية 🌐",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. Buy outright
                                val canAfford = career.budget >= player.value
                                Button(
                                    onClick = { onBuyPlayer(player) },
                                    enabled = canAfford,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD1E4FF),
                                        contentColor = Color(0xFF003355),
                                        disabledContainerColor = Color(0xFF1E252E),
                                        disabledContentColor = Color(0xFF43474E)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("buy_player_${player.id}")
                                        .height(36.dp)
                                ) {
                                    Text(
                                        text = if (canAfford) "شراء فوري ✍️" else "عاجز مالياً",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // 2. Request Loan (available either if on loan list or AI club owns them)
                                val loanFee = (player.value * 5 / 100).coerceAtLeast(100_000L)
                                val canAffordLoan = career.budget >= loanFee
                                Button(
                                    onClick = { onLoanPlayer(player) },
                                    enabled = canAffordLoan,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2ECC71),
                                        contentColor = Color.White,
                                        disabledContainerColor = Color(0xFF1E252E),
                                        disabledContentColor = Color(0xFF43474E)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("loan_player_${player.id}")
                                        .height(36.dp)
                                ) {
                                    Text(
                                        text = if (canAffordLoan) "استعارة ⏱️ ($${loanFee / 1_000}k)" else "عاجز عن الإعارة",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // 3. Custom Bidding
                                Button(
                                    onClick = {
                                        bidAmountStr = (player.value * 9 / 10).toString()
                                        onSelectBidPlayer(player)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF38495C),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("bid_player_${player.id}")
                                        .height(36.dp)
                                ) {
                                    Text(
                                        text = "تقديم عرض 💬",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5. INBOX SCREEN (Mail / Sponsorships / board feedback)
@Composable
fun InboxScreen(
    newsList: List<NewsEntity>,
    onProcessIncomingOffer: (NewsEntity, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "بريد الإدارة والأخبار / Mailbox & News", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        if (newsList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Email, contentDescription = "no_mail", tint = Color.Gray, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "صندوق الرسائل فارغ تماماً.", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(newsList) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (item.type == "Board") Color(0xFFD1E4FF) else Color(0xFF43474E)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = when (item.type) {
                                                "Board" -> Color(0xFF5C0000)
                                                "TransferOffer" -> Color(0xFF38495C)
                                                else -> Color(0xFF1E252E)
                                            },
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = when (item.type) {
                                            "Board" -> "إدارة النادي"
                                            "TransferOffer" -> "عرض مالي"
                                            else -> "عام"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = item.titleAr,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = item.contentAr,
                                fontSize = 13.sp,
                                color = Color(0xFFC2C7CF)
                            )

                            if (item.type == "TransferOffer") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onProcessIncomingOffer(item, true) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFD1E4FF),
                                            contentColor = Color(0xFF003355)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("قبول العرض وحصد الأموال 💰", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Button(
                                        onClick = { onProcessIncomingOffer(item, false) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF5C0000),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("رفض وبقاء اللاعب ❌", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- 3D-STYLE GREEN CANVAS MATCH HIGHLIGHTS PLAYGROUND SCREEN ----------------
@Composable
fun MatchSimScreen(
    viewModel: CareerViewModel,
    matchPlayState: MatchPlayState,
    userClub: ClubEntity?,
    opponentClub: ClubEntity?,
    onOptionSelected: (MatchOption) -> Unit,
    onFinishMatch: () -> Unit
) {
    if (userClub == null || opponentClub == null) return

    val infiniteTransition = rememberInfiniteTransition(label = "animation")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fade"
    )

    when (matchPlayState) {
        is MatchPlayState.Simulating -> {
            val event = matchPlayState.currentEvent

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF111318)) // Eye-safe premium dark background
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Playback speed tuner (1x, 2x, 4x)
                val activeSpeed by viewModel.matchSpeed.collectAsStateWithLifecycle()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سرعة المحاكاة / Sim Speed:",
                        fontSize = 11.sp,
                        color = Color(0xFFC2C7CF),
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(1f to "1x ⏱️", 2f to "2x ⚡", 4f to "4x 🚀").forEach { (v, label) ->
                            val isSel = activeSpeed == v
                            Box(
                                modifier = Modifier
                                    .background(if (isSel) Color(0xFFD1E4FF) else Color(0xFF1E252E), RoundedCornerShape(6.dp))
                                    .border(1.dp, if (isSel) Color(0xFFD1E4FF) else Color(0xFF43474E), RoundedCornerShape(6.dp))
                                    .clickable { viewModel.changeMatchSpeed(v) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color(0xFF003355) else Color.White
                                )
                            }
                        }
                    }
                }

                // Header scoreboard
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E252E)),
                    border = BorderStroke(1.dp, Color(0xFF43474E)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "مباراة جارية - الوقت الفعلي / Live Highlights",
                            color = Color(0xFFD1E4FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(text = userClub.nameAr, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF111318), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFF43474E), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${matchPlayState.homeScore} - ${matchPlayState.awayScore}",
                                    color = Color(0xFFD1E4FF),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Text(text = opponentClub.nameAr, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "⏱️ الدقيقة: ${matchPlayState.minute}'",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Green interactive tactical Pitch canvas!
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF19381E)) // Refined tactical green pitch background
                        .border(1.dp, Color(0xFF43474E), RoundedCornerShape(16.dp))
                ) {
                    // Compose Canvas to draw pitch lines and players
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Draw Yard line markings
                        drawLine(Color(0x77FFFFFF), Offset(w / 2, 0f), Offset(w / 2, h), strokeWidth = 3f)
                        drawCircle(Color(0x77FFFFFF), radius = 60.dp.toPx(), center = Offset(w / 2, h / 2), style = Stroke(width = 3f))

                        // Left goal box lines
                        drawRect(Color(0x77FFFFFF), topLeft = Offset(0f, h / 4), size = androidx.compose.ui.geometry.Size(w / 8, h / 2), style = Stroke(width = 3f))

                        // Right goal box lines
                        drawRect(Color(0x77FFFFFF), topLeft = Offset(w - (w / 8), h / 4), size = androidx.compose.ui.geometry.Size(w / 8, h / 2), style = Stroke(width = 3f))

                        if (event != null) {
                            // Draw attacking situations!
                            val bx = event.pitchBallX * w
                            val by = event.pitchBallY * h
                            drawCircle(Color.White, radius = 10.dp.toPx(), center = Offset(bx, by))
                            drawCircle(Color(0xFFD1E4FF), radius = 14.dp.toPx(), center = Offset(bx, by), style = Stroke(width = 2f))

                            // Draw player circles
                            // Attacking active player
                            drawCircle(
                                color = Color(userClub.primaryColor),
                                radius = 20.dp.toPx(),
                                center = Offset(bx - 30.dp.toPx(), by - 20.dp.toPx())
                            )
                            // Defending opponent player
                            drawCircle(
                                color = Color(opponentClub.primaryColor),
                                radius = 20.dp.toPx(),
                                center = Offset(bx + 40.dp.toPx(), by + 10.dp.toPx())
                            )
                        }
                    }

                    // Highlight indicator glow
                    if (event != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(12.dp)
                                .background(Color(0xFF5C0000), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFD1E4FF), RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "⚠️ حدث ساخن! خيار فوري مطلوب",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Ambient scrolling text representing continuous time simulator flow
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .background(Color(0xDD111318), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF43474E), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "المباراة مستمرة... ⚽",
                                color = Color(0xFFD1E4FF),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrolling commentary box log
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                    border = BorderStroke(1.dp, Color(0xFF43474E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    val scrollState = rememberScrollState()
                    LaunchedEffect(matchPlayState.logs.size) {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .verticalScroll(scrollState)
                    ) {
                        matchPlayState.logs.forEach { logLine ->
                            Text(
                                text = logLine,
                                color = if (logLine.contains("هدف") || logLine.contains("جوو")) Color(0xFFD1E4FF) else Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }

                // Ambient continuous skip option
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.skipRemainingMatch() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2512), contentColor = Color(0xFFFFD54F)),
                    border = BorderStroke(1.dp, Color(0xFF9E7700)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("skip_highlights_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚡ تخطي ومحاكاة فورية لنهاية اللقاء | Skip & Instantly Finish Match",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tactical decision buttons
                AnimatedVisibility(
                    visible = event != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    if (event != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                            border = BorderStroke(1.dp, Color(0xFFD1E4FF)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = event.descriptionAr,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                val teamChemistry by viewModel.teamChemistry.collectAsStateWithLifecycle()
                                val chemBonus = ((teamChemistry - 70) / 5).coerceIn(-10, 6)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .background(if (chemBonus >= 0) Color(0x3381C784) else Color(0x33E57373), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "تأثير كيمياء تشكيلة النادي: ${if (chemBonus >= 0) "+$chemBonus%" else "$chemBonus%"} فرصة حسم للمراوغة والتسديد 📊",
                                        color = if (chemBonus >= 0) Color(0xFF81C784) else Color(0xFFE57373),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (opt in event.options) {
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onOptionSelected(opt) }
                                                .testTag("match_option_${opt.id}"),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF38495C)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(text = opt.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(text = "${opt.successChance}% النجاح", color = Color(0xFFD1E4FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        is MatchPlayState.PostMatch -> {
            // Post match feedback loop showing final scorer stats
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF111318))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFF43474E))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "صفارة النهاية! Match Finished 🏁", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = userClub.nameAr, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF111318), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFF43474E), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "${matchPlayState.homeScore} - ${matchPlayState.awayScore}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFD1E4FF)
                                )
                            }
                            Text(text = opponentClub.nameAr, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(text = "ملخص الأهداف والمباراة / Summary:", fontSize = 14.sp, color = Color(0xFFC2C7CF), fontWeight = FontWeight.Bold)
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        matchPlayState.scorers.forEach { s ->
                            Text(text = s, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(vertical = 2.dp))
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                        Button(
                            onClick = onFinishMatch,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD1E4FF),
                                contentColor = Color(0xFF003355)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("exit_match_lobby_btn")
                        ) {
                            Text(text = "العودة لمقر النادي / Return Lobby 🏟️", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        else -> {}
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeasonJournalScreen(
    journals: List<JournalEntity>,
    career: CareerEntity,
    onAddManualNote: (String) -> Unit
) {
    var manualNoteText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Core Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E252E)),
            border = BorderStroke(1.dp, Color(0xFF43474E)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "مذكرات الموسم الكروي 📓 | Manager's Diary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "دَوِّن ذكرياتك، انتصاراتك وتحليلاتك التكتيكية لتخليد تاريخك الكروي.",
                    fontSize = 12.sp,
                    color = Color(0xFFC2C7CF),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Write manual input block
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
            border = BorderStroke(1.dp, Color(0xFF38495C)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "كتابة خاطرة أو مذكرة تكتيكية جديدة:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD1E4FF)
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = manualNoteText,
                    onValueChange = { if (it.length <= 200) manualNoteText = it },
                    placeholder = { Text("مثال: فوز ملحمي بالديربي بفضل ثلاثية الهجوم البديل..", color = Color.Gray, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .testTag("journal_input_field"),
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD1E4FF),
                        unfocusedBorderColor = Color(0xFF43474E),
                        focusedContainerColor = Color(0xFF111318),
                        unfocusedContainerColor = Color(0xFF111318)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${manualNoteText.length}/200 حرف",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Button(
                        onClick = {
                            if (manualNoteText.isNotBlank()) {
                                onAddManualNote(manualNoteText)
                                manualNoteText = ""
                            }
                        },
                        enabled = manualNoteText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF38495C),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF2C2F36),
                            disabledContentColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_journal_btn"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Create, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تدوين في التاريخ ✍️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Diary feed / Timeline
        Text(
            text = "تاريخ ومسيرة النادي الحافلة 📜:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (journals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد مذكرات مدونة بعد. بادر بخوض المباريات أو تدوين أولى خاطراتك الآن!",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(journals.sortedByDescending { it.id }) { journal ->
                    val (badgeColor, badgeText, icon) = if (!journal.isAuto) {
                        Triple(Color(0xFF8E24AA), "خاطرة حرة ✍️", Icons.Default.Create)
                    } else if (journal.titleAr.contains("بداية") || journal.titleAr.contains("تأسيس") || journal.titleAr.contains("تعيين")) {
                        Triple(Color(0xFF4CAF50), "مسيرة واعدة 👑", Icons.Default.Check)
                    } else if (journal.titleAr.contains("قرعة") || journal.titleAr.contains("أحداث")) {
                        Triple(Color(0xFFE53935), "حديث الصحافة 📢", Icons.Default.Info)
                    } else {
                        Triple(Color(0xFF1E88E5), "ملخص اللقاء ⚽", Icons.Default.PlayArrow)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E)),
                        border = BorderStroke(1.dp, Color(0xFF38495C)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Icon and Vertical Timeline Node indicator
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(badgeColor.copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, badgeColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(badgeColor, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(text = badgeText, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }

                                    Text(
                                        text = "الموسم ${journal.season} • الأسبوع ${journal.week}",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = journal.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = journal.content,
                                    fontSize = 12.sp,
                                    color = Color(0xFFC2C7CF),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoutingScreen(
    career: CareerEntity,
    discoveries: List<PlayerEntity>,
    onUpgradeScout: () -> Unit,
    onLaunchMission: (String) -> Unit,
    onSignProdigy: (PlayerEntity) -> Unit
) {
    var selectedPos by remember { mutableStateOf("ANY") }
    val scoutLevel = career.scoutLevel
    val budget = career.budget

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E252E)),
                border = BorderStroke(1.dp, Color(0xFF43474E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.0f)) {
                        Text(
                            text = "مستكشف المواهب والشباب 🕵️‍♂️ | Scouting Department",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "أرسل طاقم الكشافة للتفتيش عن نجوم ومواهب صاعدة في الدوريات الأخرى وضخ دماء جديدة بالنادي.",
                            fontSize = 12.sp,
                            color = Color(0xFFC2C7CF)
                        )
                    }
                }
            }
        }

        // Scout Upgrade & Mission Dispatches
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Upgrade Hired Scout Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161C24)),
                    border = BorderStroke(1.dp, Color(0xFF38495C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "رتبة كشّافك الحالي: ليفل ${scoutLevel}/3 🎖️",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD1E4FF)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "كلما ارتفع ليفل كشّافك، زادت احتمالية عثوره على مواهب (Wonderkids) بتقييمات تقترب من 88 OVR وبدقة أعلى.",
                            fontSize = 11.sp,
                            color = Color(0xFF9EA3AE),
                            lineHeight = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (scoutLevel < 3) {
                            val upgradeCost = if (scoutLevel == 1) 2_500_000L else 5_000_000L
                            Button(
                                onClick = onUpgradeScout,
                                enabled = budget >= upgradeCost,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FAF)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "ترقية الكشاف ($${upgradeCost / 1_000_000}M) ⬆️",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F2D1F), RoundedCornerShape(8.dp))
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "وصل كشّافك للرتبة العالمية القصوى! 👑",
                                    color = Color(0xFF81C784),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 2. Scout Mission Dispatch Card (World Soccer Champs style)
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161C24)),
                    border = BorderStroke(1.dp, Color(0xFF38495C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "إرسال بعثة تنقيب ميداني ✈️",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD180)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        if (career.scoutingMissionActive) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF2E1A05), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "جاري ترحال الكشافة للتفتيش عن مركز: [${career.scoutingMissionPosition}]...\nسيعودون بتقرير وافٍ الأسبوع القادم! ⏳",
                                    color = Color(0xFFFFB74D),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 14.sp
                                )
                            }
                        } else {
                            Text(
                                text = "اختر المركز الذي تبحث عنه، كلفة البعثة $200k وتستغرق أسبوعاً تكتيكياً واحداً لتكتمل:",
                                fontSize = 11.sp,
                                color = Color(0xFF9EA3AE),
                                lineHeight = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Position choices chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val positions = listOf("ANY" to "الكل", "ATT" to "هجوم", "MID" to "وسط", "DEF" to "دفاع")
                                positions.forEach { (posCode, label) ->
                                    val isSelected = selectedPos == posCode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isSelected) Color(0xFFFFA726) else Color(0xFF2C323D),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) Color.White else Color.Transparent,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .clickable { selectedPos = posCode }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onLaunchMission(selectedPos) },
                                enabled = budget >= 200_000L,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "إرسال الكشافة ($200k) ✈️",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Title for Scouted Wonderkids list
        item {
            Text(
                text = "جواهر مكتشفة جاهزة للتفاوض والتوقيع 💎 | Scout Discoveries",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (discoveries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D2027)),
                    border = BorderStroke(1.dp, Color(0xFF38495C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد مواهب مستكشفة حالياً 📭\nقم بإرسال كشافك الكروي لينقّب ثمار الملاعب ثم تقدّم للأسبوع القادم بالدوري لرؤية تقاريرهم الاستقصائية هنا!",
                            color = Color(0xFF9EA3AE),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            items(discoveries.size) { index ->
                val p = discoveries[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121820)),
                    border = BorderStroke(1.dp, Color(0xFF81C784)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .background(Color(0xFF0F3D24), CircleShape)
                                    .border(1.dp, Color(0xFF81C784), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p.position,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF81C784)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = p.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "العمر: ${p.age} سنة | السرعة: ${p.speed} | التسديد: ${p.shooting}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9EA3AE)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Talent overall rating star
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF3E2723), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "OVR",
                                        tint = Color(0xFFFDD835),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${p.rating} OVR",
                                        color = Color(0xFFFDD835),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "قيمة التوقيع",
                                    fontSize = 10.sp,
                                    color = Color(0xFFC2C7CF)
                                )
                                Text(
                                    text = "$${p.value / 1_000_000f}M",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF81C784)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { onSignProdigy(p) },
                                    enabled = budget >= p.value,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("شراء وتوقيع ✍️", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AcademyScreen(
    career: CareerEntity,
    onUpgradeAcademy: () -> Unit,
    onPromoteProdigy: () -> Unit
) {
    val level = career.academyLevel
    val budget = career.budget

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Academy Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF211D2A)),
                border = BorderStroke(1.dp, Color(0xFF5B3E7A)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "أكاديمية النادي للشباب 🎓 | Youth Football Academy",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "صناعة الجيل الكروي الجديد والموهوب! تعاقد مع واعدين وصغار السن (16 سنة) بترقيات عالية لإدراج صفقات مجانية خارقة للنادي.",
                        fontSize = 12.sp,
                        color = Color(0xFFD3C5E3)
                    )
                }
            }
        }

        // Two Column Upgrade vs Produce Star Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Upgrade Facilities Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF191421)),
                    border = BorderStroke(1.dp, Color(0xFF50346B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "مستوى منشآت الأكاديمية: ليفل ${level}/5 🏫",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE1BEE7)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "بترقية منشآت البراعم، يتخرج النجوم الجدد بتقييم عام (OVR) يتراوح من 75 إلى 85 ليفيدوا خطط فريقك مباشرة أو يتم بيعه بملايين فلكية في السوق.",
                            fontSize = 11.sp,
                            color = Color(0xFFBCAAA4),
                            lineHeight = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (level < 5) {
                            val upgradeCost = when (level) {
                                1 -> 1_500_000L
                                2 -> 3_000_000L
                                3 -> 6_000_000L
                                4 -> 12_000_000L
                                else -> 0L
                            }
                            Button(
                                onClick = onUpgradeAcademy,
                                enabled = budget >= upgradeCost,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "ترقية مرافق الأكاديمية ($${upgradeCost / 1_000_000}M) 🏫",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF2E004B), RoundedCornerShape(8.dp))
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "وصلت أكاديميتك لأفضل ليفل عالمي! 🏆",
                                    color = Color(0xFFE040FB),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 2. Promote Graduate Prodigy Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF191421)),
                    border = BorderStroke(1.dp, Color(0xFF50346B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "تصعيد برعم كروي جديد 🎓",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFF59D)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "قم بالتصعيد والتعاقد الفوري مع الموهوب الخارق القادم بعمر 16/17 سنة. كلفة استثمار الأكاديمية للتصفية $250k فقط!",
                            fontSize = 11.sp,
                            color = Color(0xFFBCAAA4),
                            lineHeight = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = onPromoteProdigy,
                            enabled = budget >= 250_000L,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBC02D)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "تصعيد وتوقيع برعم ($250k) ✍️",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
        
        // Quality advisory table based on level
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0E12)),
                border = BorderStroke(1.dp, Color(0xFF38495C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "ℹ️ معلومات خريجي الأكاديمية تكتيكياً:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    listOf(
                        "ليفل 1: براعم بجودة من 52 - 58 OVR (رسوم منخفضة)",
                        "ليفل 2: براعم بجودة من 58 - 64 OVR",
                        "ليفل 3: براعم بجودة من 64 - 70 OVR (واعد جداً)",
                        "ليفل 4: مواهب بجودة من 70 - 76 OVR",
                        "ليفل 5: نجوم عالمية بجودة من 76 - 85 OVR (فخر الأكاديميات)"
                    ).forEachIndexed { index, description ->
                        val isCurrent = level == (index + 1)
                        Text(
                            text = if (isCurrent) "👉 $description (المستوى الحالي)" else "⚪ $description",
                            fontSize = 11.sp,
                            color = if (isCurrent) Color(0xFFE040FB) else Color(0xFF9EA3AE),
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
