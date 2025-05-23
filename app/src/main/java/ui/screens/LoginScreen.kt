package ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import data.NoteRepository
import kotlinx.coroutines.launch
import com.example.notesapp.R

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int
)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var showOnboarding by remember { mutableStateOf(true) }

    if (showOnboarding) {
        OnboardingScreen(
            onFinish = { showOnboarding = false },
            onGetStarted = { showOnboarding = false }
        )
    } else {
        AuthenticationScreen(
            onLoginSuccess = onLoginSuccess,
            onRegisterSuccess = onRegisterSuccess
        )
    }
}

@Composable
private fun OnboardingScreen(
    onFinish: () -> Unit,
    onGetStarted: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Capture Your Ideas",
            description = "Never lose a thought again. Write down your ideas, thoughts, and important information instantly.",
            imageRes = R.drawable.onboarding_1
        ),
        OnboardingPage(
            title = "Organize Everything",
            description = "Keep your notes organized and easily accessible. Find what you need, when you need it.",
            imageRes = R.drawable.onboarding_2
        ),
        OnboardingPage(
            title = "Stay Productive",
            description = "Boost your productivity with smart note-taking features and seamless synchronization.",
            imageRes = R.drawable.onboarding_3
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onFinish) {
                    Text("Skip", color = MaterialTheme.colorScheme.primary)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painter = painterResource(id = R.drawable.stickies_logo),
                    contentDescription = "Stickies Logo",
                    modifier = Modifier.size(91.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page])
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color(0xFFFFA726)
                                else MaterialTheme.colorScheme.outline
                            )
                    )
                    if (index < pages.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally // Center the column content
            ) {
                if (pagerState.currentPage < pages.size - 1) { // Only show Previous/Next if not on last page
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (pagerState.currentPage > 0) {
                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                }
                            ) {
                                Text("Previous")
                            }
                        } else {
                            Spacer(modifier = Modifier.width(80.dp))
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            modifier = Modifier
                                .width(120.dp)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFA726)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "Next",
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onGetStarted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFA726)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "Get Started",
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = null,
            modifier = Modifier
                .width(261.dp)
                .height(196.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun AuthenticationScreen(
    onLoginSuccess: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }

    if (isLoginMode) {
        LoginViewScreen(
            onLoginSuccess = onLoginSuccess,
            onSwitchToRegister = { isLoginMode = false }
        )
    } else {
        RegisterViewScreen(
            onRegisterSuccess = onRegisterSuccess,
            onSwitchToLogin = { isLoginMode = true }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginViewScreen(
    onLoginSuccess: () -> Unit,
    onSwitchToRegister: () -> Unit
) {
    val repository = NoteRepository()
    val coroutineScope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Image(
            painter = painterResource(id = R.drawable.background_waves),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(120.dp))


            Text(
                text = "Hello!",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )


            Text(
                text = "Sign in to your account",
                fontSize = 16.sp,
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp, bottom = 60.dp)
            )


            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text(
                        "Username",
                        color = Color.Gray.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                            elevation = 4.dp,
                        shape = RoundedCornerShape(28.dp),
                        clip = false
            ),
                enabled = !isLoading,
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(20.dp))


            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text(
                        "Password",
                        color = Color.Gray.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                            elevation = 4.dp,
                        shape = RoundedCornerShape(28.dp),
                        clip = false
                    ),
                enabled = !isLoading,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White
                )
            )


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { /* Handle forgot password */ },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "Forgot your password?",
                        color = Color.Gray.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
            ) {
                Text(
                    text = "Sign in",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                isLoading = true
                                val result = repository.login(email, password)
                                if (result.isSuccess) {
                                    Log.d("LoginScreen", "Login successful")
                                    onLoginSuccess()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                                    Log.e("LoginScreen", "Login failed: $errorMessage")
                                }
                            } catch (e: Exception) {
                                Log.e("LoginScreen", "Exception during login: ${e.message}", e)
                                errorMessage = "Error: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .width(56.dp)
                        .height(34.dp), // Rectangular dimensions
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA726)
                    ),
                    shape = RoundedCornerShape(20.dp), // Rounded corners (half of height for fully rounded edges)
                    contentPadding = PaddingValues(0.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_forward),
                            contentDescription = "Sign in arrow",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center, // Center the text
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Don't have an account? ",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                TextButton(
                    onClick = onSwitchToRegister,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "Create",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))


            if (errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterViewScreen(
    onRegisterSuccess: () -> Unit,
    onSwitchToLogin: () -> Unit
) {
    val repository = NoteRepository() // Assuming NoteRepository is available
    val coroutineScope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_waves), // Ensure this drawable exists
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp), // Consistent padding with LoginScreen
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(120.dp)) // Consistent top spacing

            // Title "Create account"
            Text(
                text = "Create account",
                fontSize = 36.sp, // Consistent font size
                fontWeight = FontWeight.Bold,
                color = Color.Black // Consistent color
            )

            // Subtitle
            Text(
                text = "Sign up to your account", // New subtitle for consistency
                fontSize = 16.sp,
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp, bottom = 60.dp) // Consistent padding
            )

            // Username (Email) Input Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { // Changed to placeholder for consistency
                    Text(
                        "Username",
                        color = Color.Gray.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person, // Changed to Person icon for username
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.6f) // Consistent tint
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow( // Consistent shadow
                        elevation = 4.dp,
                        shape = RoundedCornerShape(28.dp),
                        clip = false
                    ),
                enabled = !isLoading,
                singleLine = true,
                shape = RoundedCornerShape(28.dp), // Consistent shape
                colors = OutlinedTextFieldDefaults.colors( // Consistent colors
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Password Input Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { // Changed to placeholder for consistency
                    Text(
                        "Password",
                        color = Color.Gray.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.6f) // Consistent tint
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow( // Consistent shadow
                        elevation = 4.dp,
                        shape = RoundedCornerShape(28.dp),
                        clip = false
                    ),
                enabled = !isLoading,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(28.dp), // Consistent shape
                colors = OutlinedTextFieldDefaults.colors( // Consistent colors
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp)) // Adjusted spacer height

            // Push content down to match the design
            Spacer(modifier = Modifier.weight(1f))

            // Create Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp), // Consistent padding
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End) // Consistent arrangement
            ) {
                Text(
                    text = "Create",
                    fontSize = 28.sp, // Consistent font size
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black // Consistent color
                )

                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                isLoading = true
                                // Assuming repository.register function exists for registration
                                val result = repository.register(email, password)
                                if (result.isSuccess) {
                                    Log.d("LoginScreen", "Registration successful")
                                    onRegisterSuccess()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Registration failed"
                                    Log.e("LoginScreen", "Registration failed: $errorMessage")
                                }
                            } catch (e: Exception) {
                                Log.e("LoginScreen", "Exception during registration: ${e.message}", e)
                                errorMessage = "Error: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .width(56.dp)
                        .height(34.dp), // Rectangular dimensions
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA726)
                    ),
                    shape = RoundedCornerShape(20.dp), // Rounded corners (half of height for fully rounded edges)
                    contentPadding = PaddingValues(0.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_forward), // Ensure you have this drawable
                            contentDescription = "Create account arrow",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Back to login link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center, // Centered arrangement
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Already have an account? ",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                TextButton(
                    onClick = onSwitchToLogin,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "Sign in",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp)) // Consistent spacing

            // Error Message
            if (errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.1f) // Consistent error background
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = Color.Red, // Consistent error text color
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// Custom Canvas composable for drawing curved shapes
@Composable
private fun Canvas(
    modifier: Modifier = Modifier,
    onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit
) {
    androidx.compose.foundation.Canvas(
        modifier = modifier,
        onDraw = onDraw
    )
}

// Preview functions
@Preview(showBackground = true, name = "Modern Login Screen")
@Composable
private fun loginpreview() {
    MaterialTheme {
        LoginViewScreen(
            onLoginSuccess = {},
            onSwitchToRegister = {}
        )
    }
}

@Preview(showBackground = true, name = "Modern Register Screen")
@Composable
private fun registerpreview() {
    MaterialTheme {
        RegisterViewScreen(
            onRegisterSuccess = {},
            onSwitchToLogin = {}
        )
    }
}

@Preview(showBackground = true, name = "Enhanced Auth Flow")
@Composable
private fun onboardingflowprevkew() {
    MaterialTheme {
        AuthenticationScreen(
            onLoginSuccess = {},
            onRegisterSuccess = {}
        )
    }
}