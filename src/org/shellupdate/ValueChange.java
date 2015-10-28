package org.shellupdate;

public interface ValueChange<T> {
	public T getValue();

	public void setValue(T newValue);
}
