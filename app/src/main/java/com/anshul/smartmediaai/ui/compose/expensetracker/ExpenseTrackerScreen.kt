package com.anshul.smartmediaai.ui.compose.expensetracker

import android.Manifest
import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavController
import com.anshul.smartmediaai.BuildConfig
import com.anshul.smartmediaai.BuildConfig.WEB_CLIENT_ID
import com.anshul.smartmediaai.ui.compose.expensetracker.ExpenseTrackerViewModel.Companion.TAG
import com.anshul.smartmediaai.ui.compose.expensetracker.state.ExpenseTrackerSideEffect
import com.anshul.smartmediaai.ui.compose.expensetracker.state.ExpenseTrackerSideEffect.ShowToast
import com.anshul.smartmediaai.ui.nav.Screen
import com.anshul.smartmediaai.ui.theme.PrimaryBlue
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.compose.collectSideEffect
import java.util.UUID
import androidx.core.content.edit
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.anshul.smartmediaai.core.wm.CleanUpWorker
import com.anshul.smartmediaai.core.wm.GmailSyncWorker
import com.anshul.smartmediaai.util.constants.ExpenseConstant.EMAIL_PREFS
import com.anshul.smartmediaai.util.constants.ExpenseConstant.EXPENSE_SHARED_PREFS
import com.anshul.smartmediaai.util.constants.ExpenseConstant.FIRST_GMAIL_SIGN_IN_PREF
import java.util.concurrent.TimeUnit


