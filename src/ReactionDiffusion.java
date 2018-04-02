import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.WindowConstants;

public class ReactionDiffusion implements ActionListener {

    private static final int DELAY = 0;

    private static final int[][] DIRECTIONS = new int[][]{{-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}};

    private int width, height;
    private double[][][] grid;

    private double cent, card, ord; // Laplace weights

    private double DA, DB, f, k, dt; // Reaction-Diffusion weights

    private Timer timer = new Timer(DELAY, this);

    private JFrame frame = new JFrame("Reaction-Diffusion");
    private JButton reset = new JButton("Reset");
    private JLabel label = new JLabel();
    private ImageIcon icon = new ImageIcon();
    private BufferedImage image;

    public ReactionDiffusion(int width, int height, double cent, double card, double ord, double DA, double DB, double f, double k, double dt) {
        this.width = width;
        this.height = height;
        grid = new double[width][height][2];

        this.cent = cent;
        this.card = card;
        this.ord = ord;

        this.DA = DA;
        this.DB = DB;
        this.f = f;
        this.k = k;
        this.dt = dt;

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        init(); // initializes grid and image

        icon.setImage(image);
        label.setIcon(icon);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        reset.addActionListener(this);

        frame.add(reset, BorderLayout.NORTH);
        frame.add(label, BorderLayout.CENTER);

        frame.pack();
        frame.setResizable(false);

        frame.setVisible(true);

        timer.start();
    }

    public ReactionDiffusion(int width, int height) {
        this(width, height, -1, .2, .05, 1, .5, .055, .062, 1);
    }

    /**
     * Initializes grid and image.
     */
    private void init() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[x][y][0] = 1;

                //TODO add some chemical B
                int size = 100; // temp
                if ((width - size) / 2 < x && x < (width + size) / 2 && (height - size) / 2 < y && y < (height + size) / 2) {
                    grid[x][y][1] = 1;
                }

                int r = 0;
                int g = (int) (grid[x][y][0] * 255);
                int b = (int) (grid[x][y][1] * 255);
                int argb = new Color(r, g, b).getRGB();
                image.setRGB(x, y, argb);
            }
        }
    }

    private static double constrain(double val, double min, double max) {
        if (min > val) {
            return min;
        }
        else if (val > max) {
            return max;
        }
        return val;
    }

    /**
     * Returns a weighted sum of quantities of a chemical c from the 3x3 square around point (x, y).
     * @param x x coordinate
     * @param y y coordinate
     * @param c chemical type
     * @return weighted sum
     */
    private double laplace(int x, int y, int c) {
        double sum = cent * grid[x][y][c];
        for (int[] direction : DIRECTIONS) {
            int ax = (x + direction[0]);
            int ay = (y + direction[1]);
            if (-1 < ay && ay < height) {
                if (-1 < ax && ax < width) {
                    if (x == ax || y == ay) {
                        sum += card * grid[ax][ay][c];
                    }
                    else {
                        sum += ord * grid[ax][ay][c];
                    }
                }
            }
        }
        return sum;
    }

    /**
     * Updates grid and image.
     */
    private void updateImage() {
        double[][][] temp = new double[width][height][2];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double A = grid[x][y][0];
                double B = grid[x][y][1];

                temp[x][y][0] = constrain(A + (DA * laplace(x, y, 0) - A * B * B + f * (1 - A)) * dt, 0, 1);
                temp[x][y][1] = constrain(B + (DB * laplace(x, y, 1) + A * B * B - (k + f) * B) * dt, 0, 1);

                int r = 0;
                int g = (int) (temp[x][y][0] * 255);
                int b = (int) (temp[x][y][1] * 255);
                int argb = new Color(r, g, b).getRGB();
                image.setRGB(x, y, argb);
            }
        }
        grid = temp;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource().equals(timer)) {
            updateImage();
            frame.repaint();
        }
        else if (event.getSource().equals(reset)) {
            grid = new double[width][height][2];
            init();
        }
    }

    public static void main(String[] args) {
        new ReactionDiffusion(256, 256);
    }
}
