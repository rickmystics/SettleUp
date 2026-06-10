package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

// Screen switcher state holder
sealed interface SettleUpScreen {
    object Dashboard : SettleUpScreen
    data class FriendLedger(val friendId: Int) : SettleUpScreen
    data class GroupDetails(val groupId: Int) : SettleUpScreen
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleUpMainApp(viewModel: SettleUpViewModel) {
    var activeScreen by remember { mutableStateOf<SettleUpScreen>(SettleUpScreen.Dashboard) }

    // Dialog sheets state
    var showAddFriend by remember { mutableStateOf(false) }
    var showCreateGroup by remember { mutableStateOf(false) }
    var showAddTransaction by remember { mutableStateOf(false) }
    var showQuickSettleFriendId by remember { mutableStateOf<Int?>(null) }

    val friends by viewModel.allFriends.collectAsState()
    val groups by viewModel.allGroups.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BrandDarkBackground)) {
        // High fidelity background glow (Professional Polish soft purple gradient)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BrandNetBalanceCardBg.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )

        // Cross-fade window swapping
        AnimatedContent(
            targetState = activeScreen,
            transitionSpec = {
                fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
            },
            label = "screen_navigation"
        ) { screen ->
            when (screen) {
                is SettleUpScreen.Dashboard -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToFriend = { fId ->
                            viewModel.selectFriend(fId)
                            activeScreen = SettleUpScreen.FriendLedger(fId)
                        },
                        onNavigateToGroup = { gId ->
                            viewModel.selectGroup(gId)
                            activeScreen = SettleUpScreen.GroupDetails(gId)
                        },
                        onAddFriendClick = { showAddFriend = true },
                        onAddGroupClick = { showCreateGroup = true },
                        onAddTransactionClick = { showAddTransaction = true }
                    )
                }

                is SettleUpScreen.FriendLedger -> {
                    FriendLedgerDetailsScreen(
                        friendId = screen.friendId,
                        viewModel = viewModel,
                        onBack = {
                            viewModel.selectFriend(null)
                            activeScreen = SettleUpScreen.Dashboard
                        },
                        onQuickSettle = { showQuickSettleFriendId = screen.friendId }
                    )
                }

                is SettleUpScreen.GroupDetails -> {
                    GroupDetailsScreen(
                        groupId = screen.groupId,
                        viewModel = viewModel,
                        onBack = {
                            viewModel.selectGroup(null)
                            activeScreen = SettleUpScreen.Dashboard
                        }
                    )
                }
            }
        }

        // --- Dialogs ---
        if (showAddFriend) {
            AddFriendDialog(
                onDismiss = { showAddFriend = false },
                onConfirm = { name, phone, upi, nickname ->
                    viewModel.addFriend(name, phone, upi, nickname)
                    showAddFriend = false
                }
            )
        }

        if (showCreateGroup) {
            CreateGroupDialog(
                friends = friends,
                onDismiss = { showCreateGroup = false },
                onConfirm = { name, desc, selectedIds ->
                    viewModel.createGroup(name, desc, selectedIds)
                    showCreateGroup = false
                }
            )
        }

        if (showAddTransaction) {
            AddTransactionDialog(
                friends = friends,
                groups = groups,
                onDismiss = { showAddTransaction = false },
                onConfirm = { fId, gId, pId, amt, type, note, cat, splits ->
                    viewModel.addTransaction(fId, gId, pId, amt, type, note, cat, splits)
                    showAddTransaction = false
                }
            )
        }

        if (showQuickSettleFriendId != null) {
            val fId = showQuickSettleFriendId!!
            val fObj = friends.find { it.id == fId }
            fObj?.let { friend ->
                val fBal = viewModel.friendBalances.collectAsState().value[fId] ?: 0.0
                QuickSettleDialog(
                    friend = friend,
                    balance = fBal,
                    onDismiss = { showQuickSettleFriendId = null },
                    onConfirm = { amt ->
                        // Settle up: if fBal is positive, friend owes we. They repay we.
                        // If fBal is negative, we owe friend. We repay them (we are payer, 0).
                        val payer = if (fBal > 0) fId else 0
                        viewModel.addTransaction(
                            friendId = fId,
                            groupId = null,
                            payerId = payer,
                            amount = amt,
                            type = "settlement",
                            note = "Settle Ledger Dues",
                            category = "Other",
                            splits = emptyMap()
                        )
                        showQuickSettleFriendId = null
                    }
                )
            }
        }
    }
}

