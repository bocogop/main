package org.bocogop.shared.persistence;

import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.type.TimestampType;

public class SQLServer2012Dialect extends org.hibernate.dialect.SQLServer2012Dialect {

	public static final String[] SUPPORTED_DATE_PARTS = new String[] { "year", "quarter", "month", "dayofyear", "day",
			"week", "weekday", "hour", "minute", "second", "millisecond" };

	public SQLServer2012Dialect() {
		for (String datepart : SUPPORTED_DATE_PARTS) {
			registerFunction("add_" + datepart, new VarArgsSQLFunction(TimestampType.INSTANCE,
					"DATEADD(" + datepart.toUpperCase() + ",", ",", ")"));
			registerFunction(datepart + "_diff", new VarArgsSQLFunction(TimestampType.INSTANCE,
					"DATEDIFF(" + datepart.toUpperCase() + ",", ",", ")"));
			registerFunction(datepart + "_part", new VarArgsSQLFunction(TimestampType.INSTANCE,
					"DATEPART(" + datepart.toUpperCase() + ",", ",", ")"));
		}
	}
}