private const val TAG = "ExpenseTrackerScreen"
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerScreen(
    navController: NavController,
    viewModel: ExpenseTrackerViewModel = hiltViewModel(),
) {

    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sp = context.getSharedPreferences(EXPENSE_SHARED_PREFS, Context.MODE_PRIVATE)
    val coroutineScope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionResult(true)
            Log.d("SMS", "Permission Granted. Proceed with reading messages.")
        } else {
            viewModel.onPermissionResult(false)
            Log.d("SMS", "Permission Denied. Proceed without reading messages.")
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when(sideEffect){
            is ShowToast -> {
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            }

            is ExpenseTrackerSideEffect.RequestSmsPermission -> {
                permissionLauncher.launch(Manifest.permission.READ_SMS)
            }
        }

    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.fetchGmailAccessToken(context, "")

        } else {
            Toast.makeText(context, "Google Sign-in failed for user", Toast.LENGTH_SHORT).show()
        }

    }

    LaunchedEffect(state.gmailConsentIntent) {
        if(state.gmailConsentIntent !=null){
            launcher.launch(state.gmailConsentIntent!!)
            context.startActivity(state.gmailConsentIntent)
        }
    }

    fun scheduleWorkManager() {
        val workRequest = PeriodicWorkRequestBuilder<GmailSyncWorker>(
            1, TimeUnit.DAYS // Run once every 24 hours
        )
            .setInitialDelay(1, TimeUnit.HOURS) // optional delay before first run
            .addTag("gmail_sync_worker") // optional tag to identify it
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "gmail_sync_worker",
            ExistingPeriodicWorkPolicy.KEEP, // keep existing if already scheduled
            workRequest
        )


        val cleanupRequest = PeriodicWorkRequestBuilder<CleanUpWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "cleanup_old_expenses",
                ExistingPeriodicWorkPolicy.KEEP,
                cleanupRequest
            )


    }
    LaunchedEffect (Unit){
        scheduleWorkManager()
    }

    LaunchedEffect(Unit) {
        viewModel.delete30DaysOldExpenses()
    }


    fun handleSignInWithGoogleOption( context: Context,
                                      result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        val credential = result.credential

        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        context.getSharedPreferences(EXPENSE_SHARED_PREFS, Context.MODE_PRIVATE).edit {
                            putString(EMAIL_PREFS, googleIdTokenCredential.id)
                        }

                      //  viewModel.fetchGmailAccessToken(context, googleIdTokenCredential.id)
                        viewModel.fetchGmailData(context)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    // Catch any unrecognized credential type here.
                    //  postSideEffect(ExpenseTrackerSideEffect.ShowToast("Unexpected type of credential"))
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential")
                //  postSideEffect(ExpenseTrackerSideEffect.ShowToast("Sign-in failed for user"))
            }
        }
    }


    fun handleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        val credential = result.credential
        val responseJson: String

        when (credential) {

            // Passkey credential
            is PublicKeyCredential -> {
                // Share responseJson such as a GetCredentialResponse to your server to validate and
                // authenticate
                responseJson = credential.authenticationResponseJson
            }

            // Password credential
            is PasswordCredential -> {
                // Send ID and password to your server to validate and authenticate.
                val username = credential.id
                val password = credential.password
            }

            // GoogleIdToken credential
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract the ID to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        context.getSharedPreferences(EXPENSE_SHARED_PREFS, Context.MODE_PRIVATE).edit {
                            putString(EMAIL_PREFS, googleIdTokenCredential.id)
                        }
                       // viewModel.fetchGmailAccessToken(context, googleIdTokenCredential.id)
                        viewModel.fetchGmailData(context)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                    Log.e(TAG, "Unexpected type of credential")
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }


     fun createGoogleSignIn() {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(BuildConfig.WEB_CLIENT_ID)
            .setAutoSelectEnabled(true)
            // nonce string to use when generating a Google ID token
            .build()

        val credentialManager = CredentialManager.create(context)

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch {
            coroutineScope {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = context,
                    )
                    handleSignIn(result)
                } catch (e: GetCredentialException) {
                    e.printStackTrace()
                }
            }
        }

    }

    fun createGoogleSignInWithButton() {

        val activity = context as? Activity ?: return
        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(
            serverClientId = WEB_CLIENT_ID
        ).setNonce(UUID.randomUUID().toString())
            .build()


        val credentialManager = CredentialManager.create(activity)

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        coroutineScope.launch {
            try {

                val result = withContext(Dispatchers.IO) {
                    credentialManager.getCredential(
                        request = request,
                        context = activity,
                    )
                }
                handleSignInWithGoogleOption(activity, result)
            } catch (e: GetCredentialException) {
                e.printStackTrace()
            }
        }

    }



    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Expense Tracker") },
                colors = topAppBarColors(
                containerColor = PrimaryBlue,
                titleContentColor = Color.White,
            ))
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
           /* item {
                Button(onClick = { viewModel.scanSmsForExpenses() }) {
                    Text("Scan SMS for Expenses")
                }
            }*/

            item {
                GoogleSignInButtonCompose( {
                   if(sp.getBoolean(FIRST_GMAIL_SIGN_IN_PREF, true)){
                       createGoogleSignInWithButton()
                       sp.edit { putBoolean(FIRST_GMAIL_SIGN_IN_PREF, false) }
                   } else {
                       createGoogleSignIn()

                   }
                })
            }

            if (state.isLoading) {
                item {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage?.let {
                item {
                    Text("Error: $it", color = MaterialTheme.colorScheme.error)
                }
            }

            if(state.nativeChart.isNotEmpty()){
                item {
                    Text("Expense Categories Chart:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExpenseNativeChart(viewModel)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (state.expenses.isNotEmpty()) {
                item {
                    Button( onClick = {navController.navigate(Screen.ExpenseDetails.route)}) {
                        Text("View Expenses")
                    }
                }

            }

            if (state.recommendation?.isNotEmpty() == true) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        shape = AbsoluteRoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Header Row with Icon + Title
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ThumbUp, // 💡 Material icon
                                    contentDescription = "Recommendation",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Recommendation",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Body Text
                            Text(
                                text = state.recommendation ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ExpenseTrackerScreenPreview() {
    val context = LocalContext.current
    ExpenseTrackerScreen(
        navController = NavHostController(context)
    )
}

