package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, upi: String, nickname: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var upi by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }

    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Friend",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (error.isNotEmpty()) {
                    Text(error, color = SettleRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = "" },
                    label = { Text("Friend's Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = BrandSecondary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_friend_name_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; error = "" },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    placeholder = { Text("9988776655") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = BrandSecondary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_friend_phone_input")
                )

                OutlinedTextField(
                    value = upi,
                    onValueChange = { upi = it },
                    label = { Text("UPI ID (Optional)") },
                    leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                    singleLine = true,
                    placeholder = { Text("friend@okaxis") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = BrandSecondary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_friend_upi_input")
                )

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname (Optional)") },
                    leadingIcon = { Icon(Icons.Default.Face, contentDescription = null) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = BrandSecondary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_friend_nickname_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) {
                        error = "Name and Phone cannot be empty!"
                    } else {
                        onConfirm(name, phone, upi, nickname)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White),
                modifier = Modifier.testTag("submit_friend_button")
            ) {
                Text("Add Friend")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupDialog(
    friends: List<Friend>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, selectedFriendIds: List<Int>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateListOf<Int>() }

    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Create Shared Group Ledger",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (error.isNotEmpty()) {
                    Text(error, color = SettleRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = "" },
                    label = { Text("Group Name") },
                    leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                    singleLine = true,
                    placeholder = { Text("e.g. Goa Trip 2026") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = BrandSecondary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("group_name_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                    singleLine = true,
                    placeholder = { Text("Groceries & expenses split") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = BrandSecondary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("group_desc_input")
                )

                Text(
                    "Include Friends:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (friends.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(BrandCardSurfaceElevated, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No friends joined yet. Add some first!", color = TextSecondary, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(BrandCardSurfaceElevated, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        items(friends) { friend ->
                            val isChecked = selectedIds.contains(friend.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) selectedIds.remove(friend.id)
                                        else selectedIds.add(friend.id)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { check ->
                                        if (check == true && !isChecked) selectedIds.add(friend.id)
                                        else if (check == false) selectedIds.remove(friend.id)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = BrandPrimary,
                                        uncheckedColor = TextSecondary,
                                        checkmarkColor = Color.Black
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(friend.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text(friend.phone, color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Group Name cannot be empty!"
                    } else if (selectedIds.isEmpty()) {
                        error = "Please include at least 1 friend!"
                    } else {
                        onConfirm(name, description, selectedIds.toList())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White),
                modifier = Modifier.testTag("submit_group_button")
            ) {
                Text("Create Group")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    friends: List<Friend>,
    groups: List<ExpenseGroup>,
    onDismiss: () -> Unit,
    onConfirm: (
        friendId: Int?,
        groupId: Int?,
        payerId: Int, // 0 for self, or Friend ID
        amount: Double,
        type: String, // "lent" | "borrowed" | "split" | "settlement"
        note: String,
        category: String,
        splits: Map<Int, Double>
    ) -> Unit
) {
    var type by remember { mutableStateOf("lent") } // lent, borrowed, split, settlement
    var friendId by remember { mutableStateOf<Int?>(null) }
    var groupId by remember { mutableStateOf<Int?>(null) }
    var payerId by remember { mutableStateOf(0) } // Default: Self (0) paid

    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }

    // Split parameters
    var splitMethod by remember { mutableStateOf("Equal") } // Equal, Exact, Percentage
    val exactSplits = remember { mutableStateMapOf<Int, String>() }
    val percentSplits = remember { mutableStateMapOf<Int, String>() }

    var error by remember { mutableStateOf("") }

    val fid = friendId
    val gid = groupId

    // Dynamic members selection list based on group or friend selected
    val activeMembersList: List<Int> = remember(gid, fid) {
        if (gid != null) {
            val list = mutableListOf<Int>(0)
            friends.map { it.id }.forEach { list.add(it) }
            list.toList()
        } else if (fid != null) {
            listOf(0, fid)
        } else {
            listOf(0)
        }
    }

    val categories = listOf("Food", "Travel", "Rent", "Entertainment", "Shopping", "Work", "Other")

    // Helper: auto-calculate splits based on total amount
    val calculatedSplits: Map<Int, Double> = remember(amountStr, type, splitMethod, activeMembersList, exactSplits, percentSplits, fid, gid) {
        val total = amountStr.toDoubleOrNull() ?: 0.0
        val results = mutableMapOf<Int, Double>()
        if (total <= 0.0) return@remember emptyMap()

        if (type == "lent") {
            if (fid != null) {
                results[fid] = total
            }
        } else if (type == "borrowed") {
            results[0] = total
        } else if (type == "settlement") {
            if (payerId == 0) {
                if (fid != null) {
                    results[fid] = total
                }
            } else {
                results[0] = total
            }
        } else {
            val members = if (gid != null) {
                activeMembersList
            } else if (fid != null) {
                listOf(0, fid)
            } else {
                listOf(0)
            }

            when (splitMethod) {
                "Equal" -> {
                    val share = total / members.size
                    for (m in members) {
                        results[m] = share
                    }
                }
                "Exact" -> {
                    for (m in members) {
                        results[m] = exactSplits[m]?.toDoubleOrNull() ?: 0.0
                    }
                }
                "Percentage" -> {
                    for (m in members) {
                        val pct = percentSplits[m]?.toDoubleOrNull() ?: 0.0
                        results[m] = total * (pct / 100.0)
                    }
                }
            }
        }
        results
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandDarkBackground),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Add New Expense / Ledger Entry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (error.isNotEmpty()) {
                    item {
                        Text(error, color = SettleRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // 1. Transaction Type Toggle
                item {
                    val types = listOf("I Lent" to "lent", "I Borrowed" to "borrowed", "Split Bill" to "split", "Settle Up" to "settlement")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BrandCardSurfaceElevated, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        types.forEach { (label, value) ->
                            val selected = type == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) BrandPrimary else Color.Transparent)
                                    .clickable {
                                        type = value
                                        error = ""
                                        // Auto-configure default payers and splits
                                        if (value == "lent" || value == "settlement") {
                                            payerId = 0
                                        } else if (value == "borrowed") {
                                            val currentFid = friendId
                                            if (currentFid != null) {
                                                payerId = currentFid
                                            }
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    color = if (selected) Color.White else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // 2. Select context (Who did you transact with?)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Associate Entry With:", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        
                        if (type == "split") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { groupId = null; error = "" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (groupId == null) BrandPrimary else BrandCardSurfaceElevated,
                                        contentColor = if (groupId == null) Color.White else TextPrimary
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("A Friend", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = {
                                        if (groups.isNotEmpty()) {
                                            groupId = groups.first().id
                                            friendId = null
                                            error = ""
                                        } else {
                                            error = "Create a Group first!"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (groupId != null) BrandPrimary else BrandCardSurfaceElevated,
                                        contentColor = if (groupId != null) Color.White else TextPrimary
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("A Group", fontSize = 12.sp)
                                }
                            }
                        }

                        // Render relevant Selectors:
                        if (groupId != null && type == "split") {
                            Text("Select Group:", fontSize = 11.sp, color = TextSecondary)
                            ScrollableRowSelector(
                                items = groups,
                                selectedId = groupId,
                                labelMapper = { it.name },
                                onSelected = { groupId = it; error = "" }
                            )
                        } else {
                            Text("Primary Friend:", fontSize = 11.sp, color = TextSecondary)
                            ScrollableRowSelector(
                                items = friends,
                                selectedId = friendId,
                                labelMapper = { it.name },
                                onSelected = {
                                    friendId = it
                                    error = ""
                                    if (type == "borrowed") {
                                        payerId = it
                                    }
                                }
                            )
                        }
                    }
                }

                // 3. Amount Field
                item {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it; error = "" },
                        label = { Text("Amount (₹ INR)") },
                        leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = BrandPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = BrandSecondary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("tx_amount_input")
                    )
                }

                // 4. Note/Description Field
                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note / Short description") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        singleLine = true,
                        placeholder = { Text("e.g. Dinner splits, rent share") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = BrandSecondary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("tx_note_input")
                    )
                }

                // 5. Category Carousel
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Category:", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BrandCardSurfaceElevated, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            LazyColumn(modifier = Modifier.height(100.dp).fillMaxWidth()) {
                                items(categories.chunked(3)) { chunk ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        chunk.forEach { cat ->
                                            val isSel = category == cat
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                                    .padding(2.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSel) BrandPrimary else Color.Transparent)
                                                    .clickable { category = cat }
                                                    .padding(horizontal = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    cat,
                                                    fontSize = 11.sp,
                                                    color = if (isSel) Color.White else TextSecondary,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. Split & Payer controls for Splits
                if (type == "split") {
                    item {
                        Divider(color = BrandCardSurfaceElevated, modifier = Modifier.padding(vertical = 4.dp))
                        Text("Payer (Who paid?):", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        
                        val payersMap = mutableMapOf<Int, String>(0 to "You")
                        if (groupId != null) {
                            friends.forEach { payersMap[it.id] = it.name }
                        } else if (friendId != null) {
                            friends.find { it.id == friendId }?.let { payersMap[it.id] = it.name }
                        }

                        ScrollableKeyValueSelector(
                            items = payersMap,
                            selectedKey = payerId,
                            onSelected = { payerId = it }
                        )
                    }

                    item {
                        Text("Split Calculation Method:", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        val methods = listOf("Equal", "Exact", "Percentage")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BrandCardSurfaceElevated, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            methods.forEach { m ->
                                val selected = splitMethod == m
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) BrandPrimary else Color.Transparent)
                                        .clickable { splitMethod = m }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        m,
                                        color = if (selected) Color.White else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // Dynamically render splits user list & direct balance checks
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BrandCardSurfaceElevated, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text("Splits Preview / Breakdown:", fontSize = 11.sp, color = BrandTertiary, fontWeight = FontWeight.Bold)
                            
                            val listToIterate = if (groupId != null) {
                                activeMembersList
                            } else {
                                listOfNotNull(0, friendId)
                            }

                            listToIterate.forEach { mId ->
                                val mName = if (mId == 0) "Your Share" else friends.find { it.id == mId }?.name ?: "Friend $mId"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(mName, fontSize = 12.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                                    
                                    when (splitMethod) {
                                        "Equal" -> {
                                            val amt = calculatedSplits[mId] ?: 0.0
                                            Text("₹ ${String.format("%.2f", amt)}", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                        }
                                        "Exact" -> {
                                            OutlinedTextField(
                                                value = exactSplits[mId] ?: "",
                                                onValueChange = { exactSplits[mId] = it },
                                                placeholder = { Text("0.0") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = TextPrimary,
                                                    unfocusedTextColor = TextPrimary,
                                                    focusedBorderColor = BrandPrimary,
                                                    unfocusedBorderColor = BrandSecondary
                                                ),
                                                modifier = Modifier.width(100.dp).height(50.dp)
                                            )
                                        }
                                        "Percentage" -> {
                                            OutlinedTextField(
                                                value = percentSplits[mId] ?: "",
                                                onValueChange = { percentSplits[mId] = it },
                                                placeholder = { Text("%") },
                                                suffix = { Text("%") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = TextPrimary,
                                                    unfocusedTextColor = TextPrimary,
                                                    focusedBorderColor = BrandPrimary,
                                                    unfocusedBorderColor = BrandSecondary
                                                ),
                                                modifier = Modifier.width(100.dp).height(50.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 7. Save and Cancel actions
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val amtValue = amountStr.toDoubleOrNull() ?: 0.0
                                if (amtValue <= 0.0) {
                                    error = "Please enter a valid amount!"
                                } else if (type != "split" && friendId == null) {
                                    error = "Please select a friend!"
                                } else {
                                    // Verify splits sum for non-equal methods
                                    if (type == "split") {
                                        if (splitMethod == "Exact") {
                                            val sum = calculatedSplits.values.sum()
                                            if (Math.abs(sum - amtValue) > 0.05) {
                                                error = "Exact split amounts (cur: ₹$sum) must equal total (₹$amtValue)!"
                                                return@Button
                                            }
                                        } else if (splitMethod == "Percentage") {
                                            val totalPercent = activeMembersList.sumOf { percentSplits[it]?.toDoubleOrNull() ?: 0.0 }
                                            if (Math.abs(totalPercent - 100.0) > 0.01) {
                                                error = "Select splits percentage sum (cur: $totalPercent%) must equal 100%!"
                                                return@Button
                                            }
                                        }
                                    }
                                    onConfirm(
                                        friendId,
                                        groupId,
                                        payerId,
                                        amtValue,
                                        type,
                                        if (note.isBlank()) category else note,
                                        category,
                                        calculatedSplits
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White),
                            modifier = Modifier.weight(1f).testTag("submit_tx_button")
                        ) {
                            Text("Save Entry")
                        }
                    }
                }
            }
        }
    }
}

// Custom horizontal scroll selector
@Composable
fun <T> ScrollableRowSelector(
    items: List<T>,
    selectedId: Int?,
    labelMapper: (T) -> String,
    onSelected: (Int) -> Unit
) {
    if (items.isEmpty()) {
        Text("No items loaded. Add some first!", fontSize = 11.sp, color = TextMuted)
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LazyColumn(modifier = Modifier.height(100.dp).fillMaxWidth()) {
                val chunked = items.chunked(2)
                items(chunked) { chunk ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        chunk.forEach { item ->
                            val id = when (item) {
                                is Friend -> item.id
                                is ExpenseGroup -> item.id
                                else -> 0
                            }
                            val selected = selectedId == id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) BrandPrimary else BrandCardSurfaceElevated)
                                    .clickable { onSelected(id) }
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    labelMapper(item),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    color = if (selected) Color.White else TextPrimary,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
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
fun ScrollableKeyValueSelector(
    items: Map<Int, String>,
    selectedKey: Int,
    onSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        LazyColumn(modifier = Modifier.height(80.dp).fillMaxWidth()) {
            val chunked = items.entries.toList().chunked(2)
            items(chunked) { chunk ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    chunk.forEach { entry ->
                        val selected = selectedKey == entry.key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) BrandPrimary else BrandCardSurfaceElevated)
                                .clickable { onSelected(entry.key) }
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                entry.value,
                                fontSize = 11.sp,
                                color = if (selected) Color.White else TextPrimary,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
