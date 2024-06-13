package mpsc.vyukov;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import mpsc.Queue;

abstract class RPadded128Write implements Serializable {
	protected transient volatile RPadded128.Node write;
}

abstract class RPadded128WritePad extends RPadded128Write {
	protected long _0;
	protected long _1;
	protected long _2;
	protected long _3;
	protected long _4;
	protected long _5;
	protected long _6;
	protected long _7;
	protected long _8;
	protected long _9;
	protected long _a;
	protected long _b;
	protected long _c;
	protected long _d;
}

public final class RPadded128<A> extends RPadded128WritePad implements Queue<A> {

	private transient Node read;

	public RPadded128() {
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

	static final AtomicReferenceFieldUpdater<RPadded128Write, Node> WRITE = AtomicReferenceFieldUpdater
			.newUpdater(RPadded128Write.class, Node.class, "write");
}
