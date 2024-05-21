package mpsc;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

abstract class JiffyLast implements Serializable {
	volatile int last;
}

abstract class JiffyLastPadding extends JiffyLast {
	protected long _000;
	protected long _001;
	protected long _002;
	protected long _003;
	protected long _004;
	protected long _005;
	protected long _006;
	protected long _007;
	protected long _008;
	protected long _009;
	protected long _010;
	protected long _011;
	protected long _012;
	protected long _013;
	protected long _014;
	protected long _015;
}

abstract class JiffyWrite extends JiffyLastPadding {
	volatile JiffyAligned.Segment write;
}

abstract class JiffyWritePadding extends JiffyWrite {
	protected int _100;
	protected long _101;
	protected long _102;
	protected long _103;
	protected long _104;
	protected long _105;
	protected long _106;
	protected long _107;
	protected long _108;
	protected long _109;
	protected long _110;
	protected long _111;
	protected long _112;
	protected long _113;
	protected long _114;
	protected long _115;
}

public class JiffyAligned<A> extends JiffyWritePadding implements Queue<A> {

	private Segment read;
	private final int grow;

	public JiffyAligned(int step) {
		this(step, -1);
	}

	public JiffyAligned(int step, int grow) {
		this.read = this.write = new Segment(null, step, 0);
		this.grow = grow;
	}

	public void add(A data) {
		assert (data != null);

		final AtomicReferenceFieldUpdater<SegmentNext, Segment> NEXT = Segment.NEXT;
		final AtomicReferenceFieldUpdater<JiffyWrite, Segment> WRITE = JiffyAligned.WRITE;
		final AtomicIntegerFieldUpdater<JiffyLast> LAST = JiffyAligned.LAST;

		final int grow = this.grow;

		Segment tail = this.write;
		var isTail = true;
		int index = 0;

		final int step = tail.curr.length();
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
				tail.curr.lazySet(index, data);
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

		final Object HANDLED = JiffyAligned.HANDLED;

		if (start + index == this.last) {
			// queue is empty
			return null;
		}

		final int step = head.curr.length();

		while (true) {
			if (index < step) {
				// search segment

				@SuppressWarnings("unchecked")
				A data = (A) head.curr.get(index);

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
				head.curr.lazySet(index, HANDLED);
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

	private static abstract class SegmentNext implements Serializable {
		volatile Segment next;
	}

	@SuppressWarnings("unused")
	private static abstract class SegmentNextPadding extends SegmentNext {
		protected long _000;
		protected long _001;
		protected long _002;
		protected long _003;
		protected long _004;
		protected long _005;
		protected long _006;
		protected long _007;
		protected long _008;
		protected long _009;
		protected long _010;
		protected long _011;
		protected long _012;
		protected long _013;
		protected long _014;
		protected long _015;
	}

	static class Segment extends SegmentNextPadding {

		final Segment prev;
		final AtomicReferenceArray<Object> curr;
		final int start;
		int index;

		Segment(Segment prev, int size, int start) {
			this.prev = prev;
			this.curr = new AtomicReferenceArray<Object>(size);
			this.start = start;
		}

		static final AtomicReferenceFieldUpdater<SegmentNext, Segment> NEXT = AtomicReferenceFieldUpdater
				.newUpdater(SegmentNext.class, Segment.class, "next");
	}

	private static final Object HANDLED = new Object();

	private static final AtomicIntegerFieldUpdater<JiffyLast> LAST = AtomicIntegerFieldUpdater
			.newUpdater(JiffyLast.class, "last");
	private static final AtomicReferenceFieldUpdater<JiffyWrite, Segment> WRITE = AtomicReferenceFieldUpdater
			.newUpdater(JiffyWrite.class, Segment.class, "write");
}