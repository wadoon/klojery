package de.uka.ilkd.key.clojure;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ServiceLoader;

import org.key_project.util.collection.ImmutableList;
import org.key_project.util.collection.ImmutableSLList;

import de.uka.ilkd.key.control.UserInterfaceControl;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.PosInTerm;
import de.uka.ilkd.key.logic.Sequent;
import de.uka.ilkd.key.logic.SequentFormula;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Quantifier;
import de.uka.ilkd.key.logic.op.SchemaVariable;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.macros.ProofMacro;
import de.uka.ilkd.key.macros.TryCloseMacro;
import de.uka.ilkd.key.macros.scripts.ProofScriptEngine;
import de.uka.ilkd.key.macros.scripts.ScriptException;
import de.uka.ilkd.key.parser.DefaultTermParser;
import de.uka.ilkd.key.parser.ParserException;
import de.uka.ilkd.key.pp.AbbrevMap;
import de.uka.ilkd.key.proof.ApplyStrategy;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.proof.ProverTaskListener;
import de.uka.ilkd.key.proof.RuleAppIndex;
import de.uka.ilkd.key.proof.init.Profile;
import de.uka.ilkd.key.proof.rulefilter.TacletFilter;
import de.uka.ilkd.key.rule.IBuiltInRuleApp;
import de.uka.ilkd.key.rule.NoPosTacletApp;
import de.uka.ilkd.key.rule.PosTacletApp;
import de.uka.ilkd.key.rule.Taclet;
import de.uka.ilkd.key.rule.TacletApp;
import de.uka.ilkd.key.settings.ProofIndependentSettings;
import de.uka.ilkd.key.settings.ProofSettings;
import de.uka.ilkd.key.settings.SMTSettings;
import de.uka.ilkd.key.smt.RuleAppSMT;
import de.uka.ilkd.key.smt.SMTProblem;
import de.uka.ilkd.key.smt.SMTSolverResult.ThreeValuedTruth;
import de.uka.ilkd.key.smt.SolverLauncher;
import de.uka.ilkd.key.smt.SolverType;
import de.uka.ilkd.key.smt.SolverTypeCollection;

public class ProofScript {
	public static final String GOAL_KEY = "goal";
	private static DefaultTermParser PARSER = new DefaultTermParser();
	private static AbbrevMap EMPTY_MAP = new AbbrevMap();

	/*public final Goal getFirstOpenGoal(Proof proof, Map<String, Object> state) throws ScriptException {
		Object fixedGoal = state.get(GOAL_KEY);
		if (fixedGoal instanceof Node) {
			Goal g = getGoal(proof.openGoals(), (Node) fixedGoal);
			if (g != null) {
				return g;
			}
		}

		Node node = proof.root();

		if (node.isClosed()) {
			throw new ScriptException("The proof is closed already");
		}

		Goal g;
		Deque<Node> choices = new LinkedList<Node>();

		while (node != null) {
			assert!node.isClosed();
			int childCount = node.childrenCount();

			switch (childCount) {
			case 0:
				g = getGoal(proof.openGoals(), node);
				if (g.isAutomatic()) {
					return g;
				}
				node = choices.pollLast();
				break;

			case 1:
				node = node.child(0);
				break;

			default:
				Node next = null;
				for (int i = 0; i < childCount; i++) {
					Node child = node.child(i);
					if (!child.isClosed()) {
						if (next == null) {
							next = child;
						} else {
							choices.add(child);
						}
					}
				}
				assert next != null;
				node = next;
				break;
			}
		}
		assert false : "There must be an open goal at this point";
		return null;
	}
	*/
	
	public final static Term toTerm(Proof proof, String string, Sort sort) throws ParserException {
		StringReader reader = new StringReader(string);
		Services services = proof.getServices();
		Term formula = PARSER.parse(reader, sort, services, services.getNamespaces(), EMPTY_MAP);
		return formula;
	}

	public static Goal getGoal(ImmutableList<Goal> openGoals, Node node) {
		for (Goal goal : openGoals) {
			if (goal.node() == node) {
				return goal;
			}
		}
		return null;
	}

