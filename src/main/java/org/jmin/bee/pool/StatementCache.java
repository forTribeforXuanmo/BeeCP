/*
 * Copyright Chris2018998
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
package org.jmin.bee.pool;
import static org.jmin.bee.pool.util.ConnectionUtil.oclose;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Statement cache
 *
 * @author Chris.liao
 * @version 1.0
 */
public class StatementCache{
	private int capacity;
	public CacheNode head=null;//old
	public CacheNode tail=null;//new
	private HashMap<Object,CacheNode>nodeMap;
	public StatementCache(int capacity) {
		this.nodeMap = new HashMap<Object,CacheNode>((int)Math.ceil(capacity/0.75f)+1,0.75f);
		this.capacity = capacity;
	}
	public PreparedStatement get(Object key) {
		if(nodeMap.isEmpty())return null;
		
		CacheNode n=nodeMap.get(key);
		if(n!=null){
			moveToTail(n);
			return n.v;
		}
		return null;
	}
	public void put(Object k,PreparedStatement v) {
		CacheNode n = nodeMap.get(k);
		if (n==null) {
			n = new CacheNode(k,v);
			nodeMap.put(k,n);
			addNewNode(n);
			
			if(nodeMap.size()>capacity) {
			  CacheNode oldHead=removeHead();
			  nodeMap.remove(oldHead.k);
			  onRemove(oldHead.k,oldHead.v);
			}
		} else {
			n.v = v;
			moveToTail(n);
		}
	}
	public void clear() {
		Iterator<Map.Entry<Object, CacheNode>> itor=nodeMap.entrySet().iterator();
		while (itor.hasNext()) {
			Map.Entry<Object,CacheNode> entry = (Map.Entry<Object, CacheNode>) itor.next();
			itor.remove();
			 CacheNode node= entry.getValue();
			 onRemove(node.k,node.v);
		}
		
		head=null;
		tail=null;
	}
	private void onRemove(Object key, PreparedStatement obj) {
		oclose(obj);
	}
	//add new node
	private void addNewNode(CacheNode n) {
		if (head == null) {
			head = n;
			tail = n;
		} else {
			tail.next = n;
			n.pre = tail;
			tail = n;
		}
	}
	//below are node chain operation method
	private void moveToTail(CacheNode n) {
		if(n==tail)return;
		//remove from chain
		if (head == n) {
			head = n.next;
			head.pre = null;
		} else {
			n.pre.next = n.next;
			n.next.pre = n.pre;
		}

		//append to tail
		tail.next = n;
		n.pre = tail;
		n.next = null;
		tail = tail.next;//new tail
	}
	//remove head when size more than capacity
	private CacheNode removeHead() {
		CacheNode n = head;
		if (head == tail) {
			head = null;
			tail = null;
		} else {
			head = head.next;
			head.pre = null;
		}
		return n;
	}
	static class CacheNode {// double linked chain node
		private Object k;
		private PreparedStatement v;
		private CacheNode pre = null;
		private CacheNode next = null;
		public CacheNode(Object k, PreparedStatement v) {
			this.k = k;
			this.v = v;
		}
	}
}
class StatementCacheUtil {
	public static final Object createPsCaecheKey(String sql) {
		return new StatementPsCacheKey1(sql);
	}
	public static final Object createPsCaecheKey(String sql, int autoGeneratedKeys) {
		return new StatementPsCacheKey2(sql, autoGeneratedKeys);
	}
	public static final Object createPsCaecheKey(String sql, int[] columnIndexes) {
		return new StatementPsCacheKey3(sql, columnIndexes);
	}
	public static final Object createPsCaecheKey(String sql, String[] columnNames) {
		return new StatementPsCacheKey4(sql, columnNames);
	}
	public static final Object createPsCaecheKey(String sql, int resultSetType, int resultSetConcurrency) {
		return new StatementPsCacheKey5(sql, resultSetType, resultSetConcurrency);
	}
	public static final Object createPsCaecheKey(String sql, int resultSetType, int resultSetConcurrency,int resultSetHoldability) {
		return new StatementPsCacheKey6(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}
	public static final Object createCsCaecheKey(String sql) {
		return new StatementCsCacheKey1(sql);
	}
	public static final Object createCsCaecheKey(String sql, int resultSetType, int resultSetConcurrency) {
		return new StatementCsCacheKey2(sql, resultSetType, resultSetConcurrency);
	}
	public static final Object createCsCaecheKey(String sql, int resultSetType, int resultSetConcurrency,int resultSetHoldability) {
		return new StatementCsCacheKey3(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}
}


class CacheKey {
	protected static final int Prime = 31;
}
final class StatementPsCacheKey1 extends CacheKey{
	private String sql;
	private int hashCode;
	private static final int HashCodeInit = StatementPsCacheKey1.class.hashCode();

	public StatementPsCacheKey1(String sql) {
		this.sql = sql;
		hashCode = HashCodeInit + sql.hashCode();
	}

	public int hashCode() {
		return this.hashCode;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof StatementPsCacheKey1))
			return false;
		StatementPsCacheKey1 other = (StatementPsCacheKey1) obj;
		if (hashCode != other.hashCode)
			return false;
		return sql.equals(other.sql);
	}
}
final class StatementPsCacheKey2 extends CacheKey{
	private String sql;
	int autoGeneratedKeys;
	private int hashCode;
	private static final int HashCodeInit=StatementPsCacheKey2.class.hashCode();
	public StatementPsCacheKey2(String sql, int autoGeneratedKeys) {
		this.sql = sql;
		this.autoGeneratedKeys = autoGeneratedKeys;

		hashCode = HashCodeInit + autoGeneratedKeys;
		hashCode = Prime * hashCode + sql.hashCode();
	}
	public int hashCode() {
		return this.hashCode;
	}
	public boolean equals(Object obj) {
		if (!(obj instanceof StatementPsCacheKey2))
			return false;
		StatementPsCacheKey2 other = (StatementPsCacheKey2) obj;
		if (hashCode != other.hashCode)
			return false;
		if (autoGeneratedKeys != other.autoGeneratedKeys)
			return false;
		return sql.equals(other.sql);
	}
}
final class StatementPsCacheKey3 extends CacheKey{
	private String sql;
	private int[] columnIndexes;
	private int hashCode;
	private static final int HashCodeInit=StatementPsCacheKey3.class.hashCode();

