package cluedo.ui.graphical.controls;

import javax.swing.*;
import java.awt.*;

/**
 * Convenience class which makes it easier to use GridBagLayout.
 * Items can be added using panel.setup(component).addToLayout() - they are added to the first row until finishRow() is
 * called, then to the next row and so on
 */
public class GridPanel extends JPanel {
    /**
     * Builds an instance of GridBagConstraints to be added to the panel
     */
    public class GridItemBuilder {
        private Component component;
        private GridBagConstraints constraints;

        /**
         * Creates a new grid item builder
         * @param component component to add to the layout
         */
        public GridItemBuilder(Component component) {
            this.component = component;
            this.constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.WEST;
        }

        /**
         * Adds the component to the panel this was set up from, at the current row and column position
         *
         * @return the component that was added
         */
        public Component addToLayout() {
            constraints.gridx = currentColumn;
            constraints.gridy = currentRow;
            GridPanel.this.add(component, constraints);
            GridPanel.this.currentColumn += constraints.gridwidth;
            return component;
        }

        /**
         * Center the component horizontally and vertically
         *
         * @return this
         */
        public GridItemBuilder center() {
            constraints.anchor = GridBagConstraints.CENTER;
            return this;
        }

        /**
         * Causes the item to take up multiple columns of the grid
         *
         * @param span how many columns this item should span across
         * @return this
         */
        public GridItemBuilder spanH(int span) {
            constraints.gridwidth = span;
            return this;
        }

        /**
         * Sets the item to expand horizontally, taking up all available room in the layout
         *
         * @return this
         */
        public GridItemBuilder flexH() {
            return flexH(1);
        }

        /**
         * Sets the item to expand horizontally, taking up all available room in the layout
         *
         * @param weight how much space this item should take up relative to other expanding components
         * @return this
         */
        public GridItemBuilder flexH(double weight) {
            constraints.weightx = weight;
            constraints.fill = (constraints.fill == GridBagConstraints.VERTICAL) ?
                    GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
            return this;
        }

        /**
         * Sets the item to expand vertically, taking up all available room in the layout
         *
         * @return this
         */
        public GridItemBuilder flexV() {
            return flexV(1);
        }

        /**
         * Sets the item to expand vertically, taking up all available room in the layout
         *
         * @param weight how much space this item should take up relative to other expanding components
         * @return this
         */
        public GridItemBuilder flexV(double weight) {
            constraints.weighty = weight;
            constraints.fill = (constraints.fill == GridBagConstraints.HORIZONTAL) ?
                    GridBagConstraints.BOTH : GridBagConstraints.VERTICAL;
            return this;
        }

        /**
         * Adds the specified amount of blank space on all sides of the component
         *
         * @param padding the amount of space to add
         * @return this
         */
        public GridItemBuilder pad(int padding) {
            constraints.insets = new Insets(padding, padding, padding, padding);
            return this;
        }
    }

    private int currentRow = 0;
    private int currentColumn = 0;

    /**
     * Creates a new grid panel
     */
    public GridPanel() {
        super(new GridBagLayout());
    }

    /**
     * Creates a grid item builder for the specified component, linked to this panel
     *
     * @param component component to set up
     * @return an instance of GridItemBuilder that can be used to configure the component's position and size, and
     *         add it to the layout
     */
    public GridItemBuilder setup(Component component) {
        return new GridItemBuilder(component);
    }

    /**
     * Start a new row in the layout
     */
    public void finishRow() {
        currentRow++;
        currentColumn = 0;
    }
}
