package com.prajwalhs.learningdashboard.presentation.coursedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.prajwalhs.learningdashboard.domain.usecase.MarkLessonCompletedUseCase
import com.prajwalhs.learningdashboard.domain.usecase.ObserveCourseDetailUseCase
import com.prajwalhs.learningdashboard.presentation.navigation.AppRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCourseDetail: ObserveCourseDetailUseCase,
    private val markLessonCompleted: MarkLessonCompletedUseCase,
) : ViewModel() {

    // Type-safe navigation argument; survives process death via SavedStateHandle.
    private val courseId: Int = savedStateHandle.toRoute<AppRoute.CourseDetail>().courseId

    val uiState: StateFlow<CourseDetailUiState> =
        observeCourseDetail(courseId)
            .map { detail ->
                if (detail == null) CourseDetailUiState.NotFound
                else CourseDetailUiState.Success(detail)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = CourseDetailUiState.Loading,
            )

    /**
     * Writes to Room only. The UI updates because the observed Flow re-emits,
     * and the dashboard's progress query updates too. No manual state patching.
     * The update is idempotent, so double taps are harmless.
     */
    fun onLessonCompleteClick(lessonId: Int) {
        viewModelScope.launch { markLessonCompleted(lessonId) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}