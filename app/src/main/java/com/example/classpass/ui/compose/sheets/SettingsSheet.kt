package com.example.classpass.ui.compose.sheets

import android.app.Application
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.classpass.ui.theme.BackgroundDark
import com.example.classpass.ui.theme.CardDark
import com.example.classpass.ui.theme.ErrorRed
import com.example.classpass.ui.theme.PrimaryGreen
import com.example.classpass.ui.theme.TextSecondaryDark
import com.example.classpass.ui.theme.ToggleInactive
import com.example.classpass.ui.compose.common.BottomSheetDragHandle
import com.example.classpass.ui.viewmodel.SettingsViewModel

/**
 * Settings bottom sheet with nested navigation (Claude-style).
 */

// Settings sheet constants
private val SHEET_PADDING_HORIZONTAL = 20.dp
private val SHEET_PADDING_TOP = 24.dp
private val SHEET_PADDING_BOTTOM = 40.dp
private val HEADER_PADDING_BOTTOM = 24.dp
private val ITEM_PADDING_VERTICAL = 4.dp
private val ITEM_PADDING_INTERNAL = 16.dp
private val ICON_SIZE = 24.dp
private val CORNER_RADIUS = 12.dp
private val SPACER_HEIGHT = 20.dp

// Text sizes
private val TITLE_TEXT_SIZE = 20.sp
private val ITEM_TEXT_SIZE = 16.sp
private val SUBTITLE_TEXT_SIZE = 14.sp
private val EMAIL_TEXT_SIZE = 16.sp

// Toggle switch dimensions
private val TOGGLE_WIDTH = 48.dp
private val TOGGLE_HEIGHT = 28.dp
private val TOGGLE_THUMB_SIZE = 24.dp
private val TOGGLE_PADDING = 2.dp

/**
 * Settings navigation screens.
 */
enum class SettingsScreen {
    MAIN,
    PROFILE,
    PROFILE_EDIT_NAME,
    PROFILE_EDIT_EMAIL,
    PROFILE_EDIT_AGE_GENDER,
    PROFILE_EDIT_HEIGHT_WEIGHT,
    PROFILE_EDIT_FITNESS_LEVEL,
    PROFILE_EDIT_PRIMARY_GOAL,
    PROFILE_EDIT_INJURIES,
    PREFERENCES_EDIT_UNITS,
    PREFERENCES_EDIT_TRAINING_DAYS,
    BILLING,
    SPEECH_LANGUAGE,
    PRIVACY
}

/**
 * Settings option data class.
 */
