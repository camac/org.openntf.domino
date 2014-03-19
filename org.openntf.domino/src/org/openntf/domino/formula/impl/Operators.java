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
package org.openntf.domino.formula.impl;

import java.util.Collection;

import org.openntf.domino.DateTime;
import org.openntf.domino.formula.AtFunctionFactory;
import org.openntf.domino.formula.FormulaContext;
import org.openntf.domino.formula.ValueHolder;

/**
 * This class implements the default arithmetic, boolean and compare operators.
 * 
 * As Operations are used at most, this is implemented to be (or to try to be) as fast as possible
 * 
 * @author Roland Praml, Foconis AG
 * 
 */
public class Operators extends OperatorsAbstract {

	/**
	 * The Factory that returns a set of operators
	 */
	public static class Factory extends AtFunctionFactory {

		public Factory() {
			super();

			// Define the computers
			OperatorImpl add = new OperatorImpl() {

				@Override
				public int compute(final int v1, final int v2) {
					return v1 + v2;
				}

				@Override
				public double compute(final double v1, final double v2) {
					return v1 + v2;
				}

				@Override
				public String compute(final String v1, final String v2) {
					return v1.concat(v2);
				}
			};

			OperatorImpl sub = new OperatorImpl() {

				@Override
				public int compute(final int v1, final int v2) {
					return v1 - v2;
				}

				@Override
				public double compute(final double v1, final double v2) {
					return v1 - v2;
				}

			};

			OperatorImpl mul = new OperatorImpl() {

				@Override
				public int compute(final int v1, final int v2) {
					return v1 * v2;
				}

				@Override
				public double compute(final double v1, final double v2) {
					return v1 * v2;
				}

			};

			OperatorImpl div = new OperatorImpl() {

				@Override
				public int compute(final int v1, final int v2) {
					return v1 / v2; // TODO, throw Exception
				}

				@Override
				public double compute(final double v1, final double v2) {
					return v1 / v2;
				}

			};
			init(new Operators(add, "+"), 		// 
					new Operators(add, "*+"), 	//
					new Operators(sub, "-"), 	//
					new Operators(sub, "*-"), 	//
					new Operators(mul, "*"), 	//
					new Operators(mul, "**"), 	//
					new Operators(div, "/"), 	//
					new Operators(div, "*/"));
		}
	}

	private OperatorImpl computer;

	/**
	 * The constructor. Operators shoud be constructed via Operator.Factory
	 * 
	 * @param operation
	 * @param image
	 */
	private Operators(final OperatorImpl computer, final String image) {
		super(image);
		this.computer = computer;
		// Autodetect if the operation is permutative
		this.isPermutative = (image.charAt(0) == '*' && image.length() > 1);
	}

	// ----------- Strings
	@Override
	protected ValueHolder evaluateString(final FormulaContext ctx, final ValueHolder[] params) {
		ValueHolder ret = new ValueHolder();
		Collection<String[]> values = new ParameterCollectionObject<String>(params, String.class, isPermutative);

		ret.grow(values.size());
		for (String[] value : values) {
			ret.add(computer.compute(value[0], value[1]));
		}
		return ret;

	}

	@Override
	protected ValueHolder evaluateString(final FormulaContext ctx, final String s1, final String s2) {
		return new ValueHolder(computer.compute(s1, s2));
	}

	// ----------- Numbers
	@Override
	protected ValueHolder evaluateNumber(final FormulaContext ctx, final ValueHolder[] params) {
		ValueHolder ret = new ValueHolder();
		Collection<double[]> values = new ParameterCollectionDouble(params, isPermutative);

		ret.grow(values.size());
		for (double[] value : values) {
			ret.add(computer.compute(value[0], value[1]));
		}
		return ret;
	}

	@Override
	protected ValueHolder evaluateNumber(final FormulaContext ctx, final double d1, final double d2) {
		return new ValueHolder(computer.compute(d1, d2));
	}

	// ----------- Integers
	@Override
	protected ValueHolder evaluateInt(final FormulaContext ctx, final ValueHolder[] params) {
		ValueHolder ret = new ValueHolder();
		Collection<int[]> values = new ParameterCollectionInt(params, isPermutative);

		ret.grow(values.size());
		for (int[] value : values) {
			ret.add(computer.compute(value[0], value[1]));
		}
		return ret;
	}

	@Override
	protected ValueHolder evaluateInt(final FormulaContext ctx, final int i1, final int i2) {
		return new ValueHolder(computer.compute(i1, i2));
	}

	// ----------- DateTimes
	@Override
	protected ValueHolder evaluateDateTime(final FormulaContext ctx, final ValueHolder[] params) {
		ValueHolder ret = new ValueHolder();
		Collection<int[]> values = new ParameterCollectionInt(params, isPermutative);

		ret.grow(values.size());
		for (int[] value : values) {
			ret.add(computer.compute(value[0], value[1]));
		}
		return ret;
	}

	@Override
	protected ValueHolder evaluateDateTime(final FormulaContext ctx, final DateTime dt1, final DateTime dt2) {
		return new ValueHolder(computer.compute(dt1, dt2));
	}

}