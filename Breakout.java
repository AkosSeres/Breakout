/*MIT License

Copyright (c) 2016 Ákos Seres

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.*/

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_D;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;

public class Breakout extends JFrame implements Runnable {
	private static final long serialVersionUID = -7130255355742484287L;
    private static Breakout game;
    private static boolean goLeft = false;
    private static boolean goRight = false;
    private static int counter = 0;
    private static boolean paused = true;
    private static boolean menuSeen = true;
    private static boolean playHover = false;
    private static boolean quitHover = false;
    private final int ballSize = 6;
    private final int xBricks = 16;
    private final int yBricks = 6;
    private final Rectangle[][] bricks = new Rectangle[xBricks][yBricks];
    private final boolean[][] isKilled = new boolean[xBricks][yBricks];
    private final Rectangle playButton = new Rectangle(30, 200, 150, 30);
    private final Rectangle quitButton = new Rectangle(230, 200, 150, 30);
    private final int padSize = 80;
    private int ballX = 197;
    private int ballY = 250;
    private int ballSpeedX = (int) (Math.random() * 11) - 5;
    private int ballSpeedY = -3;
    private int score = 0;
    private int mistakes = 0;
    private int padPos = 160;

    private Breakout() {
        configureBricks();

        setTitle("Breakout");
        setSize(400, 300);
        setVisible(true);
        setResizable(false);
        setLayout(null);
        setBackground(Color.black);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setFocusable(true);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.gif")));
        addKeyListener(new Key());

        Mouse m = new Mouse();
        addMouseListener(m);
        addMouseMotionListener(m);
    }

    public static void main(String[] args) {
        game = new Breakout();
        Thread gameUpdater = new Thread(game);
        gameUpdater.start();
    }

    public void run() {
        try {
            while (true) {
            	long startTime, elapsedTime, wait=0;
            	startTime = System.nanoTime();
                if (!paused) {
                    movePad();
                    moveBall();
                    collisionWithBricks();
                    collisionWithPad();
                }
                elapsedTime = System.nanoTime() - startTime;
                if(elapsedTime < 20000000 && wait==0) TimeUnit.NANOSECONDS.sleep(20000000-elapsedTime);
                else if(elapsedTime >= 20000000 && wait==0){ TimeUnit.NANOSECONDS.sleep(20000000); wait = elapsedTime - 20000000;}
                else if(wait != 0){ TimeUnit.NANOSECONDS.sleep(20000000-elapsedTime-wait); wait = 0;}
            }
        } catch (Exception e) {
            System.out.println("ERROR");
        }
    }

    private void collisionWithPad() {
        Rectangle ballRect = new Rectangle(ballX, ballY, ballSize, ballSize);
        Rectangle padRect = new Rectangle(padPos, 280, padSize, 10);
        if (ballRect.intersects(padRect)) {
            if (ballY + ballSize / 2 > 280) {
                ballSpeedX = -ballSpeedX;
            } else {
                if (ballX + ballSize / 2 <= padPos + (padSize / 4)) {
                    ballSpeedX = -4;
                    ballSpeedY = -ballSpeedY;
                } else if (ballX + ballSize / 2 > padPos + (padSize / 4) && ballX + ballSize / 2 < padPos + (padSize / 2)) {
                    ballSpeedX = -3;
                    ballSpeedY = -ballSpeedY;
                } else if (ballX + ballSize / 2 >= padPos + (padSize / 4) * 3) {
                    ballSpeedX = +4;
                    ballSpeedY = -ballSpeedY;
                } else if (ballX + ballSize / 2 < padPos + (padSize / 4) * 3 && ballX + ballSize / 2 > padPos + (padSize / 2)) {
                    ballSpeedX = +3;
                    ballSpeedY = -ballSpeedY;
                } else {
                    ballSpeedX = (int) (Math.random() * 3)-1;
                    ballSpeedY = -3;
                }
            }
        }
    }

    private void collisionWithBricks() {
        Rectangle ballRect = new Rectangle(ballX, ballY, ballSize, ballSize);
        outerloop:
        for (int i = 0; i < xBricks; i++) {
            for (int j = 0; j < yBricks; j++) {
                if (ballRect.intersects(bricks[i][j]) && !isKilled[i][j]) {
                    score += 10 - j;
                    isKilled[i][j] = true;
                    if (ballY + ballSize / 2 > bricks[i][j].y && ballY + ballSize / 2 < bricks[i][j].y + bricks[i][j].height) {
                        ballSpeedX = -ballSpeedX;
                    } else {
                        ballSpeedY = -ballSpeedY;
                    }
                    break outerloop;
                }
            }
        }
    }

