#!/usr/bin/env python3
"""Generate original syllabus notes + practice items (not official PYQ text)."""
from __future__ import annotations

from pathlib import Path

EXAMS = {
    "JEE": "c0000000-0000-0000-0000-000000000001",
    "NEET": "c0000000-0000-0000-0000-000000000002",
    "NDA": "c0000000-0000-0000-0000-000000000003",
}
SUBJECTS = {
    "PHY": "e1000000-0000-0000-0000-000000000001",
    "CHEM": "e1000000-0000-0000-0000-000000000002",
    "MATH": "e1000000-0000-0000-0000-000000000003",
    "BIO": "e1000000-0000-0000-0000-000000000004",
    "GAT": "e1000000-0000-0000-0000-000000000005",
    "ZOO": "e1000000-0000-0000-0000-000000000006",
}
MCQ = "d0000000-0000-0000-0000-000000000001"
YEARS = list(range(2016, 2026))


def uid(kind: int, n: int) -> str:
    return f"a{kind:02d}00000-0000-4000-8000-{n:012d}"


def sql_str(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


# Each row: exam, subject, chapter, topic, key_points, detailed,
# (stem, [A,B,C,D], correct_index_0, difficulty, simple, detailed, why, tip)
BANK = [
    ("JEE", "PHY", "Units and Measurement", "Dimensions and errors",
     "• [M L T] base dimensions\n• Least count vs accuracy\n• Percentage error adds in products",
     "Every mechanical quantity is a combination of mass, length and time. If z = a^p b^q, the relative error is p(Δa/a)+q(Δb/b). Significant figures are limited by the least precise measurement. Vernier/least-count sets the smallest readable difference, not the same as systematic bias.",
     [
         ("The dimensional formula of force is:", ["[M L T^-2]", "[M L T^-1]", "[M L^2 T^-2]", "[M T^-2]"], 0, "EASY",
          "Force is mass times acceleration.", "F = ma. Acceleration is L T^-2, so M L T^-2. Energy is M L^2 T^-2.",
          "Momentum is M L T^-1; energy has an extra length.", "Write the defining equation first, then dimensions."),
         ("If A = 2.0 ± 0.1 and B = 3.0 ± 0.1, the absolute error in A+B is about:", ["0.1", "0.2", "0.01", "0.00"], 1, "MEDIUM",
          "Errors add for sums.", "For sum/difference, absolute errors add: 0.1+0.1=0.2.", "Products use relative errors.", "Never subtract errors for a sum."),
     ]),
    ("JEE", "PHY", "Kinematics", "Equations of motion",
     "• v = u + at\n• s = ut + ½at²\n• v² = u² + 2as\n• Average velocity ≠ average of speeds if direction changes",
     "These three relations hold only for constant acceleration. Graphically, slope of v–t is a and area is displacement. Free fall uses a = g downward. Relative velocity in 1-D is algebraic subtraction of velocities.",
     [
         ("A body starts from rest with constant a. Distance in time t is:", ["at", "½at²", "vt", "u²/2a"], 1, "EASY",
          "u = 0 so s = ½at².", "Substitute u=0 in s = ut + ½at².", "vt is for uniform velocity.", "List u, a, t before choosing a formula."),
         ("The slope of a velocity–time graph equals:", ["Displacement", "Acceleration", "Jerk", "Power"], 1, "EASY",
          "a = dv/dt, the slope.", "Area under v–t is displacement; slope is acceleration.", "Jerk is da/dt.", "Read axes before reading slope."),
     ]),
    ("JEE", "PHY", "Laws of Motion", "Friction and connected bodies",
     "• Static friction ≤ μN, kinetic = μkN\n• Pseudo force −ma in a non-inertial frame\n• Pulley: constraint  a1 = a2 for inextensible string",
     "Newton’s second law is F_net = dp/dt. For constant mass this is ma. Friction opposes impending relative motion. On an incline, N = mg cosθ and the downhill component is mg sinθ. For two masses on a string over a light pulley, treat the string tension as internal if you take both masses as a system.",
     [
         ("On a rough horizontal floor the maximum static friction is:", ["μmg", "mg", "μ/mg", "m/μ"], 0, "EASY",
          "f_max = μN and N = mg.", "N = mg when the surface is horizontal and no other vertical force acts.", "μ/mg is dimensionally wrong.", "Draw N before writing friction."),
         ("A block of mass m rests in a lift accelerating up with a. The normal force is:", ["mg", "m(g+a)", "m(g−a)", "ma"], 1, "MEDIUM",
          "Non-inertial: apparent weight m(g+a).", "Net F_up = N − mg = ma, so N = m(g+a).", "g−a is for downward acceleration.", "Take upward positive consistently."),
     ]),
    ("JEE", "PHY", "Work Energy Power", "Work–energy theorem",
     "• W_net = ΔK\n• Conservative force: W = −ΔU\n• Power P = F·v",
     "The work–energy theorem is always true: total work by all forces equals change in kinetic energy. For conservative forces you may replace their work by minus the potential change and keep only non-conservative work on the left. Gravitational potential near Earth is mgh if h ≪ R.",
     [
         ("Work done by a conservative force around a closed path is:", ["Positive", "Negative", "Zero", "mgh"], 2, "EASY",
          "Conservative ⇔ path independent ⇔ closed-path work 0.", "Electrostatic and gravity are conservative (in usual intro models).", "Friction is not conservative.", "If work around a loop is not zero, the force is non-conservative."),
         ("Instantaneous power delivered by force F is:", ["F/v", "F·v", "Fv²", "F/t"], 1, "EASY",
          "P = dW/dt = F·v.", "Dot product: only the parallel component of F does work.", "F/t is not power.", "Check watts = newton × metre/second."),
     ]),
    ("JEE", "PHY", "Electrostatics", "Coulomb and Gauss",
     "• F = k q1 q2 / r²\n• Φ = q_enc/ε0\n• E of infinite sheet σ/2ε0",
     "Gauss’s law is always true but useful when symmetry makes E constant on a surface: spherical, infinite line, infinite sheet. Field inside a uniformly charged spherical shell is zero. Superposition is linear: add vectors, not magnitudes, unless they are collinear.",
     [
         ("Electric field inside a uniformly charged thin spherical shell is:", ["kQ/R²", "Zero", "σ/ε0", "kQ/r²"], 1, "MEDIUM",
          "Gaussian surface inside: q_enc = 0 so E = 0.", "Outside it behaves like a point charge.", "σ/ε0 is a conductor surface just outside.", "Use Gauss only with matching symmetry."),
         ("Two like charges repel because:", ["Gravity fails", "Force is along the joining line and repulsive for same sign", "They have mass", "Fields cancel"], 1, "EASY",
          "Coulomb force is repulsive for like signs.", "Direction is central, along the line joining charges.", "Gravity is always attractive.", "Sign of q1 q2 sets attraction vs repulsion."),
     ]),
    ("JEE", "PHY", "Current Electricity", "Ohm and Kirchhoff",
     "• V = IR for ohmic\n• Kirchhoff junction: ΣI = 0\n• Loop: Σε = ΣIR\n• P = I²R = V²/R",
     "Drift speed is small (~mm/s) but the electric field is established at nearly c, so the lamp lights quickly. Series: same I, resistances add. Parallel: same V, reciprocals add. A Wheatstone bridge balances when P/Q = R/S and the galvanometer current is zero.",
     [
         ("In a series circuit the quantity that is the same through each resistor is:", ["Voltage", "Current", "Power", "Resistance"], 1, "EASY",
          "Charge is conserved so current is the same in series.", "Voltages add to the battery emf.", "Power is I²R, larger on larger R.", "Draw the single loop before assigning I."),
         ("Kirchhoff’s junction rule is a statement of:", ["Energy conservation", "Charge conservation", "Ohm’s law", "Faraday’s law"], 1, "MEDIUM",
          "ΣI = 0 because charge does not pile up.", "The loop rule is energy per unit charge.", "Ohm is a material relation.", "Write current arrows first, then signs."),
     ]),
    ("JEE", "PHY", "Ray Optics", "Lenses and mirrors",
     "• 1/v − 1/u = 1/f (Cartesian sign convention)\n• Magnification m = v/u for lenses with the usual signs\n• Total internal reflection when i > critical angle",
     "Use Cartesian signs consistently: for a lens, light travels +x, object distance u is negative. A convex lens has f > 0. Power P = 1/f (f in metres) in dioptre. For TIR, light must be in the denser medium and i > sin⁻¹(n2/n1).",
     [
         ("A convex lens in air is:", ["Diverging, f < 0", "Converging, f > 0", "Always forms virtual images", "Has zero power"], 1, "EASY",
          "Convex (converging) lens: positive focal length in the Cartesian convention used in JEE.", "Concave lens diverges (f < 0).", "Real images occur when the object is outside F.", "Power is 1/f, not zero."),
         ("Critical angle for glass (n=1.5) to air is sin⁻¹ of:", ["1.5", "1/1.5", "0", "2"], 1, "MEDIUM",
          "sin c = n2/n1 = 1/1.5.", "Light goes glass → air, so n1=1.5, n2=1.", "1.5 would be > 1, impossible for sine.", "TIR needs denser to rarer."),
     ]),
    ("JEE", "PHY", "Modern Physics", "Photoelectric effect",
     "• K_max = hν − φ\n• Stopping potential eV0 = K_max\n• Intensity changes number of electrons, not K_max (above threshold)",
     "Einstein’s equation explains the threshold frequency and the linear V0 vs ν graph whose slope is h/e. Intensity at fixed frequency changes photocurrent (number of electrons per second) but not K_max. Below threshold, no emission regardless of intensity in the Einstein model.",
     [
         ("If frequency is above threshold, increasing intensity mainly increases:", ["K_max", "Stopping potential", "Photoelectric current", "Work function"], 2, "MEDIUM",
          "More photons per second → more electrons per second → larger current.", "K_max depends on frequency, not intensity.", "Work function is a material property.", "Threshold is about ν, not brightness."),
         ("Work function is:", ["Maximum KE of electrons", "Minimum energy to emit an electron", "h/e", "Stopping voltage"], 1, "EASY",
          "φ is the binding energy of the least-bound electron.", "K_max = hν − φ.", "Stopping potential measures K_max.", "Do not confuse φ with V0."),
     ]),
    ("JEE", "CHEM", "Atomic Structure", "Quantum numbers",
     "• n, l, m_l, m_s\n• l = 0…n−1; orbitals s,p,d,f\n• Pauli: no two electrons same four quantum numbers",
     "The principal quantum number n sets the shell and roughly the energy in hydrogen. Azimuthal l sets orbital shape (s,p,d,f). Magnetic m_l sets orientation (−l to +l). Spin m_s is ±1/2. Aufbau fills lower energy first; Hund keeps unpaired electrons in degenerate orbitals before pairing.",
     [
         ("For n = 3, the allowed l values are:", ["0 only", "0,1,2", "1,2,3", "3,4,5"], 1, "EASY",
          "l = 0,1,2,…,n−1 so 0,1,2 for n=3 (s,p,d).", "l cannot equal n.", "3d is l=2, n=3.", "Write n first, then l max = n−1."),
         ("Pauli exclusion principle forbids:", ["Same n for two electrons", "Two electrons with all four quantum numbers equal", "Hund pairing", "s orbitals"], 1, "MEDIUM",
          "At most one electron per unique (n,l,m_l,m_s).", "Two electrons in 1s have opposite spins.", "Hund is about degenerate orbitals.", "Pauli is not a statement about n alone."),
     ]),
    ("JEE", "CHEM", "Chemical Bonding", "VSEPR and hybridisation",
     "• Electron domains: 2=sp linear, 3=sp2 trigonal, 4=sp3 tetrahedral\n• Lone pairs occupy more space than bonds\n• H2O is bent, ~104.5°",
     "VSEPR counts electron domains around the central atom (bonds + lone pairs). Hybridisation is a model to explain those geometries with overlapping orbitals. Electronegativity difference guides ionic vs covalent character; Fajans’ rules refine polarisation of ions.",
     [
         ("The shape of PCl5 is:", ["Tetrahedral", "Trigonal bipyramidal", "Octahedral", "Linear"], 1, "EASY",
          "Five bond pairs, no lone pair → TBP.", "PCl3 is pyramidal; SF6 is octahedral.", "Linear is two domains.", "Count domains on the central atom."),
         ("Water is bent because:", ["Oxygen is sp hybridised", "Two lone pairs distort tetrahedral domains", "Hydrogen bonding in vapour", "It is ionic"], 1, "MEDIUM",
          "Four domains (2 bond + 2 lp) → tetrahedral arrangement, molecular shape bent.", "Oxygen is sp3, not sp.", "H-bonding affects boiling point, not the H–O–H angle origin.", "Start from AX2E2."),
     ]),
    ("JEE", "CHEM", "Equilibrium", "Kc, Kp and Le Chatelier",
     "• Kc from activities ≈ concentrations for dilute solutions\n• Kp = Kc(RT)^{Δn_g}\n• Catalyst does not change K, only rate",
     "At a given T, K is constant. Adding a reactant shifts the mixture so Q returns toward K. A catalyst speeds both forward and reverse rates equally, so equilibrium is reached faster but K is unchanged. For exothermic reactions, increasing T decreases K.",
     [
         ("A catalyst at equilibrium:", ["Increases K", "Decreases K", "Does not change K", "Changes ΔG°"], 2, "EASY",
          "Catalysts change rate, not the equilibrium constant.", "ΔG° = −RT ln K is a state function of T, not of path.", "K changes with T, not with catalyst.", "Separate kinetics from thermodynamics."),
         ("For N2 + 3H2 ⇌ 2NH3, increasing pressure at constant T:", ["Favours reactants", "Favours ammonia", "Does nothing", "Stops the reaction"], 1, "MEDIUM",
          "Fewer gas moles on the product side (4 → 2), so high P favours NH3.", "Le Chatelier: system reduces pressure by forming fewer molecules.", "K itself is not changed by P (ideal gases) but composition is.", "Count Δn_g first."),
     ]),
    ("JEE", "CHEM", "Organic Chemistry Basics", "IUPAC and isomerism",
     "• Longest chain + functional suffix\n• Structural vs stereoisomers\n• Inductive, resonance, hyperconjugation",
     "Number the chain to give the principal functional group the lowest locant. Chain, position, and functional isomers are structural. Geometrical (cis–trans / E–Z) and optical isomers are stereo. Stability of carbocations: 3° > 2° > 1° due to hyperconjugation and inductive donation.",
     [
         ("2-chlorobutane and 1-chlorobutane are:", ["Enantiomers", "Position isomers", "Identical", "Metamers"], 1, "EASY",
          "Same formula, Cl at different carbons → position isomerism.", "Enantiomers are non-superimposable mirror images.", "Metamers differ in alkyl groups on either side of a divalent function.", "Compare the locant of the functional group."),
         ("The most stable carbocation among the following types is:", ["Methyl", "Primary", "Secondary", "Tertiary"], 3, "EASY",
          "Tertiary is most stabilised by alkyl hyperconjugation/induction.", "Methyl has no alkyl donors.", "Allylic/benzylic can rival 3° but are not in this list.", "More hyperconjugative structures → more stable."),
     ]),
    ("JEE", "CHEM", "p-Block", "Group 15 and 16 trends",
     "• Nitrogen: triple bond, inert N2\n• Oxygen vs sulphur: catenation, allotropy\n• Inert pair effect down the group",
     "Ionisation enthalpy and electronegativity fall down a group. Nitrogen’s pπ–pπ bonding is strong, so N2 is very stable. Phosphorus prefers single bonds and tetrahedral P4. Inert pair effect makes +3 oxidation state more stable for heavier p-block elements (e.g. Bi(III) vs Bi(V)).",
     [
         ("N2 is less reactive than P4 at room temperature mainly because:", ["N is metal", "Strong N≡N triple bond", "Phosphorus is a gas", "N has d orbitals"], 1, "MEDIUM",
          "Bond energy of N≡N is very high.", "P4 has strained P–P bonds and is more reactive in white phosphorus.", "Nitrogen is a non-metal gas.", "Nitrogen has no 2d orbitals."),
         ("The inert pair effect is most pronounced in:", ["Boron", "Carbon", "Bismuth", "Oxygen"], 2, "MEDIUM",
          "Heavier p-block: ns² electrons remain inert; Bi prefers +3.", "B and C are light.", "Oxygen is group 16 period 2.", "Look to the bottom of groups 13–15."),
     ]),
    ("JEE", "MATH", "Quadratic Equations", "Nature of roots",
     "• D = b² − 4ac\n• Sum = −b/a, product = c/a\n• For real roots D ≥ 0 (real coefficients)",
     "A quadratic ax²+bx+c=0 (a≠0) has discriminant D=b²−4ac. D>0 two distinct real roots, D=0 repeated real root, D<0 complex conjugate roots if coefficients are real. Completing the square or the quadratic formula both work; substitution checks option traps.",
     [
         ("For x² − 4x + 3 = 0 the roots are:", ["1 and 3", "−1 and −3", "1 and 4", "3 and 4"], 0, "EASY",
          "Factor (x−1)(x−3)=0.", "Sum 4, product 3.", "−1 and −3 would need sum −4.", "Check product and sum against options."),
         ("If D < 0 for a quadratic with real coefficients, roots are:", ["Equal real", "Distinct real", "Complex conjugates", "Infinite"], 2, "EASY",
          "Negative discriminant → non-real conjugates.", "Equal real needs D=0.", "A quadratic has exactly two roots in ℂ, counting multiplicity.", "State the coefficient field (real) before concluding conjugates."),
     ]),
    ("JEE", "MATH", "Sequences and Series", "AP and GP",
     "• AP: a_n = a+(n−1)d, S_n = n/2 [2a+(n−1)d]\n• GP: a_n = ar^{n−1}, S_n = a(r^n−1)/(r−1)\n• AM ≥ GM for positive reals",
     "An AP has constant difference; a GP has constant ratio. Many JEE problems hide an AP of squares or a GP inside a nested radical. For |r|<1, infinite GP sums to a/(1−r). AM–GM equality holds iff all terms are equal.",
     [
         ("The 10th term of AP 3, 7, 11, … is:", ["39", "40", "43", "36"], 0, "EASY",
          "a=3, d=4, a10=3+9×4=39.", "Formula a+(n−1)d.", "40 would be if someone used n d.", "Count (n−1) steps, not n."),
         ("Sum of infinite GP 1 + 1/2 + 1/4 + … is:", ["1", "2", "1/2", "Does not exist"], 1, "EASY",
          "a=1, r=1/2, S=a/(1−r)=2.", "|r|<1 is required for convergence.", "Partial sums approach 2, not 1.", "Write |r| before summing to infinity."),
     ]),
    ("JEE", "MATH", "Limits and Derivatives", "Standard limits",
     "• lim x→0 sin x / x = 1 (x in radians)\n• Derivative is limit of [f(x+h)−f(x)]/h\n• Chain, product, quotient rules",
     "A limit exists only if left and right limits agree. Continuity requires the limit equal the function value. Differentiability is stronger than continuity. For composites, d/dx f(g(x)) = f'(g(x)) g'(x). L’Hôpital applies to 0/0 or ∞/∞ forms after checking hypotheses.",
     [
         ("lim x→0 (sin 5x)/x equals:", ["0", "1", "5", "1/5"], 2, "MEDIUM",
          "Write (sin 5x)/(5x) × 5 → 1×5=5.", "x must be radians.", "sin 5x / x is not 1.", "Factor the angle coefficient."),
         ("d/dx (x³) is:", ["x²", "3x²", "3x³", "x³/3"], 1, "EASY",
          "Power rule n x^{n−1}.", "Integral of x³ is x⁴/4, not the derivative.", "3x³ would be if exponent stayed.", "Bring the power down, then reduce the power by 1."),
     ]),
    ("JEE", "MATH", "Integral Calculus", "Definite integrals",
     "• ∫_a^b f = F(b)−F(a)\n• Odd function about 0: integral 0 if it exists\n• Even: 2∫_0^a",
     "A definite integral equals net signed area. Substitution must change limits. For even/odd, use f(−x)=±f(x). Fundamental theorem: if F'=f and f is continuous on [a,b], the integral is F(b)−F(a).",
     [
         ("∫_0^1 2x dx equals:", ["0", "1", "2", "1/2"], 1, "EASY",
          "Antiderivative x² from 0 to 1 is 1.", "This is the area of a triangle of base 1 height 2, which is 1.", "2 would be if you forgot to divide in geometry.", "Evaluate F(b)−F(a), do not drop limits."),
         ("∫_{−1}^{1} x³ dx equals:", ["0", "1", "1/2", "2"], 0, "MEDIUM",
          "x³ is odd and the interval is symmetric.", "The two areas cancel.", "x² is even, not x³.", "Check f(−x) before integrating across zero."),
     ]),
    ("JEE", "MATH", "Probability", "Independent events and Bayes",
     "• P(A∪B)=P(A)+P(B)−P(A∩B)\n• Independent: P(A∩B)=P(A)P(B)\n• Bayes: P(A|B)=P(B|A)P(A)/P(B)",
     "Mutually exclusive means empty intersection, which is not the same as independent (except in degenerate cases). Conditional probability P(A|B)=P(A∩B)/P(B). Bayes reverses a conditional using the law of total probability in the denominator.",
     [
         ("If A and B are independent with P(A)=1/2, P(B)=1/3, then P(A∩B)=", ["5/6", "1/6", "1", "0"], 1, "EASY",
          "Independent ⇒ product 1/6.", "5/6 would be a naive union without inclusion-exclusion.", "0 would be mutually exclusive.", "Independent ≠ mutually exclusive."),
         ("P(A|B) equals:", ["P(A)/P(B)", "P(A∩B)/P(B)", "P(A)P(B)", "P(B|A)"], 1, "EASY",
          "Definition of conditional probability.", "P(B|A) is the reverse conditional.", "Always condition on the event after the bar.", "Draw a two-way table for Bayes problems."),
     ]),
    ("NEET", "PHY", "Kinematics", "Projectile motion",
     "• Time of flight 2u sinθ / g\n• Range u² sin2θ / g, max at 45° on level ground\n• Horizontal velocity is constant (no air)",
     "Resolve u into u cosθ and u sinθ. Horizontal motion is uniform; vertical is uniformly accelerated with −g. Complementary angles θ and 90°−θ give the same range. Maximum height is (u sinθ)² / 2g.",
     [
         ("On level ground, maximum range (no air) occurs at:", ["30°", "45°", "60°", "90°"], 1, "EASY",
          "sin2θ = 1 ⇒ 2θ=90° ⇒ θ=45°.", "30° and 60° share a smaller range.", "90° is vertical, range 0.", "Level ground and no air are required for 45°."),
         ("Horizontal component of velocity in a projectile (no air) :", ["Increases", "Decreases", "Stays constant", "Is zero"], 2, "EASY",
          "No horizontal force ⇒ no horizontal acceleration.", "Vertical component changes due to gravity.", "Zero only if thrown vertically.", "Resolve before applying equations."),
     ]),
    ("NEET", "PHY", "Gravitation", "Escape and orbit",
     "• g = GM/R²\n• Escape speed √(2GM/R)\n• Orbital (circular) √(GM/r)",
     "Escape speed is from energy: K + U ≥ 0 with U = −GMm/r. For Earth it is about 11.2 km/s, independent of mass of the projectile. Weightlessness in orbit is free-fall with the satellite, not zero gravity.",
     [
         ("Escape speed from a planet does not depend on:", ["Planet mass", "Planet radius", "Mass of the escaping body", "G"], 2, "MEDIUM",
          "v_esc = √(2GM/R), no m of projectile.", "Heavier planets or smaller R increase escape speed.", "G is in the formula.", "Energy cancels m."),
         ("A satellite in circular orbit is accelerating because:", ["Speed changes", "Direction of velocity changes", "Mass changes", "G changes"], 1, "EASY",
          "Centripetal acceleration GM/r² toward the planet.", "Speed can be constant while velocity (vector) changes.", "This is why it does not fly off tangentially.", "Acceleration can be perpendicular to velocity."),
     ]),
    ("NEET", "PHY", "Current Electricity", "Resistivity and combination",
     "• R = ρL/A\n• Series add; parallel 1/R = Σ 1/R_i\n• Colour code for carbon resistors",
     "Resistivity ρ is a material property; resistance depends on geometry. Heating I²R is why thin filaments glow. In household wiring, appliances are in parallel so each gets the same voltage and can be switched independently.",
     [
         ("If length of a wire is doubled and area halved, R becomes:", ["Same", "2 times", "4 times", "Half"], 2, "MEDIUM",
          "R ∝ L/A → 2 / (1/2) = 4.", "Resistivity unchanged if temperature is same.", "Half would invert the proportion.", "Write R=ρL/A before plugging numbers."),
         ("Three equal resistors in parallel have equivalent:", ["3R", "R/3", "R", "9R"], 1, "EASY",
          "1/Req = 3/R ⇒ Req=R/3.", "Series would be 3R.", "Parallel always ≤ the smallest branch.", "More parallel paths, smaller Req."),
     ]),
    ("NEET", "PHY", "Optics", "Human eye and defects",
     "• Myopia: far point near, diverging lens\n• Hypermetropia: near point far, converging lens\n• Power in dioptre P=1/f(m)",
     "The eye’s crystalline lens accommodates by changing f. Myopia (short-sight) is corrected by a concave lens that forms a virtual image of a distant object at the far point. Hypermetropia needs a convex lens. Astigmatism uses cylindrical lenses.",
     [
         ("Myopia is corrected by a:", ["Convex lens", "Concave lens", "Cylindrical convex only", "Prism only"], 1, "EASY",
          "Diverging (concave) lens.", "Hypermetropia uses convex.", "Cylindrical mainly for astigmatism.", "Match defect to whether the image falls in front or behind the retina."),
         ("SI unit of lens power is:", ["Metre", "Dioptre", "Newton", "Candela"], 1, "EASY",
          "1 D = 1 m⁻¹.", "f must be in metres: P=1/f.", "Candela is luminous intensity.", "Sign of P follows sign of f."),
     ]),
    ("NEET", "CHEM", "Chemical Bonding", "Ionic vs covalent",
     "• Large Δχ → ionic\n• Similar Δχ → covalent\n• Coordinate bond: both electrons from one atom",
     "Ionic solids have high melting points and conduct when molten or aqueous because ions are free. Covalent network solids (diamond) are hard; molecular covalent (I2) are soft. A coordinate (dative) bond is still a covalent pair, just donated by one atom (e.g. NH4⁺, NH3→BF3).",
     [
         ("A coordinate bond is best described as:", ["Ionic transfer only", "A covalent pair donated by one atom", "Hydrogen bond", "Metallic sea only"], 1, "EASY",
          "Both electrons in the shared pair come from one atom.", "After formation it behaves like an ordinary covalent bond.", "H-bond is intermolecular.", "Look for Lewis bases (lone pairs)."),
         ("Molten NaCl conducts electricity because:", ["Electrons flow like metal", "Free ions", "It is covalent", "It is a gas"], 1, "EASY",
          "Mobile Na+ and Cl− carry current.", "Solid NaCl has ions locked in the lattice, so it does not conduct.", "Metals use delocalised electrons.", "State of matter matters for ionic conductors."),
     ]),
    ("NEET", "CHEM", "Equilibrium", "Ionic equilibrium and pH",
     "• pH = −log10 [H+]\n• Kw = 10⁻¹⁴ at 25 °C\n• Buffer: weak acid + salt of its conjugate",
     "Strong acids ionise essentially fully; weak acids use Ka. For a weak acid, [H+] ≈ √(Ka C). A buffer resists pH change (Henderson–Hasselbalch). Hydrolysis of salts of weak acids/bases shifts pH away from 7.",
     [
         ("pH of 0.001 M HCl (strong, 25 °C) is about:", ["1", "3", "11", "7"], 1, "EASY",
          "[H+]=10⁻³ so pH=3.", "pH 1 would be 0.1 M.", "Do not take −log of molarity without checking complete ionisation.", "10^{-pH} = [H+] for strong monoprotic acids (dilute)."),
         ("A buffer can be made from:", ["HCl and NaCl", "CH3COOH and CH3COONa", "NaOH only", "Sugar water"], 1, "MEDIUM",
          "Weak acid + its salt (conjugate base).", "HCl/NaCl is not a buffer; HCl is strong.", "NaOH alone is a strong base.", "Need a conjugate pair."),
     ]),
    ("NEET", "CHEM", "Biomolecules", "Carbohydrates and proteins",
     "• Glucose is an aldohexose\n• Peptide bond: −CO−NH−\n• Denaturation unfolds structure, not usually the primary sequence",
     "Monosaccharides are classified by aldose/ketose and carbon count. Starch and cellulose are glucose polymers with different glycosidic links (α vs β) which is why humans digest starch but not cellulose. Proteins have primary→quaternary structure; enzymes bind substrates at an active site.",
     [
         ("The bond joining amino acids in a protein backbone is a:", ["Glycosidic bond", "Peptide bond", "Phosphodiester", "Ionic lattice"], 1, "EASY",
          "Peptide (amide) −CO−NH−.", "Glycosidic joins sugars; phosphodiester joins nucleotides.", "Ionic bonds may stabilise tertiary structure but are not the backbone.", "Backbone = peptide; side chains vary."),
         ("Cellulose is not digested by humans mainly because:", ["It is protein", "We lack cellulase for β-1,4 glucose links", "It is toxic", "It is a lipid"], 1, "MEDIUM",
          "Starch has α links; cellulose has β-1,4.", "It is a polysaccharide, not a protein.", "Herbivores often rely on microbes.", "Link structure to enzyme specificity."),
     ]),
    ("NEET", "CHEM", "Hydrocarbons", "Alkanes alkenes alkynes",
     "• Alkanes: substitution (e.g. halogenation, free radical)\n• Alkenes: addition across C=C\n• Markovnikov: H to carbon with more H (unless peroxide with HBr)",
     "Unsaturated hydrocarbons add reagents across the multiple bond. Benzene prefers substitution to keep aromaticity. Anti-Markovnikov HBr addition with peroxides is a radical exception. Heat of hydrogenation compares alkene stability.",
     [
         ("Ethene mainly undergoes:", ["Substitution like benzene", "Addition reactions", "Ionic lattice formation", "Nuclear fusion"], 1, "EASY",
          "C=C is the site of addition (H2, Br2, HBr, H2O).", "Benzene substitutes to keep the aromatic sextet.", "Alkanes more often substitute.", "Identify the functional multiple bond first."),
         ("Markovnikov addition of HBr to propene gives mainly:", ["1-bromopropane", "2-bromopropane", "3-bromopropane", "Propane"], 1, "MEDIUM",
          "H adds to CH2, Br to the CH (more substituted carbon).", "Peroxides reverse this for HBr only.", "Propane would be hydrogenation.", "Draw the two possible carbocations."),
     ]),
    ("NEET", "BIO", "Cell: The Unit of Life", "Organelles",
     "• Mitochondria: ATP, double membrane, own DNA\n• Chloroplast: photosynthesis, plants/algae\n• Ribosome: protein synthesis (70S/80S)",
     "Prokaryotes lack a membrane-bound nucleus and mitochondria. The endomembrane system (ER, Golgi, vesicles, lysosomes) traffics proteins. Fluid mosaic model: phospholipid bilayer with embedded proteins. Nucleolus makes rRNA. Vacuoles are large in plant cells.",
     [
         ("70S ribosomes are typical of:", ["Cytosol of animals only", "Mitochondria, chloroplasts and prokaryotes", "Golgi lumen", "Nuclear pores"], 1, "MEDIUM",
          "Endosymbiotic organelles and bacteria have 70S.", "Cytosolic eukaryotic ribosomes are 80S.", "This supports the endosymbiotic theory.", "S is a sedimentation coefficient, not mass directly."),
         ("Which organelle sorts and packages proteins?", ["Mitochondrion", "Golgi apparatus", "Ribosome", "Nucleolus"], 1, "EASY",
          "Golgi modifies and packages for secretion or lysosomes.", "Ribosomes synthesise; they do not package.", "Nucleolus is rRNA/ribosome assembly.", "Follow RER → Golgi → vesicle."),
     ]),
    ("NEET", "BIO", "Plant Physiology", "Photosynthesis",
     "• Light reaction: thylakoid, O2, ATP, NADPH\n• Calvin cycle: stroma, CO2 → sugar\n• C4 concentrates CO2 at Rubisco",
     "Photosystem II splits water (photolysis) releasing O2. ATP and NADPH fuel the Calvin cycle where Rubisco carboxylates RuBP. Photorespiration wastes energy when Rubisco binds O2. C4 and CAM are CO2-concentrating adaptations in hot/dry climates.",
     [
         ("Oxygen released in photosynthesis comes from:", ["Carbon dioxide", "Water", "Glucose", "ATP"], 1, "EASY",
          "Photolysis of H2O at PSII.", "Rubisco uses CO2 carbon for sugar, not as the O2 source.", "Van Niel and later isotope work established this.", "Light reaction vs Calvin: know where each product is made."),
         ("The Calvin cycle occurs in the:", ["Thylakoid lumen", "Stroma", "Cytosol only in animals", "Mitochondrial matrix"], 1, "EASY",
          "Stroma of the chloroplast.", "Light reactions are on thylakoid membranes.", "Matrix is Krebs cycle.", "Map compartment to process."),
     ]),
    ("NEET", "BIO", "Genetics", "Mendel and DNA",
     "• Segregation and independent assortment\n• DNA: A–T, G–C; replication semi-conservative\n• Central dogma: DNA → RNA → protein",
     "Mendel’s factors are genes. A test cross with homozygous recessive reveals heterozygotes. DNA replication is semi-conservative (Meselson–Stahl). Transcription makes RNA; translation on ribosomes uses tRNA. Mutations can be silent, missense, nonsense, or frameshift.",
     [
         ("A test cross uses a:", ["Homozygous dominant", "Homozygous recessive", "F1 × F1 only", "Polyploid"], 1, "MEDIUM",
          "Unknown × recessive; 1:1 means the unknown was heterozygous.", "Crossing two heterozygotes is an F2, not a test cross.", "Independent assortment needs two genes on different chromosomes (or far apart).", "Write gametes in a Punnett square."),
         ("In DNA, adenine pairs with:", ["Guanine", "Cytosine", "Thymine", "Uracil"], 2, "EASY",
          "A–T (two H-bonds), G–C (three).", "Uracil replaces thymine in RNA (A–U).", "Purine pairs with pyrimidine, keeping width constant.", "Chargaff: A=T and G=C in duplex DNA."),
     ]),
    ("NEET", "BIO", "Ecology", "Ecosystems and biodiversity",
     "• 10% energy rule (typical trophic transfer)\n• Pyramid of energy is always upright\n• In-situ vs ex-situ conservation",
     "Producers capture sunlight; each trophic level loses energy as heat and waste, so food chains are short. Biodiversity includes genetic, species, and ecosystem diversity. Hotspots have high endemism and threat. Greenhouse gases trap long-wave radiation.",
     [
         ("Energy pyramid is always upright because:", ["Numbers never invert", "Energy is lost as heat at each transfer", "Decomposers are at the top", "Sunlight is unlimited"], 1, "MEDIUM",
          "Second law: inefficient transfer, often ~10%.", "Number pyramids can invert (tree vs insects).", "Decomposers act at all levels.", "Follow energy, not just head-counts."),
         ("National parks are an example of:", ["Ex-situ conservation", "In-situ conservation", "Cryopreservation only", "None"], 1, "EASY",
          "In-situ: conserve in the natural habitat.", "Zoos/seed banks are ex-situ.", "Both strategies are used together.", "Match the method to whether organisms stay in habitat."),
     ]),
    ("NEET", "BIO", "Sexual Reproduction in Flowering Plants", "Pollination and double fertilisation",
     "• Double fertilisation: 2n zygote + 3n endosperm\n• Self vs cross pollination\n• Seeds: embryo + store + coat",
     "Pollen lands on stigma, tube grows to ovule. One male gamete fuses with egg (syngamy); the other with polar nuclei (triple fusion). Endosperm nourishes the embryo. Apomixis produces seeds without fertilisation — useful in hybrid agriculture.",
     [
         ("Endosperm in angiosperms is typically:", ["Haploid", "Diploid", "Triploid", "Tetraploid"], 2, "EASY",
          "Triple fusion: two polar nuclei + one male gamete → 3n.", "Zygote is 2n.", "This is unique to angiosperms.", "Track each male gamete."),
         ("Transfer of pollen to a stigma is:", ["Fertilisation", "Pollination", "Germination", "Dispersal of fruit"], 1, "EASY",
          "Pollination ≠ fertilisation (which is gamete fusion).", "Germination of pollen is tube growth after landing.", "Fruit dispersal moves seeds.", "Sequence: pollination → fertilisation → seed."),
     ]),
    ("NEET", "ZOO", "Digestion and Absorption", "Human gut and enzymes",
     "• Mouth: amylase; stomach: pepsin; pancreas: trypsin, lipase, amylase\n• Bile emulsifies fats (not an enzyme)\n• Villi increase surface area",
     "Pepsin is secreted as pepsinogen (inactive) and activated by HCl — a protection against self-digestion. Bile salts emulsify lipids so lipase can act. Most absorption of monomers occurs in the jejunum/ileum via villi and microvilli. Liver processes absorbed nutrients via the hepatic portal vein.",
     [
         ("Bile helps fat digestion mainly by:", ["Hydrolysing peptide bonds", "Emulsifying lipids", "Digesting cellulose", "Killing all bacteria only"], 1, "EASY",
          "Emulsification increases surface area for lipase.", "Bile is not a hydrolase enzyme.", "Pepsin/trypsin hydrolyse proteins.", "Liver/gall bladder vs pancreas roles."),
         ("Pepsin is secreted as pepsinogen to:", ["Speed digestion of starch", "Prevent the stomach from digesting itself", "Emulsify fat", "Absorb glucose"], 1, "MEDIUM",
          "Zymogen activation by acid/pepsin is a safety mechanism.", "Salivary amylase acts on starch in the mouth.", "Inactive precursors are common for proteases.", "Name the organ and the substrate together."),
     ]),
    ("NEET", "ZOO", "Body Fluids and Circulation", "Heart and ECG",
     "• SA node is pacemaker\n• Double circulation: pulmonary + systemic\n• P wave: atrial depolarisation; QRS: ventricular",
     "Human heart is four-chambered, preventing mixing of oxygenated and deoxygenated blood. SA node in the right atrium sets sinus rhythm. Cardiac output = stroke volume × heart rate. Blood pressure is systolic/diastolic; hypertension is a major risk factor. Lymph returns fluid and is central to immunity.",
     [
         ("The natural pacemaker of the heart is the:", ["AV node", "SA node", "Purkinje fibres only", "Bundle of His only"], 1, "EASY",
          "Sino-atrial node fires fastest and drives the rest.", "AV node delays so atria finish contracting.", "Purkinje fibres spread ventricular excitation.", "Sequence: SA → AV → bundle → Purkinje."),
         ("Pulmonary veins carry:", ["Deoxygenated blood to lungs", "Oxygenated blood to the left atrium", "Lymph", "Bile"], 1, "EASY",
          "Exception: pulmonary veins are oxygen-rich.", "Pulmonary arteries are oxygen-poor, to lungs.", "Name vessel by where it goes, then oxygenation.", "Left atrium receives pulmonary veins."),
     ]),
    ("NEET", "ZOO", "Neural Control", "Neuron and synapse",
     "• Resting potential ~ −70 mV\n• Action potential: Na+ in, then K+ out\n• Synapse: neurotransmitter in cleft",
     "Resting potential is maintained by the Na+/K+ ATPase and leak channels. When threshold is reached, voltage-gated Na+ channels open (depolarisation), then K+ channels repolarise. Myelination enables saltatory conduction. Neurotransmitters may be excitatory or inhibitory (e.g. GABA).",
     [
         ("During the rising phase of an action potential, the main ion influx is:", ["K+", "Na+", "Cl− only", "Ca2+ in the axon hillock as the only ion"], 1, "MEDIUM",
          "Voltage-gated Na+ channels open; Na+ enters.", "K+ exit dominates repolarisation.", "Ca2+ is crucial at the synaptic terminal for vesicle release.", "Separate axon spike from synaptic transmission."),
         ("A synapse transmits by:", ["Direct cytoplasmic fusion of two axons", "Chemical (or electrical) coupling, typically neurotransmitter", "Hormones only from the pancreas", "Bone conduction"], 1, "EASY",
          "Most CNS synapses are chemical.", "Electrical synapses use gap junctions.", "Hormones are endocrine, slower and broadcast.", "Cleft + receptor = chemical synapse."),
     ]),
    ("NEET", "ZOO", "Human Reproduction", "Gametogenesis and hormones",
     "• Spermatogenesis in testes; oogenesis in ovaries\n• LH surge triggers ovulation\n• hCG maintains corpus luteum in early pregnancy",
     "GnRH from hypothalamus drives pituitary FSH/LH. FSH supports gamete production; LH supports hormone output and ovulation. Fertilisation is typically in the ampulla of the oviduct. Implantation in the uterus follows blastocyst formation. Placenta is both nutrient organ and endocrine gland.",
     [
         ("Ovulation is triggered mainly by a surge of:", ["Insulin", "LH", "Thyroxine", "ADH"], 1, "EASY",
          "Mid-cycle LH surge.", "FSH also rises but LH is the classic trigger.", "hCG later mimics LH to keep the corpus luteum.", "Map hormone to day of the cycle."),
         ("Fertilisation in humans normally occurs in the:", ["Uterus", "Ovary", "Ampulla of the fallopian tube", "Vagina"], 2, "EASY",
          "Ampullary region of the oviduct.", "Implantation is uterine.", "Ovary releases the oocyte; it is not the fusion site.", "Track gamete meeting place vs embedding place."),
     ]),
    ("NEET", "ZOO", "Evolution", "Evidence and mechanisms",
     "• Natural selection changes allele frequencies\n• Homologous organs: common ancestry\n• Analogous: similar function, different origin",
     "Darwin plus genetics (modern synthesis): variation is heritable; selection, drift, mutation, and gene flow change populations. Fossils, biogeography, comparative anatomy, and molecular sequences are evidence. Industrial melanism is a textbook selection example. Speciation often needs reproductive isolation.",
     [
         ("Wings of a bat and a butterfly are:", ["Homologous", "Analogous", "Vestigial in both", "Identical in origin"], 1, "MEDIUM",
          "Same function (flight), different structural origin → analogous.", "Forelimbs of bat and whale are homologous.", "Analogy is convergent evolution.", "Ask: common ancestor structure or common function?"),
         ("Natural selection acts on:", ["Individual phenotypes, changing population genetics over generations", "The origin of the universe", "Acquired skills of one lifetime Lamarck-style as DNA", "Only fossils"], 0, "EASY",
          "Selection filters phenotypes; genes are what is inherited.", "Acquired characters are not generally encoded in germline DNA.", "Populations evolve, not single organisms in one lifetime.", "Variation must be heritable."),
     ]),
    ("NEET", "ZOO", "Human Health and Disease", "Immunity",
     "• Innate vs acquired\n• Antibodies from B cells; T cells cellular\n• Vaccines prime memory without causing full disease",
     "Innate barriers (skin, acid, phagocytes) act first. Acquired immunity is specific and has memory. Humoral: antibodies. Cell-mediated: cytotoxic T cells vs infected cells. AIDS is caused by HIV attacking helper T cells (CD4). Antibiotics target bacteria, not viruses.",
     [
         ("Antibiotics are ineffective against typical colds because colds are usually:", ["Bacterial", "Viral", "Fungal only", "Prion only"], 1, "EASY",
          "Common cold is viral; antibiotics target prokaryotic biochemistry.", "Misuse drives resistance.", "Antivirals are a different drug class.", "Identify the pathogen type first."),
         ("Vaccination works primarily by generating:", ["Immediate antibiotics in blood", "Immunological memory", "More red cells", "Stomach acid"], 1, "EASY",
          "Memory B/T cells respond faster on re-exposure.", "It is acquired immunity, not an antibiotic dose.", "Herd immunity appears when coverage is high.", "Antigen without full disease is the idea of most vaccines."),
     ]),
    ("NEET", "ZOO", "Excretory Products", "Nephron",
     "• Filtration in glomerulus\n• Reabsorption in PCT (bulk)\n• ADH acts on collecting duct for water",
     "Each kidney has about a million nephrons. Ultrafiltration needs hydrostatic pressure. PCT reabsorbs glucose, amino acids, and much Na+ and water. Loop of Henle creates a medullary osmotic gradient (countercurrent). ADH inserts aquaporins; aldosterone increases Na+ reabsorption.",
     [
         ("Glucose is normally reabsorbed mainly in the:", ["Glomerulus", "PCT", "Collecting duct only", "Ureter"], 1, "MEDIUM",
          "Proximal convoluted tubule has transporters for glucose.", "Glomerulus filters; it does not selectively reabsorb.", "Glycosuria when threshold is exceeded (e.g. diabetes).", "Filtration vs reabsorption vs secretion."),
         ("ADH increases water reabsorption in the:", ["PCT only", "Collecting ducts (and late DT)", "Bowman’s capsule", "Urinary bladder lining as the main site"], 1, "EASY",
          "Aquaporins in collecting-duct cells.", "Without ADH, large volumes of dilute urine (diabetes insipidus).", "Bladder stores urine, does not fine-tune osmolarity.", "Link hormone to the segment."),
     ]),
    ("NDA", "MATH", "Algebra", "Linear and quadratic",
     "• Solve by transposition and factoring\n• Discriminant for quadratics\n• Simultaneous equations: substitution/elimination",
     "NDA mathematics rewards speed and accuracy on algebra. Isolate the variable; check by substitution. For two linear equations, elimination of one variable is often faster than substitution. Inconsistent systems have parallel lines (no solution).",
     [
         ("If 3x − 7 = 8, then x =", ["5", "3", "15", "1"], 0, "EASY",
          "3x=15, x=5.", "Add 7 then divide by 3.", "Do not divide only part of an expression.", "Inverse operations in reverse order."),
         ("The system x+y=2, x+y=5 has:", ["Unique solution", "No solution", "Infinitely many", "x=2 only"], 1, "EASY",
          "Parallel inconsistent lines.", "Same left side, different right side.", "Infinitely many would need the same line twice.", "Compare coefficients and constants."),
     ]),
    ("NDA", "MATH", "Trigonometry", "Identities and values",
     "• sin²θ + cos²θ = 1\n• sin 30°=1/2, sin 90°=1\n• tan = sin/cos",
     "Standard angles 0°, 30°, 45°, 60°, 90° must be automatic. CAST rule gives signs by quadrant. Many NDA items are one-step identity applications. Keep the calculator in degree mode if the question uses °.",
     [
         ("sin 30° + cos 60° equals:", ["0", "1", "1/2", "√3"], 1, "EASY",
          "1/2 + 1/2 = 1.", "cos 60° = sin 30° = 1/2.", "√3 is sin 60° or tan 60° related.", "Memorise the 30-45-60 triangle."),
         ("1 − sin²θ equals:", ["tan²θ", "cos²θ", "sec²θ", "0"], 1, "EASY",
          "Pythagorean identity.", "1 + tan² = sec² is a different form.", "Rearrange the identity you know rather than inventing one.", "sin²+cos²=1 is the seed identity."),
     ]),
    ("NDA", "MATH", "Statistics", "Mean median mode",
     "• Mean = Σx/n\n• Median is the middle value\n• Mode is the most frequent",
     "The mean is pulled by outliers; the median is more robust. For an even number of observations the median is the average of the two central values after sorting. A bimodal set has two modes. Range is a crude spread; standard deviation is preferred for further work.",
     [
         ("The median of 2, 5, 9, 11, 15 is:", ["5", "9", "11", "8.4"], 1, "EASY",
          "Sorted already; middle of five is the third value, 9.", "8.4 is the mean.", "Do not average all five for the median.", "Sort first if needed."),
         ("The mean is most affected by:", ["The middle value", "Extreme outliers", "The mode", "Sample size only"], 1, "EASY",
          "Mean uses every value, so tails pull it.", "Median resists outliers.", "Choose the average that matches the question’s robustness need.", "Sketch a dot plot."),
     ]),
    ("NDA", "GAT", "Mechanics", "Force and motion",
     "• Inertia resists change in velocity\n• Weight mg vs mass\n• Momentum p=mv",
     "NDA GAT science is conceptual. Mass is inertial/gravitational amount of matter; weight is force. Action–reaction pairs act on different bodies. Friction can start walking (backward push on the ground, forward reaction).",
     [
         ("Mass of a body is the same on the Moon but weight is less because:", ["Mass depends on g", "Weight is mg and g is smaller", "Inertia vanishes", "Moon has no gravity"], 1, "EASY",
          "g_Moon ≈ g_Earth/6, so weight drops; mass is unchanged.", "The Moon has gravity — that is why there is some weight.", "Inertia depends on mass.", "Separate mass (kg) from weight (N)."),
         ("Momentum of a body is:", ["mv", "m/v", "½mv²", "mgh"], 0, "EASY",
          "p = mv, vector along velocity.", "½mv² is kinetic energy (scalar).", "Impulse changes momentum.", "Check units: kg·m/s."),
     ]),
    ("NDA", "GAT", "Everyday Chemistry", "Acids bases salts",
     "• Acids: sour, pH<7, turn blue litmus red\n• Bases: bitter, pH>7\n• Neutralisation: acid + base → salt + water",
     "Household: vinegar is acetic acid; baking soda is a mild base. Strong acids (HCl in stomach) vs weak (acetic). Indicators are dyes whose colour depends on pH. Corrosion of iron needs both air and moisture; galvanisation coats with zinc.",
     [
         ("Blue litmus turns red in:", ["Base", "Acid", "Distilled water always", "Salt of strong acid strong base"], 1, "EASY",
          "Acids turn blue litmus red.", "Bases turn red litmus blue.", "Neutral salts of SA/SB are near pH 7.", "Keep a two-row litmus table."),
         ("A common weak acid in vinegar is:", ["HCl", "Acetic acid", "H2SO4", "HNO3"], 1, "EASY",
          "Vinegar is dilute ethanoic (acetic) acid.", "HCl/H2SO4/HNO3 are strong mineral acids.", "Weak ≠ dilute; vinegar is both weak and typically dilute.", "Name the household source."),
     ]),
    ("NDA", "GAT", "Human Biology", "Respiration and blood",
     "• Aerobic: glucose + O2 → CO2 + H2O + ATP\n• Haemoglobin carries O2\n• Alveoli: gas exchange",
     "Breathing is ventilation; cellular respiration is biochemistry in mitochondria. Haemoglobin binds O2 in lungs (high pO2) and releases it in tissues. CO2 is carried as bicarbonate, dissolved, and bound to Hb. Smoking damages alveoli (emphysema risk).",
     [
         ("Oxygen is carried in blood mainly by:", ["Plasma only as bubbles", "Haemoglobin in RBCs", "Platelets", "White cells"], 1, "EASY",
          "Hb in erythrocytes.", "A little O2 dissolves in plasma.", "Platelets clot; WBC defend.", "Structure (Hb) matches function (O2 transport)."),
         ("Alveoli are adapted for exchange because they have:", ["Thick cartilage", "Large surface area and thin walls", "Cilia like trachea only", "Valves like veins"], 1, "EASY",
          "Millions of thin-walled sacs, rich capillaries.", "Trachea has cartilage and cilia for a different job.", "Fick’s law: rate ∝ area / thickness.", "Match structure to diffusion."),
     ]),
    ("NDA", "GAT", "Diversity of Life", "Kingdoms and diseases",
     "• Bacteria: prokaryotes; antibiotics may work\n• Viruses: acellular, need host\n• Vectors: mosquito (malaria, dengue)",
     "Pasteur/germ theory: many diseases have infectious agents. Malaria is Plasmodium, mosquito vector. Tuberculosis is bacterial. Vaccination prevents many viral diseases. Hygiene and sanitation cut faecal–oral transmission.",
     [
         ("Malaria is transmitted by:", ["Anopheles mosquito", "Housefly only", "Air only", "Contaminated metal"], 0, "EASY",
          "Female Anopheles carries Plasmodium.", "Housefly is more typhoid/dysentery mechanically.", "Know pathogen vs vector.", "Vector control is part of prevention."),
         ("Viruses multiply:", ["On dead organic matter as saprophytes", "Only inside living host cells", "By binary fission in blood plasma freely like bacteria always", "By forming seeds"], 1, "MEDIUM",
          "Obligate intracellular parasites.", "Bacteria can be free-living or pathogenic.", "Antibiotics typically fail on viruses.", "Host machinery is hijacked."),
     ]),
]


def marks_for(exam: str) -> tuple[str, str]:
    if exam == "NDA":
        return "4", "1.33"
    return "4", "1"


def main() -> None:
    out = Path(__file__).resolve().parents[1] / "src/main/resources/db/migration/V5__syllabus_and_question_bank.sql"
    chapters: list[str] = []
    topics: list[str] = []
    questions: list[str] = []
    options: list[str] = []
    expls: list[str] = []
    steps: list[str] = []
    ci = ti = qi = oi = ei = si = 1
    seen_chapter: dict[tuple[str, str, str], int] = {}

    header = """-- Original syllabus notes + practice bank covering 2016–2025 style topics.
-- Not official NTA/UPSC paper text (copyright). Source type ORIGINAL_BANK.

ALTER TABLE topics
    ADD COLUMN IF NOT EXISTS key_points TEXT,
    ADD COLUMN IF NOT EXISTS detailed_explanation TEXT,
    ADD COLUMN IF NOT EXISTS syllabus_year INT NOT NULL DEFAULT 2025;

INSERT INTO subjects (id, code, name) VALUES
    ('e1000000-0000-0000-0000-000000000006', 'ZOO', 'Zoology')
ON CONFLICT (id) DO NOTHING;

INSERT INTO exam_subjects (exam_id, subject_id, sort_order) VALUES
    ('c0000000-0000-0000-0000-000000000002', 'e1000000-0000-0000-0000-000000000006', 4)
ON CONFLICT DO NOTHING;

"""

    for exam, subject, chapter, topic, keys, detailed, qs in BANK:
        ch_key = (exam, subject, chapter)
        if ch_key not in seen_chapter:
            seen_chapter[ch_key] = ci
            chapters.append(
                f"    ('{uid(21, ci)}', '{EXAMS[exam]}', '{SUBJECTS[subject]}', {sql_str(chapter)}, {len(seen_chapter)})"
            )
            ci += 1
        ch_id = uid(21, seen_chapter[ch_key])
        tid = uid(31, ti)
        topics.append(
            f"    ('{tid}', '{ch_id}', {sql_str(topic)}, 1, {sql_str(keys)}, {sql_str(detailed)}, 2025)"
        )
        marks, neg = marks_for(exam)
        for j, (stem, opts, correct, diff, simple, det, why, tip) in enumerate(qs):
            qid = uid(41, qi)
            year = YEARS[(qi - 1) % len(YEARS)]
            questions.append(
                "    ('{id}', '{exam}', '{sub}', '{ch}', '{top}', '{typ}', {stem}, {diff}, {marks}, {neg}, 60, {year}, 'ORIGINAL_BANK', 'MedyCatalog original · 2016-2025 syllabus coverage', 'en', 'PUBLISHED', NULL)".format(
                    id=qid,
                    exam=EXAMS[exam],
                    sub=SUBJECTS[subject],
                    ch=ch_id,
                    top=tid,
                    typ=MCQ,
                    stem=sql_str(stem),
                    diff=sql_str(diff),
                    marks=marks,
                    neg=neg,
                    year=year,
                )
            )
            for k, opt in enumerate(opts):
                options.append(
                    f"    ('{uid(51, oi)}', '{qid}', {sql_str(opt)}, {k + 1}, {'TRUE' if k == correct else 'FALSE'})"
                )
                oi += 1
            expls.append(
                f"    ('{uid(61, ei)}', '{qid}', {sql_str(simple)}, {sql_str(det)}, {sql_str(why)}, '{tid}', {sql_str(tip)})"
            )
            ei += 1
            steps.append(
                f"    ('{uid(71, si)}', '{qid}', 1, 'Idea', {sql_str(simple)})"
            )
            si += 1
            steps.append(
                f"    ('{uid(71, si)}', '{qid}', 2, 'Why', {sql_str(det)})"
            )
            si += 1
            steps.append(
                f"    ('{uid(71, si)}', '{qid}', 3, 'Answer', {sql_str(opts[correct])})"
            )
            si += 1
            qi += 1
        ti += 1

    body = f"""INSERT INTO chapters (id, exam_id, subject_id, name, sort_order) VALUES
{',\n'.join(chapters)};

INSERT INTO topics (id, chapter_id, name, sort_order, key_points, detailed_explanation, syllabus_year) VALUES
{',\n'.join(topics)};

INSERT INTO questions (
    id, exam_id, subject_id, chapter_id, topic_id, question_type_id, question_text,
    difficulty, marks, negative_marks, estimated_time_seconds, exam_year, source_type, source_reference, language, status, numerical_answer
) VALUES
{',\n'.join(questions)};

INSERT INTO question_options (id, question_id, option_text, option_order, is_correct) VALUES
{',\n'.join(options)};

INSERT INTO question_explanations (id, question_id, simple_text, detailed_text, why_others_wrong, related_concept_topic_id, exam_tip) VALUES
{',\n'.join(expls)};

INSERT INTO explanation_steps (id, question_id, step_order, title, body) VALUES
{',\n'.join(steps)};
"""
    out.write_text(header + body, encoding="utf-8")
    print(f"Wrote {out} chapters={len(chapters)} topics={len(topics)} questions={len(questions)}")


if __name__ == "__main__":
    main()
