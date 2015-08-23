package cluedo.ui.graphical.controls;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;

/**
 * TextField implementation which can contain greyed-out placeholder text
 *
 * Based on http://stackoverflow.com/questions/16213836/java-swing-jtextfield-set-placeholder
 */
public class PlaceholderTextField extends JTextField {
    private String placeholder;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

        if (placeholder.length() == 0 || getText().length() > 0) {
            return;
        }

        if (g instanceof Graphics2D) {
            final Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setColor(getDisabledTextColor());
        g.drawString(placeholder, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);
    }

    /**
     * Sets the placeholder text of this field
     * @param text the new placeholder text
     */
    public void setPlaceholder(final String text) {
        placeholder = text;
    }

}