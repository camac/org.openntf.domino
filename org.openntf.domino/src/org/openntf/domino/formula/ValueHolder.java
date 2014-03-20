/*
 * © Copyright FOCONIS AG, 2014
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 */
package org.openntf.domino.formula;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

import org.openntf.domino.DateTime;

/**
 * Valueholder to hold single or multiple values.
 * 
 * When evaluating a formula, every String/int/double value is wrapped in a "ValueHolder". The holder has several get-methods to return the
 * different types. You always must check the datatype before calling one of the getters, because a ValueHolder that contains Strings cannot
 * return
 * 
 * The code itself might look strange, but this was done to be as fast as possible
 * 
 * @author Roland Praml, Foconis AG
 * 
 */
public abstract class ValueHolder implements Serializable {
	private static final long serialVersionUID = 8290517470597891417L;

	/**
	 * These are the possible datatypes. <br>
	 * DOUBLE means 'only doubles', INTEGER means 'only integers'. If you mix Integers and Doubles, the type changes to "NUMBER"
	 */
	public enum DataType {
		ERROR, STRING, INTEGER(true), NUMBER(true), DOUBLE(true), DATETIME, BOOLEAN, _UNSET, OBJECT;
		public boolean numeric = false;

		DataType() {
		}

		DataType(final boolean n) {
			numeric = n;
		}
	}

	protected boolean immutable;

	// For performance reasons we allow direct access to these members
	public int size;
	public DataType dataType = DataType._UNSET;

	protected RuntimeException currentError;

	// Caches
	protected static final ValueHolder TRUE;
	protected static final ValueHolder FALSE;

	protected static final ValueHolder integerCache[];
	protected static final ValueHolder stringCache[];

	static {
		TRUE = new ValueHolderBoolean(1);
		TRUE.add(Boolean.TRUE);
		TRUE.immutable = true;

		FALSE = new ValueHolderBoolean(1);
		FALSE.add(Boolean.FALSE);
		FALSE.immutable = true;

		integerCache = new ValueHolder[256];
		stringCache = new ValueHolder[256];
		for (int i = 0; i < 256; i++) {
			ValueHolder vhn = integerCache[i] = new ValueHolderNumber(1);
			vhn.add(i - 128);
			vhn.immutable = true;

			ValueHolder vho = stringCache[i] = new ValueHolderObject<Object>(1);
			if (i == 0) {
				vho.add("");
			} else {
				vho.add(String.valueOf((char) i));
			}
			vho.immutable = true;
		}

	}

	public ValueHolder() {
	}

	public static ValueHolder createValueHolder(final Class<?> clazz, final int size) {

		if (boolean.class.equals(clazz))
			return new ValueHolderBoolean(size);

		if (double.class.equals(clazz))
			return new ValueHolderNumber(size);

		if (int.class.equals(clazz))
			return new ValueHolderNumber(size);

		// the rest of the numeric values in JAVA -> handled as double
		if (byte.class.equals(clazz))
			return new ValueHolderNumber(size);
		if (char.class.equals(clazz))
			return new ValueHolderNumber(size);
		if (short.class.equals(clazz))
			return new ValueHolderNumber(size);
		if (long.class.equals(clazz))
			return new ValueHolderNumber(size);
		if (float.class.equals(clazz))
			return new ValueHolderNumber(size);

		if (clazz.isPrimitive()) {
			throw new UnsupportedOperationException("Cannot return objectholder for " + clazz);
		}

		return new ValueHolderObject<Object>(size);

	}

