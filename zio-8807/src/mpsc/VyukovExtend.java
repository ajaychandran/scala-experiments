package mpsc;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

final public class VyukovExtend<A> implements Queue<A> {

	private transient Node read;
	@SuppressWarnings("unused")
	private transient volatile Node write;

	public VyukovExtend() {
		this.read = this.write = new Node(null);
	}

	public void add(A data) {
		Node next = new Node(data);
		Node prev = WRITE.getAndSet(this, next);
		prev.lazySet(next);
	}

	@SuppressWarnings("unchecked")
	public A poll() {
		Node next = this.read.get();

		if (next == null)
			return null;

		this.read = next;
		Object data = next.data;
		next.data = null;
		return (A) (data);
	}

	static class Node extends AtomicReference<Node> {
		Object data;

		Node(Object data) {
			this.data = data;
		}
	}

	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<VyukovExtend, Node> WRITE = AtomicReferenceFieldUpdater
			.newUpdater(VyukovExtend.class, Node.class, "write");
}