// Global Category Icon lookup helper
fun getCategoryIcon(category: String) = when (category) {
    "Food" -> Icons.Outlined.Fastfood
    "Travel" -> Icons.Outlined.DirectionsCar
    "Rent" -> Icons.Outlined.Home
    "Entertainment" -> Icons.Outlined.LocalActivity
    "Shopping" -> Icons.Outlined.ShoppingBag
    "Work" -> Icons.Outlined.Work
    else -> Icons.Outlined.Paid
}

@Composable
fun DashboardScreen(
    viewModel: SettleUpViewModel,
    onNavigateToFriend: (Int) -> Unit,
    onNavigateToGroup: (Int) -> Unit,
    onAddFriendClick: () -> Unit,
    onAddGroupClick: () -> Unit,
    onAddTransactionClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Home, 1: Friends, 2: Groups, 3: Activity, 4: Profile

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = Color.Transparent, // Transparent so background gradient glows
        bottomBar = {
            NavigationBar(
                containerColor = BrandCardSurface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                val tabs = listOf(
                    Triple("Home", Icons.Default.Home, Icons.Outlined.Home),
                    Triple("Friends", Icons.Default.Person, Icons.Outlined.Person),
                    Triple("Groups", Icons.Default.Groups, Icons.Outlined.Groups),
                    Triple("Activity", Icons.Default.Notifications, Icons.Outlined.Notifications),
                    Triple("Profile", Icons.Default.AccountCircle, Icons.Outlined.AccountCircle)
                )
                tabs.forEachIndexed { index, (label, filledIcon, emptyIcon) ->
                    val selected = selectedTab == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedTab = index },
                        icon = { Icon(if (selected) filledIcon else emptyIcon, contentDescription = label) },
                        label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandTertiary,
                            selectedTextColor = BrandTertiary,
                            indicatorColor = BrandNetBalanceCardBg,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = BrandNetBalanceCardBorder,
                contentColor = BrandTertiary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add transaction", modifier = Modifier.size(24.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen switcher representing Tabs
            Crossfade(targetState = selectedTab, label = "tabs_crossfade") { tab ->
                when (tab) {
                    0 -> HomeTab(viewModel, onNavigateToFriend, onAddTransactionClick)
                    1 -> FriendsTab(viewModel, onNavigateToFriend, onAddFriendClick)
                    2 -> GroupsTab(viewModel, onNavigateToGroup, onAddGroupClick)
                    3 -> ActivityTab(viewModel)
                    4 -> ProfileTab(viewModel)
                }
            }
        }
    }
}

// ----------------------------------------------------
// TABS IMPLEMENTATION
// ----------------------------------------------------

