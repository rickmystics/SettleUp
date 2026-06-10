package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettleUpViewModel(
    application: Application,
    private val repository: SettleUpRepository
) : AndroidViewModel(application) {

    // --- Core Flows ---
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allFriends: StateFlow<List<Friend>> = repository.allFriends
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGroups: StateFlow<List<ExpenseGroup>> = repository.allGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allActivityLogs: StateFlow<List<ActivityLog>> = repository.allActivityLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Reactive Balance Analytics ---
    val friendBalances: StateFlow<Map<Int, Double>> = combine(allFriends, allTransactions) { friends, transactions ->
        repository.calculateFriendBalances(friends, transactions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Net Summary (Owed, Receive, Net Balance)
    data class NetSummary(
        val totalToReceive: Double,
        val totalOwed: Double,
        val netBalance: Double
    )

    val netSummary: StateFlow<NetSummary> = friendBalances.map { balances ->
        var receive = 0.0
        var owed = 0.0
        for (bal in balances.values) {
            if (bal > 0) {
                receive += bal
            } else if (bal < 0) {
                owed += Math.abs(bal)
            }
        }
        NetSummary(
            totalToReceive = receive,
            totalOwed = owed,
            netBalance = receive - owed
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NetSummary(0.0, 0.0, 0.0))

    // --- Navigation/Section States ---
    private val _selectedFriendId = MutableStateFlow<Int?>(null)
    val selectedFriendId: StateFlow<Int?> = _selectedFriendId.asStateFlow()

    private val _selectedGroupId = MutableStateFlow<Int?>(null)
    val selectedGroupId: StateFlow<Int?> = _selectedGroupId.asStateFlow()

    // Friend LEDGER Transactions & Details
    val selectedFriend: StateFlow<Friend?> = selectedFriendId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else allFriends.map { list -> list.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedFriendTransactions: StateFlow<List<Transaction>> = selectedFriendId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getFriendTransactions(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Group ledger details
    val selectedGroup: StateFlow<ExpenseGroup?> = selectedGroupId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else allGroups.map { list -> list.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedGroupTransactions: StateFlow<List<Transaction>> = selectedGroupId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getGroupTransactions(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedGroupMembers: StateFlow<List<Friend>> = selectedGroupId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getGroupMembersFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedGroupMemberIds: StateFlow<List<Int>> = selectedGroupId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getGroupMemberIdsFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Group Balance Analysis & Smart Settlements
    val selectedGroupBalances: StateFlow<Map<Int, Double>> = combine(
        selectedGroupId,
        selectedGroupMemberIds,
        selectedGroupTransactions
    ) { gId, mIds, txs ->
        if (gId == null || mIds.isEmpty()) emptyMap()
        else repository.calculateGroupBalances(gId, mIds, txs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val selectedGroupSmartSettlements: StateFlow<List<SettlementSuggestion>> = selectedGroupBalances.map { balances ->
        repository.runSmartSettlements(balances)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Actions/Operations ---
    
    fun selectFriend(friendId: Int?) {
        _selectedFriendId.value = friendId
    }

    fun selectGroup(groupId: Int?) {
        _selectedGroupId.value = groupId
    }

    fun addFriend(name: String, phone: String, upiId: String, nickname: String = "") {
        viewModelScope.launch {
            repository.addFriend(name, phone, upiId, nickname)
        }
    }

    fun deleteFriend(friend: Friend) {
        viewModelScope.launch {
            repository.removeFriend(friend)
            if (_selectedFriendId.value == friend.id) {
                _selectedFriendId.value = null
            }
        }
    }

    fun createGroup(name: String, description: String, memberIds: List<Int>) {
        viewModelScope.launch {
            repository.createGroup(name, description, memberIds)
        }
    }

    fun deleteGroup(group: ExpenseGroup) {
        viewModelScope.launch {
            repository.deleteGroup(group)
            if (_selectedGroupId.value == group.id) {
                _selectedGroupId.value = null
            }
        }
    }

    fun addTransaction(
        friendId: Int?,
        groupId: Int?,
        payerId: Int,
        amount: Double,
        type: String,
        note: String,
        category: String,
        splits: Map<Int, Double>
    ) {
        viewModelScope.launch {
            val splitsStr = repository.formatSplits(splits)
            val tx = Transaction(
                friendId = friendId,
                groupId = groupId,
                payerId = payerId,
                amount = amount,
                type = type,
                note = note,
                category = category,
                splits = splitsStr,
                date = System.currentTimeMillis()
            )
            repository.addTransaction(tx)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun updateProfile(name: String, phone: String, email: String, upiId: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.updateProfile(current.copy(name = name, phone = phone, email = email, upiId = upiId))
            repository.addActivityLog("Profile Updated", "You updated your personal information.")
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.markAllLogsAsRead()
        }
    }

    fun addActivityLog(title: String, content: String) {
        viewModelScope.launch {
            repository.addActivityLog(title, content)
        }
    }

    fun parseSplits(splitsStr: String): Map<Int, Double> {
        return repository.parseSplits(splitsStr)
    }

    // Static Factory to instantiate our SettleUpViewModel
    class Factory(
        private val application: Application,
        private val repository: SettleUpRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettleUpViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettleUpViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
