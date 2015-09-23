package de.uka.ilkd.key.clojure;

import java.io.File;

import de.uka.ilkd.key.proof.ProofAggregate;
import de.uka.ilkd.key.proof.TaskFinishedInfo;
import de.uka.ilkd.key.proof.init.IPersistablePO.LoadedPOContainer;
import de.uka.ilkd.key.proof.io.AbstractProblemLoader;
import de.uka.ilkd.key.proof.io.AbstractProblemLoader.ReplayResult;
import de.uka.ilkd.key.proof.io.ProblemLoaderException;
import de.uka.ilkd.key.ui.ConsoleUserInterfaceControl;

public class Core {
	public static ProofAggregate loadProblem(String file) {
		ClojureControl control = new ClojureControl();
		control.loadProblem(new File(file));
		// return control.getMediator().getSelectedProof();
		//System.out.println(control.proofList.getProof(0).getStatistics());
		return control.proofList;
	}

	public static void main(String[] args) {
		loadProblem("/home/weigl/work/key/key/key.ui/examples/standard_key/prop_log/contraposition.key");
	}
	
}

/**
 * First! This is not a user interface.
 * 
 * @author Alexander Weigl
 *
 */
class ClojureControl extends ConsoleUserInterfaceControl {
	public ClojureControl() {
		super(false, false);
	}

	ProofAggregate proofList;

	@Override
	public void loadingFinished(AbstractProblemLoader loader, LoadedPOContainer poContainer, ProofAggregate proofList,
			ReplayResult result) throws ProblemLoaderException {
		System.out.println(proofList.getProof(0).getStatistics());
		this.proofList = proofList;
	}

	@Override
	public void taskFinished(TaskFinishedInfo info) {
	}
}
