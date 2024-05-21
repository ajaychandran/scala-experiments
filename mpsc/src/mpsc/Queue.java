package mpsc;

import java.io.Serializable;

public interface Queue<A> extends Serializable {

	public void add(A data);

	public A poll();
}
