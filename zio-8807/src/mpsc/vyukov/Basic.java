package mpsc.vyukov;

import mpsc.Queue;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public final class Basic<A> implements Queue<A> {

	private transient Node read;
	@SuppressWarnings("unused")
	private transient volatile Node write;

	public Basic() {
		read = write = new Node(null);
	}

	public void add(A data) {
		Node next = new Node(data);
		Node prev = WRITE.getAndSet(this, next);
		NEXT.lazySet(prev, next);
	}

	@SuppressWarnings("unchecked")
	public A poll() {
		Node next = read.next;

		if (next == null)
			return null;

		read = next;
		Object data = next.data;
		next.data = null;
		return (A) (data);
	}

	static class Node {
		Object data;
		volatile Node next;

		Node(Object data) {
			this.data = data;
		}

		Node(Object data, Node next) {
			this.data = data;
			this.next = next;
		}
	}

	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<Basic, Node> WRITE = AtomicReferenceFieldUpdater
			.newUpdater(Basic.class, Node.class, "write");
	private static final AtomicReferenceFieldUpdater<Node, Node> NEXT = AtomicReferenceFieldUpdater
			.newUpdater(Node.class, Node.class, "next");
}