data class SettingsOption(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

/**
 * Get the depth of a screen in the navigation hierarchy.
 * Used to determine animation direction (back vs forward).
 */
private fun getScreenDepth(screen: SettingsScreen): Int {
    return when (screen) {
        SettingsScreen.MAIN -> 0
        SettingsScreen.PROFILE,
        SettingsScreen.BILLING,
        SettingsScreen.SPEECH_LANGUAGE,
        SettingsScreen.PRIVACY -> 1
        SettingsScreen.PROFILE_EDIT_NAME,
        SettingsScreen.PROFILE_EDIT_EMAIL,
        SettingsScreen.PROFILE_EDIT_AGE_GENDER,
        SettingsScreen.PROFILE_EDIT_HEIGHT_WEIGHT,
        SettingsScreen.PROFILE_EDIT_FITNESS_LEVEL,
        SettingsScreen.PROFILE_EDIT_PRIMARY_GOAL,
        SettingsScreen.PROFILE_EDIT_INJURIES -> 2
        SettingsScreen.PREFERENCES_EDIT_UNITS,
        SettingsScreen.PREFERENCES_EDIT_TRAINING_DAYS -> 1
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    // Navigation state for nested screens
    var currentScreen by remember { mutableStateOf(SettingsScreen.MAIN) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // Observe ViewModel state
    val currentUser by viewModel.currentUser.observeAsState()
    
    // Animated content with slide transitions
    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            // Determine navigation depth for proper animation direction
            val targetDepth = getScreenDepth(targetState)
            val initialDepth = getScreenDepth(initialState)
            
            if (targetDepth < initialDepth) {
                // Going back (slide from left to right)
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it })
            } else {
                // Going forward (slide from right to left)
                slideInHorizontally(initialOffsetX = { it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { -it })
            }
        },
        label = "settings_navigation"
    ) { screen ->
        when (screen) {
            SettingsScreen.MAIN -> MainSettingsContent(
                currentUser = currentUser,
                onDismiss = onDismiss,
                onNavigateToProfile = { currentScreen = SettingsScreen.PROFILE },
                onNavigateToBilling = { currentScreen = SettingsScreen.BILLING },
                onNavigateToSpeechLanguage = { currentScreen = SettingsScreen.SPEECH_LANGUAGE },
                onNavigateToPrivacy = { currentScreen = SettingsScreen.PRIVACY },
                onNavigateToUnits = { currentScreen = SettingsScreen.PREFERENCES_EDIT_UNITS },
                onNavigateToTrainingDays = { currentScreen = SettingsScreen.PREFERENCES_EDIT_TRAINING_DAYS },
                viewModel = viewModel
            )
            SettingsScreen.PROFILE -> ProfileSettingsContent(
                currentUser = currentUser,
                selectedUnits = viewModel.selectedUnits.observeAsState().value ?: "Metric",
                onBack = { currentScreen = SettingsScreen.MAIN },
                onEditName = { currentScreen = SettingsScreen.PROFILE_EDIT_NAME },
                onEditEmail = { currentScreen = SettingsScreen.PROFILE_EDIT_EMAIL },
                onEditAgeGender = { currentScreen = SettingsScreen.PROFILE_EDIT_AGE_GENDER },
                onEditHeightWeight = { currentScreen = SettingsScreen.PROFILE_EDIT_HEIGHT_WEIGHT },
                onEditFitnessLevel = { currentScreen = SettingsScreen.PROFILE_EDIT_FITNESS_LEVEL },
                onEditPrimaryGoal = { currentScreen = SettingsScreen.PROFILE_EDIT_PRIMARY_GOAL },
                onEditInjuries = { currentScreen = SettingsScreen.PROFILE_EDIT_INJURIES },
                onDeleteAccount = { showDeleteConfirmation = true }
            )
            SettingsScreen.PROFILE_EDIT_NAME -> EditNameScreen(
                currentUser = currentUser,
                onBack = { currentScreen = SettingsScreen.PROFILE },
                onSave = { newName ->
                    viewModel.updateUserName(newName)
                    currentScreen = SettingsScreen.PROFILE
                }
            )
            SettingsScreen.PROFILE_EDIT_EMAIL -> EditEmailScreen(
                currentUser = currentUser,
                onBack = { currentScreen = SettingsScreen.PROFILE },
                onSave = { newEmail ->
                    viewModel.updateUserEmail(newEmail)
                    currentScreen = SettingsScreen.PROFILE
                }
            )
            SettingsScreen.PROFILE_EDIT_AGE_GENDER -> EditAgeGenderScreen(
                currentUser = currentUser,
                onBack = { currentScreen = SettingsScreen.PROFILE },
                onSave = { age, gender ->
                    viewModel.updateUserAgeGender(age, gender)
                    currentScreen = SettingsScreen.PROFILE
                }
            )
            SettingsScreen.PROFILE_EDIT_HEIGHT_WEIGHT -> EditHeightWeightScreen(
                currentUser = currentUser,
                selectedUnits = viewModel.selectedUnits.observeAsState().value ?: "Metric",
                onBack = { currentScreen = SettingsScreen.PROFILE },
                onSave = { height, weight ->
                    viewModel.updateUserHeightWeight(height, weight)
                    currentScreen = SettingsScreen.PROFILE
                }
            )
            SettingsScreen.PROFILE_EDIT_FITNESS_LEVEL -> EditFitnessLevelScreen(
                currentUser = currentUser,
                onBack = { currentScreen = SettingsScreen.PROFILE },
                onSave = { level ->
                    viewModel.updateUserFitnessLevel(level)
                    currentScreen = SettingsScreen.PROFILE
                }
            )
            SettingsScreen.PROFILE_EDIT_PRIMARY_GOAL -> EditPrimaryGoalScreen(
                currentUser = currentUser,
                onBack = { currentScreen = SettingsScreen.PROFILE },
                onSave = { goal ->
                    viewModel.updateUserPrimaryGoal(goal)
                    currentScreen = SettingsScreen.PROFILE
                }
            )
            SettingsScreen.PROFILE_EDIT_INJURIES -> EditInjuriesScreen(
                currentUser = currentUser,
                onBack = { currentScreen = SettingsScreen.PROFILE },
                onSave = { injuries ->
                    viewModel.updateUserInjuries(injuries)
                    currentScreen = SettingsScreen.PROFILE
                }
            )
            SettingsScreen.PREFERENCES_EDIT_UNITS -> EditUnitsScreen(
                currentUnits = viewModel.selectedUnits.observeAsState().value ?: "Metric",
                onBack = { currentScreen = SettingsScreen.MAIN },
                onSave = { units ->
                    viewModel.updateUnits(units)
                    currentScreen = SettingsScreen.MAIN
                }
            )
            SettingsScreen.PREFERENCES_EDIT_TRAINING_DAYS -> EditTrainingDaysScreen(
                currentDays = viewModel.trainingDaysPerWeek.observeAsState().value ?: 4,
                onBack = { currentScreen = SettingsScreen.MAIN },
                onSave = { days ->
                    viewModel.updateTrainingDays(days)
                    currentScreen = SettingsScreen.MAIN
                }
            )
            SettingsScreen.BILLING -> BillingSettingsContent(
                onBack = { currentScreen = SettingsScreen.MAIN }
            )
            SettingsScreen.SPEECH_LANGUAGE -> PlaceholderSettingsContent(
                title = "Speech language",
                onBack = { currentScreen = SettingsScreen.MAIN }
            )
            SettingsScreen.PRIVACY -> PlaceholderSettingsContent(
                title = "Privacy",
                onBack = { currentScreen = SettingsScreen.MAIN }
            )
        }
    }
    
    // Delete Account Confirmation Bottom Sheet
    if (showDeleteConfirmation) {
        ModalBottomSheet(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = CardDark,
            dragHandle = { BottomSheetDragHandle() }
        ) {
            DeleteAccountConfirmationSheet(
                onConfirm = {
                    viewModel.deleteAccount(onSuccess = {
                        showDeleteConfirmation = false
                        onDismiss()
                        // TODO: Navigate to login/welcome screen
                    })
                },
                onCancel = { showDeleteConfirmation = false }
            )
        }
    }
}

