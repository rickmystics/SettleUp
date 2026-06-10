package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendLedgerDetailsScreen(
    friendId: Int,
    viewModel: SettleUpViewModel,
    onBack: () -> Unit,
    onQuickSettle: () -> Unit
) {
    val friend by viewModel.selectedFriend.collectAsState()
    val transactions by viewModel.selectedFriendTransactions.collectAsState()
    val friendBalances by viewModel.friendBalances.collectAsState()

    val balance = friendBalances[friendId] ?: 0.0
    val context = LocalContext.current

    // Tone reminder selector states
    var showReminderPanel by remember { mutableStateOf(false) }
    var selectedTone by remember { mutableStateOf("Friendly") } // Friendly, Professional, Playful

    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(friend?.name ?: "Friend Ledger", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(friend?.phone ?: "", fontSize = 11.sp, color = TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            friend?.let {
                                viewModel.deleteFriend(it)
                                onBack()
                                Toast.makeText(context, "Friend Deleted", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Friend", tint = SettleRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandDarkBackground)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BrandDarkBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Friend Balance Status Card (Professional Polish - Lavender 28.dp curved card)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BrandNetBalanceCardBg),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrandNetBalanceCardBorder, RoundedCornerShape(28.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "CURRENT BALANCE",
                            fontSize = 11.sp,
                            color = BrandTertiary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        
                        val balColor = when {
                            balance > 0 -> SettleGreen
                            balance < 0 -> SettleRed
                            else -> SettleGrey
                        }
                        val balText = when {
                            balance > 0 -> "${friend?.nickname.orEmpty().ifBlank { friend?.name.orEmpty() }} Owes You"
                            balance < 0 -> "You Owe ${friend?.nickname.orEmpty().ifBlank { friend?.name.orEmpty() }}"
                            else -> "All Settled Up"
                        }
                        
                        Text(
                            "₹ ${String.format("%.2f", Math.abs(balance))}",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Light,
                            color = BrandTertiary,
                            modifier = Modifier.padding(top = 6.dp, bottom = 6.dp)
                        )
                        Text(
                            balText.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = balColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (balance > 0) SettleGreenBg else if (balance < 0) SettleRedBg else BrandCardSurfaceElevated)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )

                        // Quick action buttons (Professional Polish Rounded shapes & high-contrast labels)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onQuickSettle,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f).height(48.dp).testTag("friend_settle_up_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Settle dues", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            if (balance > 0) {
                                Button(
                                    onClick = { showReminderPanel = !showReminderPanel },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = BrandPrimary),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .border(1.dp, BrandPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                ) {
                                    Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Nudge friend", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 2. Reminder Engine Panel
            if (showReminderPanel && balance > 0) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BrandCardSurface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, BrandPrimary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("FRICTIONLESS NUDGE GENERATOR", fontSize = 11.sp, color = BrandPrimary, fontWeight = FontWeight.Bold)
                            
                            // Tone selecting chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val tones = listOf("Friendly", "Professional", "Playful")
                                tones.forEach { t ->
                                    val isSel = selectedTone == t
                                    ElevatedFilterChip(
                                        selected = isSel,
                                        onClick = { selectedTone = t },
                                        label = { Text(t, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.elevatedFilterChipColors(
                                            selectedContainerColor = BrandPrimary,
                                            selectedLabelColor = Color.White,
                                            containerColor = BrandCardSurfaceElevated,
                                            labelColor = TextSecondary
                                        )
                                    )
                                }
                            }

                            // Generate message
                            val noteTitle = transactions.find { it.type == "lent" || it.type == "split" }?.note ?: "our expenses"
                            val reminderText = when (selectedTone) {
                                "Friendly" -> "Hey ${friend?.nickname ?: friend?.name}! Just a quick reminder 😄 ₹${String.format("%.0f", balance)} is still pending."
                                "Professional" -> "Reminder: ₹${String.format("%.0f", balance)} is pending from our partition for \"$noteTitle\"."
                                "Playful" -> "My pockets are screaming: my wallet misses its ₹${String.format("%.0f", balance)} 🥲 Settle up?"
                                else -> ""
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BrandCardSurfaceElevated, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                                    .border(1.dp, BrandCardSurfaceElevated, RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    reminderText,
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Copy or Send triggers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("SettleUp", reminderText)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = BrandPrimary)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy Text", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        try {
                                            val number = friend?.phone ?: ""
                                            val cleanNumber = number.replace("+", "").replace(" ", "")
                                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=${URLEncoder.encode(reminderText, "UTF-8")}")
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            context.startActivity(intent)
                                            viewModel.addActivityLog(
                                                title = "Sent Nudge",
                                                content = "WhatsApp reminder triggered for ${friend?.name}."
                                            )
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "WhatsApp is not installed on this device", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SettleGreen, contentColor = Color.White),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.SendToMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Send on WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Transactions List
            item {
                Text(
                    "TRANSACTION LEDGER HISTORY",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(BrandCardSurface, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No records with this friend yet.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                items(transactions) { tx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BrandCardSurface, RoundedCornerShape(14.dp))
                            .border(1.dp, BrandCardSurfaceElevated, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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
                                Text(
                                    tx.note,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    sdf.format(Date(tx.date)),
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                val txColor = when (tx.type) {
                                    "lent" -> SettleGreen
                                    "borrowed" -> SettleRed
                                    "settlement" -> if (tx.payerId == 0) SettleGreen else SettleRed
                                    else -> if (tx.payerId == 0) SettleGreen else SettleRed
                                }
                                val symbolSign = when (tx.type) {
                                    "lent" -> "+"
                                    "borrowed" -> "-"
                                    "settlement" -> if (tx.payerId == 0) "+" else "-"
                                    else -> if (tx.payerId == 0) "+" else "-"
                                }

                                Text(
                                    "$symbolSign₹ ${String.format("%.1f", tx.amount)}",
                                    color = txColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                                
                                Text(
                                    when (tx.type) {
                                        "lent" -> "Lent"
                                        "borrowed" -> "Borrowed"
                                        "settlement" -> "Settle Up"
                                        else -> "Group Split"
                                    },
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.deleteTransaction(tx) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Delete Tx", tint = TextMuted, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    groupId: Int,
    viewModel: SettleUpViewModel,
    onBack: () -> Unit
) {
    val group by viewModel.selectedGroup.collectAsState()
    val transactions by viewModel.selectedGroupTransactions.collectAsState()
    val members by viewModel.selectedGroupMembers.collectAsState()
    val memberBalances by viewModel.selectedGroupBalances.collectAsState()
    val smartSettlements by viewModel.selectedGroupSmartSettlements.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Transactions, 1: Balances & Engine
    val context = LocalContext.current
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(group?.name ?: "Group Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${members.size + 1} members added", fontSize = 11.sp, color = TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            group?.let {
                                viewModel.deleteGroup(it)
                                onBack()
                                Toast.makeText(context, "Group Ledger Deleted", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Group", tint = SettleRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandDarkBackground)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BrandDarkBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            
            // 1. Group info card
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandCardSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, BrandCardSurfaceElevated, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        group?.description?.takeIf { it.isNotBlank() } ?: "Shared splits ledger tracker",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab bar
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = Color.Transparent,
                        contentColor = BrandPrimary,
                        divider = {},
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                                color = BrandPrimary
                            )
                        }
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = { Text("Bills Ledger", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            selectedContentColor = BrandPrimary,
                            unselectedContentColor = TextSecondary
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = { Text("Smart Settlements", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            selectedContentColor = BrandPrimary,
                            unselectedContentColor = TextSecondary
                        )
                    }
                }
            }

            // 2. Active view contents
            if (activeTab == 0) {
                // Group Bills Tab
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CHRONOLOGICAL EXPENSES", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                }

                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(BrandCardSurface, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No group splits recorded yet.", color = TextSecondary, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(transactions) { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BrandCardSurface, RoundedCornerShape(14.dp))
                                    .border(1.dp, BrandCardSurfaceElevated, RoundedCornerShape(14.dp))
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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
                                        val splitsCount = viewModel.parseSplits(tx.splits).size
                                        val payerName = if (tx.payerId == 0) "You" else members.find { it.id == tx.payerId }?.name ?: "Member"
                                        Text("Paid by $payerName · split with $splitsCount", color = TextSecondary, fontSize = 10.sp)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("₹ ${String.format("%.1f", tx.amount)}", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                        Text(sdf.format(Date(tx.date)), fontSize = 9.sp, color = TextSecondary)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { viewModel.deleteTransaction(tx) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Smart Settlements Engine Tab
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Balances Table
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("GROUP MEMBER CREDITS/DEBITS", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BrandCardSurface),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().border(1.dp, BrandCardSurfaceElevated, RoundedCornerShape(14.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    
                                    // 1. Render Current User balance
                                    val selfBal = memberBalances[0] ?: 0.0
                                    val selfColor = if (selfBal >= 0) SettleGreen else SettleRed
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("You (Profile Payer)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            "${if (selfBal > 0) "+" else ""}₹ ${String.format("%.2f", selfBal)}",
                                            color = selfColor,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    
                                    // 2. Render other members
                                    members.forEach { m ->
                                        val mBal = memberBalances[m.id] ?: 0.0
                                        val mColor = if (mBal >= 0) SettleGreen else SettleRed
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(m.name, color = TextPrimary, fontSize = 13.sp)
                                            Text(
                                                "${if (mBal > 0) "+" else ""}₹ ${String.format("%.2f", mBal)}",
                                                color = mColor,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Simplifications suggetions
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("DEBT SIMPLIFICATIONS (ENGINE OUTPUT)", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                if (smartSettlements.size > 1) {
                                    Box(
                                        modifier = Modifier
                                            .background(SettleGreenBg, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Calculated Minimizations!", color = SettleGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (smartSettlements.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .background(BrandCardSurface, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("All member balances are perfectly simplified!", color = SettleGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    smartSettlements.forEach { suggestion ->
                                        val fromName = if (suggestion.fromMemberId == 0) "You" else members.find { it.id == suggestion.fromMemberId }?.name ?: "Member"
                                        val toName = if (suggestion.toMemberId == 0) "You" else members.find { it.id == suggestion.toMemberId }?.name ?: "Member"

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(BrandCardSurface, RoundedCornerShape(12.dp))
                                                .border(1.dp, BrandCardSurfaceElevated, RoundedCornerShape(12.dp))
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(fromName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp).padding(horizontal = 2.dp))
                                                    Text(toName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                }
                                                Text("Transfer simplification suggest", color = TextSecondary, fontSize = 10.sp)
                                            }
                                            
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text("₹ ${String.format("%.0f", suggestion.amount)}", color = BrandPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(100.dp))
                                                        .background(BrandPrimary)
                                                        .clickable {
                                                            // One-tap split settlement recorder!
                                                            viewModel.addTransaction(
                                                                friendId = if (suggestion.fromMemberId != 0) suggestion.fromMemberId else suggestion.toMemberId,
                                                                groupId = groupId,
                                                                payerId = suggestion.fromMemberId, // Who paid (debtor pays creditor)
                                                                amount = suggestion.amount,
                                                                type = "settlement",
                                                                note = "Group Simplification Rec",
                                                                category = "Other",
                                                                splits = mapOf(
                                                                    suggestion.fromMemberId to suggestion.amount,
                                                                    suggestion.toMemberId to -suggestion.amount
                                                                )
                                                            )
                                                            Toast.makeText(context, "Dues Settled!", Toast.LENGTH_SHORT).show()
                                                        }
                                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                                ) {
                                                    Text("Pay", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
        }
    }
}

@Composable
fun QuickSettleDialog(
    friend: Friend,
    balance: Double,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var amountStr by remember { mutableStateOf(String.format("%.0f", Math.abs(balance))) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Settle up Ledger Dues",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "You are recording a full or partial settlement with ${friend.name}.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Settlement Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = BrandSecondary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("settle_amount_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onConfirm(amt)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White),
                modifier = Modifier.testTag("confirm_settlement_button")
            ) {
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
            ) {
                Text("Cancel")
            }
        },
        containerColor = BrandCardSurface,
        shape = RoundedCornerShape(16.dp)
    )
}
