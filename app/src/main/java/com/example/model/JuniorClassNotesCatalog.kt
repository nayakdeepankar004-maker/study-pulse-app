package com.example.model

object JuniorClassNotesCatalog {
    val notes: List<ClassNoteTemplate> = listOf(
        // --- CLASS 6 ---
        ClassNoteTemplate(
            id = "c6_sci_food",
            classLevel = "Class 6",
            subject = "Science",
            track = "Foundation",
            chapterTitle = "Components of Food & Balanced Diet",
            summary = "Major nutrients (carbohydrates, proteins, fats, vitamins, minerals), dietary fibres, water, and deficiency diseases.",
            detailedPoints = listOf(
                "Carbohydrates & Fats are energy-giving foods; Fats give more energy per gram than carbohydrates.",
                "Proteins are body-building nutrients essential for growth and tissue repair.",
                "Iodine test detects starch (blue-black colour); Copper sulphate + caustic soda tests protein (violet colour).",
                "Vitamin A deficiency causes night blindness; Vitamin C causes scurvy; Vitamin D causes rickets; Iron deficiency causes anaemia."
            ),
            keyFormulas = listOf(
                "Starch + Iodine solution → Blue-black colour",
                "Protein + CuSO₄ + NaOH → Violet colour",
                "Fat on paper → Translucent oily patch"
            ),
            examTip = "Memorize the Vitamin-Deficiency Disease table (A, B1, C, D, Calcium, Iodine, Iron) for 1-mark and matching questions."
        ),
        ClassNoteTemplate(
            id = "c6_sci_motion_light",
            classLevel = "Class 6",
            subject = "Science",
            track = "Foundation",
            chapterTitle = "Motion, Measurement & Light Shadows",
            summary = "SI units of length, types of motion (rectilinear, circular, periodic), luminous objects, and pinhole camera.",
            detailedPoints = listOf(
                "Standard unit of length in SI system is metre (m). 1 km = 1000 m, 1 m = 100 cm = 1000 mm.",
                "Rectilinear motion: straight line path; Circular motion: fixed distance from centre; Periodic motion: repeats after fixed intervals.",
                "Shadows form when an opaque object blocks light from a source; shadows are always dark regardless of object colour.",
                "A pinhole camera forms an inverted (upside-down) real image using rectilinear propagation of light."
            ),
            keyFormulas = listOf(
                "1 m = 100 cm = 1000 mm",
                "1 km = 1000 m",
                "Pinhole Image: Inverted & Real"
            ),
            examTip = "Always place the eye directly above the scale mark to avoid parallax error during measurement."
        ),
        ClassNoteTemplate(
            id = "c6_math_numbers",
            classLevel = "Class 6",
            subject = "Mathematics",
            track = "Foundation",
            chapterTitle = "Whole Numbers, Factors & Fractions",
            summary = "Properties of whole numbers, divisibility tests, HCF & LCM basics, and equivalent/unlike fractions.",
            detailedPoints = listOf(
                "Every whole number except 0 is a natural number; 0 is the smallest whole number and additive identity.",
                "Divisibility: by 3 if digit sum is divisible by 3; by 6 if divisible by both 2 and 3; by 11 if difference of alternating digit sums is 0 or multiple of 11.",
                "HCF is the greatest factor dividing given numbers; LCM is the smallest common multiple.",
                "Equivalent fractions are formed by multiplying or dividing numerator and denominator by the same non-zero number."
            ),
            keyFormulas = listOf(
                "Product of 2 numbers = HCF × LCM",
                "Perimeter of Rectangle = 2(l + b)",
                "Area of Rectangle = l × b  |  Square = side²"
            ),
            examTip = "Reduce final fraction answers to simplest form by dividing numerator and denominator by their HCF."
        ),
        ClassNoteTemplate(
            id = "c6_sst_earth",
            classLevel = "Class 6",
            subject = "Social Science",
            track = "Foundation",
            chapterTitle = "Solar System, Latitudes & Early Civilizations",
            summary = "Planets, rotation vs revolution, globe grid (Equator, Tropics, Prime Meridian), and Harappan cities.",
            detailedPoints = listOf(
                "Rotation on axis (24 hrs) causes day and night; Revolution around Sun (365¼ days) causes seasons.",
                "Important Parallels: Equator (0°), Tropic of Cancer (23.5° N), Tropic of Capricorn (23.5° S).",
                "Standard Meridian of India is 82.5° E (IST = GMT + 5 hours 30 minutes).",
                "Harappan cities (Harappa, Mohenjo-daro, Lothal) featured citadel, baked-brick drainage, and Great Bath."
            ),
            keyFormulas = listOf(
                "15° Longitude = 1 Hour (4 min per 1°)",
                "IST = UTC + 5h 30m (82°30' E)"
            ),
            examTip = "Remember Earth moves west to east, so places east of Greenwich are ahead in time."
        ),

        // --- CLASS 7 ---
        ClassNoteTemplate(
            id = "c7_sci_acids_heat",
            classLevel = "Class 7",
            subject = "Science",
            track = "Foundation",
            chapterTitle = "Acids, Bases, Salts & Heat Transfer",
            summary = "Natural indicators (litmus, turmeric, China rose), neutralization, conduction, convection, and radiation.",
            detailedPoints = listOf(
                "Acids are sour and turn blue litmus red; Bases are bitter, soapy, and turn red litmus blue.",
                "Turmeric turns reddish-brown in base; China rose turns dark pink (magenta) in acid and green in base.",
                "Neutralization: Acid + Base → Salt + Water + Heat (used in antacids and soil treatment).",
                "Conduction occurs in solids, convection in liquids/gases, and radiation needs no medium (heat from Sun)."
            ),
            keyFormulas = listOf(
                "Acid + Base → Salt + H₂O (Exothermic)",
                "°C = (5/9)(°F - 32)  |  °F = (9/5)°C + 32",
                "Normal human body temp = 37°C = 98.6°F"
            ),
            examTip = "Sea breeze blows from sea to land during daytime (convection); land breeze blows at night."
        ),
        ClassNoteTemplate(
            id = "c7_sci_electricity",
            classLevel = "Class 7",
            subject = "Science",
            track = "Foundation",
            chapterTitle = "Electric Current, Light & Motion",
            summary = "Circuit symbols, heating and magnetic effects of current, spherical mirrors, and speed-time graphs.",
            detailedPoints = listOf(
                "Heating effect depends on wire material, length, and thickness — used in heaters, irons, and safety fuses (MCB).",
                "Oersted effect: A current-carrying wire deflects a nearby magnetic compass needle (electromagnet principle).",
                "Convex mirror always forms virtual, erect, diminished image (wide rear-view mirror); Concave mirror can form magnified virtual or inverted real images.",
                "Uniform motion has a straight inclined line on a distance-time graph."
            ),
            keyFormulas = listOf(
                "Speed = Distance / Time",
                "Distance = Speed × Time",
                "White light = 7 colours (VIBGYOR)"
            ),
            examTip = "Remember longer line in a cell symbol is positive (+) terminal and shorter thick line is negative (-)."
        ),
        ClassNoteTemplate(
            id = "c7_math_algebra",
            classLevel = "Class 7",
            subject = "Mathematics",
            track = "Foundation",
            chapterTitle = "Integers, Simple Equations & Comparing Quantities",
            summary = "Sign rules for integers, solving linear equations by balancing, triangle angle-sum property, and percentage/SI.",
            detailedPoints = listOf(
                "Product of two integers with same sign is positive; opposite signs give negative product.",
                "Angle sum property: Sum of interior angles of any triangle is 180°; exterior angle equals sum of two opposite interior angles.",
                "Congruence criteria for triangles: SSS, SAS, ASA, and RHS.",
                "Profit% and Loss% are always calculated on Cost Price (CP)."
            ),
            keyFormulas = listOf(
                "Profit% = (Profit / CP) × 100",
                "Simple Interest (SI) = (P × R × T) / 100",
                "Amount = P + SI  |  Area of Circle = πr²"
            ),
            examTip = "Check units of Time (years) and Rate (% per annum) before substituting into the Simple Interest formula."
        ),

        // --- CLASS 8 ---
        ClassNoteTemplate(
            id = "c8_sci_force_sound",
            classLevel = "Class 8",
            subject = "Science",
            track = "Foundation",
            chapterTitle = "Force, Pressure, Friction & Sound",
            summary = "Contact & non-contact forces, fluid/atmospheric pressure, rolling vs sliding friction, and sound wave properties.",
            detailedPoints = listOf(
                "Pressure = Force / Area; smaller contact area gives greater pressure (sharp knives, pointed nails).",
                "Liquid pressure increases with depth and acts equally in all directions at the same depth.",
                "Static friction > Sliding friction > Rolling friction; ball bearings convert sliding into rolling friction.",
                "Loudness depends on Amplitude² (decibels); Pitch depends on Frequency (Hertz); audible range is 20 Hz to 20,000 Hz."
            ),
            keyFormulas = listOf(
                "Pressure P = F / A  (SI unit: Pascal = N/m²)",
                "Frequency f = 1 / T (unit: Hz)",
                "Speed of sound in air ≈ 340 m/s"
            ),
            examTip = "Remember frequency = oscillations per second; higher frequency means shriller (higher pitch) sound."
        ),
        ClassNoteTemplate(
            id = "c8_sci_chem_cell",
            classLevel = "Class 8",
            subject = "Science",
            track = "Foundation",
            chapterTitle = "Combustion, Chemical Effects & Cell Basics",
            summary = "Ignition temperature, zones of candle flame, electroplating, and prokaryotic vs eukaryotic cell organelles.",
            detailedPoints = listOf(
                "Three conditions for combustion: fuel, oxygen (air), and heat above ignition temperature.",
                "Candle flame zones: Outer (blue, hottest, complete combustion), Middle (yellow, luminous), Innermost (black, unburnt wax).",
                "Electroplating coats a metal over another using electric current (e.g., chromium plating prevents rusting).",
                "Mitochondria produce ATP energy; Chloroplasts occur in plant cells for photosynthesis; Cell wall gives rigidity to plant cells."
            ),
            keyFormulas = listOf(
                "Calorific value unit: kJ / kg",
                "CO₂ fire extinguisher cuts O₂ & cools fuel",
                "Angle of incidence (∠i) = Angle of reflection (∠r)"
            ),
            examTip = "For electroplating, the object to be coated is always connected to the negative electrode (cathode)."
        ),
        ClassNoteTemplate(
            id = "c8_math_identities",
            classLevel = "Class 8",
            subject = "Mathematics",
            track = "Foundation",
            chapterTitle = "Algebraic Identities, Exponents & Mensuration",
            summary = "Standard algebraic identities, laws of exponents, compound interest, and surface area/volume of 3D solids.",
            detailedPoints = listOf(
                "Factorisation reverses expansion using common factors, grouping, or standard identities.",
                "Any non-zero number raised to power 0 equals 1 (a⁰ = 1); negative exponent a⁻ⁿ = 1/aⁿ.",
                "In direct proportion x/y = constant; in inverse proportion x·y = constant.",
                "Lateral surface area excludes top and bottom faces; Total surface area includes all faces."
            ),
            keyFormulas = listOf(
                "(a ± b)² = a² ± 2ab + b²  |  a² - b² = (a+b)(a-b)",
                "CI Amount A = P(1 + R/100)ⁿ",
                "Cylinder: CSA = 2πrh, TSA = 2πr(r+h), V = πr²h"
            ),
            examTip = "When interest is compounded half-yearly, double the time periods (2n) and halve the rate (R/2)."
        ),

        // --- CLASS 9 ---
        ClassNoteTemplate(
            id = "c9_phy_motion_laws",
            classLevel = "Class 9",
            subject = "Physics",
            track = "Board Exams",
            chapterTitle = "Motion, Force, Laws of Motion & Gravitation",
            summary = "Three equations of motion, Newton's 3 laws, momentum conservation, universal gravitation, and Archimedes' principle.",
            detailedPoints = listOf(
                "Slope of distance-time graph = speed; Slope of velocity-time graph = acceleration; Area under v-t graph = displacement.",
                "Newton's 1st law defines inertia; 2nd law gives F = ma = Δp/Δt; 3rd law states action-reaction act on two different bodies.",
                "Acceleration due to gravity g = GM/R² ≈ 9.8 m/s² is independent of the falling object's mass.",
                "Archimedes' Principle: Upward buoyant force equals weight of fluid displaced by the submerged object."
            ),
            keyFormulas = listOf(
                "v = u + at  |  s = ut + ½at²  |  v² - u² = 2as",
                "p = mv  |  F = (mv - mu)/t = ma",
                "F = G·m₁·m₂ / r²  |  g = GM/R²  |  W = mg"
            ),
            examTip = "If a body starts from rest take u = 0; if it stops take v = 0; for vertical throw upward take a = -g."
        ),
        ClassNoteTemplate(
            id = "c9_chem_atoms",
            classLevel = "Class 9",
            subject = "Chemistry",
            track = "Board Exams",
            chapterTitle = "Atoms, Molecules & Atomic Structure",
            summary = "Laws of chemical combination, Avogadro number & mole concept, Bohr model, valency, isotopes & isobars.",
            detailedPoints = listOf(
                "Law of Conservation of Mass (Lavoisier): Mass is neither created nor destroyed in a chemical reaction.",
                "1 Mole = 6.022 × 10²³ particles (Avogadro's number N₀) and weighs equal to atomic/molecular mass in grams.",
                "Maximum electrons in nth shell = 2n² (K=2, L=8, M=18, N=32); outermost shell cannot hold more than 8 electrons (octet).",
                "Isotopes have same atomic number Z (same element, different mass A); Isobars have same mass number A (different Z)."
            ),
            keyFormulas = listOf(
                "n (moles) = Given mass (m) / Molar mass (M)",
                "n = Number of particles (N) / (6.022 × 10²³)",
                "Mass number A = Protons (Z) + Neutrons (n)"
            ),
            examTip = "Remember isotopes have identical chemical properties (same valence electrons) but different physical properties."
        ),
        ClassNoteTemplate(
            id = "c9_bio_cell_tissues",
            classLevel = "Class 9",
            subject = "Biology",
            track = "Foundation",
            chapterTitle = "The Fundamental Unit of Life & Tissues",
            summary = "Osmosis, plasma membrane, cell organelles, meristematic vs permanent plant tissues, and animal tissues.",
            detailedPoints = listOf(
                "Hypotonic solution swells the cell (endosmosis); Hypertonic shrinks it (plasmolysis); Isotonic keeps size unchanged.",
                "Rough ER synthesizes proteins (has ribosomes); Smooth ER synthesizes lipids and detoxifies poisons.",
                "Lysosomes are suicide bags (digestive enzymes); Mitochondria have folded cristae for ATP synthesis.",
                "Xylem conducts water/minerals unidirectionally (mostly dead); Phloem conducts food bidirectionally (living sieve tubes)."
            ),
            keyFormulas = listOf(
                "Mitochondria & Plastids have own DNA + 70S ribosomes",
                "Meristematic: Apical (length), Lateral (girth), Intercalary (nodes)",
                "Parenchyma (living), Collenchyma (flexibility), Sclerenchyma (lignin, dead)"
            ),
            examTip = "Distinguish clearly between ligaments (bone-to-bone, elastic) and tendons (muscle-to-bone, fibrous)."
        ),
        ClassNoteTemplate(
            id = "c9_math_poly_heron",
            classLevel = "Class 9",
            subject = "Mathematics",
            track = "Board Exams",
            chapterTitle = "Number Systems, Polynomials & Geometry",
            summary = "Rationalisation, remainder & factor theorems, Euclidean postulates, circle theorems, Heron's formula, and spheres/cones.",
            detailedPoints = listOf(
                "Factor Theorem: (x - a) is a factor of polynomial p(x) if and only if p(a) = 0.",
                "Angle subtended by an arc at the centre is double the angle subtended by it at any point on the remaining circle.",
                "Opposite angles of a cyclic quadrilateral sum to 180°.",
                "Heron's formula finds area of any triangle using semi-perimeter s = (a+b+c)/2."
            ),
            keyFormulas = listOf(
                "x³ + y³ + z³ - 3xyz = (x+y+z)(x²+y²+z²-xy-yz-zx)",
                "Area = √[s(s-a)(s-b)(s-c)],  s = (a+b+c)/2",
                "Cone: l = √(r²+h²), CSA = πrl, V = (1/3)πr²h | Sphere V = (4/3)πr³"
            ),
            examTip = "If x + y + z = 0, immediately use the identity x³ + y³ + z³ = 3xyz to solve algebraic problems in seconds."
        ),
        ClassNoteTemplate(
            id = "c9_eng_grammar",
            classLevel = "Class 9",
            subject = "English",
            track = "Foundation",
            chapterTitle = "Grammar Mastery: Tenses, Modals & Reported Speech",
            summary = "Tense sequence, subject-verb concord, determiners, modals, and rules for converting direct to indirect speech.",
            detailedPoints = listOf(
                "Present Simple changes to Past Simple, Present Continuous to Past Continuous, and Present Perfect to Past Perfect in reported speech.",
                "Universal truths and habitual facts remain in Present Simple inside reported speech.",
                "Collective nouns take singular verbs when acting as one unit and plural verbs when acting individually.",
                "Modals like must, should, ought to, might never change form with subject person or number."
            ),
            keyFormulas = listOf(
                "Reporting: 'will' → 'would' | 'can' → 'could' | 'today' → 'that day'",
                "Either/Neither + singular noun → singular verb",
                "Each / Every + noun → always singular verb"
            ),
            examTip = "In error-correction questions, first verify tense consistency across the entire sentence and subject-verb agreement."
        )
    )
}