	public final static int getMaxAutomaticSteps(Proof proof) {
		if (proof != null) {
			return proof.getSettings().getStrategySettings().getMaxSteps();
		} else {
			return ProofSettings.DEFAULT_SETTINGS.getStrategySettings().getMaxSteps();
		}
	}

	public static void setMaxAutomaticSteps(Proof proof, int steps) {
		if (proof != null) {
			proof.getSettings().getStrategySettings().setMaxSteps(steps);
		}
		ProofSettings.DEFAULT_SETTINGS.getStrategySettings().setMaxSteps(steps);
	}

	private static void checkGoalBelongsToProof(Goal goal, Proof proof) {
		if (goal.proof() != proof)
			throw new IllegalStateException("The given goal does not belong to the given proof");
	}

	/*
	 * *************************************************************************
	 * ** auto
	 */
	public static void auto(Proof proof, Goal goal) throws ScriptException, InterruptedException {
		checkGoalBelongsToProof(goal, proof);

		final Profile profile = proof.getServices().getProfile();
		final ApplyStrategy applyStrategy = new ApplyStrategy(profile.getSelectedGoalChooserBuilder().create());

		// applyStrategy.addProverTaskObserver((ProverTaskListener) uiControl);
		applyStrategy.start(proof, ImmutableSLList.<Goal> nil().prepend(goal));

		// only now reraise the interruption exception
		if (applyStrategy.hasBeenInterrupted()) {
			throw new InterruptedException();
		}
	}

	public static final Name CUT_TACLET_NAME = new Name("cut");
	public static final Map<String, ProofMacro> MACRO_MAP = loadMacroMap();

	/*
	 * *************************************************************************
	 * ** cut
	 */
	public static void cut(Proof proof, Goal goal, Term formula) {
		Taclet cut = proof.getEnv().getInitConfigForEnvironment().lookupActiveTaclet(CUT_TACLET_NAME);
		TacletApp app = NoPosTacletApp.createNoPosTacletApp(cut);
		SchemaVariable sv = app.uninstantiatedVars().iterator().next();
		app = app.addCheckedInstantiation(sv, formula, proof.getServices(), true);
		goal.apply(app);
	}

	/*
	 * *************************************************************************
	 * ** leave
	 */
	public static void leave(Proof proof, Goal goal) {
		goal.setEnabled(false);
		System.err.println("Deactivating " + goal.node().serialNr());
	}

	/*
	 * *************************************************************************
	 * ** macro
	 */
	public static void macro(Proof proof, Goal goal, ProofMacro macro) throws InterruptedException, Exception {
		ClojureControl cc = new ClojureControl();
		macro.applyTo(cc, goal.node(), null, cc);
	}

	public static Map<String, ProofMacro> loadMacroMap() {
		ServiceLoader<ProofMacro> loader = ServiceLoader.load(ProofMacro.class);
		Map<String, ProofMacro> result = new HashMap<String, ProofMacro>();

		for (ProofMacro proofMacro : loader) {
			String commandName = proofMacro.getScriptCommandName();
			if (commandName != null) {
				result.put(commandName, proofMacro);
			}
		}

		return result;
	}

	/*
	 * *************************************************************************
	 * ** instantiate
	 */

	public static void instantiate(Proof proof, Goal g, Term with, Term var, Term formula, int occ, boolean hide) throws ScriptException {
		if ((var == null) == (formula == null)) {
			throw new ScriptException("One of 'var' or 'formula' must be specified");
		}

		if (var != null) {
			formula = computeFormula(g, var, occ);
		}

		String rulename;
		if (formula.op() == Quantifier.ALL) {
			rulename = "allLeft" + (hide ? "Hide" : "");
		} else {
			rulename = "exRight" + (hide ? "Hide" : "");
		}

		Services services = proof.getServices();
		TacletFilter filter = new TacletNameFilter(rulename);
		TacletApp theApp = findTacletApp(proof, g, rulename, formula, formula, occ);
		if (theApp == null) {
			throw new ScriptException("No taclet applicatin found");
		}

		SchemaVariable sv = theApp.uninstantiatedVars().iterator().next();
		theApp = theApp.addInstantiation(sv, with, true /* ??? */, proof.getServices());
		theApp = theApp.tryToInstantiate(proof.getServices());
		g.apply(theApp);
	}

