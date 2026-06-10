package com.example.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettleUpRepository(private val dao: SettleUpDao) {

    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val allFriends: Flow<List<Friend>> = dao.getAllFriends()
    val allGroups: Flow<List<ExpenseGroup>> = dao.getAllGroups()
    val allTransactions: Flow<List<Transaction>> = dao.getAllTransactions()
    val allActivityLogs: Flow<List<ActivityLog>> = dao.getAllActivityLogs()

    // Database pre-population or check
    init {
        CoroutineScope(Dispatchers.IO).launch {
            // Seed a default user profile if none exists
            userProfile.first()?.let {
                // profile exists
            } ?: run {
                dao.insertOrUpdateProfile(UserProfile())
            }

            // Seed sample friends if empty
            val friends = dao.getAllFriends().first()
            if (friends.isEmpty()) {
                seedSampleData()
            }
        }
    }

    private suspend fun seedSampleData() {
        val f1Id = dao.insertFriend(Friend(name = "Abhishek Sharma", phone = "+91 9988776655", upiId = "abhishek@okaxis", nickname = "Abhi"))
        val f2Id = dao.insertFriend(Friend(name = "Kriti Sen", phone = "+91 8877665544", upiId = "kriti@okhdfc", nickname = "Kriti"))
        val f3Id = dao.insertFriend(Friend(name = "Rohan Mehta", phone = "+91 7766554433", upiId = "rohan@okicici", nickname = "Rohan"))

        // Add typical transactions
        // Abhishek lent us 500 for taxi
        dao.insertTransaction(Transaction(
            friendId = f1Id.toInt(),
            payerId = f1Id.toInt(),
            amount = 500.0,
            type = "borrowed",
            note = "Taxi ride back from airport",
            date = System.currentTimeMillis() - 86400000 * 3, // 3 days ago
            category = "Travel"
        ))

        // We lent Kriti 1200 for concert ticket
        dao.insertTransaction(Transaction(
            friendId = f2Id.toInt(),
            payerId = 0, // Current user
            amount = 1200.0,
            type = "lent",
            note = "Concert Ticket booking",
            date = System.currentTimeMillis() - 86400000 * 2, // 2 days ago
            category = "Entertainment"
        ))

        // Rohan and us split food bill of 1500 (Rohan paid, we owe his share or split)
        dao.insertTransaction(Transaction(
            friendId = f3Id.toInt(),
            payerId = f3Id.toInt(),
            amount = 1500.0,
            type = "split",
            note = "Sushi dinner",
            date = System.currentTimeMillis() - 86400000, // 1 day ago
            category = "Food",
            splits = "0:750,${f3Id}:750"
        ))

        // Create a sample group
        val gId = dao.insertGroup(ExpenseGroup(
            name = "Cozy Flatmates",
            description = "Groceries, Wifi, Maid and Rent tracking"
        ))

        // Add members
        dao.insertGroupMember(GroupMember(gId.toInt(), 0)) // Self
        dao.insertGroupMember(GroupMember(gId.toInt(), f1Id.toInt())) // Abhishek
        dao.insertGroupMember(GroupMember(gId.toInt(), f2Id.toInt())) // Kriti

        // Group expense: Electricity bill 1800 paid by Self (0)
        dao.insertTransaction(Transaction(
            groupId = gId.toInt(),
            payerId = 0,
            amount = 1800.0,
            type = "split",
            note = "Electricity Bill June",
            date = System.currentTimeMillis() - 86400000 * 5,
            category = "Rent",
            splits = "0:600,${f1Id}:600,${f2Id}:600"
        ))

        // Group expense: Grocery Shopping 1200 paid by Abhishek (f1)
        dao.insertTransaction(Transaction(
            groupId = gId.toInt(),
            payerId = f1Id.toInt(),
            amount = 1200.0,
            type = "split",
            note = "Daily Groceries & Snacks",
            date = System.currentTimeMillis() - 86400000 * 1,
            category = "Food",
            splits = "0:400,${f1Id}:400,${f2Id}:400"
        ))

        // Log some activity
        dao.insertActivityLog(ActivityLog(
            title = "Welcome to SettleUp!",
            content = "Keep track of bills and expenses cleanly. Slide or tap to record splits!"
        ))
        dao.insertActivityLog(ActivityLog(
            title = "Seed data added",
            content = "Sample ledger with Abhishek, Kriti, and Rohan loaded."
        ))
    }

    // --- Profile actions ---
    suspend fun updateProfile(profile: UserProfile) {
        withContext(Dispatchers.IO) {
            dao.insertOrUpdateProfile(profile)
        }
    }

    // --- Friend actions ---
    suspend fun addFriend(name: String, phone: String, upiId: String, nickname: String = ""): Long {
        return withContext(Dispatchers.IO) {
            val fId = dao.insertFriend(Friend(name = name, phone = phone, upiId = upiId, nickname = nickname))
            dao.insertActivityLog(ActivityLog(
                title = "Added Friend",
                content = "You added $name (+91 $phone) to your friends list."
            ))
            fId
        }
    }

    suspend fun getFriend(id: Int): Friend? {
        return withContext(Dispatchers.IO) {
            dao.getFriendById(id)
        }
    }

    suspend fun removeFriend(friend: Friend) {
        withContext(Dispatchers.IO) {
            dao.deleteFriend(friend)
        }
    }

    // --- Group actions ---
    suspend fun createGroup(name: String, description: String, memberIds: List<Int>): Long {
        return withContext(Dispatchers.IO) {
            val gId = dao.insertGroup(ExpenseGroup(name = name, description = description))
            // Insert current user (0) as member
            dao.insertGroupMember(GroupMember(gId.toInt(), 0))
            for (id in memberIds) {
                dao.insertGroupMember(GroupMember(gId.toInt(), id))
            }
            dao.insertActivityLog(ActivityLog(
                title = "Created Group",
                content = "Group \"$name\" created with ${memberIds.size + 1} members."
            ))
            gId
        }
    }

    suspend fun getGroupMembers(groupId: Int): List<Friend> {
        return withContext(Dispatchers.IO) {
            dao.getGroupFriends(groupId)
        }
    }

    fun getGroupMembersFlow(groupId: Int): Flow<List<Friend>> {
        return dao.getGroupFriendsFlow(groupId)
    }

    fun getGroupMemberIdsFlow(groupId: Int): Flow<List<Int>> {
        return dao.getGroupMemberIdsFlow(groupId)
    }

    suspend fun deleteGroup(group: ExpenseGroup) {
        withContext(Dispatchers.IO) {
            dao.deleteGroupMembers(group.id)
            dao.deleteTransactionsByGroup(group.id)
            dao.deleteGroup(group)
        }
    }

    // --- Transaction actions ---
    suspend fun addTransaction(transaction: Transaction): Long {
        return withContext(Dispatchers.IO) {
            val id = dao.insertTransaction(transaction)
            
            // Add activity log
            val title = when (transaction.type) {
                "lent" -> "Lent money"
                "borrowed" -> "Borrowed money"
                "settlement" -> "Settle Payment"
                else -> "Split expense"
            }
            val desc = if (transaction.groupId != null) {
                "Added \"${transaction.note}\" of ₹${transaction.amount} in Group."
            } else {
                "Recorded \"${transaction.note}\" of ₹${transaction.amount} with friend."
            }
            dao.insertActivityLog(ActivityLog(title = title, content = desc))
            id
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            dao.deleteTransaction(transaction)
        }
    }

    fun getFriendTransactions(friendId: Int): Flow<List<Transaction>> {
        return dao.getTransactionsWithFriend(friendId)
    }

    fun getGroupTransactions(groupId: Int): Flow<List<Transaction>> {
        return dao.getTransactionsInGroup(groupId)
    }

    // --- Activity log actions ---
    suspend fun addActivityLog(title: String, content: String) {
        withContext(Dispatchers.IO) {
            dao.insertActivityLog(ActivityLog(title = title, content = content))
        }
    }

    suspend fun markAllLogsAsRead() {
        withContext(Dispatchers.IO) {
            dao.markAllLogsAsRead()
        }
    }

    // --- Balance Calculation Helpers ---

    /**
     * Parse split string "0:100,1:200" into a Map of userId to amount
     */
    fun parseSplits(splitsStr: String): Map<Int, Double> {
        if (splitsStr.isBlank()) return emptyMap()
        return try {
            splitsStr.split(",").associate {
                val parts = it.split(":")
                val id = parts[0].toInt()
                val valAmt = parts[1].toDouble()
                id to valAmt
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Map split items to splits string
     */
    fun formatSplits(splitsMap: Map<Int, Double>): String {
        return splitsMap.entries.joinToString(",") { "${it.key}:${it.value}" }
    }

    /**
     * Calculates the balance of each friend relative to the current user (0)
     * Positive balance → Friend owes current user (Green)
     * Negative balance → Current user owes friend (Red)
     */
    fun calculateFriendBalances(
        friends: List<Friend>,
        transactions: List<Transaction>
    ): Map<Int, Double> {
        val balances = mutableMapOf<Int, Double>()
        // Initialize with zero
        for (f in friends) {
            balances[f.id] = 0.0
        }

        // Process personal transactions
        val personalTransactions = transactions.filter { it.friendId != null && it.groupId == null }
        for (tx in personalTransactions) {
            val fId = tx.friendId ?: continue
            val currentBal = balances[fId] ?: 0.0
            
            when (tx.type) {
                "lent" -> {
                    // Current User lent Friend
                    balances[fId] = currentBal + tx.amount
                }
                "borrowed" -> {
                    // Friend lent Current User (We owe Friend)
                    balances[fId] = currentBal - tx.amount
                }
                "settlement" -> {
                    if (tx.payerId == 0) {
                        // Current User paid Friend (repaying debt / lending)
                        balances[fId] = currentBal + tx.amount
                    } else {
                        // Friend paid Current User (repaying we)
                        balances[fId] = currentBal - tx.amount
                    }
                }
                "split" -> {
                    // Personal split
                    val splitsMap = parseSplits(tx.splits)
                    if (splitsMap.isNotEmpty()) {
                        val friendShare = splitsMap[fId] ?: 0.0
                        val selfShare = splitsMap[0] ?: 0.0
                        if (tx.payerId == 0) {
                            // Current user paid F_total. Friend owes their share
                            balances[fId] = currentBal + friendShare
                        } else {
                            // Friend paid. Current user owes their share
                            balances[fId] = currentBal - selfShare
                        }
                    } else {
                        // Equal splits fallback
                        if (tx.payerId == 0) {
                            balances[fId] = currentBal + (tx.amount / 2)
                        } else {
                            balances[fId] = currentBal - (tx.amount / 2)
                        }
                    }
                }
            }
        }

        // Process all group expenses that include Current User (0) and affect balances
        // NOTE: In typical money ledger systems, group expenses are rolled into the net friend state or kept separate.
        // Let's roll them into the total friend balance so the Home Dashboard shows the TRUE consolidated net balance!
        // To do this, let's look at all group transactions.
        val groupTransactions = transactions.filter { it.groupId != null }
        for (tx in groupTransactions) {
            val splitsMap = parseSplits(tx.splits)
            if (splitsMap.isEmpty()) continue

            val selfOwed = splitsMap[0] ?: 0.0
            
            if (tx.payerId == 0) {
                // Self paid. Others owe self.
                for ((fId, owedAmt) in splitsMap) {
                    if (fId == 0) continue
                    val currentBal = balances[fId] ?: 0.0
                    balances[fId] = currentBal + owedAmt
                }
            } else {
                // Someone else paid.
                // Critical: Self (0) owes payerId (tx.payerId) their selfOwed share
                val payerId = tx.payerId
                if (balances.containsKey(payerId)) {
                    val currentBal = balances[payerId] ?: 0.0
                    balances[payerId] = currentBal - selfOwed
                }
            }
        }

        return balances
    }

    /**
     * Returns net balances of members of a specific group
     * Member IDs can be 0 (self) or any friend ID in the group list.
     */
    fun calculateGroupBalances(
        groupId: Int,
        memberIds: List<Int>,
        groupTransactions: List<Transaction>
    ): Map<Int, Double> {
        val netBalances = mutableMapOf<Int, Double>()
        for (id in memberIds) {
            netBalances[id] = 0.0
        }

        for (tx in groupTransactions) {
            val splitsMap = parseSplits(tx.splits)
            if (splitsMap.isEmpty()) continue

            // 1. Credit the payer
            val payer = tx.payerId
            val payerCredit = netBalances[payer] ?: 0.0
            netBalances[payer] = payerCredit + tx.amount

            // 2. Debit everyone for their share
            for ((mId, owedAmt) in splitsMap) {
                val currentBal = netBalances[mId] ?: 0.0
                netBalances[mId] = currentBal - owedAmt
            }
        }

        return netBalances
    }

    /**
     * Smart Settlement Engine (Graph Reduction)
     * Takes map of member balances in group and simplifies into direct transfers
     */
    fun runSmartSettlements(
        groupBalances: Map<Int, Double>
    ): List<SettlementSuggestion> {
        // Filter out people with near-zero balances
        val balanceList = groupBalances.entries
            .map { it.key to it.value }
            .filter { Math.abs(it.second) > 0.01 }

        val creditors = balanceList.filter { it.second > 0 }.map { it.first to it.second }.toMutableList()
        val debtors = balanceList.filter { it.second < 0 }.map { it.first to -it.second }.toMutableList()

        // Sort: Creditors descending (biggest first), Debtors descending (biggest debt first)
        creditors.sortByDescending { it.second }
        debtors.sortByDescending { it.second }

        val suggestions = mutableListOf<SettlementSuggestion>()

        var cIndex = 0
        var dIndex = 0

        while (cIndex < creditors.size && dIndex < debtors.size) {
            val (creditorId, creditAmt) = creditors[cIndex]
            val (debtorId, debtAmt) = debtors[dIndex]

            val transferAmt = Math.min(creditAmt, debtAmt)
            
            suggestions.add(SettlementSuggestion(
                fromMemberId = debtorId,
                toMemberId = creditorId,
                amount = transferAmt
            ))

            // Update remaining
            creditors[cIndex] = creditorId to (creditAmt - transferAmt)
            debtors[dIndex] = debtorId to (debtAmt - transferAmt)

            if (Math.abs(creditors[cIndex].second) < 0.01) {
                cIndex++
            }
            if (Math.abs(debtors[dIndex].second) < 0.01) {
                dIndex++
            }
        }

        return suggestions
    }
}

data class SettlementSuggestion(
    val fromMemberId: Int, // The member who owes money
    val toMemberId: Int,   // The member who is owed
    val amount: Double     // Amount to transfer
)
