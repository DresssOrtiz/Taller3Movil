package com.example.taller3movil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.taller3movil.screens.LoginScreen
import com.example.taller3movil.screens.MenuScreen
import com.example.taller3movil.screens.ProfileScreen
import com.example.taller3movil.screens.RegisterScreen
import com.example.taller3movil.ui.theme.Taller3MovilTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Taller3MovilTheme {
                val auth = FirebaseAuth.getInstance()
                var currentScreen by remember {
                    mutableStateOf(
                        if (auth.currentUser != null) AppScreen.Menu else AppScreen.Login
                    )
                }

                when (currentScreen) {
                    AppScreen.Login -> LoginScreen(
                        onLoginSuccess = { currentScreen = AppScreen.Menu },
                        onGoToRegister = { currentScreen = AppScreen.Register },
                        auth = auth
                    )

                    AppScreen.Register -> RegisterScreen(
                        onRegisterSuccess = { currentScreen = AppScreen.Menu },
                        onBackToLogin = { currentScreen = AppScreen.Login },
                        auth = auth
                    )

                    AppScreen.Menu -> MenuScreen(
                        onProfileClick = { currentScreen = AppScreen.Profile },
                        onLogout = {
                            auth.signOut()
                            currentScreen = AppScreen.Login
                        }
                    )

                    AppScreen.Profile -> ProfileScreen(
                        onBack = { currentScreen = AppScreen.Menu },
                        auth = auth
                    )
                }
            }
        }
    }
}

private enum class AppScreen {
    Login,
    Register,
    Menu,
    Profile
}
