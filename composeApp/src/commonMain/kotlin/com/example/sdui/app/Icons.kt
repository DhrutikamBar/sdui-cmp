package com.example.sdui.app


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward

fun materialIcon(name: String): ImageVector? = when (name) {
    "notifications" -> Icons.Default.Notifications
    "arrowUp" -> Icons.Default.ArrowUpward
    "arrowDown" -> Icons.Default.ArrowDownward
    "arrowForward" -> Icons.Default.ArrowForward
    "arrowBack" -> Icons.Default.ArrowBack
    "add" -> Icons.Default.Add
    "money" -> Icons.Default.AttachMoney
    "person" -> Icons.Default.Person
    "check" -> Icons.Default.Check
    "close" -> Icons.Default.Close
    "search" -> Icons.Default.Search
    "settings" -> Icons.Default.Settings
    "home" -> Icons.Default.Home
    "favorite" -> Icons.Default.Favorite
    "star" -> Icons.Default.Star
    "delete" -> Icons.Default.Delete
    "edit" -> Icons.Default.Edit
    "info" -> Icons.Default.Info
    "warning" -> Icons.Default.Warning
    "cart" -> Icons.Default.ShoppingCart
    "arrowForward" -> Icons.AutoMirrored.Filled.ArrowForward
    "arrowBack" -> Icons.AutoMirrored.Filled.ArrowBack
    else -> null
}