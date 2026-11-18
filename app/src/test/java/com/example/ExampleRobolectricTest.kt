package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.remote.AiSolveMode
import com.example.model.StudyCatalog
import com.example.utils.ScientificMathEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches JeetXToppers`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JeetXToppers", appName)
    }

    @Test
    fun `scientific calculator evaluates arithmetic and trigonometry accurately`() {
        assertEquals("14", ScientificMathEvaluator.evaluate("2+3×4"))
        assertEquals("0.5", ScientificMathEvaluator.evaluate("sin(30)", useDegrees = true))
        assertEquals("5", ScientificMathEvaluator.evaluate("√(25)"))
        assertEquals("2", ScientificMathEvaluator.evaluate("log(100)"))
    }

    @Test
    fun `study catalog contains NEET, JEE Main, and Board chapters and questions`() {
        assertTrue(StudyCatalog.chapters.any { it.track == "NEET" })
        assertTrue(StudyCatalog.chapters.any { it.track == "JEE Main" })
        assertTrue(StudyCatalog.questionBank.size >= 10)
    }

    @Test
    fun `class notes catalog covers every class from Class 6 to Class 12`() {
        StudyCatalog.classes.forEach { className ->
            val count = StudyCatalog.classNotes.count { it.classLevel == className }
            assertTrue("Expected class notes for $className", count >= 3)
        }
        assertTrue(StudyCatalog.classNotes.all { it.detailedPoints.isNotEmpty() && it.keyFormulas.isNotEmpty() })
        assertEquals(3, AiSolveMode.entries.size)
    }
}
