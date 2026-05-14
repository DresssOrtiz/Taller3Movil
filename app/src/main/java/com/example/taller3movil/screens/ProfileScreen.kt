package com.example.taller3movil.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.taller3movil.data.UserRepository
import com.example.taller3movil.data.model.User
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    userRepository: UserRepository = UserRepository()
) {
    var user by remember { mutableStateOf<User?>(null) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSavingProfile by remember { mutableStateOf(false) }
    var isChangingPassword by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid.orEmpty()

        if (uid.isEmpty()) {
            isLoading = false
            message = "No hay usuario autenticado"
            return@LaunchedEffect
        }

        userRepository.getUser(uid)
            .addOnSuccessListener { snapshot ->
                val loadedUser = snapshot.getValue(User::class.java)
                user = loadedUser
                name = loadedUser?.name.orEmpty()
                phone = loadedUser?.phone.orEmpty()
                isLoading = false

                if (loadedUser == null) {
                    message = "No se encontró información del usuario"
                }
            }
            .addOnFailureListener { error ->
                isLoading = false
                message = error.message ?: "No se pudo cargar el perfil"
            }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Perfil",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                }

                user != null -> {
                    val currentUser = user ?: User()
                    val uid = auth.currentUser?.uid.orEmpty()

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileText(label = "Email", value = currentUser.email)

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val cleanName = name.trim()
                            val cleanPhone = phone.trim()

                            if (cleanName.isEmpty() || cleanPhone.isEmpty()) {
                                message = "Nombre y teléfono son obligatorios"
                                return@Button
                            }

                            isSavingProfile = true
                            message = ""

                            userRepository.updateUserProfile(uid, cleanName, cleanPhone)
                                .addOnSuccessListener {
                                    user = currentUser.copy(
                                        name = cleanName,
                                        phone = cleanPhone
                                    )
                                    isSavingProfile = false
                                    message = "Perfil actualizado"
                                }
                                .addOnFailureListener { error ->
                                    isSavingProfile = false
                                    message = error.message ?: "No se pudo actualizar el perfil"
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSavingProfile && uid.isNotEmpty()
                    ) {
                        Text("Guardar cambios")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nueva contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (newPassword.isEmpty()) {
                                message = "Ingresa una nueva contraseña"
                                return@Button
                            }

                            isChangingPassword = true
                            message = ""

                            auth.currentUser?.updatePassword(newPassword)
                                ?.addOnSuccessListener {
                                    newPassword = ""
                                    isChangingPassword = false
                                    message = "Contraseña actualizada"
                                }
                                ?.addOnFailureListener { error ->
                                    isChangingPassword = false
                                    message = error.message ?: "No se pudo cambiar la contraseña"
                                }
                                ?: run {
                                    isChangingPassword = false
                                    message = "No hay usuario autenticado"
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isChangingPassword
                    ) {
                        Text("Cambiar contraseña")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    ProfileText(label = "Online", value = currentUser.online.toString())
                    ProfileText(label = "Latitude", value = currentUser.latitude.toString())
                    ProfileText(label = "Longitude", value = currentUser.longitude.toString())

                    if (currentUser.photoUrl.isNotEmpty()) {
                        ProfileText(label = "PhotoUrl", value = currentUser.photoUrl)
                    }
                }

                message.isNotEmpty() -> {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            if (isSavingProfile || isChangingPassword) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            if (message.isNotEmpty() && user != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }
    }
}

@Composable
private fun ProfileText(label: String, value: String) {
    Text(
        text = "$label: $value",
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(8.dp))
}
