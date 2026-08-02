package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "user_accounts")
@Serializable
data class UserAccount(
    @PrimaryKey val id: String,
    val email: String,
    val passwordKey: String,
    val displayName: String
)
