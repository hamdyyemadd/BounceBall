package BounceBall;

import com.sun.opengl.util.j2d.TextRenderer;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Ball_File extends AnimListener implements GLEventListener, MouseListener, MouseMotionListener, KeyListener {

    TextRenderer ren = new TextRenderer(new Font("sanaSerif", Font.BOLD, 10));
    rectangle ball;
    AudioInputStream audioStream;
    Clip clip;
    ArrayList<block> blocksArray;
    GL gl;
    String page = "Home", level, direction = "up-right";
    boolean sound = true, startGame;
    int maxWidth = 1200, maxHeight = 700, borderX = 550, borderY = 680, ballX = 550, ballY = 655,
            speed = 7, borderSize = 130, lives = 3, delayLives, numberofBlocks, score, time, delaytime;

    String textureNames[] = {"home", "empty", "credits", "how_to_play", "sound", "no_sound", "levels",
        "ball", "border", "square", "pause", "loser", "won", "last-win"};
    TextureReader.Texture[] texture = new TextureReader.Texture[textureNames.length];
    int textures[] = new int[textureNames.length];

    @Override
    public void init(GLAutoDrawable gld) {
        for (int i = 0; i < textureNames.length; i++) {
            System.out.println(i + " = " + textureNames[i]);
        }
        try {
            audioStream = AudioSystem.getAudioInputStream(new File("Assets//songs//converted_background_music.wav"));
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

        gl = gld.getGL();
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);    //This Will Clear The Background Color To Black

        gl.glEnable(GL.GL_TEXTURE_2D);  // Enable Texture Mapping
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glGenTextures(textureNames.length, textures, 0);

        for (int i = 0; i < textureNames.length; i++) {
            try {
                texture[i] = TextureReader.readTexture(assetsFolderName + textureNames[i] + ".png", true);
                gl.glBindTexture(GL.GL_TEXTURE_2D, textures[i]);

                new GLU().gluBuild2DMipmaps(
                        GL.GL_TEXTURE_2D,
                        GL.GL_RGBA, // Internal Texel Format,
                        texture[i].getWidth(), texture[i].getHeight(),
                        GL.GL_RGBA, // External format from image,
                        GL.GL_UNSIGNED_BYTE,
                        texture[i].getPixels() // Imagedata
                );
            } catch (IOException e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }
        createBlocks();
    }

    @Override
    public void display(GLAutoDrawable gld) {
        gl = gld.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        switch (page) {
            case "Home":
                DrawBackground(0);
                int index = sound ? 4 : 5;
                drawObject(100, 130, 2, 2, index);
                break;
            case "Play":
                DrawBackground(6);
                break;
            case "Credits":
                DrawBackground(2);
                break;
            case "How_To_Play":
                DrawBackground(3);
                break;
            case "Game":
                ball = new rectangle(ballX, ballY, 35, 25);
                DrawBackground(1);
                drawBlocks();
                drawObject(borderX, borderY, 1, 0.3, 8);
                drawObject(ballX, ballY, 0.4, 0.5, 7);
                borderCollision();
                bounceBall();
                blocksCollsion();
                if ((level == "easy" && score >= 5) || (level == "medium" && score >= 5)) {
                    page = "Win";
                } else if (score >= 5) {
                    page = "Lastwin";
                }
                // 42 - 70 - 98
                delaytime++;
                if (delaytime > 20 && startGame) {
                    delaytime = 0;
                    time++;
                }
                ren.beginRendering(300, 300);
                ren.setColor(Color.RED);
                ren.draw("Score: " + score, 5, 285);

                ren.draw("Time: " + time, 250, 285);
                ren.setColor(Color.WHITE);
                ren.endRendering();
                break;
            case "Pause":
                DrawBackground(10);
                break;
            case "Lose":
                DrawBackground(11);
                break;
            case "Win":
                DrawBackground(12);
                break;
            case "Lastwin":
                DrawBackground(13);
                break;
        }

    }

    @Override
    public void reshape(GLAutoDrawable glad, int i, int i1, int i2, int i3) {
    }

    @Override
    public void displayChanged(GLAutoDrawable glad, boolean bln, boolean bln1) {
    }

    public void DrawBackground(int index) {
        gl.glEnable(GL.GL_BLEND);	// Turn Blending On
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[index]);

        gl.glBegin(GL.GL_QUADS);
        // Front Face
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glEnd();

        gl.glDisable(GL.GL_BLEND);
    }

    public void drawObject(int x, int y, double scaleX, double scaleY, int index) {
        gl.glEnable(GL.GL_BLEND);	// Turn Blending On
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[index]);
        gl.glPushMatrix();
        gl.glTranslated(x / (maxWidth / 2.0) - 0.9, ((maxHeight - y) / (maxHeight / 2.0) - 0.9), 1);
        gl.glScaled(0.1 * scaleX, 0.1 * scaleY, 1);
//        gl.glRotated(degree, 0, 0, 1);

        gl.glBegin(GL.GL_QUADS);
        // Front Face
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glEnd();
        gl.glPopMatrix();

        gl.glDisable(GL.GL_BLEND);
    }

    public void bounceBall() {
        if (startGame) {
            switch (direction) {
                case "up-right":
                    if (ballX > maxWidth - 100) {
                        direction = "up-left";
                    }
                    if (ballY < 50) {
                        direction = "down-right";
                    }
                    ballX += speed;
                    ballY -= speed;
                    break;
                case "up-left":
                    if (ballX < 0) {
                        direction = "up-right";
                    }
                    if (ballY < 50) {
                        direction = "down-left";
                    }
                    ballX -= speed;
                    ballY -= speed;
                    break;
                case "down-right":
                    if (ballX > maxWidth - 100) {
                        direction = "down-left";
                    }
                    ballX += speed;
                    ballY += speed;
                    break;
                case "down-left":
                    if (ballX < 0) {
                        direction = "down-right";
                    }
                    ballX -= speed;
                    ballY += speed;
                    break;

            }

        }
    }

    void borderCollision() {
        if (ballY > 645 && ballX - borderX < borderSize / 2 && ballX - borderX > 0) {
            direction = "up-right";
        } else if (ballY > 645 && ballX - borderX + 70 < borderSize / 2 && ballX - borderX + 70 > 0) {
            direction = "up-left";
        } else if (ballY > 680) {
            Lose();
        }
    }

    void Lose() {
        startGame = false;
        delayLives++;
        ren.beginRendering(100, 100);
        ren.setColor(Color.RED);

        if (lives - 1 > 0) {
            ren.draw("Lives: " + (lives - 1), 25, 48);
        } else {
            ren.draw("Gameover", 25, 60);
        }
        ren.setColor(Color.WHITE);
        ren.endRendering();
        if (delayLives > 20) {
            lives--;
            direction = "up-right";
            ballX = 550;
            ballY = 655;
            borderX = 550;
            borderY = 680;

            delayLives = 0;
            if (lives == 0) {
                time = 0;
                createBlocks();
                score = 0;
                lives = 3;
                page = "Lose";
            }
        }
    }

    void createBlocks() {
        blocksArray = new ArrayList<>();
        int x = 100, y = 100;
        for (int i = 0; i < 98; i++) {
            if (i != 0) {
                if (i % 14 == 0) {
                    y += 50;
                    x = 100;
                } else {
                    x += 70;
                }
            }
            blocksArray.add(new block(x, y));
        }

    }

    public void drawBlocks() {
        numberofBlocks = level == "easy" ? 42 : level == "medium" ? 70 : 98;
        for (int i = 0; i < numberofBlocks; i++) {
            if (!blocksArray.get(i).crach) {
                drawObject(blocksArray.get(i).x, blocksArray.get(i).y, 0.5, 0.5, 9);
            }
        }

    }

    void blocksCollsion() {
        // widthball = 35 , heightball = 25 , widthblock = 65 , heightblock = 35 ; 
        for (int i = 0; i < numberofBlocks; i++) {
            block block = blocksArray.get(i);
            if (ball.intersect(new rectangle(block.x - 5, block.y, 45, 10)) && !block.crach) {
                direction = direction == "up-right" ? "down-right" : "down-left";
                block.crach = true;
                System.out.println("down");
                score++;
            } else if (ball.intersect(new rectangle(block.x - 5, block.y - 15, 45, 10)) && !block.crach) {
                direction = direction == "down-right" ? "up-right" : "up-left";
                block.crach = true;
                System.out.println("up");
                score++;
            } else if (ball.intersect(new rectangle(block.x - 10, block.y, 5, 25)) && !block.crach) {
                direction = direction == "up-right" ? "up-left" : "down-left";
                block.crach = true;
                System.out.println("left");
                score++;
            } else if (ball.intersect(new rectangle(block.x + 40, block.y, 5, 25)) && !block.crach) {
                direction = direction == "up-left" ? "up-right" : "down-right";
                block.crach = true;
                System.out.println("right");
                score++;
            }
            System.out.println("score = " + score);

        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        System.out.println(e.getX() + " " + e.getY());
        switch (page) {
            // if Home Screen
            case "Home":
                if (e.getX() > 546 && e.getX() < 685 && e.getY() < 277 && e.getY() > 225) {
                    page = "Play";
                } else if (e.getX() > 427 && e.getX() < 805 && e.getY() < 388 && e.getY() > 334) {
                    page = "How_To_Play";
                } else if (e.getX() > 510 && e.getX() < 722 && e.getY() < 499 && e.getY() > 443) {
                    page = "Credits";
                } else if (e.getX() > 951 && e.getX() < 1071 && e.getY() < 616 && e.getY() > 565) {
                    System.exit(0);
                } else if (e.getX() > 105 && e.getX() < 185 && e.getY() < 110 && e.getY() > 65) {
                    sound = !sound;
                    System.out.println("sound = " + sound);
                    if (sound) {
                        clip.start();
                    } else {
                        clip.stop();
                    }
                }
                break;
            case "Play":
                if (e.getX() > 350 && e.getX() < 445 && e.getY() > 330 && e.getY() < 430) {
                    page = "Game";
                    level = "easy";
                } else if (e.getX() > 515 && e.getX() < 710 && e.getY() > 330 && e.getY() < 430) {
                    page = "Game";
                    level = "medium";
                } else if (e.getX() > 770 && e.getX() < 900 && e.getY() > 330 && e.getY() < 415) {
                    page = "Game";
                    level = "hard";
                } else if (e.getX() > 940 && e.getX() < 1080 && e.getY() > 562 && e.getY() < 617) {
                    page = "Home";
                }
                break;

            case "Game":
                startGame = true;
                if (e.getX() > 940 && e.getX() < 1080 && e.getY() > 562 && e.getY() < 617) {
                    page = "Home";
                }

            case "How_To_Play":
            case "Credits":
                if (e.getX() > 940 && e.getX() < 1080 && e.getY() > 562 && e.getY() < 617) {
                    page = "Home";
                }
                break;

            case "Pause":
                if (e.getX() > 507 && e.getX() < 736 && e.getY() > 228 && e.getY() < 279) {
                    System.out.println("resume");
                    page = "Game";
                }
                if (e.getX() > 506 && e.getX() < 733 && e.getY() > 339 && e.getY() < 387) {
                    System.out.println("restart");
                    reset();
                    page = "Game";
                }
                if (e.getX() > 557 && e.getX() < 681 && e.getY() > 451 && e.getY() < 502) {
                    System.out.println("exit");
                    page = "Home";
                    reset();
                }
                break;
            case "Lose":

                break;
            case "Win":
                if (e.getX() > 119 && e.getX() < 253 && e.getY() > 519 && e.getY() < 563) {
                    System.out.println("exit");
                    page = "Home";
                    reset();
                }
                if (e.getX() > 911 && e.getX() < 1042 && e.getY() > 513 && e.getY() < 565) {
                    System.out.println("next");
                    level = level == "easy" ? "medium" : "hard";
                    page = "Game";
                    reset();
                }
                break;
            case "Lastwin":
                if (e.getX() > 119 && e.getX() < 253 && e.getY() > 519 && e.getY() < 563) {
                    System.out.println("exit");
                    page = "Home";
                    reset();
                }
                break;

        }
    }

    void reset() {
        time = 0 ;
        startGame = false;
        score = 0;
        createBlocks();
        lives = 3;
        direction = "up-right";
        ballX = 550;
        ballY = 655;
        borderX = 550;
        borderY = 680;
    }

    @Override
    public void mousePressed(MouseEvent me) {
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void mouseDragged(MouseEvent me) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
//        System.out.println(e.getX() + " " + e.getY());

        if (e.getX() < 1078 && e.getX() > 0 && startGame) {
            borderX = e.getX();
        }

    }

    BitSet keybits = new BitSet(256);

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keybits.set(e.getKeyCode());

        if (keybits.get(KeyEvent.VK_RIGHT)) {
            ballX += 5;
        }
        if (keybits.get(KeyEvent.VK_LEFT)) {
            ballX -= 5;
        }
        if (keybits.get(KeyEvent.VK_UP)) {
            ballY -= 5;
//            ballX = 550;
//            ballY = 300;
        }
        if (keybits.get(KeyEvent.VK_DOWN)) {
            ballY += 5;
        }

        if (keybits.get(KeyEvent.VK_ESCAPE) || keybits.get(KeyEvent.VK_P)) {
            page = "Pause";
            startGame = !startGame;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keybits.clear(e.getKeyCode());

    }

}

class block {

    int x, y;
    boolean crach;

    block(int x, int y) {
        this.x = x;
        this.y = y;

    }
}

class rectangle {

    int lx, ly, rx, ry;

    rectangle(int x, int y, int width, int height) {
        lx = x;
        ly = y;
        rx = width + x;
        ry = y - height;
    }

    boolean intersect(rectangle r) {

        if (lx > r.rx || r.lx > rx) {
            return false;
        }
        if (ry > r.ly || r.ry > ly) {
            return false;
        }
        return true;
    }

}
