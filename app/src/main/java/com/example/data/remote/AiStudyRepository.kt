package com.example.data.remote

import android.graphics.Bitmap
import android.util.Log
import com.example.model.QuizQuestion
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

enum class AiSolveMode(val label: String, val subtitle: String) {
    DEEP_SOLVER(
        label = "Deep Solver",
        subtitle = "Rigorous step-by-step hard numericals, proofs & verification"
    ),
    CONCEPT_TUTOR(
        label = "Concept Tutor",
        subtitle = "Intuitive explanations, NCERT clarity & revision notes"
    ),
    EXAM_TRICK(
        label = "JEE / NEET Trick",
        subtitle = "Fast exam shortcuts, elimination & time-saving methods"
    )
}

class AiStudyRepository {

    private val deepSolverModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.15f
                topP = 0.90f
                maxOutputTokens = 8192
            },
            systemInstruction = content {
                text(
                    "You are JeetXToppers Pro STEM Solver — an elite Indian Physics, Chemistry, Mathematics, Biology, and Board Exam tutor built to solve the hardest JEE Main, JEE Advanced, NEET, Olympiad, and Class 6–12 numerical and conceptual problems accurately.\n\n" +
                        "STRICT SOLVING PROTOCOL FOR DIFFICULT QUESTIONS:\n" +
                        "1. **Read & Transcribe Carefully**: If an image is attached, first read every symbol, subscript, superscript, graph axis, circuit value, organic structure, or boundary condition accurately. List **Given Data** with exact SI units (convert non-SI units upfront).\n" +
                        "2. **Identify Core Principle**: State the exact physical law, theorem, reaction mechanism, or mathematical identity being applied.\n" +
                        "3. **Step-by-Step Derivation**: Show EVERY algebraic, calculus, vector, stoichiometric, or mechanistic step explicitly. Never skip intermediate calculations or combine multiple tricky steps silently.\n" +
                        "4. **Watch Traps & Edge Cases**: Check sign conventions, domain restrictions, limiting reagents, stereochemistry, or frame of reference.\n" +
                        "5. **Self-Verify Before Concluding**: Double-check arithmetic, plug the result back into the original equation or verify dimensional consistency / limiting cases.\n" +
                        "6. **Final Answer & Shortcut**: End with a clear **Final Answer** (exact value + units / option) and, when helpful, a concise **Pro Tip / Common Mistake Alert**.\n\n" +
                        "Formatting rules: Use clean mobile-friendly Markdown (**bold headings**, bullet points, readable Unicode math like √, π, Δ, ∫, →, ≥, ≤, θ, ×, ÷). Match the student's language (English, Hindi, or Hinglish) if they ask in Hindi/Hinglish."
                )
            }
        )
    }

    private val conceptTutorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.45f
                topP = 0.95f
                maxOutputTokens = 6144
            },
            systemInstruction = content {
                text(
                    "You are the JeetXToppers AI Study Assistant for Indian school board (CBSE, ICSE, BSE Odisha, State Boards) and competitive exam (NEET, JEE Main) students (Classes 6–12). " +
                        "Explain concepts with intuitive analogies, crisp bullet points, key formulas, mnemonics, and exam-focused tips. " +
                        "Keep formatting clean and readable on mobile. Respond in Hinglish/Hindi if the student writes in Hindi/Hinglish."
                )
            }
        )
    }

    private val examTrickModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.2f
                topP = 0.9f
                maxOutputTokens = 6144
            },
            systemInstruction = content {
                text(
                    "You are a JEE & NEET Speed-Solving Strategist for JeetXToppers. " +
                        "For any problem or topic:\n" +
                        "1. Give the **Fastest Exam-Hall Method** (dimensional analysis, symmetry, special/limiting value substitution, option elimination, or shortcut formula) while ensuring 100% mathematical correctness.\n" +
                        "2. Briefly explain why the shortcut works and when NOT to use it.\n" +
                        "3. Show the **Final Answer** clearly with units and expected solve time (< 45 sec)."
                )
            }
        )
    }

    private val quizSchema = Schema.obj(
        properties = mapOf(
            "questions" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "question" to Schema.string(description = "Clear multiple choice question text"),
                        "options" to Schema.array(
                            items = Schema.string(),
                            description = "Exactly 4 answer choices"
                        ),
                        "correctIndex" to Schema.integer(description = "0-based index (0 to 3) of the correct option"),
                        "explanation" to Schema.string(description = "Detailed step-by-step explanation of the correct answer")
                    )
                ),
                description = "List of generated MCQs"
            )
        )
    )

    private val quizModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.25f
                responseMimeType = "application/json"
                responseSchema = quizSchema
            }
        )
    }

    fun streamTutorResponse(
        historyMessages: List<Pair<Boolean, String>>,
        userPrompt: String,
        attachedImage: Bitmap? = null,
        studentContext: String,
        solveMode: AiSolveMode = AiSolveMode.DEEP_SOLVER,
        subjectHint: String = "Auto"
    ): Flow<String> = flow {
        val chatHistory: List<Content> = historyMessages.takeLast(12).map { (isUser, msg) ->
            content(role = if (isUser) "user" else "model") {
                text(msg)
            }
        }
        val subjectTag = if (subjectHint == "Auto") "Auto-detect subject" else "Focus Subject: $subjectHint"
        val promptContent = content {
            text(
                "Student Profile: $studentContext\n" +
                    "Mode: ${solveMode.label} | $subjectTag\n\n" +
                    "Question / Request:\n$userPrompt"
            )
            if (attachedImage != null) {
                image(attachedImage)
            }
        }
        val activeModel = when (solveMode) {
            AiSolveMode.DEEP_SOLVER -> deepSolverModel
            AiSolveMode.CONCEPT_TUTOR -> conceptTutorModel
            AiSolveMode.EXAM_TRICK -> examTrickModel
        }
        val chat = activeModel.startChat(history = chatHistory)
        val accumulated = StringBuilder()
        withTimeout(60_000L) {
            chat.sendMessageStream(promptContent).collect { chunk ->
                val part = chunk.text.orEmpty()
                if (part.isNotEmpty()) {
                    accumulated.append(part)
                    emit(accumulated.toString())
                }
            }
        }
        if (accumulated.isBlank()) {
            throw IllegalStateException("AI Tutor returned an empty response.")
        }
    }

    suspend fun generatePracticeQuestions(
        topic: String,
        subject: String,
        track: String,
        count: Int
    ): Result<List<QuizQuestion>> {
        return try {
            val safeCount = count.coerceIn(3, 10)
            val prompt = "Generate $safeCount conceptual and numerical multiple choice questions on '$topic' in $subject tailored for $track preparation. " +
                "Include a balanced mix of medium and challenging multi-concept problems. Each question must have exactly 4 distinct options, a valid 0-based correctIndex (0..3), and a clear step-by-step explanation."
            val response = withTimeout(60_000L) {
                quizModel.generateContent(prompt)
            }
            val rawJson = response.text ?: throw IllegalStateException("Empty JSON response from AI.")
            val root = JSONObject(rawJson)
            val array = root.getJSONArray("questions")
            val parsed = buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val qText = obj.getString("question").trim()
                    val optsJson = obj.getJSONArray("options")
                    val opts = (0 until optsJson.length()).map { optsJson.getString(it).trim() }
                    val correctIdx = obj.optInt("correctIndex", 0).coerceIn(0, (opts.size - 1).coerceAtLeast(0))
                    val explanation = obj.optString("explanation", "Review the core concept for this topic.").trim()
                    if (qText.isNotEmpty() && opts.size >= 2) {
                        add(
                            QuizQuestion(
                                id = "ai_${System.currentTimeMillis()}_$i",
                                subject = subject,
                                track = "AI Generated",
                                chapter = topic,
                                question = qText,
                                options = opts.take(4),
                                correctIndex = correctIdx,
                                explanation = explanation
                            )
                        )
                    }
                }
            }
            if (parsed.isEmpty()) {
                Result.failure(IllegalStateException("Could not parse generated questions. Please try again."))
            } else {
                Result.success(parsed)
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI quiz generation timed out", e)
            Result.failure(IllegalStateException("AI generation timed out after 60 seconds. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI quiz generation failed", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "AiStudyRepository"
    }
}
