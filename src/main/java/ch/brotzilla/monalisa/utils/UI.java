package ch.brotzilla.monalisa.utils;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;

import com.google.common.base.Preconditions;

public class UI {

    private UI() {
    }

    public static Rectangle getTotalScreenSize() {
        return new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    }
    
    public static Rectangle getFirstScreen() {
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] screens = ge.getScreenDevices();
        if (screens.length > 0) {
            return screens[0].getDefaultConfiguration().getBounds();
        }
        return getTotalScreenSize();
    }
    
    public static Rectangle computeScreenCenteredWindowBounds(Dimension windowSize, Dimension minWindowSize) {
        return computeScreenCenteredWindowBounds(getFirstScreen(), windowSize, minWindowSize);
    }
    
    public static Rectangle computeScreenCenteredWindowBounds(Rectangle screen, Dimension windowSize, Dimension minWindowSize) {
        Preconditions.checkNotNull(screen, "The parameter 'screen' must not be null");
        Preconditions.checkNotNull(windowSize, "The parameter 'windowSize' must not be null");
        if (minWindowSize == null) {
            minWindowSize = windowSize;
        }
        final int width = Math.min(Math.max(windowSize.width, minWindowSize.width), screen.width - 20);
        final int height = Math.min(Math.max(windowSize.height, minWindowSize.height), screen.height - 20);
        return new Rectangle(screen.x + screen.width / 2 - width / 2, screen.y + screen.height / 2 - height / 2, width, height);
    }
}
