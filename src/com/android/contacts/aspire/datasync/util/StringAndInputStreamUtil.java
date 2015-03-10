package com.android.contacts.aspire.datasync.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Filename: StringAndInputStreamUtil.java Description: Copyright: Copyright
 * (c)2009 Company: company
 * 
 * @author: liangbo
 * @version: 1.0 Create at: 2010-9-3 上午11:13:25
 * 
 *           Modification History: Date Author Version Description
 *           ------------------------------------------------------------------
 *           2010-9-3 liangbo 1.0 1.0 Version
 */

public class StringAndInputStreamUtil {
	// 1. String --> InputStream
	public static InputStream String2InputStream(String str) {
		ByteArrayInputStream stream = null;
		try {
			stream = new ByteArrayInputStream(str.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stream;
	}

	// 2. InputStream --> String
	public static String inputStream2String(InputStream is) {

		String str = "";
		int c;
		if (is != null) {
			ByteArrayOutputStream bais = new ByteArrayOutputStream();
			try {
				while ((c = is.read()) != -1) {
					bais.write(c);
				}
				byte data[] = bais.toByteArray();
				str = new String(data, "utf-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return str;

	}

}
