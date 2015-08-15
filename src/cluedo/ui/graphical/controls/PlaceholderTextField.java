package cluedo.ui.graphical.controls;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;

/**
 * From http://stackoverflow.com/questions/16213836/java-swing-jtextfield-set-placeholder
 */
public class PlaceholderTextField extends JTextField {
    private String placeholder;

    public PlaceholderTextField() { }

    @Override
    protected void paintComponent(final Graphics pG) {
        super.paintComponent(pG);

        if (placeholder.length() == 0 || getText().length() > 0) {
            return;
        }

        final Graphics2D g = (Graphics2D) pG;
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(getDisabledTextColor());
        g.drawString(placeholder, getInsets().left, pG.getFontMetrics().getMaxAscent() + getInsets().top);
    }

    public void setPlaceholder(final String s) {
        placeholder = s;
    }

}