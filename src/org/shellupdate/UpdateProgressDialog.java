package org.shellupdate;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets; extends JDialog {

	/**
	 * Laun

import java.io.File;

public class UpdateProgressDialog

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;ch the application.
	 */
	public static void main(String[] args) {
		try {
			UpdateProgressDialog dialog = new UpdateProgressDialog();
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private final JPanel contentPanel = new JPanel();
	private final JLabel lblKeyStorePass;
	private final JLabel lblUpdaterPassword;
	private final JTextField textField;
	private final JPasswordField passwordField;
	private final JLabel lblUpdatedVersion;
	private final JButton btnLoad;
	private final JPanel panel;
	private final JTextField txtVersion;
	private final JButton button;
	private final JLabel lblUpdateName;
	private final JTextField txtUpdateName;
	private File newVersion;

	/**
	 * Create the dialog.
	 */
	public UpdateProgressDialog() {
		setResizable(false);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 200, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);

		lblKeyStorePass = new JLabel("Key Store Password:");
		GridBagConstraints gbc_lblKeyStorePass = new GridBagConstraints();
		gbc_lblKeyStorePass.anchor = GridBagConstraints.WEST;
		gbc_lblKeyStorePass.insets = new Insets(5, 10, 10, 10);
		gbc_lblKeyStorePass.gridx = 0;
		gbc_lblKeyStorePass.gridy = 0;
		contentPanel.add(lblKeyStorePass, gbc_lblKeyStorePass);

		textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(5, 0, 10, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		contentPanel.add(textField, gbc_textField);
		textField.setColumns(10);

		lblUpdaterPassword = new JLabel("Updater Password:");
		GridBagConstraints gbc_lblUpdaterPassword = new GridBagConstraints();
		gbc_lblUpdaterPassword.anchor = GridBagConstraints.WEST;
		gbc_lblUpdaterPassword.insets = new Insets(0, 10, 5, 10);
		gbc_lblUpdaterPassword.gridx = 0;
		gbc_lblUpdaterPassword.gridy = 1;
		contentPanel.add(lblUpdaterPassword, gbc_lblUpdaterPassword);

		passwordField = new JPasswordField();
		GridBagConstraints gbc_passwordField = new GridBagConstraints();
		gbc_passwordField.insets = new Insets(0, 0, 5, 0);
		gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
		gbc_passwordField.gridx = 1;
		gbc_passwordField.gridy = 1;
		contentPanel.add(passwordField, gbc_passwordField);

		lblUpdatedVersion = new JLabel("New Version:");
		GridBagConstraints gbc_lblUpdatedVersion = new GridBagConstraints();
		gbc_lblUpdatedVersion.anchor = GridBagConstraints.WEST;
		gbc_lblUpdatedVersion.insets = new Insets(0, 10, 5, 5);
		gbc_lblUpdatedVersion.gridx = 0;
		gbc_lblUpdatedVersion.gridy = 2;
		contentPanel.add(lblUpdatedVersion, gbc_lblUpdatedVersion);

		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 2;
		contentPanel.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		txtVersion = new JTextField();
		txtVersion.setEditable(false);
		GridBagConstraints gbc_txtVersion = new GridBagConstraints();
		gbc_txtVersion.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtVersion.gridx = 0;
		gbc_txtVersion.gridy = 0;
		panel.add(txtVersion, gbc_txtVersion);
		txtVersion.setColumns(10);

		button = new JButton("...");
		button.addActionListener(e -> {

		});
		button.setPreferredSize(new Dimension(23, 23));
		button.setMinimumSize(new Dimension(23, 23));
		button.setMaximumSize(new Dimension(23, 23));
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.gridx = 1;
		gbc_button.gridy = 0;
		panel.add(button, gbc_button);

		lblUpdateName = new JLabel("Update Name:");
		GridBagConstraints gbc_lblUpdateName = new GridBagConstraints();
		gbc_lblUpdateName.anchor = GridBagConstraints.WEST;
		gbc_lblUpdateName.insets = new Insets(0, 10, 5, 5);
		gbc_lblUpdateName.gridx = 0;
		gbc_lblUpdateName.gridy = 3;
		contentPanel.add(lblUpdateName, gbc_lblUpdateName);

		txtUpdateName = new JTextField();
		GridBagConstraints gbc_txtUpdateName = new GridBagConstraints();
		gbc_txtUpdateName.anchor = GridBagConstraints.NORTH;
		gbc_txtUpdateName.insets = new Insets(0, 0, 5, 0);
		gbc_txtUpdateName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtUpdateName.gridx = 1;
		gbc_txtUpdateName.gridy = 3;
		contentPanel.add(txtUpdateName, gbc_txtUpdateName);
		txtUpdateName.setColumns(10);

		btnLoad = new JButton("Load");
		GridBagConstraints gbc_btnLoad = new GridBagConstraints();
		gbc_btnLoad.anchor = GridBagConstraints.EAST;
		gbc_btnLoad.gridx = 1;
		gbc_btnLoad.gridy = 4;
		contentPanel.add(btnLoad, gbc_btnLoad);
		pack();
	}

}
