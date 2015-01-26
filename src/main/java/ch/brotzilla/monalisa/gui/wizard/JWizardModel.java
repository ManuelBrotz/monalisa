package ch.brotzilla.monalisa.gui.wizard;

import javax.swing.JFrame;

public interface JWizardModel {

    JWizardDesign getDesign();
    
    void setup(JFrame frame);
    
}
