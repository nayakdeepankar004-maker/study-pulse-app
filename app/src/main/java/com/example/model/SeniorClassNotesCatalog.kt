package com.example.model

object SeniorClassNotesCatalog {
    val notes: List<ClassNoteTemplate> = listOf(
        // --- CLASS 10 ---
        ClassNoteTemplate(
            id = "c10_sci_chem_reactions",
            classLevel = "Class 10",
            subject = "Science",
            track = "Board Exams",
            chapterTitle = "Chemical Reactions, Acids-Bases & Carbon Compounds",
            summary = "Balanced equations, redox reactions, pH scale, Chlor-alkali process, homologous series, esterification & saponification.",
            detailedPoints = listOf(
                "Oxidation = gain of O₂ / loss of H₂ or electrons; Reduction = loss of O₂ / gain of H₂ or electrons.",
                "Bleaching powder: Ca(OH)₂ + Cl₂ → CaOCl₂ + H₂O; Baking soda: NaHCO₃; Washing soda: Na₂CO₃·10H₂O.",
                "Carbon forms covalent bonds due to tetravalency and catenation; saturated hydrocarbons have single bonds (alkanes).",
                "Ethanol + Ethanoic acid (conc. H₂SO₄) → Ester (sweet smell) + H₂O; Saponification hydrolyzes esters with NaOH to form soap."
            ),
            keyFormulas = listOf(
                "Alkanes: CₙH₂ₙ₊₂ | Alkenes: CₙH₂ₙ | Alkynes: CₙH₂ₙ₋₂",
                "pH < 7 Acidic | pH = 7 Neutral | pH > 7 Basic (Tooth decay < 5.5)",
                "2NaCl + 2H₂O → 2NaOH + Cl₂(anode) + H₂(cathode)"
            ),
            examTip = "Always mention physical states (s, l, g, aq) and reaction conditions above the arrow in Board Chemistry answers."
        ),
        ClassNoteTemplate(
            id = "c10_sci_phy_elec_light",
            classLevel = "Class 10",
            subject = "Science",
            track = "Board Exams",
            chapterTitle = "Light (Reflection & Refraction) & Electricity",
            summary = "Mirror & lens formulas, magnification sign conventions, Ohm's law, series/parallel resistors, Joule heating & Fleming's rules.",
            detailedPoints = listOf(
                "New Cartesian sign convention: object distance u is always negative; concave focal length f is negative; convex f is positive.",
                "Series resistors share same current (R_eq = R₁ + R₂ + ...); Parallel resistors share same voltage (1/R_eq = 1/R₁ + 1/R₂).",
                "Resistivity (ρ) depends only on material and temperature, NOT on length or cross-sectional area.",
                "Fleming's Left-Hand Rule gives force direction on a current-carrying conductor in a magnetic field (motor principle)."
            ),
            keyFormulas = listOf(
                "Mirror: 1/f = 1/v + 1/u, m = -v/u | Lens: 1/f = 1/v - 1/u, m = v/u",
                "V = I·R | R = ρ·L/A | P = V·I = I²R = V²/R",
                "H = I²Rt | 1 kWh = 3.6 × 10⁶ Joules"
            ),
            examTip = "Virtual erect images have positive magnification (m > 0); real inverted images have negative magnification (m < 0)."
        ),
        ClassNoteTemplate(
            id = "c10_sci_bio_life",
            classLevel = "Class 10",
            subject = "Science",
            track = "Board Exams",
            chapterTitle = "Life Processes, Control & Heredity",
            summary = "Autotrophic/heterotrophic nutrition, nephron & double circulation, plant/animal hormones, Mendel's laws & sex determination.",
            detailedPoints = listOf(
                "Photosynthesis: light reaction splits H₂O and makes ATP/NADPH; dark reaction reduces CO₂ to glucose.",
                "Aerobic respiration yields 38 ATP (mitochondria); anaerobic in muscle forms lactic acid (2 ATP); in yeast forms ethanol + CO₂.",
                "Human heart achieves double circulation (pulmonary + systemic) preventing mixing of oxygenated and deoxygenated blood.",
                "Mendel's Monohybrid F₂ phenotypic ratio = 3:1 (genotypic 1:2:1); Dihybrid phenotypic ratio = 9:3:3:1."
            ),
            keyFormulas = listOf(
                "6CO₂ + 12H₂O —(chlorophyll, sunlight)→ C₆H₁₂O₆ + 6O₂ + 6H₂O",
                "Plant hormones: Auxin, Gibberellin, Cytokinin (growth); ABA (inhibitor)",
                "Sex determination in humans: XX (female), XY (male — father determines)"
            ),
            examTip = "Practice neat labelled diagrams of Human Heart, Nephron, Reflex Arc, and Stomata — they carry 3–5 marks in Boards."
        ),
        ClassNoteTemplate(
            id = "c10_math_board",
            classLevel = "Class 10",
            subject = "Mathematics",
            track = "Board Exams",
            chapterTitle = "Quadratics, AP, Triangles (BPT) & Trigonometry",
            summary = "Discriminant & roots, nth term and sum of AP, Basic Proportionality Theorem, trigonometric identities, and heights-distances.",
            detailedPoints = listOf(
                "Fundamental Theorem of Arithmetic: Every composite number has a unique prime factorisation.",
                "Basic Proportionality Theorem (Thales): A line parallel to one side of a triangle divides the other two sides in equal ratio.",
                "A tangent to a circle is perpendicular to the radius at the point of contact; two tangents from an external point are equal.",
                "Mode = 3 Median - 2 Mean (empirical relationship in Statistics)."
            ),
            keyFormulas = listOf(
                "aₙ = a + (n-1)d  |  Sₙ = (n/2)[2a + (n-1)d] = (n/2)(a + l)",
                "sin²θ + cos²θ = 1 | 1 + tan²θ = sec²θ | 1 + cot²θ = cosec²θ",
                "Distance = √[(x₂-x₁)² + (y₂-y₁)²] | Section: [(mx₂+nx₁)/(m+n), ...]"
            ),
            examTip = "In Trigonometry proofs, convert everything into sin θ and cos θ first whenever stuck."
        ),
        ClassNoteTemplate(
            id = "c10_sst_nationalism",
            classLevel = "Class 10",
            subject = "Social Science",
            track = "Board Exams",
            chapterTitle = "Nationalism in India & Europe + Federalism",
            summary = "19th-century European revolutions, Gandhi's Satyagraha movements, federalism tiers, and sectors of Indian economy.",
            detailedPoints = listOf(
                "1848 French revolution & Sorrieu's vision; unification of Italy (Cavour, Garibaldi, Mazzini) and Germany (Bismarck — blood & iron).",
                "Non-Cooperation (1920–22) called off after Chauri Chaura incident; Civil Disobedience launched with 1930 Dandi Salt March.",
                "Poona Pact (1932) between Gandhi and Ambedkar gave reserved seats to Depressed Classes in provincial/central councils.",
                "Indian Federalism: Union List (Defence, Banking), State List (Police, Agriculture), Concurrent List (Education, Forests)."
            ),
            keyFormulas = listOf(
                "1919: Rowlatt Act & Jallianwala Bagh (April 13)",
                "1929 Lahore Congress: Purna Swaraj Resolution",
                "Primary (Agri) → Secondary (Mfg) → Tertiary (Services)"
            ),
            examTip = "Memorize exact years (1919, 1920, 1922, 1929, 1930, 1931, 1932) and map locations (Dandi, Champaran, Kheda, Nagpur)."
        ),

        // --- CLASS 11 ---
        ClassNoteTemplate(
            id = "c11_phy_mechanics",
            classLevel = "Class 11",
            subject = "Physics",
            track = "JEE Main",
            chapterTitle = "Kinematics, Laws of Motion, Work-Energy & Rotation",
            summary = "Projectile motion, pseudo forces, friction & banking, work-energy theorem, elastic collisions, and rotational dynamics.",
            detailedPoints = listOf(
                "In projectile motion, horizontal velocity u·cosθ stays constant; maximum range occurs at θ = 45°; equal ranges at θ and (90°-θ).",
                "Work done by all forces (conservative + non-conservative) equals change in kinetic energy (W_all = ΔK).",
                "In pure rolling without slipping on a plane: v_cm = ω·R and a_cm = α·R; static friction does zero net work.",
                "Angular momentum L = I·ω is conserved whenever net external torque τ_ext = 0."
            ),
            keyFormulas = listOf(
                "H = u²sin²θ/(2g), T = 2u·sinθ/g, R = u²sin(2θ)/g",
                "v_max (banked rough) = √[Rg(μ + tanθ)/(1 - μ tanθ)]",
                "I = I_cm + Md² | I_z = I_x + I_y | K_roll = ½mv²(1 + k²/R²)"
            ),
            examTip = "For JEE/NEET rotational problems, conserve angular momentum about the instantaneous axis or hinge where unknown reaction forces pass."
        ),
        ClassNoteTemplate(
            id = "c11_phy_thermo_waves",
            classLevel = "Class 11",
            subject = "Physics",
            track = "NEET",
            chapterTitle = "Gravitation, Thermodynamics, SHM & Waves",
            summary = "Kepler's laws, escape/orbital velocity, Carnot engine, degree of freedom & C_p/C_v, SHM equations, and organ pipes.",
            detailedPoints = listOf(
                "Escape velocity v_e = √(2gR) = √2 · v_orbital; independent of mass and launch angle.",
                "First Law: ΔQ = ΔU + ΔW (Physics sign convention: work done BY gas is +ΔW).",
                "In SHM, restoring force F = -kx; velocity is max at mean position (ωA) and acceleration is max at extremes (ω²A).",
                "Closed organ pipe produces only odd harmonics (n, 3n, 5n); Open pipe produces all harmonics (n, 2n, 3n)."
            ),
            keyFormulas = listOf(
                "η_Carnot = 1 - T₂/T₁ | C_p - C_v = R | γ = 1 + 2/f",
                "SHM: x = A sin(ωt + φ) | T = 2π√(l/g) | T_spring = 2π√(m/k)",
                "Beats = |f₁ - f₂| | Open f = v/(2L), Closed f = v/(4L)"
            ),
            examTip = "Watch sign conventions carefully: Physics uses ΔU = Q - W (work by gas), while Chemistry IUPAC uses ΔU = q + w (work on gas)."
        ),
        ClassNoteTemplate(
            id = "c11_chem_bonding_thermo",
            classLevel = "Class 11",
            subject = "Chemistry",
            track = "JEE Main",
            chapterTitle = "Atomic Structure, Chemical Bonding & Thermodynamics",
            summary = "Quantum numbers, Heisenberg uncertainty, VSEPR geometries, Molecular Orbital Theory (MOT), Hess's law & Gibbs free energy.",
            detailedPoints = listOf(
                "Quantum numbers: n (shell), l (0..n-1 subshell), m_l (-l..+l orbital), s (±½ spin); Hund's & Pauli exclusion rules.",
                "Hybridisation steric number = ½(V + M - C + A); sp (linear 180°), sp² (trigonal planar 120°), sp³ (tetrahedral 109.5°).",
                "MOT Bond Order = ½(N_b - N_a); species with unpaired electrons in π* or σ orbitals are paramagnetic (e.g., O₂, B₂).",
                "Spontaneity: ΔG = ΔH - TΔS < 0; at equilibrium ΔG = 0 and ΔG° = -RT ln K_eq."
            ),
            keyFormulas = listOf(
                "λ = h/(mv) | Δx·Δp ≥ h/(4π) | E_n = -13.6 Z²/n² eV",
                "Bond Order ∝ Bond Strength ∝ 1 / Bond Length",
                "ΔH = ΔU + Δn_g RT | ΔG° = -2.303 RT log₁₀ K"
            ),
            examTip = "Remember N₂ (14 e⁻) and below fill π2p before σ2p, whereas O₂, F₂ fill σ2p before π2p."
        ),
        ClassNoteTemplate(
            id = "c11_chem_goc_eq",
            classLevel = "Class 11",
            subject = "Chemistry",
            track = "NEET",
            chapterTitle = "Equilibrium, GOC & Hydrocarbons",
            summary = "Le Chatelier principle, pH of buffers & K_sp, inductive/resonance/hyperconjugation effects, Markovnikov & ozonolysis.",
            detailedPoints = listOf(
                "Carbocation & free radical stability order: 3° > 2° > 1° > CH₃⁺ (hyperconjugation & +I effect); Carbanion order is opposite.",
                "Aromaticity (Hückel rule): Cyclic, planar, fully conjugated, and (4n + 2) π electrons (e.g. benzene = 6π, cyclopentadienyl anion = 6π).",
                "Acidic strength increases with -I / -R groups (e.g., -NO₂, -CN) and decreases with +I / +R groups (-CH₃, -OCH₃).",
                "Alkenes undergo electrophilic addition (Markovnikov); HBr + peroxide follows anti-Markovnikov free radical mechanism."
            ),
            keyFormulas = listOf(
                "K_p = K_c (RT)^Δn_g | Henderson Buffer: pH = pK_a + log([Salt]/[Acid])",
                "pH + pOH = 14 (at 298 K) | K_w = 10⁻¹⁴",
                "Ozonolysis (O₃, Zn/H₂O): >C=C< cleaves cleanly into >C=O + O=C<"
            ),
            examTip = "Remember peroxide (Kharasch) anti-Markovnikov effect works ONLY with HBr, NOT with HCl or HI."
        ),
        ClassNoteTemplate(
            id = "c11_math_algebra_conics",
            classLevel = "Class 11",
            subject = "Mathematics",
            track = "JEE Main",
            chapterTitle = "Complex Numbers, Quadratics, P&C & Conic Sections",
            summary = "Argand plane, De Moivre basics, AM≥GM, binomial coefficients, circle/parabola/ellipse/hyperbola standard equations & limits.",
            detailedPoints = listOf(
                "Complex number z = x + iy: |z| = √(x²+y²), z·z̄ = |z|², and Euler form z = r·e^(iθ).",
                "Binomial general term T_(r+1) = ⁿC_r x^(n-r) y^r; sum of all binomial coefficients = 2ⁿ.",
                "Parabola y² = 4ax has focus (a, 0), directrix x = -a, latus rectum = 4a.",
                "Standard limits: lim(x→0) sin(x)/x = 1, lim(x→0) (eˣ - 1)/x = 1, lim(x→0) (1+x)^(1/x) = e."
            ),
            keyFormulas = listOf(
                "ⁿP_r = n!/(n-r)! | ⁿC_r = n!/[r!(n-r)!] | ⁿC_r + ⁿC_(r-1) = ⁿ⁺¹C_r",
                "Ellipse x²/a² + y²/b² = 1 (a>b): e = √(1 - b²/a²), LR = 2b²/a",
                "Hyperbola x²/a² - y²/b² = 1: e = √(1 + b²/a²), LR = 2b²/a"
            ),
            examTip = "For JEE 1^∞ indeterminate forms lim f(x)^g(x), immediately compute e^[lim (f(x)-1)·g(x)]."
        ),
        ClassNoteTemplate(
            id = "c11_bio_cell_physio",
            classLevel = "Class 11",
            subject = "Biology",
            track = "NEET",
            chapterTitle = "Cell Division, Photosynthesis & Human Physiology",
            summary = "Mitosis vs Meiosis I stages, C₃ vs C₄ cycle, respiratory volumes, cardiac cycle, ECG waves, and sliding filament theory.",
            detailedPoints = listOf(
                "Meiosis I Prophase I: Leptotene (condensation), Zygotene (synapsis), Pachytene (crossing over via recombinase), Diplotene (chiasmata), Diakinesis (terminalisation).",
                "C₃ cycle (Calvin) first stable product is 3-PGA (RuBisCO); C₄ cycle (Hatch-Slack) avoids photorespiration via Kranz anatomy & PEPcase.",
                "ECG: P-wave (atrial depolarisation), QRS complex (ventricular depolarisation), T-wave (ventricular repolarisation).",
                "Vital Capacity (VC) = ERV + TV + IRV ≈ 4000–4600 mL; Residual Volume never leaves lungs (~1100–1200 mL)."
            ),
            keyFormulas = listOf(
                "1 Glucose (C₃) requires 18 ATP + 12 NADPH",
                "1 Glucose (C₄) requires 30 ATP + 12 NADPH",
                "Cardiac Output = Stroke Volume (70 mL) × Heart Rate (72/min) ≈ 5 L/min"
            ),
            examTip = "Every line of NCERT Class 11 Plant & Human Physiology tables is direct NEET 4-mark material."
        ),

        // --- CLASS 12 ---
        ClassNoteTemplate(
            id = "c12_phy_electro_current",
            classLevel = "Class 12",
            subject = "Physics",
            track = "JEE Main",
            chapterTitle = "Electrostatics, Capacitors & Current Electricity",
            summary = "Gauss's law applications, equipotential surfaces, dielectric capacitor energy, drift velocity, Kirchhoff's loops & Wheatstone bridge.",
            detailedPoints = listOf(
                "Electric field inside a uniformly charged hollow shell is zero; outside it behaves as if all charge is at the centre.",
                "Inserting a dielectric K with battery disconnected: Q constant, V→V/K, E→E/K, C→KC, U→U/K. With battery connected: V constant, Q→KQ, U→KU.",
                "Kirchhoff's Current Law (junction) conserves charge; Voltage Law (loop) conserves energy.",
                "Balanced Wheatstone bridge (P/Q = R/S) carries zero current through the central galvanometer."
            ),
            keyFormulas = listOf(
                "Φ = ∮E·dA = q_enc/ε₀ | E_line = λ/(2πε₀r) | E_sheet = σ/(2ε₀)",
                "C = Kε₀A/d | U = ½CV² = Q²/(2C) | u_energy_density = ½ε₀E²",
                "I = n·e·A·v_d | v_d = (eEτ)/m | r_internal = R(E/V - 1)"
            ),
            examTip = "Remember potential inside a charged hollow metal sphere is constant and equals surface potential kQ/R (NOT zero)."
        ),
        ClassNoteTemplate(
            id = "c12_phy_mag_optics_modern",
            classLevel = "Class 12",
            subject = "Physics",
            track = "NEET",
            chapterTitle = "Magnetism, AC, Optics & Modern Physics",
            summary = "Biot-Savart, Ampere law, Faraday EMI, LCR resonance, Lens maker & YDSE, photoelectric effect & Bohr/radioactivity laws.",
            detailedPoints = listOf(
                "Lorentz force F = q(E + v × B); magnetic force q(v × B) is perpendicular to velocity so it does zero work.",
                "Series LCR at resonance: X_L = X_C, Z = R (minimum), current is maximum, power factor cosφ = 1.",
                "In Young's Double Slit (YDSE), fringe width β = λD/d; immersing setup in medium μ reduces λ and β by factor μ.",
                "Photoelectric current ∝ intensity; Maximum KE (eV₀) depends ONLY on frequency above threshold ν₀."
            ),
            keyFormulas = listOf(
                "B_ring_centre = μ₀I/(2R) | B_solenoid = μ₀nI | ε = -dΦ/dt",
                "LCR: ω₀ = 1/√(LC) | P_avg = V_rms·I_rms·cosφ",
                "K_max = hν - φ₀ = eV₀ | λ_deBroglie = h/√(2mK) | N = N₀e^(-λt)"
            ),
            examTip = "For quick de Broglie calculations of an electron accelerated by V volts, use λ(Å) ≈ 12.27 / √V."
        ),
        ClassNoteTemplate(
            id = "c12_chem_physical_inorg",
            classLevel = "Class 12",
            subject = "Chemistry",
            track = "JEE Main",
            chapterTitle = "Solutions, Electrochemistry, Kinetics & Coordination",
            summary = "Raoult's law & van 't Hoff factor i, Nernst equation & Kohlrausch law, 1st-order kinetics, CFT splitting & isomerism.",
            detailedPoints = listOf(
                "Positive deviation from Raoult's law (ΔH_mix > 0, minimum boiling azeotrope); Negative deviation (ΔH_mix < 0, maximum boiling).",
                "Strong electrolyte Λ_m increases slowly on dilution; weak electrolyte Λ_m rises sharply near zero concentration (Kohlrausch law).",
                "For a 1st-order reaction, half-life t_½ = 0.693/k is completely independent of initial concentration.",
                "Strong field ligands (CN⁻, CO, en) cause Δ₀ > P (pairing, low spin); Weak field (F⁻, Cl⁻, H₂O) give high-spin complexes."
            ),
            keyFormulas = listOf(
                "ΔT_b = i·K_b·m | ΔT_f = i·K_f·m | π = i·C·R·T",
                "E_cell = E°_cell - (0.0591/n)log₁₀([Products]/[Reactants]) at 298K",
                "k = (2.303/t)log([A]₀/[A]) | t_½ = 0.693/k | k = A·e^(-E_a/RT)"
            ),
            examTip = "Check whether a salt dissociates (e.g., Al₂(SO₄)₃ → i = 5) or associates (benzoic acid in benzene → i = 0.5) before colligative math."
        ),
        ClassNoteTemplate(
            id = "c12_chem_organic",
            classLevel = "Class 12",
            subject = "Chemistry",
            track = "NEET",
            chapterTitle = "Haloalkanes, Alcohols, Carbonyls & Amines",
            summary = "SN1 vs SN2 mechanisms, Lucas/Tollens/Iodoform tests, Aldol/Cannizzaro/Reimer-Tiemann, Gabriel synthesis & diazonium reactions.",
            detailedPoints = listOf(
                "SN1: 2 steps, carbocation intermediate, racemisation, reactivity 3° > 2° > 1°. SN2: 1 step, inversion (Walden), 1° > 2° > 3°.",
                "Alcohol distinction (Lucas test ZnCl₂ + HCl): 3° immediate turbidity, 2° in ~5 min, 1° no turbidity at room temp.",
                "Iodoform test (I₂ + NaOH) is positive for CH₃CO- and CH₃CH(OH)- groups giving yellow CHI₃ precipitate.",
                "Hinsberg reagent (C₆H₅SO₂Cl): 1° amine gives alkali-soluble product, 2° insoluble, 3° no reaction."
            ),
            keyFormulas = listOf(
                "Basicity (aqueous): Et₂NH > Et₃N > EtNH₂ > NH₃ | Me₂NH > MeNH₂ > Me₃N",
                "Reimer-Tiemann: Phenol + CHCl₃ + NaOH → Salicylaldehyde",
                "Gabriel Phthalimide makes ONLY pure 1° aliphatic amines (never aryl)"
            ),
            examTip = "Learn the reagent-to-product map for Benzene Diazonium Chloride (Sandmeyer, Gattermann, coupling reactions) — asked every year."
        ),
        ClassNoteTemplate(
            id = "c12_math_calculus_vectors",
            classLevel = "Class 12",
            subject = "Mathematics",
            track = "JEE Main",
            chapterTitle = "Matrices, Calculus, Differential Eqns & Vectors/3D",
            summary = "Adjoint/inverse, Rolle's/LMVT, maxima-minima, King's property definite integrals, linear DEs, 3D lines/planes & Bayes' theorem.",
            detailedPoints = listOf(
                "For n×n matrix A: A·adj(A) = |A|·I, |adj(A)| = |A|^(n-1), and |kA| = kⁿ|A|.",
                "Rolle's theorem requires f continuous on [a,b], differentiable on (a,b), and f(a)=f(b) ⇒ ∃c with f'(c)=0.",
                "Linear Differential Equation dy/dx + P(x)y = Q(x) is solved using Integrating Factor IF = e^(∫P dx).",
                "Shortest distance between skew lines r = a₁ + λb₁ and r = a₂ + μb₂ is |(a₂ - a₁)·(b₁ × b₂)| / |b₁ × b₂|."
            ),
            keyFormulas = listOf(
                "∫₀ᵃ f(x)dx = ∫₀ᵃ f(a-x)dx | ∫ eˣ[f(x) + f'(x)]dx = eˣf(x) + C",
                "y·(IF) = ∫ Q(x)·(IF) dx + C",
                "Bayes: P(Eᵢ|A) = [P(Eᵢ)P(A|Eᵢ)] / Σ[P(E_j)P(A|E_j)]"
            ),
            examTip = "Whenever you see ∫ eˣ(something) dx in JEE/Boards, try splitting the bracket into f(x) + f'(x)."
        ),
        ClassNoteTemplate(
            id = "c12_bio_genetics_biotech",
            classLevel = "Class 12",
            subject = "Biology",
            track = "NEET",
            chapterTitle = "Genetics, Molecular Inheritance & Biotechnology",
            summary = "Linkage, pedigree analysis, DNA replication & transcription, genetic code, Lac operon, PCR, restriction enzymes & ecology pyramids.",
            detailedPoints = listOf(
                "DNA strands are antiparallel (5'→3' and 3'→5'); DNA Polymerase polymerizes ONLY in 5'→3' direction (leading continuous, lagging Okazaki).",
                "Genetic code is triplet, degenerate, unambiguous, non-overlapping, with AUG (start/Met) and UAA, UAG, UGA (stop codons).",
                "Lac Operon: Negative regulation by lac repressor protein; Allolactose acts as inducer to switch structural genes z, y, a ON.",
                "PCR 3 steps: Denaturation (94°C) → Annealing (50-60°C) → Extension (72°C using thermostable Taq polymerase)."
            ),
            keyFormulas = listOf(
                "Chargaff: [A]+[G] = [T]+[C] | Length of human DNA ≈ 2.2 m (6.6×10⁹ bp)",
                "Hardy-Weinberg: p + q = 1 and p² + 2pq + q² = 1",
                "Ecological Energy Transfer: Lindeman's 10% Law"
            ),
            examTip = "In pBR322 vector questions, remember amp^R (PstI, PvuI) and tet^R (BamHI, SalI) antibiotic selectable markers."
        )
    )
}
