package com.dcampus.common.util;

public class ArrayCast {
	
	@SuppressWarnings("unchecked")
	public static <T> T[] cast(Object[] o, T[] t) {
		if (t.length < o.length)
			t = (T[]) java.lang.reflect.Array.newInstance(t.getClass()
					.getComponentType(), o.length);
		
		System.arraycopy(o, 0, t, 0, o.length);
		
		if (t.length > o.length)
			t[o.length] = null;
		return t;
	}
}
