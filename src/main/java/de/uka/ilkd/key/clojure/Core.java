package de.uka.ilkd.key.clojure;

import java.io.File;

import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.ui.ConsoleUserInterfaceControl;

public class Core {
	public static Proof loadProblem(String file) {
		ConsoleUserInterfaceControl control = new ConsoleUserInterfaceControl(false, true);
		control.loadProblem(new File(file));
		return control.getMediator().getSelectedProof();
	}
}