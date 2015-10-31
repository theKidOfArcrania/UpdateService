package org.shellupdate;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

public class ProgressDialog extends JDialog {
	private final JPanel contentPanel = new JPanel();

	private final JLabel lblProgress;
	private final JProgressBar pbrLoading;

	public ProgressDialog(String title) {
		this(title, null);
	}

	/**
	 * @wbp.parser.constructor
	 */
	public ProgressDialog(String title, Window owner) {
		super(owner);
		setEnabled(false);
		setTitle(title);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 400, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		this.setIconImage(null);
		contentPanel.setLayout(gbl_contentPanel);
		lblProgress = new JLabel("Loading...");
		GridBagConstraints gbc_lblProgress = new GridBagConstraints();
		gbc_lblProgress.anchor = GridBagConstraints.WEST;
		gbc_lblProgress.insets = new Insets(20, 10, 5, 10);
		gbc_lblProgress.gridx = 0;
		gbc_lblProgress.gridy = 0;
		contentPanel.add(lblProgress, gbc_lblProgress);

		pbrLoading = new JProgressBar();
		GridBagConstraints gbc_pbrLoading = new GridBagConstraints();
		gbc_pbrLoading.insets = new Insets(0, 10, 50, 10);
		gbc_pbrLoading.fill = GridBagConstraints.BOTH;
		gbc_pbrLoading.gridx = 0;
		gbc_pbrLoading.gridy = 1;
		contentPanel.add(pbrLoading, gbc_pbrLoading);

		pack();
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent evt) {
				setLocationRelativeTo(null);
			}
		});
	}

	public int getProgress() {
		return pbrLoading.getValue();
	}

	public String getProgressText() {
		return lblProgress.getText();
	}

	public ValueChange<Double> progressProperty() {
		return progressProperty(0.0, 1.0);
	}

	public ValueChange<Double> progressProperty(double start, double end) {
		if (end < start || start < 0 || end > 1) {
			throw new IllegalArgumentException("start and end must be between 0 and 1 inclusive where end is greater than start");
		}
		return new ValueChange<Double>() {

			@Override
			public Double getValue() {
				return (pbrLoading.getValue() / 100.0 - start) / (end - start);
			}

			@Override
			public void setValue(Double newValue) {
				pbrLoading.setValue((int) ((newValue * (end - start) + start) * 100));
			}
		};
	}

	public void setProgress(int percent) {
		pbrLoading.setValue(Math.min(100, Math.max(0, percent)));
	}

	public void setProgressText(String progress) {
		lblProgress.setText(progress);
	}
}
