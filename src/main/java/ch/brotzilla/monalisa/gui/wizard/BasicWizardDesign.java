package ch.brotzilla.monalisa.gui.wizard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ch.brotzilla.util.MatrixLayout;

public class BasicWizardDesign implements JWizardDesign {

    private final List<ButtonListener> buttonListeners;
    private final JPanel headerPanel, titlePanel, bodyPanel, sidebarPanel, contentPanel, buttonPanel;
    private final JLabel titleLabel;
    private final JWizardImageDisplay imageDisplay;
    private final JButton buttonPrevious, buttonNext, buttonFinish, buttonCancel;
    
    private int headerHeight = 60;
    private int sidebarWidth = 160;

    protected JPanel setupButtonPanel() {
        final JPanel result = new JPanel(); 
        
        final String[] rows = new String[] {
                "Size=100% CellAlign=Center"
        };
        
        final String[] cols = new String[] {
                "Size=100%",
                "Size=Preferred CellInsets=0,2 CellGroup=Button",
                "Size=Preferred CellInsets=0,6 CellGroup=Button",
                "Size=Preferred CellInsets=0,4 CellGroup=Button",
                "Size=Preferred CellInsets=0,0 CellGroup=Button",
                "Size=15"
        };
        
        result.setLayout(new MatrixLayout(rows, cols));
        result.setPreferredSize(new Dimension(0, 50));
        
        result.add(buttonPrevious, "row=1 col=2");
        result.add(buttonNext    , "row=1 col=3");
        result.add(buttonFinish  , "row=1 col=4");
        result.add(buttonCancel  , "row=1 col=5");
        
        return result;
    }

    protected JPanel setupContentPanel() {
        return new JPanel();
    }

    protected JPanel setupSidebarPanel() {
        final JPanel result = new JPanel();
        
        result.setPreferredSize(new Dimension(sidebarWidth, 0));
        
        return result;
    }

    protected JPanel setupBodyPanel() {
        final JPanel result = new JPanel();

        final String[] rows = new String[] {
                "Size=100%      CellAlign=Fill CellInsets=0,2",
                "Size=Preferred CellAlign=Fill CellInsets=0,0"
        };
        
        final String[] cols = new String[] {
                "Size=Preferred CellAlign=Fill CellInsets=0,2",
                "Size=100%      CellAlign=Fill CellInsets=0,0"
        };
        
        result.setLayout(new MatrixLayout(rows, cols));
        result.setBackground(Color.white);

        result.add(sidebarPanel, "row=1 col=1 vSpan=2");
        result.add(contentPanel, "row=1 col=2");
        result.add(buttonPanel,  "row=2 col=2");
        
        return result;
    }

    protected JPanel setupHeaderPanel() {
        final JPanel result = new JPanel();

        final String[] rows = new String[] {
                "Size=100%",
        };
        
        final String[] cols = new String[] {
                "Size=Preferred",
                "Size=100%      CellAlign=Fill"
        };

        result.setLayout(new MatrixLayout(rows, cols));
        result.setBackground(Color.white);
        result.setPreferredSize(new Dimension(0, headerHeight));
        
        result.add(imageDisplay, "row=1 col=1");
        result.add(titlePanel,   "row=1 col=2");
        
        return result;
    }
    
    protected JLabel setupTitleLabel() {
        final JLabel result = new JLabel();
        
        result.setFont(new Font(result.getFont().getFontName(), result.getFont().getStyle(), 16));
        
        return result;
    }
    
    protected JPanel setupTitlePanel() {
        final JPanel result = new JPanel();

        final String[] rows = new String[] {
                "Size=50%       CellInsets=0,0",
                "Size=Preferred CellInsets=0,0 CellAlign=Top",
                "Size=50%       CellInsets=0,0"
        };
        
        final String[] cols = new String[] {
                "Size=100%",
        };

        result.setLayout(new MatrixLayout(rows, cols));
        result.setBackground(Color.white);

        result.add(titleLabel,    "row=2 col=1");
        
        return result;
    }
    
    protected JWizardImageDisplay setupImageDisplay() {
        final JWizardImageDisplay result = new JWizardImageDisplay();
        
        result.setBackground(Color.white);
        result.setPreferredSize(new Dimension(headerHeight, headerHeight));
        
        return result;
    }
    
