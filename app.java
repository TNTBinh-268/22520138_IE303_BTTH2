import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;

class Bird {

    private int posX, posY, verticalSpeed;
    private final int JUMP_STRENGTH = -12;
    private final int GRAVITY = 1;
    private BufferedImage birdSprite;

    public Bird(int x, int y) {
        this.posX = x;
        this.posY = y;
        this.verticalSpeed = 0;
        try {
            birdSprite = ImageIO.read(new File("flappybird.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updatePosition() {
        verticalSpeed += GRAVITY;
        posY += verticalSpeed;
    }

    public void flap() {
        verticalSpeed = JUMP_STRENGTH;
    }

    public void resetPosition() {
        posY = 290;
        verticalSpeed = 0;
    }

    public void render(Graphics g) {
        g.drawImage(birdSprite, posX, posY, 40, 30, null);
    }

    public int getX() {
        return posX;
    }

    public int getY() {
        return posY;
    }

    public int getWidth() {
        return 40;
    }

    public int getHeight() {
        return 30;
    }
}

class Pipe {

    private int posX, topY, bottomY;
    private static final int WIDTH = 60;
    private static final int HEIGHT = 400;
    private static final int GAP = 150;
    private static double speed = 3;
    private static BufferedImage topPipeSprite, bottomPipeSprite;

    public static void loadSprites() {
        try {
            topPipeSprite = ImageIO.read(new File("toppipe.png"));
            bottomPipeSprite = ImageIO.read(new File("bottompipe.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Pipe(int startX) {
        posX = startX;
        randomizeHeights();
    }

    private void randomizeHeights() {
        int midPoint = (int) (Math.random() * 250) + 100;
        topY = midPoint - HEIGHT;
        bottomY = midPoint + GAP;
    }

    public void updatePosition() {
        posX -= speed;
        speed += 0.001;
    }

    public boolean isOffScreen() {
        return posX + WIDTH < 0;
    }

    public boolean isPassedByBird(int birdX) {
        return posX + WIDTH < birdX;
    }

    public void render(Graphics g) {
        g.drawImage(topPipeSprite, posX, topY, WIDTH, HEIGHT, null);
        g.drawImage(bottomPipeSprite, posX, bottomY, WIDTH, HEIGHT, null);
    }

    public int getX() {
        return posX;
    }

    public int getTopY() {
        return topY;
    }

    public int getBottomY() {
        return bottomY;
    }

    public void setSpeed(int newSpeed) {
        speed = newSpeed;
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener {

    private Timer gameTimer;
    private Bird bird;
    private ArrayList<Pipe> pipeList;
    private boolean isGameOver;
    private int currentScore;
    private boolean scoreFlag = false;
    private BufferedImage bgImage;
    private boolean isGameStarted = false;

    public GamePanel() {
        setPreferredSize(new Dimension(360, 640));
        setBackground(Color.CYAN);
        setFocusable(true);
        addKeyListener(this);

        Pipe.loadSprites();
        try {
            bgImage = ImageIO.read(new File("flappybirdbg.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        setupGame();
        gameTimer = new Timer(16, this);
    }

    private void setupGame() {
        bird = new Bird(80, 290);
        pipeList = new ArrayList<>();
        pipeList.add(new Pipe(400));
        isGameOver = false;
        currentScore = 0;
        isGameStarted = false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isGameOver) {
            bird.updatePosition();

            for (int i = 0; i < pipeList.size(); i++) {
                Pipe pipe = pipeList.get(i);
                pipe.updatePosition();

                if (pipe.isPassedByBird(bird.getX()) && !scoreFlag) {
                    currentScore++;
                    scoreFlag = true;
                }

                if (pipe.isOffScreen()) {
                    pipeList.remove(i);
                    i--;
                    scoreFlag = false;
                }
            }

            if (pipeList.size() > 0 && pipeList.get(pipeList.size() - 1).getX() < 200) {
                pipeList.add(new Pipe(400));
            }

            detectCollision();
            repaint();
        }
    }

    private void detectCollision() {
        Rectangle birdRect = new Rectangle(bird.getX(), bird.getY(), bird.getWidth(), bird.getHeight());
        for (Pipe pipe : pipeList) {
            Rectangle topRect = new Rectangle(pipe.getX(), pipe.getTopY(), 60, 400);
            Rectangle bottomRect = new Rectangle(pipe.getX(), pipe.getBottomY(), 60, 400);
            if (birdRect.intersects(topRect) || birdRect.intersects(bottomRect) || bird.getY() >= 520) {
                isGameOver = true;
                gameTimer.stop();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        bird.render(g);
        for (Pipe pipe : pipeList) {
            pipe.render(g);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Roboto", Font.BOLD, 20));
        g.drawString("Score: " + currentScore, 20, 30);

        if (!isGameStarted) {
            g.drawString("Press ENTER to Start", 80, 280);
        } else if (isGameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Roboto", Font.BOLD, 40));
            g.drawString("GAME OVER!",50, 320);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Roboto", Font.BOLD, 20));
            g.drawString("Press R to Restart", 80, 280);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!isGameStarted && (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE)) {
            isGameStarted = true;
            gameTimer.start();
        } else if (isGameStarted && !isGameOver
                && (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER)) {
            bird.flap();
        } else if (isGameOver && e.getKeyCode() == KeyEvent.VK_R) {
            restartGame();
        }
    }

    private void restartGame() {
        setupGame();
        bird.resetPosition();
        pipeList.clear();
        pipeList.add(new Pipe(400));
        for (Pipe pipe : pipeList) {
            pipe.setSpeed(3);
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}

public class app {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        GamePanel gamePanel = new GamePanel();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(360, 640);
        frame.setResizable(false);
        frame.add(gamePanel);
        frame.setVisible(true);
    }
}