	/**
	 * Init a ValueHolder based on a single value or a collection<br>
	 * If possible use one of the special "valueOf" constructors as these are faster
	 * 
	 */
	@Deprecated
	public static ValueHolder valueOf(final Object init) {
		ValueHolder vh = null;
		if (init == null)
			return valueDefault();

		if (init instanceof String)
			return valueOf((String) init);

		if (init instanceof Integer)
			return valueOf(((Integer) init).intValue());

		if (init instanceof Number)
			return valueOf(((Number) init).doubleValue());

		if (init instanceof Boolean)
			return valueOf(((Boolean) init).booleanValue());

		// Array handling and other objects
		if (init.getClass().isArray()) {
			int lh = Array.getLength(init);
			if (lh == 0)
				return valueDefault();
			if (lh == 1)
				return valueOf(Array.get(init, 0));

			for (int i = 0; i < lh; i++) {
				Object o = Array.get(init, i);
				if (o != null) {
					if (vh == null) {
						vh = createValueHolder(o.getClass(), lh);
					}
					vh.add(o);
				}
			}

		} else if (init instanceof Collection) {
			Collection<?> c = (Collection<?>) init;
			int lh = c.size();
			if (lh == 0)
				return valueDefault();
			if (lh == 1)
				return valueOf(c.iterator().next());

			for (Object o : c) {
				if (o != null) {
					if (vh == null) {
						vh = createValueHolder(o.getClass(), lh);
					}
					vh.add(o);
				}
			}

		} else {
			vh = createValueHolder(init.getClass(), 1);
			vh.add(init);
		}
		if (vh == null)
			return valueDefault();
		vh.immutable = true;
		return vh;
	}

	/**
	 * Initializes a new ValueHolder that holds a RuntimeException
	 * 
	 * @param init
	 *            the RuntimeException
	 * @return the Valuholder
	 */
	public static ValueHolder valueOf(final RuntimeException init) {
		ValueHolder vh = new ValueHolderObject<Object>(1);
		vh.setError(init);
		vh.immutable = true;
		return vh;
	}

	/**
	 * Initializes a new ValueHolder that holds a integer value. Values -128..128 are cached.
	 * 
	 * @param init
	 *            the int value
	 * @return the Valuholder
	 */

	public static ValueHolder valueOf(final int init) {
		if (-128 <= init && init < 128) {
			return integerCache[init + 128];
		}
		ValueHolder vh = new ValueHolderNumber(1);
		vh.add(init);
		vh.immutable = true;
		return vh;
	}

	/**
	 * Initializes a new ValueHolder that holds a double value
	 * 
	 * @param init
	 *            the double
	 * @return the ValueHolder
	 */
	public static ValueHolder valueOf(final double init) {
		ValueHolder vh = new ValueHolderNumber(1);
		vh.add(init);
		vh.immutable = true;
		return vh;
	}

	/**
	 * Initializes a new ValueHolder that holds a String value
	 * 
	 * @param init
	 *            the String
	 * @return the ValueHolder
	 */
	public static ValueHolder valueOf(final String init) {
		if (init.length() == 0)
			return stringCache[0];
		if (init.length() == 1) {
			char ch = init.charAt(0);
			if (ch < 256)
				return stringCache[0];
		}

		ValueHolder vh = new ValueHolderObject<String>(1);
		vh.add(init);
		vh.immutable = true;
		return vh;
	}

	/**
	 * Initializes a new ValueHolder that contains a DateTime
	 * 
	 * @param init
	 *            the DateTime
	 * @return the ValueHolder
	 */
	public static ValueHolder valueOf(final DateTime init) {
		ValueHolder vh = new ValueHolderObject<DateTime>(1);
		vh.add(init);
		vh.immutable = true;
		return vh;
	}

	/**
	 * Returns one of the two cached Boolean holders that represent TRUE or FALSE
	 * 
	 * @param init
	 *            the boolean.
	 * @return the ValueHolder
	 */
	public static ValueHolder valueOf(final boolean init) {
		if (init)
			return TRUE;
		return FALSE;
	}

	/**
	 * Returns the ValueHolder for the default value .
	 * 
	 * @return the ValueHolder
	 */

	public static ValueHolder valueDefault() {
		return stringCache[0];
	}

	/**
	 * returns the error or null
	 * 
	 * @return the error or null
	 */
	public RuntimeException getError() {
		return currentError;
	}

	/**
	 * get the nth entry (0=first entry)
	 * 
	 * @param i
	 *            the position
	 * @return the entry as Object
	 * @Deprecated if you know the datatype, use the apropriate get-Method!
	 */
	@Deprecated
	public Object get(final int i) {
		switch (dataType) {
		case ERROR:
			throw currentError;
		case DOUBLE:
		case NUMBER:
			return getDouble(i);
		case INTEGER:
			return getInt(i);
		default:
			return getObject(i);
		}
	}