	private static Term computeFormula(Goal goal, Term var, int occ) throws ScriptException {
		Node n = goal.node();
		Sequent seq = n.sequent();

		for (SequentFormula form : seq.antecedent().asList()) {
			Term term = form.formula();
			if (term.op() == Quantifier.ALL) {
				String varName = term.boundVars().get(0).name().toString();
				if (var.equals(varName)) {
					occ--;
					if (occ == 0) {
						return term;
					}
				}
			}
		}

		for (SequentFormula form : seq.succedent().asList()) {
			Term term = form.formula();
			if (term.op() == Quantifier.EX) {
				String varName = term.boundVars().get(0).name().toString();
				if (var.equals(varName)) {
					occ--;
					if (occ == 0) {
						return term;
					}
				}
			}
		}

		throw new ScriptException("Variable '" + var + "' has no occurrence no. '" + occ + "'.");
	}

	/*
	 * *************************************************************************
	 * ** Rule
	 */
	private static class TacletNameFilter extends TacletFilter {

		private final Name rulename;

		public TacletNameFilter(String rulename) {
			this.rulename = new Name(rulename);
		}

		@Override
		protected boolean filter(Taclet taclet) {
			return taclet.name().equals(rulename);
		}

	}

	public static void rule(Proof proof, Goal g, String rulename, Term formula, Term on, int occ) throws ScriptException {
		TacletApp theApp = findTacletApp(proof, g, rulename, formula, on, occ);
		assert theApp != null;

		ImmutableList<TacletApp> assumesCandidates = theApp.findIfFormulaInstantiations(g.sequent(),
				proof.getServices());

		if (assumesCandidates.size() != 1) {
			throw new ScriptException("Not a unique \\assumes instantiation");
		}

		theApp = assumesCandidates.head();

		// instantiate remaining symbols
		theApp = theApp.tryToInstantiate(proof.getServices());

		if (theApp == null) {
			throw new ScriptException("Cannot instantiate this rule");
		}
		g.apply(theApp);
	}

	private static TacletApp findTacletApp(Proof proof, Goal goal, String rulename, Term formula, Term on, int occ)
			throws ScriptException {
		ImmutableList<TacletApp> allApps = findAllTacletApps(proof, goal, rulename, formula);
		List<TacletApp> matchingApps = filterList(allApps, on);

		if (matchingApps.isEmpty()) {
			throw new ScriptException("No matching applications.");
		}

		if (occ < 0) {
			if (matchingApps.size() > 1) {
				throw new ScriptException("More than one applicable occurrence");
			}
			return matchingApps.get(0);
		} else {
			if (occ >= matchingApps.size()) {
				throw new ScriptException(
						"Occurence " + occ + " has been specified, but there only " + matchingApps.size() + " hits.");
			}
			return matchingApps.get(occ);
		}
	}

	/**
	 * 
	 * @param proof
	 * @param g
	 * @param rulename
	 * @param formula
	 * @return
	 * @throws ScriptException
	 */
	public static ImmutableList<TacletApp> findAllTacletApps(Proof proof, Goal g, String rulename, Term formula)
			throws ScriptException {
		Services services = proof.getServices();
		TacletFilter filter = new TacletNameFilter(rulename);

		RuleAppIndex index = g.ruleAppIndex();
		index.autoModeStopped();

		ImmutableList<TacletApp> allApps = ImmutableSLList.nil();
		for (SequentFormula sf : g.node().sequent().antecedent()) {
			if (formula != null && !sf.formula().equalsModRenaming(formula)) {
				continue;
			}
			allApps = allApps.append(index.getTacletAppAtAndBelow(filter,
					new PosInOccurrence(sf, PosInTerm.getTopLevel(), true), services));
		}

		for (SequentFormula sf : g.node().sequent().succedent()) {
			if (formula != null && !sf.formula().equalsModRenaming(formula)) {
				continue;
			}
			allApps = allApps.append(index.getTacletAppAtAndBelow(filter,
					new PosInOccurrence(sf, PosInTerm.getTopLevel(), false), services));
		}

		return allApps;
	}