@Composable
fun HomeTab(
    viewModel: SettleUpViewModel,
    onNavigateToFriend: (Int) -> Unit,
    onAddTransactionClick: () -> Unit
) {
    val netSummary by viewModel.netSummary.collectAsState()
    val friendBalances by viewModel.friendBalances.collectAsState()
    val friends by viewModel.allFriends.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    val initials = remember(profile) {
        val name = profile?.name?.trim() ?: "AS"
        val parts = name.split(" ")
        if (parts.size > 1 && parts[0].isNotEmpty() && parts[1].isNotEmpty()) {
            "${parts[0][0]}${parts[1][0]}".uppercase()
        } else {
            name.take(2).uppercase()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Launcher Title Header (Professional Polish)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "SettleUp",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text("Zero awkward conversations", fontSize = 13.sp, color = TextSecondary)
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BrandNetBalanceCardBg)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initials,
                        color = BrandTertiary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // 1. Net Balance Summary Card (Professional Polish - Lavender 28.dp curved card)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandNetBalanceCardBg),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BrandNetBalanceCardBorder, RoundedCornerShape(28.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "TOTAL NET BALANCE",
                            color = BrandTertiary.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        
                        val balanceBadgeText = if (netSummary.netBalance >= 0) "+ ₹${String.format("%.0f", netSummary.netBalance)}" else "- ₹${String.format("%.0f", Math.abs(netSummary.netBalance))}"
                        Box(
                            modifier = Modifier
                                .background(BrandTertiary, RoundedCornerShape(100.dp))
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text(
                                balanceBadgeText,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Text(
                        "₹${String.format("%.2f", netSummary.netBalance)}",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Light,
                        color = BrandTertiary,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "YOU ARE OWED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SettleGreen.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "₹${String.format("%.0f", netSummary.totalToReceive)}",
                                color = SettleGreen,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp)
                                .align(Alignment.CenterVertically)
                                .background(BrandTertiary.copy(alpha = 0.1f))
                        )
                        
                        Column {
                            Text(
                                "YOU OWE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SettleRed.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "₹${String.format("%.0f", netSummary.totalOwed)}",
                                color = SettleRed,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Friends carousel
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("FRIENDS LEDGER SUMMARY", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    Text("See list", fontSize = 11.sp, color = BrandPrimary, modifier = Modifier.clickable { /* Tab friends */ })
                }
                
                if (friends.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(BrandCardSurface, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No friends yet. Head to the Friends tab!", color = TextSecondary, fontSize = 12.sp)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        friends.take(4).forEach { friend ->
                            val bal = friendBalances[friend.id] ?: 0.0
                            val (balText, balColor) = when {
                                bal > 0 -> "Owes you v" to SettleGreen
                                bal < 0 -> "You owe v" to SettleRed
                                else -> "Settled Up" to SettleGrey
                            }

                            val avatarColors = remember(friend.id) {
                                val list = listOf(
                                    Color(0xFFFFD8E4) to Color(0xFF31111D),
                                    Color(0xFFC2E7FF) to Color(0xFF001D35),
                                    Color(0xFFD1E1FF) to Color(0xFF001B3D),
                                    Color(0xFFEADDFF) to Color(0xFF21005D),
                                    Color(0xFFE8F5E9) to Color(0xFF1B5E20)
                                )
                                list[Math.abs(friend.id) % list.size]
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(BrandCardSurface)
                                    .clickable { onNavigateToFriend(friend.id) }
                                    .border(1.dp, BrandCardSurfaceElevated, RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(avatarColors.first),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            friend.nickname.takeIf { it.isNotBlank() } ?: friend.name.take(2).uppercase(),
                                            color = avatarColors.second,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(friend.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(friend.phone, color = TextSecondary, fontSize = 11.sp)
                                    }
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "₹${String.format("%.0f", Math.abs(bal))}",
                                        color = if (bal == 0.0) SettleGrey else balColor,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val subtitleText = when {
                                        bal > 0 -> "OWES YOU"
                                        bal < 0 -> "YOU OWE"
                                        else -> "SETTLED"
                                    }
                                    Text(
                                        subtitleText,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Recent split entries
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("RECENT ENTRIES", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                
                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(BrandCardSurface, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No entries recorded yet. Tap '+'!", color = TextSecondary, fontSize = 12.sp)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        transactions.take(5).forEach { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BrandCardSurface, RoundedCornerShape(12.dp))
                                    .border(1.dp, BrandCardSurfaceElevated, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(BrandCardSurfaceElevated, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            getCategoryIcon(tx.category),
                                            contentDescription = null,
                                            tint = BrandPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.widthIn(max = 180.dp)) {
                                        Text(tx.note, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(tx.category, color = TextSecondary, fontSize = 11.sp)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₹ ${String.format("%.1f", tx.amount)}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        when (tx.type) {
                                            "lent" -> "Lent"
                                            "borrowed" -> "Borrowed"
                                            "settlement" -> "Settle Up"
                                            else -> "Group Split"
                                        },
                                        fontSize = 10.sp,
                                        color = BrandTertiary
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

@Composable
fun FriendsTab(
    viewModel: SettleUpViewModel,
    onNavigateToFriend: (Int) -> Unit,
    onAddFriendClick: () -> Unit
) {
    val friends by viewModel.allFriends.collectAsState()
    val friendBalances by viewModel.friendBalances.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val filteredFriends = friends.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.phone.contains(searchQuery) ||
        it.nickname.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Friends Book", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text("${friends.size} friends saved", fontSize = 11.sp, color = TextSecondary)
            }
            IconButton(
                onClick = onAddFriendClick,
                colors = IconButtonDefaults.iconButtonColors(containerColor = BrandPrimary, contentColor = Color.White)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Friend")
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search friends by name or phone...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = BrandPrimary,
                unfocusedBorderColor = BrandSecondary
            ),
            modifier = Modifier.fillMaxWidth().testTag("friends_search_input")
        )

        if (filteredFriends.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.PeopleOutline, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Text("No friends found", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredFriends) { friend ->
                    val bal = friendBalances[friend.id] ?: 0.0
                    val (balText, balColor) = when {
                        bal > 0 -> "Owes you v" to SettleGreen
                        bal < 0 -> "You owe v" to SettleRed
                        else -> "Settled Up" to SettleGrey
                    }

                    val avatarColors = remember(friend.id) {
                        val list = listOf(
                            Color(0xFFFFD8E4) to Color(0xFF31111D),
                            Color(0xFFC2E7FF) to Color(0xFF001D35),
                            Color(0xFFD1E1FF) to Color(0xFF001B3D),
                            Color(0xFFEADDFF) to Color(0xFF21005D),
                            Color(0xFFE8F5E9) to Color(0xFF1B5E20)
                        )
                        list[Math.abs(friend.id) % list.size]
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(BrandCardSurface)
                            .clickable { onNavigateToFriend(friend.id) }
                            .border(1.dp, BrandCardSurfaceElevated, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(avatarColors.first),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    friend.nickname.takeIf { it.isNotBlank() } ?: friend.name.take(2).uppercase(),
                                    color = avatarColors.second,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(friend.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(friend.phone, color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "₹${String.format("%.0f", Math.abs(bal))}",
                                color = if (bal == 0.0) SettleGrey else balColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val subtitleLabelText = when {
                                bal > 0 -> "OWES YOU"
                                bal < 0 -> "YOU OWE"
                                else -> "SETTLED"
                            }
                            Text(
                                subtitleLabelText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            if (friend.upiId.isNotBlank()) {
                                Text(friend.upiId, fontSize = 9.sp, color = TextMuted, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupsTab(
    viewModel: SettleUpViewModel,
    onNavigateToGroup: (Int) -> Unit,
    onAddGroupClick: () -> Unit
) {
    val groups by viewModel.allGroups.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Groups Ledger", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text("${groups.size} active shared groups", fontSize = 11.sp, color = TextSecondary)
            }
            Button(
                onClick = onAddGroupClick,
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Group", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (groups.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Groups, contentDescription = null, tint = TextMuted, modifier = Modifier.size(56.dp))
                    Text("No shared groups yet", color = TextSecondary, fontSize = 14.sp)
                    Text("Create a group to split trip expenses equally!", color = TextMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(groups) { group ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(BrandCardSurface)
                            .border(1.dp, BrandCardSurfaceElevated, RoundedCornerShape(14.dp))
                            .clickable { onNavigateToGroup(group.id) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(BrandCardSurfaceElevated, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(group.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(group.description.takeIf { it.isNotBlank() } ?: "Shared splits ledger", color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(BrandCardSurfaceElevated, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Open", color = BrandPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityTab(viewModel: SettleUpViewModel) {
    val logs by viewModel.allActivityLogs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Dues Audit Activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text("${logs.size} activity notifications logged", fontSize = 11.sp, color = TextSecondary)
            }
            if (logs.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.clearAllLogs() },
                    colors = ButtonDefaults.textButtonColors(contentColor = SettleRed)
                ) {
                    Text("Clear Dues", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.NotificationsNone, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Text("Chronicle is clear", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BrandCardSurface, RoundedCornerShape(12.dp))
                            .border(1.dp, BrandCardSurfaceElevated, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(BrandCardSurfaceElevated, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(log.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(log.content, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileTab(viewModel: SettleUpViewModel) {
    val profile by viewModel.userProfile.collectAsState()

    var name by remember(profile) { mutableStateOf(profile?.name ?: "") }
    var phone by remember(profile) { mutableStateOf(profile?.phone ?: "") }
    var email by remember(profile) { mutableStateOf(profile?.email ?: "") }
    var upi by remember(profile) { mutableStateOf(profile?.upiId ?: "") }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "My Ledger Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandCardSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, BrandCardSurfaceElevated, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "EDIT CONTACT INFORMATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = BrandCardSurfaceElevated
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Identifier") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = BrandCardSurfaceElevated
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Handle") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = BrandCardSurfaceElevated
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = upi,
                        onValueChange = { upi = it },
                        label = { Text("Primary UPI ID") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = BrandCardSurfaceElevated
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.updateProfile(name, phone, email, upi)
                            Toast.makeText(context, "Profile Saved!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("save_profile_button")
                    ) {
                        Text("Save Configurations", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
