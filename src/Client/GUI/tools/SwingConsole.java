package Client.GUI.tools;

import javax.swing.*;

public class SwingConsole
{
    public static void run(final JFrame f,final int width, final int height)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                f.setTitle(f.getClass().getSimpleName());
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setSize(width,height);
                f.setLocationByPlatform(true);
                f.setVisible(true);
            }
        });
    }
    public static void run(final JFrame f,String title,final int width, final int height)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {

                f.setTitle(title);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setSize(width,height);
                f.setLocationByPlatform(true);
                f.setVisible(true);
            }
        });
    }

    public static void run(final JFrame f)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                f.setTitle(f.getClass().getSimpleName());
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setSize(400,400);
                f.setVisible(true);
            }
        });
    }



}