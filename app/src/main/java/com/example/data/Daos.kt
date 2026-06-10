package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SettleUpDao {

    // --- User Profile ---
    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    // --- Friends ---
    @Query("SELECT * FROM friends ORDER BY name ASC")
    fun getAllFriends(): Flow<List<Friend>>

    @Query("SELECT * FROM friends WHERE id = :id LIMIT 1")
    suspend fun getFriendById(id: Int): Friend?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: Friend): Long

    @Delete
    suspend fun deleteFriend(friend: Friend)

    // --- Groups ---
    @Query("SELECT * FROM expense_groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<ExpenseGroup>>

    @Query("SELECT * FROM expense_groups WHERE id = :id LIMIT 1")
    suspend fun getGroupById(id: Int): ExpenseGroup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: ExpenseGroup): Long

    @Delete
    suspend fun deleteGroup(group: ExpenseGroup)

    // --- Group Members ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMember(member: GroupMember)

    @Query("DELETE FROM group_members WHERE groupId = :groupId")
    suspend fun deleteGroupMembers(groupId: Int)

    @Query("SELECT friendId FROM group_members WHERE groupId = :groupId")
    fun getGroupMemberIdsFlow(groupId: Int): Flow<List<Int>>

    @Query("SELECT friendId FROM group_members WHERE groupId = :groupId")
    suspend fun getGroupMemberIds(groupId: Int): List<Int>

    @Query("SELECT f.* FROM friends f JOIN group_members gm ON f.id = gm.friendId WHERE gm.groupId = :groupId")
    fun getGroupFriendsFlow(groupId: Int): Flow<List<Friend>>

    @Query("SELECT f.* FROM friends f JOIN group_members gm ON f.id = gm.friendId WHERE gm.groupId = :groupId")
    suspend fun getGroupFriends(groupId: Int): List<Friend>

    // --- Transactions ---
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE friendId = :friendId ORDER BY date DESC")
    fun getTransactionsWithFriend(friendId: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE groupId = :groupId ORDER BY date DESC")
    fun getTransactionsInGroup(groupId: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Int): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE groupId = :groupId")
    suspend fun deleteTransactionsByGroup(groupId: Int)

    // --- Activity Logs ---
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllActivityLogs(): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: ActivityLog)

    @Query("UPDATE activity_logs SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllLogsAsRead()
}
