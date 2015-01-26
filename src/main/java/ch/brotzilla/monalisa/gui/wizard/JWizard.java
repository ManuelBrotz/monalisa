package ch.brotzilla.monalisa.gui.wizard;

import java.awt.Dimension;

import javax.swing.JFrame;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.utils.UI;

/*
 * Width : 660
 * Height: 410
 * 
 * Body Height  : 350
 * Header Height: 60  (15% of Height)
 * Footer Height: 50  (15% of Body Height)
 * 
 * Sidebar Width: 160 (25% of Width)
 * 
 */

@SuppressWarnings("serial")
public class JWizard extends JFrame {

    private final JWizardModel model;
    
    public JWizard(JWizardModel model) {
        Preconditions.checkNotNull(model, "The parameter 'model' must not be null");
        
        this.model = model;
        model.setup(this);
    }
    
    public static void main(String[] args) {
        final JWizard w = new JWizard(new StartWizardModel(new BasicWizardDesign()));
        w.setBounds(UI.computeScreenCenteredWindowBounds(new Dimension(700, (int) (700 * 0.618)), null));
        w.setVisible(true);
    }
    
}