/**
 * Main settings screen content.
 */
@Composable
private fun MainSettingsContent(
    currentUser: com.example.classpass.data.model.User?,
    onDismiss: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onNavigateToSpeechLanguage: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToUnits: () -> Unit,
    onNavigateToTrainingDays: () -> Unit,
    viewModel: SettingsViewModel
) {
    val settingsOptions = remember {
        listOf(
            SettingsOption("Profile", Icons.Default.Person, onNavigateToProfile),
            SettingsOption("Billing", Icons.Default.AttachMoney, onNavigateToBilling),
            SettingsOption("Speech language", Icons.Default.Language, onNavigateToSpeechLanguage),
            SettingsOption("Privacy", Icons.Default.Lock, onNavigateToPrivacy)
        )
    }
    
    // Preferences state
    var selectedUnits by remember { mutableStateOf("Metric") }
    var selectedDaysPerWeek by remember { mutableStateOf(4) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SHEET_PADDING_HORIZONTAL)
    ) {
        // FIXED HEADER - Does not scroll
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = SHEET_PADDING_TOP)
        ) {
            // Header with close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = HEADER_PADDING_BOTTOM),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondaryDark
                    )
                }
                
                Text(
                    text = "Settings",
                    fontSize = TITLE_TEXT_SIZE,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // Empty spacer to keep title centered
                Spacer(modifier = Modifier.size(48.dp))
            }
            
            // User email (from ViewModel)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = ITEM_PADDING_INTERNAL),
                shape = RoundedCornerShape(CORNER_RADIUS),
                color = BackgroundDark
            ) {
                Text(
                    text = currentUser?.email ?: "andyibrahim99@hotmail.com",
                    fontSize = EMAIL_TEXT_SIZE,
                    color = Color.White,
                    modifier = Modifier.padding(ITEM_PADDING_INTERNAL)
                )
            }
        }
        
        // SCROLLABLE CONTENT - Only this part scrolls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Settings options list
            settingsOptions.forEach { option ->
                SettingsOptionItem(
                    title = option.title,
                    icon = option.icon,
                    onClick = option.onClick
                )
            }
            
            Spacer(modifier = Modifier.height(SPACER_HEIGHT))
            
            // Preferences Section
            Text(
                text = "Preferences",
                fontSize = SUBTITLE_TEXT_SIZE,
                fontWeight = FontWeight.Medium,
                color = TextSecondaryDark,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            
            // Units preference
            SettingsOptionItem(
                title = "Units",
                icon = Icons.Default.Straighten,
                subtitle = viewModel.selectedUnits.observeAsState().value?.let { units ->
                    if (units == "Metric") "Metric (kg, cm)" else "Imperial (lbs, ft)"
                } ?: "Metric (kg, cm)",
                onClick = onNavigateToUnits
            )
            
            // Days per week preference
            SettingsOptionItem(
                title = "Training Days per Week",
                icon = Icons.Default.CalendarToday,
                subtitle = "${viewModel.trainingDaysPerWeek.observeAsState().value ?: 4} days",
                onClick = onNavigateToTrainingDays
            )
            
            Spacer(modifier = Modifier.height(SPACER_HEIGHT))
            
            // Log out button
            DestructiveActionItem(
                icon = Icons.Default.ExitToApp,
                label = "Log out",
                onClick = { viewModel.logout() }
            )
            
            Spacer(modifier = Modifier.height(SHEET_PADDING_BOTTOM))
        }
    }
}

/**
 * Profile settings content - Settings-style layout.
 */
