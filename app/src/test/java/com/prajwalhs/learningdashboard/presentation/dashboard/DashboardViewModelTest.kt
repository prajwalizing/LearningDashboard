package com.prajwalhs.learningdashboard.presentation.dashboard

import app.cash.turbine.test
import com.prajwalhs.learningdashboard.domain.model.AppError
import com.prajwalhs.learningdashboard.domain.model.AppResult
import com.prajwalhs.learningdashboard.domain.model.Course
import com.prajwalhs.learningdashboard.domain.usecase.ObserveCoursesUseCase
import com.prajwalhs.learningdashboard.domain.usecase.RefreshCoursesUseCase
import com.prajwalhs.learningdashboard.fake.FakeCourseRepository
import com.prajwalhs.learningdashboard.util.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCourseRepository()

    private val pythonCourse = Course(
        id = 1,
        title = "Python Programming",
        instructor = "John Smith",
        totalLessons = 20,
        completedLessons = 13,
    )

    private fun createViewModel() = DashboardViewModel(
        observeCourses = ObserveCoursesUseCase(repository),
        refreshCourses = RefreshCoursesUseCase(repository),
    )

    /** uiState uses WhileSubscribed, so a collector must be active for it to update. */
    private fun TestScope.startCollecting(viewModel: DashboardViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
    }

    @Test
    fun `first launch offline shows Loading, then Error when there is no cache`() = runTest {
        repository.refreshGate = CompletableDeferred() // hold the refresh in flight
        repository.refreshResult = AppResult.Failure(AppError.Network)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(DashboardUiState.Loading, awaitItem())

            repository.refreshGate!!.complete(Unit) // refresh finishes with a network error

            assertEquals(DashboardUiState.Error(AppError.Network), awaitItem())
        }
    }

    @Test
    fun `offline with cached courses shows the courses with a network refresh error`() = runTest {
        repository.cachedCourses.value = listOf(pythonCourse)
        repository.refreshResult = AppResult.Failure(AppError.Network)
        val viewModel = createViewModel()
        startCollecting(viewModel)

        assertEquals(
            DashboardUiState.Success(
                courses = listOf(pythonCourse),
                isRefreshing = false,
                refreshError = AppError.Network,
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `successful refresh with no courses shows Empty`() = runTest {
        repository.refreshResult = AppResult.Success(Unit)
        repository.coursesAfterRefresh = emptyList()
        val viewModel = createViewModel()
        startCollecting(viewModel)

        assertEquals(DashboardUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `retry after a failed first load shows the fetched courses`() = runTest {
        repository.refreshResult = AppResult.Failure(AppError.Network)
        val viewModel = createViewModel()
        startCollecting(viewModel)
        assertEquals(DashboardUiState.Error(AppError.Network), viewModel.uiState.value)

        // Connectivity is back
        repository.refreshResult = AppResult.Success(Unit)
        repository.coursesAfterRefresh = listOf(pythonCourse)
        viewModel.onRetryClick()

        assertEquals(
            DashboardUiState.Success(
                courses = listOf(pythonCourse),
                isRefreshing = false,
                refreshError = null,
            ),
            viewModel.uiState.value,
        )
    }
}