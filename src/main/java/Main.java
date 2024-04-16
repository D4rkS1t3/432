import javax.swing.SwingUtilities;
import com.convert.Frame;

public class Main {
    public static void main(String[] args) {
        // Użycie interfejsu Runnable do wywołania metody createAndShowGUI() w osobnym wątku
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Frame.createAndShowGUI();
            }
        });
    }
}