@Composable
private fun ProfileSettingsContent(
    currentUser: com.example.classpass.data.model.User?,
    selectedUnits: String,
    onBack: () -> Unit,
    onEditName: () -> Unit,
    onEditEmail: () -> Unit,
    onEditAgeGender: () -> Unit,
    onEditHeightWeight: () -> Unit,
    onEditFitnessLevel: () -> Unit,
    onEditPrimaryGoal: () -> Unit,
    onEditInjuries: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = SHEET_PADDING_HORIZONTAL)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = SHEET_PADDING_TOP, bottom = HEADER_PADDING_BOTTOM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextSecondaryDark
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Profile",
                fontSize = TITLE_TEXT_SIZE,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = ITEM_PADDING_INTERNAL),
                shape = RoundedCornerShape(CORNER_RADIUS),
                color = BackgroundDark
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ITEM_PADDING_INTERNAL),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile photo placeholder
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        color = PrimaryGreen
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    
                    // Name and email
                    Column {
                        Text(
                            text = "Andy Ibrahim",
                            fontSize = ITEM_TEXT_SIZE,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Text(
                            text = "andyibrahim99@hotmail.com",
                            fontSize = SUBTITLE_TEXT_SIZE,
                            color = TextSecondaryDark
                        )
                    }
                }
            }
            
            // Personal Information Section
            Text(
                text = "Personal Information",
                fontSize = SUBTITLE_TEXT_SIZE,
                fontWeight = FontWeight.Medium,
                color = TextSecondaryDark,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            
            ProfileOptionItem(
                icon = Icons.Default.Person,
                label = "Name",
                value = currentUser?.name ?: "",
                onClick = onEditName
            )
            
            ProfileOptionItem(
                icon = Icons.Default.Email,
                label = "Email",
                value = currentUser?.email ?: "",
                onClick = onEditEmail
            )
            
            Spacer(modifier = Modifier.height(SPACER_HEIGHT))
            
            // Fitness Profile Section
            Text(
                text = "Fitness Profile",
                fontSize = SUBTITLE_TEXT_SIZE,
                fontWeight = FontWeight.Medium,
                color = TextSecondaryDark,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            
            ProfileOptionItem(
                icon = Icons.Default.Cake,
                label = "Age & Gender",
                value = if (currentUser?.age != null && currentUser.gender != null) {
                    "${currentUser.age}, ${currentUser.gender}"
                } else "",
                onClick = onEditAgeGender
            )
            
            ProfileOptionItem(
                icon = Icons.Default.Height,
                label = "Height & Weight",
                value = if (currentUser?.height != null && currentUser.weight != null) {
                    if (selectedUnits == "Metric") {
                        "${currentUser.height}cm, ${currentUser.weight}kg"
                    } else {
                        // Convert to Imperial
                        val heightInFeet = (currentUser.height!! * 0.0328084).toInt()
                        val heightInInches = ((currentUser.height * 0.393701) % 12).toInt()
                        val weightInLbs = (currentUser.weight!! * 2.20462).toInt()
                        "${heightInFeet}'${heightInInches}\", ${weightInLbs}lbs"
                    }
                } else "",
                onClick = onEditHeightWeight
            )
            
            ProfileOptionItem(
                icon = Icons.Default.BarChart,
                label = "Fitness Level",
                value = currentUser?.fitnessLevel ?: "",
                onClick = onEditFitnessLevel
            )
            
            ProfileOptionItem(
                icon = Icons.Default.FitnessCenter,
                label = "Primary Goal",
                value = currentUser?.primaryGoal ?: "",
                onClick = onEditPrimaryGoal
            )
            
            ProfileOptionItem(
                icon = Icons.Default.HealthAndSafety,
                label = "Injuries/Limitations",
                value = currentUser?.injuries?.ifEmpty { "None" } ?: "None",
                onClick = onEditInjuries
            )
            
            Spacer(modifier = Modifier.height(SPACER_HEIGHT))
            
            // Delete Account
            DestructiveActionItem(
                icon = Icons.Default.Delete,
                label = "Delete Account",
                onClick = onDeleteAccount
            )
            
            Spacer(modifier = Modifier.height(SHEET_PADDING_BOTTOM))
        }
    }
}

// ========================================
// PROFILE EDIT SCREENS
// ========================================

/**
 * Edit Name Screen
 */
@Composable
private fun EditNameScreen(
    currentUser: com.example.classpass.data.model.User?,
    onBack: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentUser?.name ?: "") }
    
    EditTextFieldScreen(
        title = "Name",
        label = "Full Name",
        value = name,
        onValueChange = { name = it },
        onBack = onBack,
        onSave = { onSave(name) },
        placeholder = "Enter your full name"
    )
}

/**
 * Edit Email Screen
 */
@Composable
private fun EditEmailScreen(
    currentUser: com.example.classpass.data.model.User?,
    onBack: () -> Unit,
    onSave: (String) -> Unit
) {
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    
    EditTextFieldScreen(
        title = "Email",
        label = "Email Address",
        value = email,
        onValueChange = { email = it },
        onBack = onBack,
        onSave = { onSave(email) },
        placeholder = "Enter your email"
    )
}