	public StatementPsCacheKey3(String sql, int[] columnIndexes) {
		this.sql = sql;
		this.columnIndexes = columnIndexes;

		hashCode = HashCodeInit + Arrays.hashCode(columnIndexes);
		hashCode = Prime * hashCode + sql.hashCode();
	}
	public int hashCode() {
		return this.hashCode;
	}
	public boolean equals(Object obj) {
		if (!(obj instanceof StatementPsCacheKey3))
			return false;
		StatementPsCacheKey3 other = (StatementPsCacheKey3) obj;
		if (hashCode != other.hashCode)
			return false;
		if (!Arrays.equals(columnIndexes, other.columnIndexes))
			return false;
		return sql.equals(other.sql);
	}
}
final class StatementPsCacheKey4 extends CacheKey{
	private String sql;
	private String[] columnNames;
	private int hashCode;
	private static final int HashCodeInit=StatementPsCacheKey4.class.hashCode();

	public StatementPsCacheKey4(String sql, String[] columnNames) {
		this.sql = sql;
		this.columnNames = columnNames;

		hashCode = HashCodeInit + Arrays.hashCode(columnNames);
		hashCode = Prime * hashCode + sql.hashCode();
	}

	public int hashCode() {
		return this.hashCode;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof StatementPsCacheKey4))
			return false;
		StatementPsCacheKey4 other = (StatementPsCacheKey4) obj;

		if (hashCode != other.hashCode)
			return false;
		if (!Arrays.equals(columnNames, other.columnNames))
			return false;
		return sql.equals(other.sql);
	}
}
final class StatementPsCacheKey5 extends CacheKey{
	private String sql;
	private int resultSetType;
	private int resultSetConcurrency;
	private int hashCode;
	private static final int HashCodeInit =StatementPsCacheKey5.class.hashCode();

	public StatementPsCacheKey5(String sql, int resultSetType, int resultSetConcurrency) {
		this.sql = sql;
		this.resultSetType = resultSetType;
		this.resultSetConcurrency = resultSetConcurrency;

		hashCode = HashCodeInit + resultSetType;
		hashCode = Prime * hashCode + resultSetConcurrency;
		hashCode = Prime * hashCode + sql.hashCode();
	}

