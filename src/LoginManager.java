import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class LoginManager {

    private LoginManager() {}

    // ------------------------------------------------------------------
    // SystemNotificationDialog.show(parent, message)
    // ------------------------------------------------------------------
    public static void showSystemNotification(JFrame parent, String message) {
        JDialog dialog = new JDialog(parent, "System Notification", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(350, 150);
        dialog.setLocationRelativeTo(parent);
        dialog.getContentPane().setBackground(new Color(240, 248, 255));

        JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 15));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton okButton = new JButton("OK");
        okButton.setFocusPainted(false);
        okButton.addActionListener(e -> dialog.dispose());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(iconLabel, BorderLayout.WEST);
        centerPanel.add(messageLabel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.add(okButton);

        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // ------------------------------------------------------------------
    // ForgotPasswordDialog  -> returns username or null
    // (replaces getRequestedUsername()/isConfirmed() pattern)
    // ------------------------------------------------------------------
    public static String showForgotPasswordDialog(JFrame parent) {
        final String[] result = {null};

        JDialog dialog = new JDialog(parent, "Forgot Password", true);
        dialog.setLayout(new GridLayout(3, 1, 10, 10));

        JLabel label = new JLabel("Enter your username:");
        JTextField usernameField = new JTextField();

        JButton submitButton = new JButton("Request Reset");
        submitButton.addActionListener(e -> {
            String input = usernameField.getText().trim();
            if (!input.isEmpty()) {
                result[0] = input;
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Username cannot be empty.");
            }
        });

        dialog.add(label);
        dialog.add(usernameField);
        dialog.add(submitButton);

        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result[0];
    }

    // ------------------------------------------------------------------
    // ResetCodeInputDialog -> returns boolean verified (like isVerified())
    // NOTE: original class did not expose the entered code; it only had isVerified().
    // ------------------------------------------------------------------
    public static boolean showResetCodeInputDialog(JFrame parent, String expectedCode) {
        final boolean[] verified = {false};

        JDialog dialog = new JDialog(parent, "Enter Reset Code", true);
        dialog.setLayout(new BorderLayout());

        JTextField codeField = new JTextField();
        JButton verifyButton = new JButton("Verify");

        verifyButton.addActionListener(e -> {
            String enteredCode = codeField.getText().trim();
            if (enteredCode.equals(expectedCode)) {
                verified[0] = true;
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Invalid reset code.");
            }
        });

        dialog.add(new JLabel("Enter the code sent by the admin:"), BorderLayout.NORTH);
        dialog.add(codeField, BorderLayout.CENTER);
        dialog.add(verifyButton, BorderLayout.SOUTH);

        dialog.setSize(350, 120);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return verified[0];
    }

    // ------------------------------------------------------------------
    // PasswordResetForm -> returns new password or null (replaces isSubmitted()/getNewPassword())
    // ------------------------------------------------------------------
    public static String showPasswordResetForm(JFrame parent) {
        final String[] newPassword = {null};

        JDialog dialog = new JDialog(parent, "Reset Password", true);
        dialog.setLayout(new GridLayout(3, 2, 5, 5));

        JPasswordField newPassField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();
        JButton submitBtn = new JButton("Reset");

        submitBtn.addActionListener(e -> {
            String pass1 = new String(newPassField.getPassword());
            String pass2 = new String(confirmField.getPassword());

            if (!pass1.equals(pass2)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match.");
                return;
            }
            if (pass1.length() < 4) {
                JOptionPane.showMessageDialog(dialog, "Password must be at least 4 characters.");
                return;
            }

            newPassword[0] = pass1;
            dialog.dispose();
        });

        dialog.add(new JLabel("New Password:"));
        dialog.add(newPassField);
        dialog.add(new JLabel("Confirm Password:"));
        dialog.add(confirmField);
        dialog.add(new JLabel(""));
        dialog.add(submitBtn);

        dialog.setSize(350, 150);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return newPassword[0];
    }

    // ------------------------------------------------------------------
    // SimpleDocumentListener -> returns a DocumentListener wrapping Runnable
    // ------------------------------------------------------------------
    public static DocumentListener createDocumentListener(Runnable onChange) {
        return new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onChange.run(); }
            @Override public void removeUpdate(DocumentEvent e) { onChange.run(); }
            @Override public void changedUpdate(DocumentEvent e) { onChange.run(); }
        };
    }
}
