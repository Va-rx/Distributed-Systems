package sr.ice.server;

import Demo.A;
import Demo.Calc;
import com.zeroc.Ice.Current;

public class CalcI implements Calc {
	private static final long serialVersionUID = -2448962912780867770L;
	long counter = 0;

	@Override
	public long add(int a, int b, Current __current) {
		System.out.println("ADD: a = " + a + ", b = " + b + ", result = " + (a + b));

		if (a > 1000 || b > 1000) {
			try {
				Thread.sleep(6000);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}

		if (__current.ctx.values().size() > 0) {
			System.out.println("There are some properties in the context");
		}

		return a + b;
	}

	@Override
	public long subtract(int a, int b, Current __current) {
		System.out.println("SUBTRACT: a = " + a + ", b = " + b + ", result = " + (a - b));
		return a - b;
	}


	@Override
	public /*synchronized*/ void op(A a1, short b1, Current current) {
		System.out.println("OP" + (++counter));
		try {
			Thread.sleep(500);
		} catch (java.lang.InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}


	@Override
	public A op2(A[] a, Current current) {
		System.out.println("OP2");

		short sumA = 0;
		long sumB = 0;
		float sumC = 0.0f;
		StringBuilder sb = new StringBuilder();

		for (A item : a) {
			System.out.println("Processing object: A(a = " + item.a + ", b = " + item.b + ", c = " + item.c + ", d = " + item.d + ")");
			sumA += item.a;
			sumB += item.b;
			sumC += item.c;
			sb.append(item.d);
		}

		A result = new A(sumA, sumB, sumC, sb.toString());
		System.out.println("ADDED ALL OBJECTS:");
		System.out.println("Sum of 'a' values: " + sumA);
		System.out.println("Sum of 'b' values: " + sumB);
		System.out.println("Sum of 'c' values: " + sumC);
		System.out.println("Concatenation of 'd' strings: " + sb.toString());


		try {
			Thread.sleep(500);
		} catch (java.lang.InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		return result;
	}
}