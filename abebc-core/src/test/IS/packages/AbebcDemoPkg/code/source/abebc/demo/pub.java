package abebc.demo;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.softwareag.util.IDataMap;
// --- <<IS-END-IMPORTS>> ---

public final class pub

{
	// ---( internal utility methods )---

	final static pub _instance = new pub();

	static pub _newInstance() { return new pub(); }

	static pub _cast(Object o) { return (pub)o; }

	// ---( server methods )---




	public static final void add (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(add)>> ---
		// @sigtype java 3.5
		// [i] field:0:required int1
		// [i] field:0:required int2
		// [o] field:0:required sum
		final IDataMap pipe = new IDataMap(pipeline);
		final int int1 = pipe.getAsInteger("int1").intValue();
		final int int2 = pipe.getAsInteger("int2").intValue();
		pipe.put("sum", String.valueOf(int1 + int2));
		// --- <<IS-END>> ---

                
	}



	public static final void concat (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(concat)>> ---
		// @sigtype java 3.5
		// [i] field:0:required prefix
		// [i] field:0:required suffix
		// [o] field:0:required concatenatedValue
		final IDataMap pipe = new IDataMap(pipeline);
		final String prefix = pipe.getAsString("prefix");
		final String suffix = pipe.getAsString("suffix");
		pipe.put("concatenatedValue", prefix + suffix);			
		// --- <<IS-END>> ---

                
	}
}

