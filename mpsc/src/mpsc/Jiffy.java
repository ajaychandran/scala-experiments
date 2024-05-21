package mpsc;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

@SuppressWarnings("rawtypes")
public class Jiffy<A> implements Queue<A> {

	private Segment read;
	private final int grow;

	private volatile Segment write;
	private volatile int last;

	public Jiffy(int step) {
		this(step, -1);
	}

	public Jiffy(int step, int grow) {
		this.read = this.write = new Segment(null, step, 0);
		this.grow = grow;
	}

	public void add(A data) {
		assert (data != null);

		final AtomicReferenceFieldUpdater<Segment, Segment> NEXT = Segment.NEXT;
		final AtomicReferenceFieldUpdater<Jiffy, Segment> WRITE = Jiffy.WRITE;
		final AtomicIntegerFieldUpdater<Jiffy> LAST = Jiffy.LAST;

		final int grow = this.grow;

		Segment tail = this.write;
		var isTail = true;
		int index = 0;

		final int step = tail.length();
		final int last = LAST.getAndAdd(this, 1);

		while (true) {

			// queue could have expanded concurrently
			while (last < tail.start) {
				// segment is in the back
				isTail = false;
				tail = tail.prev;
			}

			index = last - tail.start;

			if (index < step) {
				// success!
				tail.lazySet(index, data);
				if (index == grow && isTail) {
					// expand queue eagerly
					Segment next = new Segment(tail, step, tail.start + step);
					NEXT.compareAndSet(tail, null, next);
				}
				return;
			} else {
				Segment next = tail.next;
				if (null == next) {
					// expand queue
					next = new Segment(tail, step, tail.start + step);
					if (NEXT.compareAndSet(tail, null, next)) {
						WRITE.lazySet(this, next);
					}
				} else {
					// queue expanded concurrently
					WRITE.compareAndSet(this, tail, next);
				}

				// reload last segment
				tail = this.write;
			}
		}
	}

	public boolean isEmpty() {
		Segment read = this.read;
		int index = read.index;
		final int start = read.start;

		return start + index == this.last;
	}

	public boolean nonEmpty() {
		Segment read = this.read;
		int index = read.index;
		final int start = read.start;

		return start + index < this.last;
	}

	public A poll() {

		boolean handledAll = true;
		Segment head = this.read;
		int index = head.index;
		final int start = head.start;

		final Object HANDLED = Jiffy.HANDLED;

		if (start + index == this.last) {
			// queue is empty
			return null;
		}

		final int step = head.length();

		while (true) {
			if (index < step) {
				// search segment

				@SuppressWarnings("unchecked")
				A data = (A) head.get(index);

				if (null == data) {
					// a producer is in the process of insert
					index += 1;
					handledAll = false;
					continue;
				}

				// data is either available or handled

				if (handledAll) {
					// read has caught up
					this.read.index = index + 1;
				}

				if (HANDLED == data) {
					// move to next element in the segment
					index += 1;
					continue;
				}

				// success!
				head.lazySet(index, HANDLED);
				return data;
			}

			// current segment ended
			Segment next = head.next;
			if (next == null) {
				// queue ended
				return null;
			}

			// move to next segment
			head = next;
			index = head.index;
			if (handledAll) {
				// remove segment
				this.read = next;
			}
		}
	}

	private static class Segment extends AtomicReferenceArray<Object> {

		final Segment prev;
		final int start;
		int index;

		volatile Segment next;

		Segment(Segment prev, int size, int start) {
			super(size);
			this.prev = prev;
			this.start = start;
		}

		static final AtomicReferenceFieldUpdater<Segment, Segment> NEXT = AtomicReferenceFieldUpdater
				.newUpdater(Segment.class, Segment.class, "next");
	}

	private static final Object HANDLED = new Object();

	private static final AtomicReferenceFieldUpdater<Jiffy, Segment> WRITE = AtomicReferenceFieldUpdater
			.newUpdater(Jiffy.class, Segment.class, "write");
	private static final AtomicIntegerFieldUpdater<Jiffy> LAST = AtomicIntegerFieldUpdater.newUpdater(Jiffy.class,
			"last");
}