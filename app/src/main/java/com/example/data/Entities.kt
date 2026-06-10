package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Sourik Das",
    val phone: String = "+91 9876543210",
    val email: String = "sourikdas49@gmail.com",
    val upiId: String = "sourikdas@okaxis",
    val profilePic: String = ""
)

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val upiId: String = "",
    val nickname: String = "",
    val isRegistered: Boolean = true
)

@Entity(tableName = "expense_groups")
data class ExpenseGroup(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val coverPhoto: String = ""
)

@Entity(tableName = "group_members", primaryKeys = ["groupId", "friendId"])
data class GroupMember(
    val groupId: Int,
    val friendId: Int // 0 represents the Current User
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val friendId: Int? = null, // nullable, populated if personal (between Current User and single Friend)
    val groupId: Int? = null,   // nullable, populated if part of a Group expense
    val payerId: Int,          // 0 for Current User, or Friend.id
    val amount: Double,
    val type: String,          // "lent" | "borrowed" | "split" | "settlement"
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val category: String = "Other", // Food, Travel, Rent, Entertainment, Shopping, Work, Other
    val splits: String = ""    // Format: "id:amount,id:amount" where id is 0 (current user) or friendId
)

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
