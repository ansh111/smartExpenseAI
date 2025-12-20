package com.anshul.expenseai.ui.compose.expensetracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anshul.expenseai.ui.theme.MinimalDarkColors

data class UserOnboardingInfo(
    val fullName: String = "",
    val phoneNumber: String = "",
    val email: String = ""
)

@Composable
fun LetsGetStartedOnboardingScreen(
    onContinue: (UserOnboardingInfo) -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var phoneNumberError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    
    var showInfoCard by remember { mutableStateOf(true) }
    
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    
    // Validation functions
    fun validateFullName(name: String): String? {
        return when {
            name.isBlank() -> "Full name is required"
            name.length < 2 -> "Name must be at least 2 characters"
            !name.matches(Regex("^[a-zA-Z\\s]+$")) -> "Name can only contain letters"
            else -> null
        }
    }
    
    fun validatePhoneNumber(phone: String): String? {
        return when {
            phone.isBlank() -> "Phone number is required"
            phone.length != 10 -> "Phone number must be 10 digits"
            !phone.matches(Regex("^[0-9]+$")) -> "Phone number can only contain digits"
            else -> null
        }
    }
    
    fun validateEmail(emailAddress: String): String? {
        return when {
            emailAddress.isBlank() -> "Email is required"
            !emailAddress.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) -> 
                "Please enter a valid email address"
            else -> null
        }
    }
    
    fun validateAndSubmit() {
        fullNameError = validateFullName(fullName)
        phoneNumberError = validatePhoneNumber(phoneNumber)
        emailError = validateEmail(email)
        
        if (fullNameError == null && phoneNumberError == null && emailError == null) {
            onContinue(UserOnboardingInfo(fullName, phoneNumber, email))
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MinimalDarkColors.Gray900,
                        MinimalDarkColors.Gray950,
                        MinimalDarkColors.Gray900
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Animated Welcome Icon
            AnimatedWelcomeIcon()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Title
            Text(
                text = "Let's Get Started",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Subtitle
            Text(
                text = "Help us personalize your expense tracking experience",
                fontSize = 16.sp,
                color = MinimalDarkColors.Gray400,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Info Card - Why we need this info
            AnimatedVisibility(
                visible = showInfoCard,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                WhyWeNeedThisCard(
                    onDismiss = { showInfoCard = false }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Form Fields
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Full Name Field
                OnboardingTextField(
                    value = fullName,
                    onValueChange = { 
                        fullName = it
                        fullNameError = null
                    },
                    label = "Full Name",
                    placeholder = "Enter your full name",
                    icon = Icons.Default.Person,
                    errorMessage = fullNameError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                
                // Phone Number Field
                OnboardingTextField(
                    value = phoneNumber,
                    onValueChange = { 
                        if (it.length <= 10) {
                            phoneNumber = it.filter { char -> char.isDigit() }
                            phoneNumberError = null
                        }
                    },
                    label = "Phone Number",
                    placeholder = "Enter your phone number",
                    icon = Icons.Default.Phone,
                    errorMessage = phoneNumberError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                
                // Email Field
                OnboardingTextField(
                    value = email,
                    onValueChange = { 
                        email = it.trim()
                        emailError = null
                    },
                    label = "Email Address",
                    placeholder = "Enter your email",
                    icon = Icons.Default.Email,
                    errorMessage = emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { 
                            focusManager.clearFocus()
                            validateAndSubmit()
                        }
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Benefits List
            BenefitsSection()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Continue Button
            Button(
                onClick = { validateAndSubmit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = MinimalDarkColors.Indigo500.copy(alpha = 0.5f)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MinimalDarkColors.Indigo600,
                                    MinimalDarkColors.Purple600
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Continue",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Privacy Note
            Text(
                text = "We respect your privacy. Your data is stored securely and never shared.",
                fontSize = 12.sp,
                color = MinimalDarkColors.Gray500,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AnimatedWelcomeIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "welcome icon")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring with gradient
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            MinimalDarkColors.Indigo500.copy(alpha = 0.3f),
                            MinimalDarkColors.Purple500.copy(alpha = 0.5f),
                            MinimalDarkColors.Indigo500.copy(alpha = 0.3f)
                        )
                    )
                )
        )
        
        // Inner circle
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MinimalDarkColors.Indigo600,
                            MinimalDarkColors.Purple600
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(56.dp)
            )
        }
    }
}

@Composable
fun WhyWeNeedThisCard(
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = MinimalDarkColors.Indigo500.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MinimalDarkColors.Gray800
        ),
        border = BorderStroke(1.dp, MinimalDarkColors.Indigo500.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MinimalDarkColors.Indigo500.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MinimalDarkColors.Indigo400,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Text(
                        text = "Why we need this",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MinimalDarkColors.Gray400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoPoint(
                    icon = Icons.Default.Block,
                    title = "Avoid Self-Transactions",
                    description = "We'll filter out expenses to yourself (like transfers between your own accounts)"
                )
                
                InfoPoint(
                    icon = Icons.Default.TrendingUp,
                    title = "Better Insights",
                    description = "Get personalized spending recommendations based on your profile"
                )
                
                InfoPoint(
                    icon = Icons.Default.Notifications,
                    title = "Smart Alerts",
                    description = "Receive timely notifications about your expenses and budgets"
                )
            }
        }
    }
}

@Composable
fun InfoPoint(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MinimalDarkColors.CategoryGreen,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MinimalDarkColors.Gray300
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MinimalDarkColors.Gray500,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    errorMessage: String?,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    prefix: String? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MinimalDarkColors.Gray300
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = MinimalDarkColors.Gray600
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (errorMessage != null) 
                        MinimalDarkColors.CategoryRed 
                    else 
                        MinimalDarkColors.Indigo400
                )
            },
            prefix = prefix?.let {
                {
                    Text(
                        text = it,
                        color = MinimalDarkColors.Gray400
                    )
                }
            },
            trailingIcon = {
                if (errorMessage != null) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MinimalDarkColors.CategoryRed
                    )
                } else if (value.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Valid",
                        tint = MinimalDarkColors.CategoryGreen
                    )
                }
            },
            isError = errorMessage != null,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = MinimalDarkColors.Gray300,
                focusedContainerColor = MinimalDarkColors.Gray800.copy(alpha = 0.6f),
                unfocusedContainerColor = MinimalDarkColors.Gray800.copy(alpha = 0.4f),
                errorContainerColor = MinimalDarkColors.CategoryRed.copy(alpha = 0.1f),
                focusedBorderColor = MinimalDarkColors.Indigo500,
                unfocusedBorderColor = MinimalDarkColors.Gray700,
                errorBorderColor = MinimalDarkColors.CategoryRed,
                cursorColor = MinimalDarkColors.Indigo400
            )
        )
        
        // Error message
        if (errorMessage != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MinimalDarkColors.CategoryRed,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = errorMessage,
                    fontSize = 12.sp,
                    color = MinimalDarkColors.CategoryRed
                )
            }
        }
    }
}

@Composable
fun BenefitsSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "What you'll get:",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MinimalDarkColors.Gray300
        )
        
        BenefitItem(
            icon = Icons.Default.FilterAlt,
            title = "Accurate Expense Tracking",
            description = "Self-transactions will be automatically filtered out"
        )
        
        BenefitItem(
            icon = Icons.Default.Psychology,
            title = "AI-Powered Insights",
            description = "Get personalized recommendations to optimize spending"
        )
        
        BenefitItem(
            icon = Icons.Default.Security,
            title = "Secure & Private",
            description = "Your data is encrypted and stored securely"
        )
    }
}

@Composable
fun BenefitItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MinimalDarkColors.Indigo600.copy(alpha = 0.3f),
                            MinimalDarkColors.Purple600.copy(alpha = 0.2f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MinimalDarkColors.Indigo400,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = MinimalDarkColors.Gray400,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// Preview
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LetsGetStartedOnboardingScreenPreview() {
    LetsGetStartedOnboardingScreen()
}
