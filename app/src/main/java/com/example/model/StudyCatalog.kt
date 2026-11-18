package com.example.model

data class ChapterGuide(
    val id: String,
    val title: String,
    val subject: String,
    val track: String, // "Board Exams", "NEET", "JEE Main", "Foundation"
    val classLevel: String,
    val weightage: String,
    val summary: String,
    val keyFormulas: List<String>
)

data class QuizQuestion(
    val id: String,
    val subject: String,
    val track: String,
    val chapter: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val hasImage: Boolean = false
)

data class ClassNoteTemplate(
    val id: String,
    val classLevel: String, // "Class 6" .. "Class 12"
    val subject: String,
    val track: String, // "Foundation", "Board Exams", "NEET", "JEE Main"
    val chapterTitle: String,
    val summary: String,
    val detailedPoints: List<String>,
    val keyFormulas: List<String>,
    val examTip: String
) {
    fun toPlainNoteContent(): String = buildString {
        appendLine("Class: $classLevel ($track)")
        appendLine("Summary: $summary")
        appendLine()
        appendLine("Key Concepts:")
        detailedPoints.forEach { appendLine("• $it") }
        if (keyFormulas.isNotEmpty()) {
            appendLine()
            appendLine("Key Formulas & Facts:")
            keyFormulas.forEach { appendLine("• $it") }
        }
        appendLine()
        append("Exam Tip: $examTip")
    }
}

