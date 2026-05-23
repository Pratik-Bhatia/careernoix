package com.example

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.CareeronixTheme
import com.example.ui.screens.*
import com.example.viewmodel.CareeronixViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Careeronix", appName)
  }

  @Test
  fun `test ViewModel initialization`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val app = context as android.app.Application
    val viewModel = CareeronixViewModel(app)
    assertNotNull(viewModel)
    assertNotNull(viewModel.onboardingSteps)
    assertEquals(4, viewModel.onboardingSteps.size)
  }

  @Test
  fun `test rendering HomeDashboardScreen`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val app = context as android.app.Application
    val viewModel = CareeronixViewModel(app)

    composeTestRule.setContent {
      CareeronixTheme {
        HomeDashboardScreen(
          viewModel = viewModel,
          onNavigateToTab = {}
        )
      }
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun `test rendering ResumeAnalyzerScreen`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val app = context as android.app.Application
    val viewModel = CareeronixViewModel(app)

    composeTestRule.setContent {
      CareeronixTheme {
        ResumeAnalyzerScreen(viewModel = viewModel)
      }
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun `test rendering InterviewPrepScreen`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val app = context as android.app.Application
    val viewModel = CareeronixViewModel(app)

    composeTestRule.setContent {
      CareeronixTheme {
        InterviewPrepScreen(viewModel = viewModel)
      }
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun `test rendering SkillRoadmapScreen`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val app = context as android.app.Application
    val viewModel = CareeronixViewModel(app)

    composeTestRule.setContent {
      CareeronixTheme {
        SkillRoadmapScreen(viewModel = viewModel)
      }
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun `test rendering CollegeDashboardScreen`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val app = context as android.app.Application
    val viewModel = CareeronixViewModel(app)

    composeTestRule.setContent {
      CareeronixTheme {
        CollegeDashboardScreen(viewModel = viewModel)
      }
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun `test rendering GamifiedGrowthTracker and Dialog`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val app = context as android.app.Application
    val viewModel = CareeronixViewModel(app)

    composeTestRule.setContent {
      CareeronixTheme {
        GamifiedGrowthTracker(viewModel = viewModel)
        
        // Also verify PRD Section Card renders properly
        PRDSectionCard(
          title = "Test Title",
          icon = Icons.Default.Info,
          content = "Test Content details"
        )
      }
    }
    composeTestRule.waitForIdle()
  }
}
