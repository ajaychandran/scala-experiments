package mpsc;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class Vyukov<A> implements Queue<A> {

	private transient Node read;
	@SuppressWarnings("unused")
	private transient volatile Node write;

	public Vyukov() {
		this.read = this.write = new Node(null);
	}

	public void add(A data) {
		assert (data != null);

		Node next = new Node(data);

		AtomicReferenceFieldUpdater<Node, Node> NEXT = Vyukov.NEXT;

		Node prev = WRITE.getAndSet(this, next);
		NEXT.lazySet(prev, next);
	}

	@SuppressWarnings("unchecked")
	public A poll() {
		Node next = this.read.next;

		if (next == null)
			return null;

		this.read = next;
		return (A) (next.data);
	}

	static class Node {
		Object data;
		volatile Node next;

		Node(Object data) {
			this.data = data;
		}
	}

	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<Vyukov, Node> WRITE = AtomicReferenceFieldUpdater
			.newUpdater(Vyukov.class, Node.class, "write");
	private static final AtomicReferenceFieldUpdater<Node, Node> NEXT = AtomicReferenceFieldUpdater
			.newUpdater(Node.class, Node.class, "next");
}
