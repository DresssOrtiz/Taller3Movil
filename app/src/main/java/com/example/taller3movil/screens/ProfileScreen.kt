package com.example.taller3movil.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.taller3movil.data.UserRepository
import com.example.taller3movil.data.model.User
import com.google.firebase.auth.FirebaseAuth
import java.io.File

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    userRepository: UserRepository = UserRepository()
) {
    val context = LocalContext.current
    var user by remember { mutableStateOf<User?>(null) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSavingProfile by remember { mutableStateOf(false) }
    var isChangingPassword by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var profileImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            profileImageUri = uri
            message = "Foto seleccionada localmente. Storage pendiente por configuracion."
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            profileImageUri = cameraImageUri
            message = "Foto seleccionada localmente. Storage pendiente por configuracion."
        } else {
            message = "No se tomo la foto"
        }
    }

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
                    message = "No se encontro informacion del usuario"
                }
            }
            .addOnFailureListener { error ->
                isLoading = false
                message = error.message ?: "No se pudo cargar el perfil"
            }
    }

    LaunchedEffect(profileImageUri) {
        profileImageBitmap = profileImageUri?.let { uri ->
            loadImageBitmap(context, uri)
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

                    Text(
                        text = "Foto de perfil",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileImagePreview(imageBitmap = profileImageBitmap)

                    if (profileImageUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Foto seleccionada localmente. Storage pendiente por configuracion.",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Seleccionar de galeria")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val imageFile = createProfileImageFile(context)
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                imageFile
                            )
                            cameraImageUri = uri
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tomar foto")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

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
                        label = { Text("Telefono") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val cleanName = name.trim()
                            val cleanPhone = phone.trim()

                            if (cleanName.isEmpty() || cleanPhone.isEmpty()) {
                                message = "Nombre y telefono son obligatorios"
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
                        label = { Text("Nueva contrasena") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (newPassword.isEmpty()) {
                                message = "Ingresa una nueva contrasena"
                                return@Button
                            }

                            isChangingPassword = true
                            message = ""

                            auth.currentUser?.updatePassword(newPassword)
                                ?.addOnSuccessListener {
                                    newPassword = ""
                                    isChangingPassword = false
                                    message = "Contrasena actualizada"
                                }
                                ?.addOnFailureListener { error ->
                                    isChangingPassword = false
                                    message = error.message ?: "No se pudo cambiar la contrasena"
                                }
                                ?: run {
                                    isChangingPassword = false
                                    message = "No hay usuario autenticado"
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isChangingPassword
                    ) {
                        Text("Cambiar contrasena")
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
private fun ProfileImagePreview(imageBitmap: ImageBitmap?) {
    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentScale = ContentScale.Crop
        )
    } else {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Sin foto seleccionada",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

private fun createProfileImageFile(context: Context): File {
    val imagesDirectory = File(context.cacheDir, "images")
    imagesDirectory.mkdirs()
    return File.createTempFile("profile_photo_", ".jpg", imagesDirectory)
}

private fun loadImageBitmap(context: Context, uri: Uri): ImageBitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source).asImageBitmap()
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri).asImageBitmap()
        }
    } catch (_: Exception) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
        }
    }
}