    protected void setupButtons() {
        buttonPrevious.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireButtonAction(Button.Previous, e);
            }
        });
        buttonNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireButtonAction(Button.Next, e);
            }
        });
        buttonFinish.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireButtonAction(Button.Finish, e);
            }
        });
        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireButtonAction(Button.Cancel, e);
            }
        });
    }
    
    protected void fireButtonAction(Button button, ActionEvent e) {
        for (final ButtonListener l : buttonListeners) {
            l.actionPerformed(button, e);
        }
    }
    
    public BasicWizardDesign() {
        buttonListeners = Lists.newArrayList();
        buttonPrevious = new JButton();
        buttonNext = new JButton();
        buttonFinish = new JButton();
        buttonCancel = new JButton();
        setupButtons();
        imageDisplay = setupImageDisplay();
        titleLabel = setupTitleLabel();
        titlePanel = setupTitlePanel();
        headerPanel = setupHeaderPanel();
        sidebarPanel = setupSidebarPanel();
        contentPanel = setupContentPanel();
        buttonPanel = setupButtonPanel();
        bodyPanel = setupBodyPanel();
    }


    @Override
    public void addListener(ButtonListener listener) {
        Preconditions.checkNotNull(listener, "The parameter 'listener' must not be null");
        Preconditions.checkState(!buttonListeners.contains(listener), "No duplicates allowed");
        buttonListeners.add(listener);
    }

    @Override
    public void removeListener(ButtonListener listener) {
        Preconditions.checkNotNull(listener, "The parameter 'listener' must not be null");
        buttonListeners.remove(listener);
    }

    @Override
    public Image getHeaderImage() {
        return imageDisplay.getImage();
    }

    @Override
    public void setHeaderImage(Image image) {
        imageDisplay.setImage(image);
    }

    @Override
    public int getHeaderHeight() {
        return headerHeight;
    }
    
    @Override
    public void setHeaderHeight(int value) {
        Preconditions.checkArgument(value > 0, "The parameter 'value' has to be greater than zero");
        if (value != headerHeight) {
            headerHeight = value;
            headerPanel.setPreferredSize(new Dimension(0, headerHeight));
            imageDisplay.setPreferredSize(new Dimension(headerHeight, headerHeight));
        }
    }
    
    @Override
    public int getSidebarWidth() {
        return sidebarWidth;
    }
    
    @Override
    public void setSidebarWidth(int value) {
        Preconditions.checkArgument(value > 0, "The parameter 'value' has to be greater than zero");
        if (value != sidebarWidth) {
            sidebarWidth = value;
            sidebarPanel.setPreferredSize(new Dimension(sidebarWidth, 0));
        }
    }

    @Override
    public void setup(JFrame frame) {
        final String[] rows = new String[] {
                "Size=Preferred CellAlign=Fill CellInsets=0,0",
                "Size=100%      CellAlign=Fill CellInsets=0,0"
        };
        
        final String[] cols = new String[] {
                "Size=100% CellAlign=Fill CellInsets=0,0"
        };
        
        frame.setLayout(new MatrixLayout(rows, cols));
        
        frame.add(headerPanel, "row=1 col=1");
        frame.add(bodyPanel, "row=2 col=1");

    }

    @Override
    public String getButtonText(Button button) {
        Preconditions.checkNotNull(button, "The parameter 'button' must not be null");
        switch (button) {
        case Previous:
            return buttonPrevious.getText();
        case Next:
            return buttonNext.getText();
        case Finish:
            return buttonFinish.getText();
        case Cancel:
            return buttonCancel.getText();
        default:
            throw new IllegalArgumentException("Unknown button '" + button + "'");
        }
    }

    @Override
    public void setButtonText(Button button, String value) {
        Preconditions.checkNotNull(button, "The parameter 'button' must not be null");
        switch (button) {
        case Previous:
            buttonPrevious.setText(value);
            break;
        case Next:
            buttonNext.setText(value);
            break;
        case Finish:
            buttonFinish.setText(value);
            break;
        case Cancel:
            buttonCancel.setText(value);
            break;
        default:
            throw new IllegalArgumentException("Unknown button '" + button + "'");
        }
    }

    @Override
    public boolean isButtonEnabled(Button button) {
        Preconditions.checkNotNull(button, "The parameter 'button' must not be null");
        switch (button) {
        case Previous:
            return buttonPrevious.isEnabled();
        case Next:
            return buttonNext.isEnabled();
        case Finish:
            return buttonFinish.isEnabled();
        case Cancel:
            return buttonCancel.isEnabled();
        default:
            throw new IllegalArgumentException("Unknown button '" + button + "'");
        }
    }

    @Override
    public void setButtonEnabled(Button button, boolean value) {
        Preconditions.checkNotNull(button, "The parameter 'button' must not be null");
        switch (button) {
        case Previous:
            buttonPrevious.setEnabled(value);
            break;
        case Next:
            buttonNext.setEnabled(value);
            break;
        case Finish:
            buttonFinish.setEnabled(value);
            break;
        case Cancel:
            buttonCancel.setEnabled(value);
            break;
        default:
            throw new IllegalArgumentException("Unknown button '" + button + "'");
        }
    }

}
