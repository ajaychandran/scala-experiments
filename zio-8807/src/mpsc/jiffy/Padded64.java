package mpsc.jiffy;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import mpsc.Queue;

abstract class Padded64Fields implements Serializable {
	protected transient volatile int insert;
	protected transient volatile Padded64.Segment write;
}

abstract class Padded64FieldsPad extends Padded64Fields {
	protected int _00;
	protected long _0;
	protected long _1;
	protected long _2;
	protected long _3;
	protected long _4;
	protected long _5;
	protected long _6;
	protected long _7;
}

final public class Padded64<A> extends Padded64FieldsPad implements Queue<A> {

	private final int grow;
	private transient Segment read;

	public Padded64() {
		this.grow = 1;
		read = write = new Segment(4);
	}

	public Padded64(int step) {
		this.grow = 1;
		read = write = new Segment(step);
	}

	public Padded64(int step, int grow) {
		this.grow = grow;
		read = write = new Segment(step);
	}

	public void add(A data) {
		if (null == data)
			throw new NullPointerException();

		// load instance fields locally to prevent reload after sync
		final AtomicReferenceFieldUpdater<Segment, Segment> NEXT = Segment.NEXT;
		final AtomicReferenceFieldUpdater<Padded64Fields, Segment> WRITE = Padded64.WRITE;
		final int grow = this.grow;

		Segment write = this.write;
		final int step = write.length();

		// acquire and increment global index
		final int insert = INSERT.getAndAdd(this, 1);

		// fast path
		if (insert < step) {
			// global first segment
			write.lazySet(insert, data);
			return;
		}

		var canGrow = true;
		int start, index;
		Segment next;

		// locate segment
		do {

			start = write.start;

			while (insert < start) {
				// segment is in the back
				canGrow = false;
				write = write.prev;
				start = write.start;
			}

			index = insert - start;

			if (index < step) {
				// segment located
				write.lazySet(index, data);
				if (index == grow && canGrow) {
					// expand eagerly
					next = new Segment(write, step, start + step);
					NEXT.compareAndSet(write, null, next);
				}
				return;
			}

			next = write.next;
			if (null == next) {
				// expand
				next = new Segment(write, step, start + step);
				if (NEXT.compareAndSet(write, null, next)) {
					WRITE.lazySet(this, next);
				}
			} else {
				// next segment may be stale
				WRITE.compareAndSet(this, write, next);
			}

			// reload post expansion
			write = this.write;

		} while (true);
	}

	@SuppressWarnings("unchecked")
	public A poll() {

		// load instance fields locally to prevent reload after sync
		final Object READ = Padded64.READ;

		Segment read = this.read;
		int index = read.index;
		final int start = read.start;
		final int step = read.length();

		// acquire global index
		final int insert = this.insert;

		if (start + index == insert) {
			// queue is empty
			return null;
		}

		boolean canDrop = true;
		A data;
		Segment next;

		do {

			for (; index < step; index += 1) {
				data = (A) (read.get(index));

				if (null == data) {
					// element not inserted yet
					canDrop = false;
					continue;
				}

				if (canDrop) {
					// elements upto index were read
					this.read.index = index + 1;
				}

				if (READ != data) {
					// release element
					read.lazySet(index, READ);
					return data;
				}
			}

			// acquire next segment
			next = read.next;

			if (null == next) {
				// end of queue
				return null;
			}

			read = next;
			index = read.index;

			if (canDrop) {
				// drop segment
				this.read = read;
			}

		} while (true);
	}

	static class Segment extends AtomicReferenceArray<Object> {

		final Segment prev;
		final int start;
		int index;

		volatile Segment next;

		Segment(int size) {
			super(size);
			this.prev = null;
			this.start = 0;
		}

		Segment(Segment prev, int size, int start) {
			super(size);
			this.prev = prev;
			this.start = start;
		}

		static final AtomicReferenceFieldUpdater<Segment, Segment> NEXT = AtomicReferenceFieldUpdater
				.newUpdater(Segment.class, Segment.class, "next");
	}

	private static final Object READ = new Object();

	private static final AtomicReferenceFieldUpdater<Padded64Fields, Segment> WRITE = AtomicReferenceFieldUpdater
			.newUpdater(Padded64Fields.class, Segment.class, "write");
	private static final AtomicIntegerFieldUpdater<Padded64Fields> INSERT = AtomicIntegerFieldUpdater
			.newUpdater(Padded64Fields.class, "insert");
}
