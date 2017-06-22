/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.common.searching;

import java.io.Serializable;
import java.util.Objects;

/**
 * The real question is.. Do we just want to expose the lucene classes instead?
 * 
 * @author jolz
 */
public class SortField implements Cloneable, Serializable
{
	private static final long serialVersionUID = 1L;

	public enum Type
	{
		STRING, INT, LONG, SCORE
	}

	private final String field;
	private final Type type;
	private boolean reverse;

	public SortField(String field, boolean reverse)
	{
		this(field, reverse, Type.STRING);
	}

	public SortField(String field, boolean reverse, Type type)
	{
		this.field = field;
		this.reverse = reverse;
		this.type = type;
	}

	public String getField()
	{
		return field;
	}

	public boolean isReverse()
	{
		return reverse;
	}

	public Type getType()
	{
		return type;
	}

	public void setReverse(boolean reverse)
	{
		this.reverse = reverse;
	}

	// Explicit catch of CloneNotSupportedException from super.clone()
	@Override
	public SortField clone() // NOSONAR
	{
		try
		{
			return (SortField) super.clone();
		}
		catch( CloneNotSupportedException e )
		{
			throw new Error(e);
		}
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(field, type, reverse);
	}

	@Override
	public boolean equals(Object obj)
	{
		if( obj == null || !(obj instanceof SortField) )
		{
			return false;
		}
		else if( this == obj )
		{
			return true;
		}
		else
		{
			SortField rhs = (SortField) obj;
			return reverse == rhs.reverse && Objects.equals(type, rhs.type) && Objects.equals(field, rhs.field);
		}
	}
}