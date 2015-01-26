package ch.brotzilla.monalisa.gui.wizard;

import java.awt.Image;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;

public interface JWizardDesign {

    public enum Button {
        Previous, Next, Finish, Cancel
    }
    
    public static interface ButtonListener {
        
        void actionPerformed(Button button, ActionEvent e);
        
    }
    
    public void addListener(ButtonListener listener);
    
    public void removeListener(ButtonListener listener);
    
    Image getHeaderImage();
    
    void setHeaderImage(Image image);
    
    int getHeaderHeight();
    
    void setHeaderHeight(int value);
    
    int getSidebarWidth();
    
    void setSidebarWidth(int value);
    
    String getButtonText(Button button);
    
    void setButtonText(Button button, String value);
    
    boolean isButtonEnabled(Button button);
    
    void setButtonEnabled(Button button, boolean value);
    
    void setup(JFrame frame);
}
