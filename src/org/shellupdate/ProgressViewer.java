package org.shellupdate;

public interface ProgressViewer {
	public default ValueChange<Double> progressProperty() {
		return progressProperty(0.0, 1.0);
	}

	public default ValueChange<Double> progressProperty(double start, double end) {
		if (end < start || start < 0 || end > 1) {
			throw new IllegalArgumentException("start and end must be between 0 and 1 inclusive where end is greater than start");
		}
		return new ValueChange<Double>() {

			@Override
			public Double getValue() {
				return (getProgress() / 100.0 - start) / (end - start);
			}

			@Override
			public void setValue(Double newValue) {
				setProgress((int) ((newValue * (end - start) + start) * 100));
			}
		};
	}

	void finish();

	int getProgress();

	String getProgressText();

	void setProgress(int percent);

	void setProgressText(String progress);
}