object StudyCatalog {
    val boards = listOf("CBSE", "ICSE", "BSE Odisha", "Other State Boards")
    val classes = listOf("Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "Class 11", "Class 12")
    val streams = listOf("Science PCM", "Science PCB", "Commerce", "Arts/Humanities", "Foundation")
    val targetExams = listOf("Board Exams", "NEET", "JEE Main", "Foundation")
    val subjects = listOf("Physics", "Chemistry", "Mathematics", "Biology", "Science", "Social Science", "English")

    val classNotes: List<ClassNoteTemplate> by lazy {
        JuniorClassNotesCatalog.notes + SeniorClassNotesCatalog.notes
    }

    val chapters: List<ChapterGuide> = listOf(
        ChapterGuide(
            id = "phy_electrostatics",
            title = "Electrostatics & Capacitance",
            subject = "Physics",
            track = "JEE Main",
            classLevel = "Class 12",
            weightage = "High (2-3 Qs)",
            summary = "Coulomb's law, electric field & potential, Gauss's theorem, electric dipole, and parallel plate capacitors with dielectrics.",
            keyFormulas = listOf(
                "F = k·q₁·q₂ / r²  (k = 9×10⁹ N·m²/C²)",
                "Φ = ∮ E·dA = q_enc / ε₀",
                "C = K·ε₀·A / d  |  U = ½ C V²"
            )
        ),
        ChapterGuide(
            id = "phy_rotational",
            title = "Rotational Motion & Torque",
            subject = "Physics",
            track = "JEE Main",
            classLevel = "Class 11",
            weightage = "High (2 Qs)",
            summary = "Center of mass, moment of inertia theorems, angular momentum conservation, and rolling motion without slipping.",
            keyFormulas = listOf(
                "I = I_cm + M·d² (Parallel Axis)",
                "τ = I·α = dL/dt",
                "K_rolling = ½ m v² (1 + k²/R²)"
            )
        ),
        ChapterGuide(
            id = "phy_optics",
            title = "Ray & Wave Optics",
            subject = "Physics",
            track = "NEET",
            classLevel = "Class 12",
            weightage = "Very High (3-4 Qs)",
            summary = "Snell's law, lens maker formula, prism deviation, Young's double slit interference, and diffraction.",
            keyFormulas = listOf(
                "1/f = (μ - 1)(1/R₁ - 1/R₂)",
                "β = λ·D / d (Fringe Width)",
                "n₁·sin(i) = n₂·sin(r)"
            )
        ),
        ChapterGuide(
            id = "chem_thermodynamics",
            title = "Chemical Thermodynamics",
            subject = "Chemistry",
            track = "JEE Main",
            classLevel = "Class 11",
            weightage = "High (2 Qs)",
            summary = "First law of thermodynamics, enthalpy, Hess's law, entropy, and Gibbs free energy spontaneity criteria.",
            keyFormulas = listOf(
                "ΔU = q + w  (IUPAC convention)",
                "ΔH = ΔU + Δn_g·R·T",
                "ΔG = ΔH - T·ΔS  (ΔG < 0 spontaneous)"
            )
        ),
        ChapterGuide(
            id = "chem_aldehydes",
            title = "Aldehydes, Ketones & Carboxylic Acids",
            subject = "Chemistry",
            track = "NEET",
            classLevel = "Class 12",
            weightage = "High (3 Qs)",
            summary = "Nucleophilic addition reactions, Aldol condensation, Cannizzaro reaction, Tollens' & Fehling's tests.",
            keyFormulas = listOf(
                "Aldol: α-H aldehyde + dil. NaOH → β-hydroxy aldehyde",
                "Cannizzaro: No α-H + conc. NaOH → alcohol + salt",
                "Clemmensen: >C=O + Zn-Hg/HCl → >CH₂"
            )
        ),
        ChapterGuide(
            id = "math_calculus",
            title = "Differential & Integral Calculus",
            subject = "Mathematics",
            track = "JEE Main",
            classLevel = "Class 12",
            weightage = "Very High (4-5 Qs)",
            summary = "Limits, continuity, maxima-minima applications, definite integrals (King's property), and area under curves.",
            keyFormulas = listOf(
                "∫₀ᵃ f(x)dx = ∫₀ᵃ f(a - x)dx",
                "d/dx [f(g(x))] = f'(g(x))·g'(x)",
                "∫ u·dv = u·v - ∫ v·du"
            )
        ),
        ChapterGuide(
            id = "math_quadratics",
            title = "Quadratic Equations & Sequences",
            subject = "Mathematics",
            track = "Board Exams",
            classLevel = "Class 11",
            weightage = "Core Foundation",
            summary = "Nature of roots, relation between roots and coefficients, AM-GM-HM inequalities, and AP/GP series sums.",
            keyFormulas = listOf(
                "x = [-b ± √(b² - 4ac)] / 2a",
                "α + β = -b/a  |  α·β = c/a",
                "AM ≥ GM ≥ HM for positive reals"
            )
        ),
        ChapterGuide(
            id = "bio_genetics",
            title = "Molecular Basis of Inheritance",
            subject = "Biology",
            track = "NEET",
            classLevel = "Class 12",
            weightage = "Very High (5-6 Qs)",
            summary = "DNA double helix structure, semi-conservative replication, transcription, genetic code, and Lac Operon regulation.",
            keyFormulas = listOf(
                "Chargaff's Rule: [A] = [T] and [G] = [C]",
                "Central Dogma: DNA → mRNA → Protein",
                "Lac Operon: Inducer = Allolactose"
            )
        ),
        ChapterGuide(
            id = "bio_cell",
            title = "Cell Cycle & Cell Division",
            subject = "Biology",
            track = "NEET",
            classLevel = "Class 11",
            weightage = "High (3-4 Qs)",
            summary = "Interphase (G1, S, G2), mitosis stages, and Meiosis I Prophase I (Leptotene to Diakinesis) crossing over.",
            keyFormulas = listOf(
                "S Phase: DNA doubles (2C → 4C), chromosome number (2n) unchanged",
                "Pachytene: Crossing over via recombinase",
                "Anaphase I: Homologous chromosomes separate"
            )
        ),
        ChapterGuide(
            id = "sci_light",
            title = "Light: Reflection & Refraction",
            subject = "Science",
            track = "Board Exams",
            classLevel = "Class 10",
            weightage = "Board High Yield",
            summary = "Spherical mirror and lens formulas, sign conventions, magnification, and power of a lens in dioptres.",
            keyFormulas = listOf(
                "Mirror: 1/f = 1/v + 1/u  |  m = -v/u",
                "Lens: 1/f = 1/v - 1/u  |  m = v/u",
                "Power P = 1 / f (in metres)"
            )
        ),
        ChapterGuide(
            id = "sst_nationalism",
            title = "Nationalism in India & Europe",
            subject = "Social Science",
            track = "Board Exams",
            classLevel = "Class 10",
            weightage = "Board Essay Focus",
            summary = "Key timelines of Non-Cooperation Movement, Civil Disobedience, Dandi Salt March, and European unification.",
            keyFormulas = listOf(
                "1919: Rowlatt Act & Jallianwala Bagh",
                "1920-22: Non-Cooperation Movement",
                "1930: Dandi March & Civil Disobedience"
            )
        ),
        ChapterGuide(
            id = "eng_grammar",
            title = "Reading Comprehension & Grammar Mastery",
            subject = "English",
            track = "Foundation",
            classLevel = "Class 9",
            weightage = "Core Language",
            summary = "Active/passive voice, reported speech rules, modals, determiners, and structured formal letter writing.",
            keyFormulas = listOf(
                "Reported Speech: Present Simple → Past Simple",
                "Subject-Verb Concord with collective nouns",
                "Formal Letter: Sender → Date → Receiver → Subject → Body"
            )
        )
    )

    val questionBank: List<QuizQuestion> = listOf(
        QuizQuestion(
            id = "q_phy_1",
            subject = "Physics",
            track = "NEET",
            chapter = "Current Electricity",
            question = "The SI unit of electric current is:",
            options = listOf("Volt", "Ampere", "Coulomb", "Ohm"),
            correctIndex = 1,
            explanation = "Ampere (A) is the SI base unit of electric current, equal to 1 Coulomb per second."
        ),
        QuizQuestion(
            id = "q_phy_2",
            subject = "Physics",
            track = "Board Exams",
            chapter = "Kinematics",
            question = "Which of the following is a scalar quantity?",
            options = listOf("Force", "Velocity", "Speed", "Acceleration"),
            correctIndex = 2,
            explanation = "Speed has only magnitude and no specified direction, making it a scalar quantity."
        ),
        QuizQuestion(
            id = "q_phy_3",
            subject = "Physics",
            track = "JEE Main",
            chapter = "Electrostatics & Capacitance",
            question = "Two equal charges q are separated by distance r. If both charges and the distance are doubled, the electrostatic force:",
            options = listOf("Doubles", "Becomes half", "Remains unchanged", "Becomes four times"),
            correctIndex = 2,
            explanation = "F = k(2q)(2q)/(2r)² = 4kq² / 4r² = kq²/r², so the force remains unchanged."
        ),
        QuizQuestion(
            id = "q_phy_4",
            subject = "Physics",
            track = "JEE Main",
            chapter = "Rotational Motion & Torque",
            question = "The moment of inertia of a uniform solid sphere of mass M and radius R about its diameter is:",
            options = listOf("1/2 MR²", "2/5 MR²", "2/3 MR²", "1/4 MR²"),
            correctIndex = 1,
            explanation = "A solid sphere about its diameter has moment of inertia I = (2/5)MR²."
        ),
        QuizQuestion(
            id = "q_phy_5",
            subject = "Physics",
            track = "NEET",
            chapter = "Ray & Wave Optics",
            question = "Two thin lenses of power +5 D and -2 D are placed in contact. The focal length of the combination is:",
            options = listOf("+33.3 cm", "-33.3 cm", "+14.3 cm", "+30.0 cm"),
            correctIndex = 0,
            explanation = "Net power P = +5 + (-2) = +3 D. Focal length f = 1/3 m = +33.3 cm."
        ),
        QuizQuestion(
            id = "q_chem_1",
            subject = "Chemistry",
            track = "NEET",
            chapter = "Chemical Thermodynamics",
            question = "For a reaction to be spontaneous at all temperatures, what must be the signs of ΔH and ΔS?",
            options = listOf("ΔH > 0, ΔS < 0", "ΔH < 0, ΔS > 0", "ΔH < 0, ΔS < 0", "ΔH > 0, ΔS > 0"),
            correctIndex = 1,
            explanation = "ΔG = ΔH - TΔS. When ΔH < 0 and ΔS > 0, ΔG is always negative at every temperature T > 0."
        ),
        QuizQuestion(
            id = "q_chem_2",
            subject = "Chemistry",
            track = "JEE Main",
            chapter = "Atomic Structure",
            question = "The maximum number of electrons that can occupy a subshell with azimuthal quantum number l = 2 is:",
            options = listOf("2", "6", "10", "14"),
            correctIndex = 2,
            explanation = "l = 2 corresponds to the d-subshell, which has 5 orbitals holding up to 2×5 = 10 electrons."
        ),
        QuizQuestion(
            id = "q_chem_3",
            subject = "Chemistry",
            track = "NEET",
            chapter = "Aldehydes, Ketones & Carboxylic Acids",
            question = "Which aldehyde does NOT undergo the Cannizzaro reaction?",
            options = listOf("Formaldehyde (HCHO)", "Benzaldehyde (C₆H₅CHO)", "Acetaldehyde (CH₃CHO)", "Trimethylacetaldehyde"),
            correctIndex = 2,
            explanation = "Acetaldehyde has α-hydrogens, so in alkali it undergoes Aldol condensation instead of Cannizzaro."
        ),
        QuizQuestion(
            id = "q_chem_4",
            subject = "Chemistry",
            track = "Board Exams",
            chapter = "Solutions",
            question = "Which colligative property is preferred for determining the molar mass of proteins and polymers?",
            options = listOf("Elevation in boiling point", "Depression in freezing point", "Osmotic pressure", "Relative lowering of vapour pressure"),
            correctIndex = 2,
            explanation = "Osmotic pressure is measurable at room temperature with high accuracy even for dilute macromolecular solutions."
        ),
        QuizQuestion(
            id = "q_math_1",
            subject = "Mathematics",
            track = "JEE Main",
            chapter = "Differential & Integral Calculus",
            question = "The derivative of sin(x) with respect to x is:",
            options = listOf("cos(x)", "-cos(x)", "tan(x)", "sec(x)"),
            correctIndex = 0,
            explanation = "By standard differentiation rules, d/dx[sin(x)] = cos(x)."
        ),
        QuizQuestion(
            id = "q_math_2",
            subject = "Mathematics",
            track = "JEE Main",
            chapter = "Differential & Integral Calculus",
            question = "What is the value of the definite integral ∫₀^(π/2) [sin(x) / (sin(x) + cos(x))] dx?",
            options = listOf("π/2", "π/4", "π", "1"),
            correctIndex = 1,
            explanation = "Using King's property ∫₀ᵃ f(x)dx = ∫₀ᵃ f(a-x)dx, 2I = ∫₀^(π/2) 1 dx = π/2, hence I = π/4."
        ),
        QuizQuestion(
            id = "q_math_3",
            subject = "Mathematics",
            track = "Board Exams",
            chapter = "Quadratic Equations & Sequences",
            question = "If the roots of ax² + bx + c = 0 are equal, then the discriminant b² - 4ac equals:",
            options = listOf("Greater than 0", "Less than 0", "0", "1"),
            correctIndex = 2,
            explanation = "Real and equal roots occur when the discriminant D = b² - 4ac = 0."
        ),
        QuizQuestion(
            id = "q_bio_1",
            subject = "Biology",
            track = "NEET",
            chapter = "Cell Cycle & Cell Division",
            question = "During which phase of the cell cycle does DNA replication take place?",
            options = listOf("G₁ phase", "S phase", "G₂ phase", "M phase"),
            correctIndex = 1,
            explanation = "DNA synthesis/replication occurs in the S (Synthesis) phase of interphase."
        ),
        QuizQuestion(
            id = "q_bio_2",
            subject = "Biology",
            track = "NEET",
            chapter = "Molecular Basis of Inheritance",
            question = "In a DNA molecule, if Adenine constitutes 22% of bases, what is the percentage of Cytosine?",
            options = listOf("22%", "28%", "44%", "56%"),
            correctIndex = 1,
            explanation = "By Chargaff's rule, A = T = 22% (total 44%). The remaining 56% is split equally between G and C, so C = 28%."
        ),
        QuizQuestion(
            id = "q_bio_3",
            subject = "Biology",
            track = "NEET",
            chapter = "Cell Cycle & Cell Division",
            question = "Crossing over between non-sister chromatids of homologous chromosomes occurs in:",
            options = listOf("Leptotene", "Zygotene", "Pachytene", "Diplotene"),
            correctIndex = 2,
            explanation = "Crossing over mediated by recombinase takes place during the Pachytene stage of Prophase I."
        ),
        QuizQuestion(
            id = "q_sci_1",
            subject = "Science",
            track = "Board Exams",
            chapter = "Light: Reflection & Refraction",
            question = "A concave lens of focal length 50 cm has power:",
            options = listOf("+2 D", "-2 D", "+0.5 D", "-0.5 D"),
            correctIndex = 1,
            explanation = "A concave lens has negative focal length f = -0.5 m. Power P = 1 / (-0.5) = -2 D."
        ),
        QuizQuestion(
            id = "q_sci_2",
            subject = "Science",
            track = "Foundation",
            chapter = "Acids, Bases & Salts",
            question = "The pH of pure neutral water at 25°C is:",
            options = listOf("0", "5", "7", "14"),
            correctIndex = 2,
            explanation = "At 25°C, [H⁺] = 10⁻⁷ M, giving pH = -log₁₀(10⁻⁷) = 7."
        )
    )
}
