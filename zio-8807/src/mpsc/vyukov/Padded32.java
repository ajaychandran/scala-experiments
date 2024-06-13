package mpsc.vyukov;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import mpsc.Queue;

abstract class Padded32ClassPad implements Serializable {
	protected int _0;
	protected long _1;
	protected long _2;
}

abstract class Padded32Write extends Padded32ClassPad {
	protected transient volatile Padded32.Node write;
}

abstract class Padded32WritePad extends Padded32Write {
	protected int __0;
	protected long __1;
	protected long __2;
	protected long __3;
}

public final class Padded32<A> extends Padded32WritePad implements Queue<A> {

	private transient Node read;

	public Padded32() {
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

	static final AtomicReferenceFieldUpdater<Padded32Write, Node> WRITE = AtomicReferenceFieldUpdater
			.newUpdater(Padded32Write.class, Node.class, "write");
}
