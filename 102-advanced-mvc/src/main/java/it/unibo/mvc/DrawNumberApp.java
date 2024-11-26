package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final int MIN = 0;
    private static final int MAX = 100;
    private static final int ATTEMPTS = 10;

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        int min = MIN;
        int max = MAX;
        int attempts = ATTEMPTS;
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.yml");
        if(inputStream == null) {
            JOptionPane.showMessageDialog(null, "File not founded");
        }
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = null;
            while((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                StringTokenizer stringTokenizer = new StringTokenizer(line, ": ");
                if(stringTokenizer.countTokens() == 2) {
                    String string1 = stringTokenizer.nextToken().trim();
                    String string2 = stringTokenizer.nextToken().trim();
                    switch (string1) {
                        case "minimum":
                            min = Integer.parseInt(string2);
                            break;
                        case "maximum":
                            max = Integer.parseInt(string2);
                            break;
                        case "attempts":
                            attempts = Integer.parseInt(string2);
                            break;
                        default:
                            JOptionPane.showMessageDialog(null, "In file config.yml is not found a sensed line");
                            break;
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error to open bufferedReader" + e.getMessage());
        }
        this.model = new DrawNumberImpl(min, max, attempts);
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl());
    }

}