	public abstract Object getObject(final int i);

	/**
	 * Returns the value at position i as String
	 * 
	 * @param i
	 *            the position
	 * @return the value as String
	 */
	public String getString(final int i) {
		throw new ClassCastException("STRING expected. Got '" + dataType + "'");
	}

	/**
	 * Returns the value at position i as DateTime
	 * 
	 */
	public DateTime getDateTime(final int i) {
		throw new ClassCastException("DATETIME expected. Got '" + dataType + "'");
	}

	/**
	 * Returns the value at position i as Integer
	 * 
	 */
	public int getInt(final int i) {
		throw new ClassCastException("DATETIME expected. Got '" + dataType + "'");
	}

	public double getDouble(final int i) {
		throw new ClassCastException("DOUBLE expected. Got '" + dataType + "'");
	}

	public boolean getBoolean(final int i) {
		throw new ClassCastException("BOOLEAN expected. Got '" + dataType + "'");
	}

	/**
	 * Returns TRUE if one of the values is true
	 */
	public boolean isTrue(final FormulaContext ctx) {
		if (dataType == DataType.ERROR)
			throw getError();

		if (ctx.useBooleans) {
			for (int i = 0; i < size; i++) {
				if (getBoolean(i)) {
					return true;
				}
			}
		} else {
			for (int i = 0; i < size; i++) {
				if (getInt(i) != 0) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * This is all optimized for performance
	 * 
	 */
	public boolean addAll(final ValueHolder other) {
		throw new IllegalArgumentException("Cannot add " + other.dataType + " to " + dataType);
	};

	/**
	 * Adds the value as String. You have to ensure that you have called grow() before!
	 * 
	 */
	public boolean add(final String obj) {
		throw new IllegalArgumentException("Cannot mix datatypes " + dataType + " and STRING");
	}

	/**
	 * Adds an integer as value
	 * 
	 */
	public boolean add(final int value) {
		throw new IllegalArgumentException("Cannot mix datatypes " + dataType + " and INTEGER");
	}

	/**
	 * Adds a double as value
	 */
	public boolean add(final double value) {
		throw new IllegalArgumentException("Cannot mix datatypes " + dataType + " and DOUBLE");
	}

	public boolean add(final boolean bool) {
		throw new IllegalArgumentException("Cannot mix datatypes " + dataType + " and BOOLEAN");
	}

	public boolean add(final DateTime bool) {
		throw new IllegalArgumentException("Cannot mix datatypes " + dataType + " and DATETIME");
	}

	public void setError(final RuntimeException e) {
		dataType = DataType.ERROR;
		currentError = e;
		size = 1;
	}

	protected void checkAdd() {
		if (immutable)
			throw new UnsupportedOperationException("immutable");
	}

	@Deprecated
	protected void checkGet() {
		if (currentError == null)
			return;
		throw currentError;
	}

	/**
	 * Add anything as value. Better use the apropriate "add" method. it is faster
	 */
	@Deprecated
	public boolean add(final Object obj) {
		if (immutable)
			throw new UnsupportedOperationException("immutable");

		if (dataType == DataType.ERROR) {
			return false;
		}

		if (obj instanceof String) {
			return add((String) obj);

		} else if (obj instanceof Integer) {
			return add(((Integer) obj).intValue());

		} else if (obj instanceof Number) {
			return add(((Number) obj).doubleValue());

		} else if (obj instanceof Boolean) {
			return add(((Boolean) obj).booleanValue());

		} else if (obj instanceof DateTime) {
			return add((DateTime) obj);

		} else if (obj instanceof RuntimeException) {
			setError((RuntimeException) obj);
		} else {
			dataType = DataType.OBJECT;
			throw new IllegalArgumentException("TODO: Datatype " + obj.getClass() + " is not yet supported");
		}
		return true;
	}

	public static boolean hasMultiValues(final ValueHolder[] params) {
		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				if (params[i].size > 1)
					return true;
			}
		}
		return false;
	}

	public abstract List<Object> toList();

	public abstract ValueHolder newInstance(int size2);
}
