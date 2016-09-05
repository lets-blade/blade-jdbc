package com.blade.jdbc.tx;

import com.blade.jdbc.exception.DBException;

public interface AtomTx {

	void execute() throws DBException;
	
}
