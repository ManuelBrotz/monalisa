package ch.brotzilla.monalisa.gui.wizard;

import javax.swing.JFrame;

import ch.brotzilla.monalisa.gui.wizard.JWizardDesign.*;

import com.google.common.base.Preconditions;

public class StartWizardModel implements JWizardModel {

    private final JWizardDesign design;
    
    public StartWizardModel(JWizardDesign design) {
        Preconditions.checkNotNull(design, "The parameter 'design' must not be null"); 
        this.design = design;
    }

    @Override
    public JWizardDesign getDesign() {
        return design;
    }

    @Override
    public void setup(JFrame frame) {
        design.setup(frame);
        design.setButtonText(Button.Previous, "Previous");
        design.setButtonEnabled(Button.Previous, false);
        design.setButtonText(Button.Next, "Next");
        design.setButtonText(Button.Finish, "Finish");
        design.setButtonEnabled(Button.Finish, false);
        design.setButtonText(Button.Cancel, "Cancel");
    }

}