    private void movePad() {
        int padSpeed = 7;
        if (goLeft) padPos -= padSpeed;
        if (goRight) padPos += padSpeed;
        if (padPos <= 0) padPos = 0;
        if (padPos >= getWidth() - padSize) padPos = getWidth() - padSize;
    }

    private void moveBall() {
        ballX += ballSpeedX;
        ballY += ballSpeedY;

        if (ballX <= 0) {
            ballSpeedX = -ballSpeedX;
        }
        if (ballX >= getWidth() - ballSize) {
            ballSpeedX = -ballSpeedX;
        }
        if (ballY <= 24) {
            ballSpeedY = -ballSpeedY;
        }
        if (ballY >= getHeight() - ballSize) {
            mistakes++;
            ballX = 197;
            ballY = 250;
            ballSpeedX = (int) (Math.random() * 11) - 5;
            ballSpeedY = -3;
            try {
                padPos = 160;
                counter = 3;
                Thread.sleep(500);
                counter = 2;
                Thread.sleep(500);
                counter = 1;
                Thread.sleep(500);
            } catch (Exception e) {
                System.out.println("ERROR WHILE MAKING COUNTER");
            } finally {
                counter = 0;
            }
        }
    }

    private void configureBricks() {
        for (int i = 0; i < xBricks; i++) {
            for (int j = 0; j < yBricks; j++) {
                int brickXPlace = 25;
                int brickYPlace = 15;
                bricks[i][j] = new Rectangle(brickXPlace * i + 2, 70 + (brickYPlace * j) + 2, brickXPlace - 2, brickYPlace - 2);
                isKilled[i][j] = false;
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        Image dbImage = createImage(getWidth(), getHeight());
        Graphics dBuffer = dbImage.getGraphics();
        if (score == 720) {
            paintVictory(dBuffer);
        } else {
            paintComponent(dBuffer);
        }
        g.drawImage(dbImage, 0, 0, this);
    }

    private void paintVictory(Graphics g) {
        g.setFont(new Font("ARIEL", Font.BOLD, 30));
        g.setColor(Color.WHITE);
        g.drawString("CONGTARULATIONS!", 44, 100);
        g.setFont(new Font("ARIEL", Font.BOLD, 70));
        g.drawString(Integer.toString(mistakes), 240, 180);
        g.setFont(new Font("ARIEL", Font.BOLD, 22));
        g.drawString("You made it with", 52, 140);
        g.drawString("mistakes!", 52, 180);

        if (quitHover) {
            g.setColor(Color.green);
            g.fillRect(quitButton.x, quitButton.y, quitButton.width, quitButton.height);
            g.setColor(Color.red);
            g.drawString("QUIT GAME", quitButton.x + 16, quitButton.y + 22);
        } else {
            g.setColor(Color.red);
            g.fillRect(quitButton.x, quitButton.y, quitButton.width, quitButton.height);
            g.setColor(Color.green);
            g.drawString("QUIT GAME", quitButton.x + 16, quitButton.y + 22);
        }
    }

    private void paintComponent(Graphics g) {
        g.setFont(new Font("Courier New", Font.PLAIN, 12));
        g.setColor(Color.white);
        g.drawString("Score: " + score, 4, 40);
        g.drawString("Mistakes: " + mistakes, 4, 53);

        g.setColor(Color.white);
        g.fillRect(padPos, 280, padSize, 10);

        g.setColor(Color.white);
        g.fillOval(ballX, ballY, ballSize, ballSize);

        for (int i = 0; i < xBricks; i++) {
            for (int j = 0; j < yBricks; j++) {
                if (!isKilled[i][j]) {
                    switch (j) {
                        case 0:
                            g.setColor(Color.RED);
                            break;
                        case 1:
                            g.setColor(Color.ORANGE);
                            break;
                        case 2:
                            g.setColor(Color.YELLOW);
                            break;
                        case 3:
                            g.setColor(Color.GREEN);
                            break;
                        case 4:
                            g.setColor(Color.BLUE);
                            break;
                        case 5:
                            g.setColor(Color.CYAN);
                            break;
                    }
                    g.fillRect(bricks[i][j].x, bricks[i][j].y, bricks[i][j].width, bricks[i][j].height);
                }
            }
        }

        if (counter != 0) {
            g.setFont(new Font("Courier New", Font.PLAIN, 56));
            g.setColor(Color.WHITE);
            g.drawString(Integer.toString(counter), 181, 200);
        }

        if (menuSeen) {
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            if (playHover) {
                g.setColor(Color.red);
                g.fillRect(playButton.x, playButton.y, playButton.width, playButton.height);
                g.setColor(Color.green);
                g.drawString("START GAME", playButton.x + 10, playButton.y + 22);
            } else {
                g.setColor(Color.green);
                g.fillRect(playButton.x, playButton.y, playButton.width, playButton.height);
                g.setColor(Color.red);
                g.drawString("START GAME", playButton.x + 10, playButton.y + 22);
            }

            if (quitHover) {
                g.setColor(Color.green);
                g.fillRect(quitButton.x, quitButton.y, quitButton.width, quitButton.height);
                g.setColor(Color.red);
                g.drawString("QUIT GAME", quitButton.x + 20, quitButton.y + 22);
            } else {
                g.setColor(Color.red);
                g.fillRect(quitButton.x, quitButton.y, quitButton.width, quitButton.height);
                g.setColor(Color.green);
                g.drawString("QUIT GAME", quitButton.x + 20, quitButton.y + 22);
            }
        }

        repaint();
    }

    private static class ExitMenu implements Runnable {
        public void run() {
            menuSeen = false;
            try {
                counter = 3;
                Thread.sleep(500);
                counter = 2;
                Thread.sleep(500);
                counter = 1;
                Thread.sleep(500);
            } catch (Exception e) {
                System.out.println("ERROR WHILE MAKING COUNTER");
            } finally {
                counter = 0;
            }
            paused = false;
        }
    }

    private class Key implements KeyListener {
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            if (keyCode == VK_A || keyCode == VK_LEFT) {
                goLeft = true;
            }
            if (keyCode == VK_D || keyCode == VK_RIGHT) {
                goRight = true;
            }
            if (keyCode == VK_ESCAPE)
                game.dispatchEvent(new WindowEvent(game, WindowEvent.WINDOW_CLOSING));
        }
        public void keyReleased(KeyEvent e) {
            int keyCode = e.getKeyCode();
            if (keyCode == VK_A || keyCode == VK_LEFT) {
                goLeft = false;
            }
            if (keyCode == VK_D || keyCode == VK_RIGHT) {
                goRight = false;
            }
        }
        public void keyTyped(KeyEvent e) {

        }
    }

    private class Mouse extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int xCoord = e.getX();
            int yCoord = e.getY();
            if (xCoord >= playButton.x && yCoord >= playButton.y && xCoord <= playButton.x + playButton.width && yCoord <= playButton.y + playButton.height && menuSeen) {
                Thread menuExitThread = new Thread(new ExitMenu());
                menuExitThread.start();
            }
            if (xCoord >= quitButton.x && yCoord >= quitButton.y && xCoord <= quitButton.x + quitButton.width && yCoord <= quitButton.y + quitButton.height && (menuSeen || score == 720))
                game.dispatchEvent(new WindowEvent(game, WindowEvent.WINDOW_CLOSING));
        }

        @Override
        public void mouseMoved(MouseEvent mouseEvent) {
            int xCoord = mouseEvent.getX();
            int yCoord = mouseEvent.getY();
            if (xCoord >= playButton.x && yCoord >= playButton.y && xCoord <= playButton.x + playButton.width && yCoord <= playButton.y + playButton.height)
                playHover = true;
            if (!(xCoord >= playButton.x && yCoord >= playButton.y && xCoord <= playButton.x + playButton.width && yCoord <= playButton.y + playButton.height))
                playHover = false;
            if (xCoord >= quitButton.x && yCoord >= quitButton.y && xCoord <= quitButton.x + quitButton.width && yCoord <= quitButton.y + quitButton.height)
                quitHover = true;
            if (!(xCoord >= quitButton.x && yCoord >= quitButton.y && xCoord <= quitButton.x + quitButton.width && yCoord <= quitButton.y + quitButton.height))
                quitHover = false;
        }
    }
}
