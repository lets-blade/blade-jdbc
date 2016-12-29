package com.blade.jdbc.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.blade.jdbc.utils.Utils;

public enum StatementCache {
	INSTANCE;

	private final ConcurrentMap<Connection, Map<String, PreparedStatement>> statementCache = new ConcurrentHashMap<Connection, Map<String, PreparedStatement>>();

	private StatementCache() {
	}

	public static StatementCache instance() {
		return INSTANCE;
	}

	PreparedStatement getPreparedStatement(Connection connection, String query) {
		if (!statementCache.containsKey(connection)) {
			statementCache.put(connection, new HashMap<String, PreparedStatement>());
		}
		return statementCache.get(connection).get(query);
	}

	public void cache(Connection connection, String query, PreparedStatement ps) {
		statementCache.get(connection).put(query, ps);
	}

	public void cleanStatementCache(Connection connection) {
		Map<String, PreparedStatement> stmsMap = statementCache.remove(connection);
		if (stmsMap != null) { // Close prepared statements to release cursors
								// on connection pools
			for (PreparedStatement stmt : stmsMap.values()) {
				Utils.closeQuietly(stmt);
			}
		}
	}
}