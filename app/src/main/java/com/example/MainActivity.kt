package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CreateScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AssistantViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val assistantViewModel: AssistantViewModel = viewModel()
            val themeMode by assistantViewModel.darkMode.collectAsState()

            val isDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppScaffold(viewModel = assistantViewModel)
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: AssistantViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == "home",
                    onClick = { viewModel.selectTab("home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == "chat",
                    onClick = { viewModel.selectTab("chat") },
                    icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Chat") },
                    label = { Text("Chat", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == "create",
                    onClick = { viewModel.selectTab("create") },
                    icon = { Icon(Icons.Default.Brush, contentDescription = "Create") },
                    label = { Text("Create", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == "library",
                    onClick = { viewModel.selectTab("library") },
                    icon = { Icon(Icons.Default.Source, contentDescription = "Library") },
                    label = { Text("Library", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == "settings",
                    onClick = { viewModel.selectTab("settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontSize = 11.sp) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToChat = { chatId ->
                        viewModel.selectChat(chatId)
                    }
                )
                "chat" -> ChatScreen(viewModel = viewModel)
                "create" -> CreateScreen(viewModel = viewModel)
                "library" -> LibraryScreen(viewModel = viewModel)
                "settings" -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