/**
 * Edit Age & Gender Screen - Clean design
 */
@Composable
private fun EditAgeGenderScreen(
    currentUser: com.example.classpass.data.model.User?,
    onBack: () -> Unit,
    onSave: (Int, String) -> Unit
) {
    var age by remember { mutableStateOf(currentUser?.age?.toString() ?: "") }
    var selectedGender by remember { mutableStateOf(currentUser?.gender ?: "Male") }
    val genderOptions = listOf("Male", "Female", "Other")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = SHEET_PADDING_HORIZONTAL)
    ) {
        // Header
        EditScreenHeader(title = "Age & Gender", onBack = onBack)
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Age input
            Text(
                text = "Age",
                fontSize = SUBTITLE_TEXT_SIZE,
                color = TextSecondaryDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = age,
                onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 3) age = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = SPACER_HEIGHT),
                placeholder = { Text("Enter your age", color = TextSecondaryDark) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = BackgroundDark,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = PrimaryGreen,
                    focusedContainerColor = BackgroundDark,
                    unfocusedContainerColor = BackgroundDark
                ),
                shape = RoundedCornerShape(CORNER_RADIUS),
                singleLine = true
            )
            
            // Gender selection
            Text(
                text = "Gender",
                fontSize = SUBTITLE_TEXT_SIZE,
                color = TextSecondaryDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = SPACER_HEIGHT),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                genderOptions.forEach { gender ->
                    ChipButton(
                        label = gender,
                        isSelected = selectedGender == gender,
                        onClick = { selectedGender = gender },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(SPACER_HEIGHT))
        }
        
        // Save button
        CleanSaveButton(
            enabled = age.isNotEmpty(),
            onClick = {
                age.toIntOrNull()?.let { ageInt ->
                    onSave(ageInt, selectedGender)
                }
            }
        )
    }
}

/**
 * Chip button for selection - Clean design
 */
@Composable
private fun ChipButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(CORNER_RADIUS),
        color = if (isSelected) PrimaryGreen else BackgroundDark,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, TextSecondaryDark)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = SUBTITLE_TEXT_SIZE,
                color = if (isSelected) Color.Black else Color.White,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Edit Height & Weight Screen - Clean design
 */
@Composable
private fun EditHeightWeightScreen(
    currentUser: com.example.classpass.data.model.User?,
    selectedUnits: String,
    onBack: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    // Convert stored values (always in metric) to display units
    val displayHeight = if (selectedUnits == "Imperial" && currentUser?.height != null) {
        (currentUser.height * 0.393701).toInt().toString() // cm to inches
    } else {
        currentUser?.height?.toString() ?: ""
    }
    val displayWeight = if (selectedUnits == "Imperial" && currentUser?.weight != null) {
        (currentUser.weight * 2.20462).toInt().toString() // kg to lbs
    } else {
        currentUser?.weight?.toString() ?: ""
    }
    
    var height by remember { mutableStateOf(displayHeight) }
    var weight by remember { mutableStateOf(displayWeight) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = SHEET_PADDING_HORIZONTAL)
    ) {
        // Header
        EditScreenHeader(title = "Height & Weight", onBack = onBack)
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Height input
            Text(
                text = if (selectedUnits == "Metric") "Height (cm)" else "Height (inches)",
                fontSize = SUBTITLE_TEXT_SIZE,
                color = TextSecondaryDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = height,
                onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 3) height = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = SPACER_HEIGHT),
                placeholder = { Text("Enter your height", color = TextSecondaryDark) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = BackgroundDark,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = PrimaryGreen,
                    focusedContainerColor = BackgroundDark,
                    unfocusedContainerColor = BackgroundDark
                ),
                shape = RoundedCornerShape(CORNER_RADIUS),
                singleLine = true
            )
            
            // Weight input
            Text(
                text = if (selectedUnits == "Metric") "Weight (kg)" else "Weight (lbs)",
                fontSize = SUBTITLE_TEXT_SIZE,
                color = TextSecondaryDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = weight,
                onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 3) weight = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = SPACER_HEIGHT),
                placeholder = { Text("Enter your weight", color = TextSecondaryDark) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = BackgroundDark,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = PrimaryGreen,
                    focusedContainerColor = BackgroundDark,
                    unfocusedContainerColor = BackgroundDark
                ),
                shape = RoundedCornerShape(CORNER_RADIUS),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(SPACER_HEIGHT))
        }
        
        // Save button
        CleanSaveButton(
            enabled = height.isNotEmpty() && weight.isNotEmpty(),
            onClick = {
                val heightInt = height.toIntOrNull()
                val weightInt = weight.toIntOrNull()
                if (heightInt != null && weightInt != null) {
                    // Convert to metric if needed before saving
                    val heightInCm = if (selectedUnits == "Imperial") {
                        (heightInt * 2.54).toInt() // inches to cm
                    } else {
                        heightInt
                    }
                    val weightInKg = if (selectedUnits == "Imperial") {
                        (weightInt / 2.20462).toInt() // lbs to kg
                    } else {
                        weightInt
                    }
                    onSave(heightInCm, weightInKg)
                }
            }
        )
    }
}