	public int hashCode() {
		return this.hashCode;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof StatementPsCacheKey5))
			return false;
		StatementPsCacheKey5 other = (StatementPsCacheKey5) obj;
		if (hashCode != other.hashCode)
			return false;
		if (resultSetType != other.resultSetType)
			return false;
		if (resultSetConcurrency != other.resultSetConcurrency)
			return false;
		return sql.equals(other.sql);
	}
}
final class StatementPsCacheKey6 extends CacheKey{
	private String sql;
	private int resultSetType;
	private int resultSetConcurrency;
	private int resultSetHoldability;
	private int hashCode;
	private static final int HashCodeInit =StatementPsCacheKey6.class.hashCode();

	public StatementPsCacheKey6(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
		this.sql = sql;
		this.resultSetType = resultSetType;
		this.resultSetConcurrency = resultSetConcurrency;
		this.resultSetHoldability = resultSetHoldability;

		hashCode = HashCodeInit + resultSetType;
		hashCode = Prime * hashCode + resultSetConcurrency;
		hashCode = Prime * hashCode + resultSetHoldability;
		hashCode = Prime * hashCode + sql.hashCode();
	}

	public int hashCode() {
		return this.hashCode;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof StatementPsCacheKey6))
			return false;
		StatementPsCacheKey6 other = (StatementPsCacheKey6) obj;
		if (hashCode != other.hashCode)
			return false;
		if (resultSetType != other.resultSetType)
			return false;
		if (resultSetConcurrency != other.resultSetConcurrency)
			return false;
		if (resultSetHoldability != other.resultSetHoldability)
			return false;
		return sql.equals(other.sql);
	}
}
final class StatementCsCacheKey1 extends CacheKey{
	private String sql;
	private int hashCode;
	private static final int HashCodeInit=StatementCsCacheKey1.class.hashCode();

	public StatementCsCacheKey1(String sql) {
		this.sql = sql;
		hashCode = HashCodeInit + sql.hashCode();
	}

	public int hashCode() {
		return this.hashCode;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof StatementCsCacheKey1))
			return false;
		StatementCsCacheKey1 other = (StatementCsCacheKey1) obj;
		if (hashCode != other.hashCode)
			return false;
		return sql.equals(other.sql);
	}
}
final class StatementCsCacheKey2 extends CacheKey{
	private String sql;
	private int resultSetType;
	private int resultSetConcurrency;
	private int hashCode;
	private static final int HashCodeInit=StatementCsCacheKey2.class.hashCode();

	public StatementCsCacheKey2(String sql, int resultSetType, int resultSetConcurrency) {
		this.sql = sql;
		this.resultSetType = resultSetType;
		this.resultSetConcurrency = resultSetConcurrency;

		hashCode = HashCodeInit + resultSetType;
		hashCode = Prime * hashCode + resultSetConcurrency;
		hashCode = Prime * hashCode + sql.hashCode();
	}

	public int hashCode() {
		return this.hashCode;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof StatementCsCacheKey2))
			return false;
		StatementCsCacheKey2 other = (StatementCsCacheKey2) obj;
		if (hashCode != other.hashCode)
			return false;
		if (resultSetType != other.resultSetType)
			return false;
		if (resultSetConcurrency != other.resultSetConcurrency)
			return false;
		return sql.equals(other.sql);
	}
}
final class StatementCsCacheKey3 extends CacheKey{
	private String sql;
	private int resultSetType;
	private int resultSetConcurrency;
	private int resultSetHoldability;
	private int hashCode;
	private static final int HashCodeInit =StatementCsCacheKey3.class.hashCode();

	public StatementCsCacheKey3(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
		this.sql = sql;
		this.resultSetType = resultSetType;
		this.resultSetConcurrency = resultSetConcurrency;
		this.resultSetHoldability = resultSetHoldability;

		hashCode = HashCodeInit + resultSetType;
		hashCode = Prime * hashCode + resultSetConcurrency;
		hashCode = Prime * hashCode + resultSetHoldability;
		hashCode = Prime * hashCode + sql.hashCode();
	}
	public int hashCode() {
		return this.hashCode;
	}
	public boolean equals(Object obj) {
		if (!(obj instanceof StatementCsCacheKey3))
			return false;
		StatementCsCacheKey3 other = (StatementCsCacheKey3) obj;
		if (hashCode != other.hashCode)
			return false;
		if (resultSetType != other.resultSetType)
			return false;
		if (resultSetConcurrency != other.resultSetConcurrency)
			return false;
		if (resultSetHoldability != other.resultSetHoldability)
			return false;
		return sql.equals(other.sql);
	}
}
 