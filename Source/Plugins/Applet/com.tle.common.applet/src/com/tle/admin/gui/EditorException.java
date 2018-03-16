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

package com.tle.admin.gui;

/**
 * @author Nicholas Read
 */
public class EditorException extends Exception
{
	private static final long serialVersionUID = 1L;
	private boolean isFatal;
	private String category;

	public EditorException(String message)
	{
		super(message);
		isFatal = false;
	}

	public EditorException(String errorCategory, Throwable th)
	{
		super(th);
		this.category = errorCategory;
		isFatal = true;
	}

	public String getCategory()
	{
		return category;
	}

	public boolean isFatal()
	{
		return isFatal;
	}
}