/**
 * Edit Fitness Level Screen - Clean design
 */
@Composable
private fun EditFitnessLevelScreen(
    currentUser: com.example.classpass.data.model.User?,
    onBack: () -> Unit,
    onSave: (String) -> Unit
) {
    var selectedLevel by remember { mutableStateOf(currentUser?.fitnessLevel ?: "Beginner") }
    val levels = listOf("Beginner", "Intermediate", "Advanced")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = SHEET_PADDING_HORIZONTAL)
    ) {
        // Header
        EditScreenHeader(title = "Fitness Level", onBack = onBack)
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = SPACER_HEIGHT),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                levels.forEach { level ->
                    ChipButton(
                        label = level,
                        isSelected = selectedLevel == level,
                        onClick = { selectedLevel = level },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Save button
        CleanSaveButton(
            enabled = true,
            onClick = { onSave(selectedLevel) }
        )
    }
}

/**
 * Edit Primary Goal Screen - Clean design
 */
@Composable
private fun EditPrimaryGoalScreen(
    currentUser: com.example.classpass.data.model.User?,
    onBack: () -> Unit,
    onSave: (String) -> Unit
) {
    var selectedGoal by remember { mutableStateOf(currentUser?.primaryGoal ?: "Build Muscle") }
    val goals = listOf("Lose Weight", "Build Muscle", "Get Stronger", "Improve Endurance", "General Fitness")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = SHEET_PADDING_HORIZONTAL)
    ) {
        // Header
        EditScreenHeader(title = "Primary Goal", onBack = onBack)
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            goals.chunked(2).forEach { rowGoals ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowGoals.forEach { goal ->
                        ChipButton(
                            label = goal,
                            isSelected = selectedGoal == goal,
                            onClick = { selectedGoal = goal },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Add empty space if odd number
                    if (rowGoals.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        
        // Save button
        CleanSaveButton(
            enabled = true,
            onClick = { onSave(selectedGoal) }
        )
    }
}

/**
 * Edit Injuries Screen - Clean design
 */
@Composable
private fun EditInjuriesScreen(
    currentUser: com.example.classpass.data.model.User?,
    onBack: () -> Unit,
    onSave: (String) -> Unit
) {
    var injuries by remember { mutableStateOf(currentUser?.injuries ?: "") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = SHEET_PADDING_HORIZONTAL)
    ) {
        // Header
        EditScreenHeader(title = "Injuries/Limitations", onBack = onBack)
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Injuries/Limitations",
                fontSize = SUBTITLE_TEXT_SIZE,
                color = TextSecondaryDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = injuries,
                onValueChange = { injuries = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("e.g., Bad knee, Lower back pain", color = TextSecondaryDark) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = BackgroundDark,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = PrimaryGreen,
                    focusedContainerColor = BackgroundDark,
                    unfocusedContainerColor = BackgroundDark
                ),
                shape = RoundedCornerShape(CORNER_RADIUS),
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.height(SPACER_HEIGHT))
        }
        
        // Save button
        CleanSaveButton(
            enabled = true,
            onClick = { onSave(injuries) }
        )
    }
}

/**
 * Edit Units Screen - Clean design
 */
@Composable
private fun EditUnitsScreen(
    currentUnits: String,
    onBack: () -> Unit,
    onSave: (String) -> Unit
) {
    var selectedUnits by remember { mutableStateOf(currentUnits) }
    val unitsOptions = listOf(
        "Metric (kg, cm)" to "Metric",
        "Imperial (lbs, ft)" to "Imperial"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = SHEET_PADDING_HORIZONTAL)
    ) {
        // Header
        EditScreenHeader(title = "Units", onBack = onBack)
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            unitsOptions.forEach { (label, value) ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedUnits = value },
                    shape = RoundedCornerShape(CORNER_RADIUS),
                    color = if (selectedUnits == value) Color.White.copy(alpha = 0.1f) else BackgroundDark
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ITEM_PADDING_INTERNAL),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            fontSize = ITEM_TEXT_SIZE,
                            color = Color.White,
                            fontWeight = if (selectedUnits == value) FontWeight.Medium else FontWeight.Normal
                        )
                        
                        if (selectedUnits == value) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(ICON_SIZE)
                            )
                        }
                    }
                }
            }
        }
        
        // Save button
        CleanSaveButton(
            enabled = true,
            onClick = { onSave(selectedUnits) }
        )
    }
}

/**
 * Edit Training Days Screen - Clean design
 */
