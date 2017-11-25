/*
 * Copyright 2013-2016 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.restclient;

/**
 * @author VISTALL
 * @since 27-Sep-16
 */
public class HttpHeader
{
	private String myName;
	private String myValue;

	public HttpHeader(String name, String value)
	{
		myName = name;
		myValue = value;
	}

	public String getName()
	{
		return myName;
	}

	public String getValue()
	{
		return myValue;
	}

	public void setName(String name)
	{
		myName = name;
	}

	public void setValue(String value)
	{
		myValue = value;
	}
}
