package com.example.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.di.AppContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    appContainer: AppContainer,
    snackbarHostState: SnackbarHostState,
    onGoogleSignInClick: () -> Unit,
    onAuthSuccess: (email: String, name: String, isSignUp: Boolean) -> Unit
) {
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(appContainer.settingsRepository, appContainer.authRepository)
    )

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val isLoading by viewModel.loading.collectAsState()

    var isSignUp by rememberSaveable { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var activePolicyDialog by remember { mutableStateOf<String?>(null) }

    val darkTheme = com.example.ui.theme.LocalIsDarkTheme.current
    val bgColor = if (darkTheme) Color(0xFF0F172A) else Color.White
    val cardBg = if (darkTheme) Color(0xFF1E293B) else Color.White
    val textMainCol = if (darkTheme) Color.White else Color(0xFF1E1E1E)
    val textSubtitleCol = if (darkTheme) Color(0xFF94A3B8) else Color(0xFF757575)
    val inputBorderCol = if (darkTheme) Color(0xFF334155) else Color(0xFFF3F4F6)
    val brandPurple = MaterialTheme.colorScheme.primary
    val linkPurple = MaterialTheme.colorScheme.primary

    Scaffold(
        containerColor = bgColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                // Brand Logo
                Image(
                    painter = painterResource(id = R.drawable.logo_transparent),
                    contentDescription = "Soul Sync Brand Logo",
                    modifier = Modifier.size(96.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Soul Sync",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = textMainCol,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "A calm, private space to feel, reflect, and grow.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                    color = textSubtitleCol,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Central Form Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, Color(0xFFF5F5F5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = if (isSignUp) "Begin your practice" else "Welcome back",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                            color = textMainCol
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Email Field
                        Text(
                            text = "Email", 
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = textMainCol
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("email_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = cardBg,
                                unfocusedContainerColor = cardBg,
                                disabledContainerColor = cardBg,
                                focusedTextColor = textMainCol,
                                unfocusedTextColor = textMainCol,
                                focusedBorderColor = brandPurple,
                                unfocusedBorderColor = inputBorderCol,
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password Field
                        Text(
                            text = "Password", 
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = textMainCol
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("password_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = cardBg,
                                unfocusedContainerColor = cardBg,
                                disabledContainerColor = cardBg,
                                focusedTextColor = textMainCol,
                                unfocusedTextColor = textMainCol,
                                focusedBorderColor = brandPurple,
                                unfocusedBorderColor = inputBorderCol,
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Large Brand Pill Button for Sign In / Sign Up
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (email.isBlank() || password.isBlank()) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("All fields are required!")
                                    }
                                    return@Button
                                }
                                val emailPattern = android.util.Patterns.EMAIL_ADDRESS
                                if (!emailPattern.matcher(email.trim()).matches()) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Please enter a valid email address.")
                                    }
                                    return@Button
                                }
                                if (isSignUp && password.length < 6) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Password must be at least 6 characters.")
                                    }
                                    return@Button
                                }
                                if (isSignUp) {
                                    val nameFallback = email.substringBefore("@")
                                    viewModel.performSignUp(email, password, nameFallback) { result ->
                                        when (result) {
                                            is AuthViewModel.AuthResult.Success -> {
                                                onAuthSuccess(result.email, result.name, true)
                                            }
                                            is AuthViewModel.AuthResult.Error -> {
                                                coroutineScope.launch { snackbarHostState.showSnackbar(result.message) }
                                            }
                                        }
                                    }
                                } else {
                                    viewModel.performSignIn(email, password) { result ->
                                        when (result) {
                                            is AuthViewModel.AuthResult.Success -> {
                                                onAuthSuccess(result.email, result.name, false)
                                            }
                                            is AuthViewModel.AuthResult.Error -> {
                                                coroutineScope.launch { snackbarHostState.showSnackbar(result.message) }
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("auth_submit_button"),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = brandPurple,
                                contentColor = Color(0xFF1E1E1E), // Dark text as in image
                                disabledContainerColor = brandPurple.copy(alpha = 0.5f),
                                disabledContentColor = Color(0xFF1E1E1E).copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = if (isSignUp) "Create account" else "Sign in",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Switch layout link (footer)
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isSignUp) "Already have an account? " else "New here? ",
                                    color = textSubtitleCol,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (isSignUp) "Sign in" else "Create an account",
                                    color = linkPurple,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    modifier = Modifier
                                        .clickable { isSignUp = !isSignUp }
                                        .testTag("theme_toggle_auth")
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val annotatedText = androidx.compose.ui.text.buildAnnotatedString {
                            append("By continuing you agree to our ")
                            pushStringAnnotation(tag = "TERMS", annotation = "TERMS")
                            withStyle(style = androidx.compose.ui.text.SpanStyle(color = linkPurple)) {
                                append("Terms")
                            }
                            pop()
                            append(" and ")
                            pushStringAnnotation(tag = "PRIVACY", annotation = "PRIVACY")
                            withStyle(style = androidx.compose.ui.text.SpanStyle(color = linkPurple)) {
                                append("Privacy Policy")
                            }
                            pop()
                            append(".")
                        }

                        androidx.compose.foundation.text.ClickableText(
                            text = annotatedText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = textSubtitleCol,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            onClick = { offset ->
                                annotatedText.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                                    .firstOrNull()?.let {
                                        activePolicyDialog = "TERMS"
                                    }
                                annotatedText.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                                    .firstOrNull()?.let {
                                        activePolicyDialog = "PRIVACY"
                                    }
                            }
                        )
                    }
                }
            }

            if (activePolicyDialog != null) {
                AlertDialog(
                    onDismissRequest = { activePolicyDialog = null },
                    confirmButton = {
                        TextButton(onClick = { activePolicyDialog = null }) {
                            Text("Close", color = brandPurple, fontWeight = FontWeight.Bold)
                        }
                    },
                    title = {
                        Text(
                            text = if (activePolicyDialog == "TERMS") "Terms of Service" else "Privacy Policy",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = textMainCol
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 380.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (activePolicyDialog == "TERMS") {
                                Text(
                                    text = """
                                        Welcome to Soul Sync. By creating an account or using our application, you agree to these Terms of Service.
                                        
                                        1. Personal Journaling & AI Reflection
                                        Soul Sync provides a private space for mood tracking, journaling, and AI-assisted reflection. Your entries are intended for personal well-being and mindfulness.
                                        
                                        2. Account Responsibilities
                                        You are responsible for keeping your login credentials secure. You agree to provide accurate information when registering.
                                        
                                        3. Intellectual Property & Ownership
                                        All your journal entries, mood logs, and personal notes remain 100% yours. Soul Sync claims no ownership over your thoughts or content.
                                        
                                        4. Acceptable Use
                                        You agree not to attempt to breach security, reverse engineer the application, or use the service for unlawful activities.
                                        
                                        5. Modifications to Terms
                                        We may update these terms as Soul Sync grows. Continued use of the app signifies acceptance of updated terms.
                                    """.trimIndent(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textSubtitleCol,
                                    lineHeight = 20.sp
                                )
                            } else {
                                Text(
                                    text = """
                                        Your privacy is fundamental to Soul Sync. We treat your personal journal entries and mood logs with the utmost confidentiality.
                                        
                                        1. Data Collection & Privacy
                                        We store your email address for account authentication and keep your journal entries protected on your device and secure servers.
                                        
                                        2. AI Insights & Reflection
                                        When you request AI reflections, your prompt is processed securely. We never sell or share your personal journal content with third parties.
                                        
                                        3. Data Security
                                        We employ modern industry-standard encryption and access controls to ensure your private entries remain safe from unauthorized access.
                                        
                                        4. Your Data Rights
                                        You have full control to edit or delete your journal entries, clear history, or delete your account at any time.
                                        
                                        5. Contact Us
                                        If you have questions about your privacy or data security, reach out to us at privacy@soulsync.app.
                                    """.trimIndent(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textSubtitleCol,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    },
                    containerColor = cardBg,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