@Composable
private fun EditTrainingDaysScreen(
    currentDays: Int,
    onBack: () -> Unit,
    onSave: (Int) -> Unit
) {
    var selectedDays by remember { mutableStateOf(currentDays) }
    val daysOptions = (3..7).toList()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = SHEET_PADDING_HORIZONTAL)
    ) {
        // Header
        EditScreenHeader(title = "Training Days per Week", onBack = onBack)
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            daysOptions.forEach { days ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedDays = days },
                    shape = RoundedCornerShape(CORNER_RADIUS),
                    color = if (selectedDays == days) Color.White.copy(alpha = 0.1f) else BackgroundDark
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ITEM_PADDING_INTERNAL),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$days days per week",
                            fontSize = ITEM_TEXT_SIZE,
                            color = Color.White,
                            fontWeight = if (selectedDays == days) FontWeight.Medium else FontWeight.Normal
                        )
                        
                        if (selectedDays == days) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(ICON_SIZE)
                            )
                        }
                    }
                }
            }
        }
        
        // Save button
        CleanSaveButton(
            enabled = true,
            onClick = { onSave(selectedDays) }
        )
    }
}

/**
 * Edit Appearance Screen - Clean design (Dark/Light mode)
 */
// ========================================
// REUSABLE EDIT SCREEN COMPONENTS
// ========================================

/**
 * Generic text field edit screen - Clean design
 */
@Composable
private fun EditTextFieldScreen(
    title: String,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    placeholder: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = SHEET_PADDING_HORIZONTAL)
    ) {
        // Header
        EditScreenHeader(title = title, onBack = onBack)
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = label,
                fontSize = SUBTITLE_TEXT_SIZE,
                color = TextSecondaryDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder, color = TextSecondaryDark) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = BackgroundDark,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = PrimaryGreen,
                    focusedContainerColor = BackgroundDark,
                    unfocusedContainerColor = BackgroundDark
                ),
                shape = RoundedCornerShape(CORNER_RADIUS),
                singleLine = true
            )
        }
        
        // Save button
        CleanSaveButton(
            enabled = value.isNotEmpty(),
            onClick = onSave
        )
    }
}

/**
 * Generic selection screen with options
 */
@Composable
private fun EditSelectionScreen(
    title: String,
    options: List<Pair<String, String>>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = SHEET_PADDING_HORIZONTAL)
    ) {
        // Header
        EditScreenHeader(title = title, onBack = onBack)
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            options.forEach { (option, description) ->
                SelectionOptionItemWithDescription(
                    label = option,
                    description = description,
                    isSelected = selectedOption == option,
                    onClick = { onOptionSelected(option) }
                )
            }
            
            Spacer(modifier = Modifier.height(SPACER_HEIGHT))
        }
        
        // Save button
        SaveButton(
            enabled = true,
            onClick = onSave
        )
    }
}

/**
 * Edit screen header with back button
 */
@Composable
private fun EditScreenHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = SHEET_PADDING_TOP, bottom = HEADER_PADDING_BOTTOM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextSecondaryDark
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = title,
            fontSize = TITLE_TEXT_SIZE,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Save button for edit screens (legacy - keeping for compatibility)
 */
@Composable
private fun SaveButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = SHEET_PADDING_BOTTOM),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryGreen,
            disabledContainerColor = TextSecondaryDark
        ),
        shape = RoundedCornerShape(CORNER_RADIUS)
    ) {
        Text(
            text = "Save",
            fontSize = ITEM_TEXT_SIZE,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

/**
 * Clean save button - White background with black text
 */
@Composable
private fun CleanSaveButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = SHEET_PADDING_BOTTOM),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            disabledContainerColor = TextSecondaryDark,
            contentColor = Color.Black,
            disabledContentColor = Color.White
        ),
        shape = RoundedCornerShape(CORNER_RADIUS)
    ) {
        Text(
            text = "Save",
            fontSize = ITEM_TEXT_SIZE,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

/**
 * Selection option item (simple)
 */
@Composable
private fun SelectionOptionItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ITEM_PADDING_VERTICAL)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CORNER_RADIUS),
        color = if (isSelected) Color.White.copy(alpha = 0.1f) else BackgroundDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ITEM_PADDING_INTERNAL),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = ITEM_TEXT_SIZE,
                color = Color.White,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
            
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(ICON_SIZE)
                )
            }
        }
    }
}

/**
 * Selection option item with description
 */
@Composable
private fun SelectionOptionItemWithDescription(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ITEM_PADDING_VERTICAL)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CORNER_RADIUS),
        color = if (isSelected) Color.White.copy(alpha = 0.1f) else BackgroundDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ITEM_PADDING_INTERNAL),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = ITEM_TEXT_SIZE,
                    color = Color.White,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
                Text(
                    text = description,
                    fontSize = SUBTITLE_TEXT_SIZE,
                    color = TextSecondaryDark,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(ICON_SIZE)
                )
            }
        }
    }
}

