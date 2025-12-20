package com.anshul.expenseai.ui.compose.expensetracker.onboarding

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.anshul.expenseai.data.repository.ExpenseRepo
import com.anshul.expenseai.ui.compose.expensetracker.UserOnboardingInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val repository: ExpenseRepo
): ViewModel(){

    val showOnboarding = mutableStateOf(
        repository.shouldShowOnboarding()
    )

    fun completeOnboarding(userInfo: UserOnboardingInfo) {
        repository.saveUserOnBoardingInfo(userInfo)
        showOnboarding.value = false
    }


}