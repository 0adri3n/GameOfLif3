package Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.Timer;

public class Client {

    public class ClientGui extends JPanel {

        private ArrayList<ArrayList<JPanel>> gridCells;
        public int DIMENSION;
        public int rowMax;
        public int colMax;
        public int cellLimit;
        public int offsetX = 0;
        public int offsetY = 0;
        public boolean playing = true;


        public ClientGui(int rowMax, int colmax, int cellLimit) {
            this.rowMax = rowMax;
            this.colMax = colmax;
            this.gridCells = new ArrayList<>();
            this.DIMENSION = 10;
            this.cellLimit = cellLimit;
            setLayout(new GridBagLayout());
            Random rnd = new Random();
            ArrayList<ArrayList<Integer>> randomCellsGrid = getRandomCellsGrid(rowMax, colMax, cellLimit);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = 0;
            for (int col = 0; col < rowMax; col++) {
                gbc.gridx = 0;
                ArrayList<JPanel> tempCol = new ArrayList<>();
                for (int row = 0; row < colMax; row++) {
                    JPanel cell = new JPanel();
                    cell.setPreferredSize(new Dimension(DIMENSION, DIMENSION));
                    ArrayList<Integer> temp = new ArrayList<>();
                    temp.add(col);
                    temp.add(row);
                    cell.setBackground(randomCellsGrid.contains(temp) ? Color.WHITE : Color.BLACK);
                    add(cell, gbc);
                    tempCol.add(cell);
                    gbc.gridx++;
                }
                this.gridCells.add(tempCol);
                gbc.gridy++;
            }

            Timer timer = new Timer(100, e -> updateGrid());

            InputMap im = getInputMap(WHEN_FOCUSED);
            ActionMap am = getActionMap();

            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_6, 0), "onDezoom");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0), "onZoom");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "onPause");

            am.put("onZoom", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DIMENSION++;
                    if(playing){
                        updateGrid();
                    }
                    else{
                        updateSize();
                    }

                }
            });

            am.put("onDezoom", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (DIMENSION > 1){
                        DIMENSION--;
                        if(playing){
                            updateGrid();
                        }
                        else{
                            updateSize();
                        }
                    }
                }
            });
            am.put("onPause", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(playing){
                        timer.stop();
                        playing = false;
                    }
                    else{
                        timer.start();
                        playing = true;
                    }

                }
            });

            timer.start();

            this.setFocusable(true);
            this.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    handleKeyPress(e);
                }
            });
        }


        private void handleKeyPress(KeyEvent e) {
            int keyCode = e.getKeyCode();
            int step = 1;

            switch (keyCode) {
                case KeyEvent.VK_UP:
                    offsetY = Math.max(0, offsetY - step);

                    break;
                case KeyEvent.VK_DOWN:
                    offsetY = Math.min(rowMax - getHeight() / DIMENSION, offsetY + step);

                    break;
                case KeyEvent.VK_LEFT:
                    offsetX = Math.max(0, offsetX - step);

                    break;
                case KeyEvent.VK_RIGHT:
                    offsetX = Math.min(colMax - getWidth() / DIMENSION, offsetX + step);

                    break;
            }
        }


        public void updateGrid() {
            SwingUtilities.invokeLater(() -> {
                checkGridCells();
                repaint();
                revalidate();
            });
        }

        public void updateSize(){
            SwingUtilities.invokeLater(() -> {
                for (int col = 0; col < gridCells.size(); col++) {
                    for (int row = 0; row < gridCells.get(col).size(); row++) {
                        gridCells.get(col).get(row).setPreferredSize(new Dimension(DIMENSION, DIMENSION));
                    }
                }
                repaint();
                revalidate();
            });
        }

        public ArrayList<JPanel> getNeighboringCells(int col, int row) {
            ArrayList<JPanel> neighbors = new ArrayList<>();

            // Les positions relatives des voisins par rapport à la cellule actuelle
            int[][] relativePositions = {
                    {-1, -1}, {-1, 0}, {-1, 1},
                    {0, -1},           {0, 1},
                    {1, -1}, {1, 0}, {1, 1}
            };

            for (int[] relativePos : relativePositions) {
                int neighborCol = col + relativePos[0];
                int neighborRow = row + relativePos[1];

                // Vérifier si le voisin est à l'intérieur de la grille
                if (neighborCol >= 0 && neighborCol < gridCells.size() &&
                        neighborRow >= 0 && neighborRow < gridCells.get(neighborCol).size()) {
                    neighbors.add(gridCells.get(neighborCol).get(neighborRow));
                }
            }

            return neighbors;
        }

        public void checkGridCells() {
            ArrayList<ArrayList<JPanel>> updatedGrid = new ArrayList<>();

            for (int col = 0; col < gridCells.size(); col++) {
                ArrayList<JPanel> tempCol = new ArrayList<>();
                for (int row = 0; row < gridCells.get(col).size(); row++) {
                    JPanel cell = new JPanel();
                    cell.setBackground(gridCells.get(col).get(row).getBackground());
                    tempCol.add(cell);
                }
                updatedGrid.add(tempCol);
            }

            for (int col = 0; col < gridCells.size(); col++) {
                for (int row = 0; row < gridCells.get(col).size(); row++) {
                    ArrayList<JPanel> neighboringCells = getNeighboringCells(col, row);
                    int activeCellsCount = 0;
                    for (JPanel neighboringCell : neighboringCells) {
                        if (neighboringCell.getBackground() == Color.WHITE) {
                            activeCellsCount++;
                        }
                    }

                    if (gridCells.get(col).get(row).getBackground() == Color.WHITE) { // Si la cellule est active
                        if (activeCellsCount < 2 || activeCellsCount > 3) {
                            updatedGrid.get(col).get(row).setBackground(Color.BLACK);
                        }
                    } else { // Si la cellule est inactive
                        if (activeCellsCount == 3) {
                            updatedGrid.get(col).get(row).setBackground(Color.WHITE);
                        }
                    }
                }
            }

            for (int col = 0; col < gridCells.size(); col++) {
                for (int row = 0; row < gridCells.get(col).size(); row++) {
                    gridCells.get(col).get(row).setBackground(updatedGrid.get(col).get(row).getBackground());
                    gridCells.get(col).get(row).setPreferredSize(new Dimension(DIMENSION, DIMENSION));
                }
            }
        }

        public ArrayList<ArrayList<Integer>> getRandomCellsGrid(int rowMax, int colMax, int cellLimit) {
            ArrayList<ArrayList<Integer>> randomCells = new ArrayList<>();
            Random rnd = new Random();
            for (int i = 0; i < cellLimit; i++) {
                ArrayList<Integer> rdComp = new ArrayList<>();
                rdComp.add(rnd.nextInt(rowMax));
                rdComp.add(rnd.nextInt(colMax));
                randomCells.add(rdComp);
            }
            return randomCells;
        }


    }

    public Client(int rowMax, int colMax, int cellLimit) {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }

            JFrame frame = new JFrame("Game Of Life");
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JScrollPane scrollPane = new JScrollPane(new ClientGui(rowMax, colMax, cellLimit));
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            frame.add(scrollPane);
            frame.setResizable(true);
            frame.setVisible(true);
        });
    }

}
