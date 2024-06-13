package mpsc.vyukov;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import mpsc.Queue;

abstract class RPadded64Write implements Serializable {
	protected transient volatile RPadded64.Node write;
}

abstract class RPadded64WritePad extends RPadded64Write {
	protected long _0;
	protected long _1;
	protected long _2;
	protected long _3;
	protected long _4;
	protected long _5;
}

public final class RPadded64<A> extends RPadded64WritePad implements Queue<A> {

	private transient Node read;

	public RPadded64() {
		read = write = new Node(null);
	}

	@Override
	public void add(A data) {
		Node next = new Node(data);
		Node prev = WRITE.getAndSet(this, next);
		Node.NEXT.lazySet(prev, next);
	}

	@SuppressWarnings("unchecked")
	@Override
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

		static final AtomicReferenceFieldUpdater<Node, Node> NEXT = AtomicReferenceFieldUpdater.newUpdater(Node.class,
				Node.class, "next");
	}

	static final AtomicReferenceFieldUpdater<RPadded64Write, Node> WRITE = AtomicReferenceFieldUpdater
			.newUpdater(RPadded64Write.class, Node.class, "write");
}
