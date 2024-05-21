package mpsc;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class Vyukov<A> implements Queue<A> {

	private Node read;
	@SuppressWarnings("unused")
	private volatile Node write;

	public Vyukov() {
		this.read = this.write = new Node(null);
	}

	public void add(A data) {
		assert (data != null);

		Node last = new Node(data);
		Node tail = WRITE.getAndSet(this, last);
		NEXT.lazySet(tail, last);
	}

	public A poll() {
		Node head = this.read.next;

		if (head == null)
			return null;

		@SuppressWarnings("unchecked")
		A data = (A) (head.data);
		head.data = null;
		this.read = head;
		return data;
	}

	static class Node implements Serializable {
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
