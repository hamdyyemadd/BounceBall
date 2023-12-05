/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BounceBall;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.FPSAnimator;
import java.awt.BorderLayout;
import javax.media.opengl.GLCanvas;
import javax.swing.JFrame;

public class BounceBall extends JFrame {
     public BounceBall(){
         
        Animator animator;
        GLCanvas glcanvas = new GLCanvas();
        Ball_File listener = new Ball_File();

        glcanvas.addGLEventListener(listener);
        glcanvas.addMouseMotionListener(listener);
        glcanvas.addMouseListener(listener);
        add(glcanvas, BorderLayout.CENTER);
        animator = new FPSAnimator(10);
        animator.add(glcanvas);
        animator.start();

        setTitle("Bounce Ball");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setVisible(true);
        setFocusable(true);
        setResizable(false);
        glcanvas.requestFocus();   
 }

    
    public static void main(String []args){
        new BounceBall();
    }
}