	/**
	 * Filter those apps from a list that are according to the parameters.
	 * 
	 * @param on
	 */
	private static List<TacletApp> filterList(ImmutableList<TacletApp> list, Term on) {
		List<TacletApp> matchingApps = new ArrayList<TacletApp>();
		for (TacletApp tacletApp : list) {
			if (tacletApp instanceof PosTacletApp) {
				PosTacletApp pta = (PosTacletApp) tacletApp;
				if (on == null || pta.posInOccurrence().subTerm().equals(on)) {
					matchingApps.add(pta);
				}
			}
		}
		return matchingApps;
	}

	/*
	 * *************************************************************************
	 * ** Script
	 */
	public static void script(Proof proof, String filename) throws ScriptException, InterruptedException, IOException {
		File file = new File(filename);
		ProofScriptEngine pse = new ProofScriptEngine(file);
		pse.execute(new ClojureControl(), proof);
	}

	/*
	 * *************************************************************************
	 * ** Set
	 */
	public static ProofSettings set(Proof proof, String key, String value) {
		Properties p = new Properties();
		p.put(key, value);
		proof.getSettings().update(p);
		return proof.getSettings();
	}

	public static ProofSettings set(Proof proof, Map<String, String> properties) {
		Properties p = new Properties();
		for (Entry<String, String> entry : properties.entrySet()) {
			p.put(entry.getKey(), entry.getValue());
		}
		proof.getSettings().update(p);
		return proof.getSettings();
	}

	/*
	 * *************************************************************************
	 * ** SMT
	 */
	public static final Map<String, SolverType> SOLVER_MAP = computeSolverMap();

	public static Map<String, SolverType> computeSolverMap() {
		Map<String, SolverType> result = new HashMap<String, SolverType>();

		for (SolverType type : SolverType.ALL_SOLVERS) {
			result.put(type.getName(), type);
		}

		return Collections.unmodifiableMap(result);
	}

	public static void smt(Proof proof, Goal goal, String solver) {
		checkGoalBelongsToProof(goal, proof);

		SolverTypeCollection su = computeSolvers(solver);
		SMTSettings settings = new SMTSettings(goal.proof().getSettings().getSMTSettings(),
				ProofIndependentSettings.DEFAULT_INSTANCE.getSMTSettings(), goal.proof());
		SolverLauncher launcher = new SolverLauncher(settings);
		Collection<SMTProblem> probList = new LinkedList<SMTProblem>();
		probList.add(new SMTProblem(goal));
		launcher.launch(su.getTypes(), probList, goal.proof().getServices());

		for (SMTProblem problem : probList) {
			if (problem.getFinalResult().isValid() == ThreeValuedTruth.VALID) {
				IBuiltInRuleApp app = RuleAppSMT.rule.createApp(null).setTitle(solver);
				problem.getGoal().apply(app);
			}
		}
	}

	private static SolverTypeCollection computeSolvers(String value) {
		String[] parts = value.split(" *, *");
		List<SolverType> types = new ArrayList<SolverType>();
		for (String name : parts) {
			SolverType type = SOLVER_MAP.get(name);
			if (type != null) {
				types.add(type);
			}
		}
		return new SolverTypeCollection(value, 1, types);
	}

	/*
	 * *************************************************************************
	 * ** TryClose
	 */
	public static void tryclose(Proof proof, Goal goal, int steps) throws InterruptedException, Exception {
		TryCloseMacro macro = new TryCloseMacro();
		Node root = proof.root();
		UserInterfaceControl cc = new ClojureControl();
		macro.applyTo(cc, root, null, (ProverTaskListener) cc);
	}
}