// ========================================
// PROFILE COMPONENTS
// ========================================

/**
 * Profile option item with icon, label, value, and arrow.
 */
@Composable
private fun ProfileOptionItem(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ITEM_PADDING_VERTICAL)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CORNER_RADIUS),
        color = BackgroundDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ITEM_PADDING_INTERNAL),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(ICON_SIZE)
                )
                Column {
                    Text(
                        text = label,
                        fontSize = SUBTITLE_TEXT_SIZE,
                        color = TextSecondaryDark
                    )
                    Text(
                        text = value,
                        fontSize = ITEM_TEXT_SIZE,
                        color = Color.White
                    )
                }
            }
            
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondaryDark,
                modifier = Modifier.size(ICON_SIZE)
            )
        }
    }
}

/**
 * Billing settings content (empty for now).
 */
@Composable
private fun BillingSettingsContent(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = SHEET_PADDING_HORIZONTAL)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = SHEET_PADDING_TOP, bottom = HEADER_PADDING_BOTTOM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextSecondaryDark
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Billing",
                fontSize = TITLE_TEXT_SIZE,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        // Empty content - to be designed
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Billing content\n(To be designed)",
                fontSize = SUBTITLE_TEXT_SIZE,
                color = TextSecondaryDark
            )
        }
        
        Spacer(modifier = Modifier.height(SHEET_PADDING_BOTTOM))
    }
}

/**
 * Placeholder content for other settings screens.
 */
@Composable
private fun PlaceholderSettingsContent(
    title: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = SHEET_PADDING_HORIZONTAL)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = SHEET_PADDING_TOP, bottom = HEADER_PADDING_BOTTOM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextSecondaryDark
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = title,
                fontSize = TITLE_TEXT_SIZE,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        // Empty content - to be designed
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$title content\n(To be designed)",
                fontSize = SUBTITLE_TEXT_SIZE,
                color = TextSecondaryDark
            )
        }
        
        Spacer(modifier = Modifier.height(SHEET_PADDING_BOTTOM))
    }
}

/**
 * Settings option item with icon, label, and arrow.
 */
@Composable
private fun SettingsOptionItem(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ITEM_PADDING_VERTICAL)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CORNER_RADIUS),
        color = BackgroundDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ITEM_PADDING_INTERNAL),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(ICON_SIZE)
                )
                Column {
                    Text(
                        text = title,
                        fontSize = ITEM_TEXT_SIZE,
                        color = Color.White
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = SUBTITLE_TEXT_SIZE,
                            color = TextSecondaryDark
                        )
                    }
                }
            }
            
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondaryDark,
                modifier = Modifier.size(ICON_SIZE)
            )
        }
    }
}

/**
 * Preference option item with icon, label, current value, and arrow.
 */
@Composable
private fun PreferenceOptionItem(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ITEM_PADDING_VERTICAL)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CORNER_RADIUS),
        color = BackgroundDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ITEM_PADDING_INTERNAL),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(ICON_SIZE)
                )
                Column {
                    Text(
                        text = label,
                        fontSize = SUBTITLE_TEXT_SIZE,
                        color = TextSecondaryDark
                    )
                    Text(
                        text = value,
                        fontSize = ITEM_TEXT_SIZE,
                        color = Color.White
                    )
                }
            }
            
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondaryDark,
                modifier = Modifier.size(ICON_SIZE)
            )
        }
    }
}

/**
 * Destructive action item (e.g., logout, delete).
 */
@Composable
private fun DestructiveActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CORNER_RADIUS),
        color = BackgroundDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ITEM_PADDING_INTERNAL),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(ICON_SIZE)
            )
            Text(
                text = label,
                fontSize = ITEM_TEXT_SIZE,
                color = ErrorRed
            )
        }
    }
}

/**
 * Delete Account Confirmation Bottom Sheet.
 */
@Composable
private fun DeleteAccountConfirmationSheet(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SHEET_PADDING_HORIZONTAL)
            .padding(bottom = SHEET_PADDING_BOTTOM)
    ) {
        // Title
        Text(
            text = "Delete Account",
            fontSize = TITLE_TEXT_SIZE,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Warning message
        Text(
            text = "Are you sure you want to delete your account? This action cannot be undone. All your data, including workout history, chat sessions, and progress will be permanently deleted.",
            fontSize = ITEM_TEXT_SIZE,
            color = TextSecondaryDark,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Delete button
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed
                ),
                shape = RoundedCornerShape(CORNER_RADIUS)
            ) {
                Text(
                    text = "Delete Account",
                    fontSize = ITEM_TEXT_SIZE,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            
            // Cancel button
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, TextSecondaryDark),
                shape = RoundedCornerShape(CORNER_RADIUS)
            ) {
                Text(
                    text = "Cancel",
                    fontSize = ITEM_TEXT_SIZE,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
