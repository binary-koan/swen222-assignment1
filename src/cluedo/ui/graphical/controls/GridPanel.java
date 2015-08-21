package cluedo.ui.graphical.controls;

import javax.swing.*;
import java.awt.*;

public class GridPanel extends JPanel {
    public class GridItemBuilder {
        private Component component;
        private GridBagConstraints constraints;

        public GridItemBuilder(Component component) {
            this.component = component;
            this.constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.WEST;
        }

        public Component addToLayout() {
            constraints.gridx = currentColumn;
            constraints.gridy = currentRow;
            GridPanel.this.add(component, constraints);
            GridPanel.this.currentColumn++;
            return component;
        }

        public GridItemBuilder center() {
            constraints.anchor = GridBagConstraints.CENTER;
            return this;
        }

        public GridItemBuilder spanH(int span) {
            constraints.gridwidth = span;
            return this;
        }

        public GridItemBuilder flexH() {
            return flexH(1);
        }

        public GridItemBuilder flexH(double weight) {
            constraints.weightx = weight;
            constraints.fill = (constraints.fill == GridBagConstraints.VERTICAL) ?
                    GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
            return this;
        }

        public GridItemBuilder flexV() {
            return flexV(1.0);
        }

        public GridItemBuilder flexV(double weight) {
            constraints.weighty = weight;
            constraints.fill = (constraints.fill == GridBagConstraints.HORIZONTAL) ?
                    GridBagConstraints.BOTH : GridBagConstraints.VERTICAL;
            return this;
        }

        public GridItemBuilder pad(int padding) {
            constraints.insets = new Insets(padding, padding, padding, padding);
            return this;
        }
    }

    private int currentRow = 0;
    private int currentColumn = 0;

    public GridPanel() {
        super(new GridBagLayout());
    }

    public GridItemBuilder setup(Component component) {
        return new GridItemBuilder(component);
    }

//    public Component addToLayout(GridItemBuilder item) {
//        item.constraints.gridx = currentColumn;
//        item.constraints.gridy = currentRow;
//        add(item.component, item.constraints);
//        currentColumn++;
//        return item.component;
//    }

//    public Component addToLayout(Component component, double weight) {
//        GridBagConstraints constraints = new GridBagConstraints();
//        constraints.weighty = 1;
//        constraints.weightx = weight;
//        if (weight > 0) {
//            constraints.fill = GridBagConstraints.BOTH;
//        }
//        else {
//            constraints.fill = GridBagConstraints.VERTICAL;
//        }
//        constraints.gridx = currentColumn;
//        constraints.gridy = currentRow;
//        constraints.insets = new Insets(5, 5, 5, 5);
//
//        add(component, constraints);
//
//        currentColumn++;
//        return component;
//    }

    public void finishRow() {
        currentRow++;
        currentColumn = 0;
    }
}
